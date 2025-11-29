/*
 * Copyright (c) 2018-2025 Board of Trustees of Leland Stanford Jr. University,
 * all rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Stanford University shall not
 * be used in advertising or otherwise to promote the sale, use or other dealings
 * in this Software without prior written authorization from Stanford University.
 */
package org.lockss.laaws.poller.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.lockss.app.LockssDaemon;
import org.lockss.config.Configuration;
import org.lockss.laaws.poller.api.PollsApi;
import org.lockss.laaws.poller.api.PollsApiDelegate;
import org.lockss.laaws.poller.model.*;
import org.lockss.util.rest.poller.RepairData.ResultEnum;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.plugin.CachedUrlSet;
import org.lockss.plugin.PluginManager;
import org.lockss.poller.Poll;
import org.lockss.poller.PollManager;
import org.lockss.poller.PollManager.NotEligibleException;
import org.lockss.poller.PollSpec;
import org.lockss.poller.v3.*;
import org.lockss.poller.v3.ParticipantUserData.VoteCounts;
import org.lockss.poller.v3.PollerStateBean.Repair;
import org.lockss.protocol.PeerIdentity;
import org.lockss.protocol.psm.PsmInterp;
import org.lockss.protocol.psm.PsmState;
import org.lockss.spring.auth.AuthUtil;
import org.lockss.spring.auth.Roles;
import org.lockss.spring.base.BaseSpringApiServiceImpl;
import org.lockss.spring.base.LockssConfigurableService;
import org.lockss.spring.error.LockssRestServiceException;
import org.lockss.util.ByteArray;
import org.lockss.util.StringUtil;
import org.lockss.util.TimerQueue;
import org.lockss.util.UrlUtil;
import org.lockss.util.rest.exception.LockssRestHttpException;
import org.lockss.util.rest.poller.CachedUriSetSpec;
import org.lockss.util.rest.poller.LinkDesc;
import org.lockss.util.rest.poller.PollDesc;
import org.lockss.util.rest.poller.PollerPageInfo;
import org.lockss.util.rest.poller.PollerSummary;
import org.lockss.util.rest.poller.RepairData;
import org.lockss.util.rest.poller.RepairPageInfo;
import org.lockss.util.rest.poller.UrlPageInfo;
import org.lockss.util.rest.poller.VoterPageInfo;
import org.lockss.util.rest.poller.VoterSummary;
import org.lockss.util.rest.poller.model.PollVariantEnum;
import org.lockss.util.rest.poller.model.RepairTypeEnum;
import org.lockss.util.rest.poller.model.TallyTypeEnum;
import org.lockss.util.rest.poller.model.VoterUrlsEnum;
import org.lockss.util.rest.repo.model.PageInfo;
import org.lockss.util.time.Deadline;
import org.lockss.util.time.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * The Polls api service.
 */
@Service
public class PollsApiServiceImpl extends BaseSpringApiServiceImpl implements PollsApiDelegate, LockssConfigurableService {

  private static Logger logger = LoggerFactory
      .getLogger(PollsApiServiceImpl.class);
  private PollManager pollManager;
  private PluginManager pluginManager;
  private LockssDaemon theDaemon;
  @Autowired
  private HttpServletRequest request;
  private static final String DETAIL_UNAVAILABLE = "Unable to add details link.";
  private static final String NOT_INITIALIZED_MESSAGE = "The service has not been fully initialized.";

  // The poll iterators used in pagination.
  private Map<String, Iterator<V3Poller>> pollerIterators = new ConcurrentHashMap<>();
  private Map<String, Iterator<V3Voter>> voterIterators = new ConcurrentHashMap<>();
  private Map<String, Iterator<String>> tallyIterators = new ConcurrentHashMap<>();
  private Map<String, Iterator<Repair>> repairIterators = new ConcurrentHashMap<>();
  private Map<String, Iterator<String>> peerIterators = new ConcurrentHashMap<>();

  ////////////////////////////////////////////////////////////////////////////////
  // PARAMS //////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////

  public static final String PREFIX = "org.lockss.poller.";

  /**
   * Default number of polls that will be returned in a single (paged) response
   */
  public static final String PARAM_DEFAULT_POLL_PAGESIZE = PREFIX + "poll.pagesize.default";
  public static final int DEFAULT_DEFAULT_POLL_PAGESIZE = 1000;
  private int defaultPollPageSize = DEFAULT_DEFAULT_POLL_PAGESIZE;

  /**
   * Max number of polls that will be returned in a single (paged) response
   */
  public static final String PARAM_MAX_POLL_PAGESIZE = PREFIX + "poll.pagesize.max";
  public static final int DEFAULT_MAX_POLL_PAGESIZE = 2000;
  private int maxPollPageSize = DEFAULT_MAX_POLL_PAGESIZE;

  /**
   * Interval after which unused poll iterator continuations will
   * be discarded.  Change requires restart to take effect.
   */
  public static final String PARAM_POLL_ITERATOR_TIMEOUT = PREFIX + "poll.iterator.timeout";
  public static final long DEFAULT_POLL_ITERATOR_TIMEOUT = 48 * TimeUtil.HOUR;
  private long pollIteratorTimeout = DEFAULT_POLL_ITERATOR_TIMEOUT;

  ////////////////////////////////////////////////////////////////////////////////
  // CONFIG //////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////

  @Override
  public void setConfig(Configuration newConfig,
                        Configuration prevConfig,
                        Configuration.Differences changedKeys) {
    if (changedKeys.contains(PREFIX)) {
      defaultPollPageSize =
          newConfig.getInt(PARAM_DEFAULT_POLL_PAGESIZE,
              DEFAULT_DEFAULT_POLL_PAGESIZE);
      maxPollPageSize = newConfig.getInt(PARAM_MAX_POLL_PAGESIZE,
          DEFAULT_MAX_POLL_PAGESIZE);
      pollIteratorTimeout =
          newConfig.getTimeInterval(PARAM_POLL_ITERATOR_TIMEOUT,
              DEFAULT_POLL_ITERATOR_TIMEOUT);

      // The first time setConfig() is called, replace the temporary
      // iterator continuation maps with PassiveExpiringMap instances.
      // PassiveExpiringMap is already thread-safe (uses ConcurrentHashMap)
      // and handles expiration automatically, so no manual synchronization
      // or timer is needed.
      if (!(pollerIterators instanceof PassiveExpiringMap)) {
        pollerIterators = new PassiveExpiringMap<>(pollIteratorTimeout);
      }
      if (!(voterIterators instanceof PassiveExpiringMap)) {
        voterIterators = new PassiveExpiringMap<>(pollIteratorTimeout);
      }
      if (!(tallyIterators instanceof PassiveExpiringMap)) {
        tallyIterators = new PassiveExpiringMap<>(pollIteratorTimeout);
      }
      if (!(repairIterators instanceof PassiveExpiringMap)) {
        repairIterators = new PassiveExpiringMap<>(pollIteratorTimeout);
      }
      if (!(peerIterators instanceof PassiveExpiringMap)) {
        peerIterators = new PassiveExpiringMap<>(pollIteratorTimeout);
      }
    }
  }

  /* ------------------------------------------------------------------------
      PollsApiDelegate implementation.
     ------------------------------------------------------------------------
  */

  /*  -------------------Poll Service methods. --------------------------- */

  /**
   * Call a new poll
   *
   * @param body a description of the poll to call
   * @return the identifier for this poll.
   * @see PollsApi#callPoll
   */
  @Override
  public ResponseEntity<String> callPoll(PollDesc body) {
    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    ArchivalUnit au = null;
    String auId = body.getAuId();
    CachedUriSetSpec cuSetSpec = body.getCuSetSpec();
    if (logger.isDebugEnabled()) {
      logger.debug("request to start a poll for au: " + auId);
    }
    try {
      if (!StringUtil.isNullString(auId)) {
        au = getPluginManager().getAuFromId(auId);
      }
    }
    catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.error("No valid au: " + auId);
      }
    }
    if (au != null) {
      PollSpec ps = pollSpecFromDesc(au, cuSetSpec);
      PollManager pm = getPollManager();
      try {
        pm.requestPoll(ps);
      }
      catch (NotEligibleException e) {
        logger.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
      }
      return new ResponseEntity<>(auId, HttpStatus.ACCEPTED);
    }
    return new ResponseEntity<>("No valid au: " + auId, HttpStatus.NOT_FOUND);

  }


  /**
   * Cancel Poll a previously called poll
   *
   * @param psId the poll service id of the called poll
   * @return Void.
   * @see PollsApi#cancelPoll
   */
  @Override
  public ResponseEntity<Void> cancelPoll(String psId) {
    if (logger.isDebugEnabled()) {
      logger.debug("request to cancel poll for " + psId);
    }

    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    PollManager pm = getPollManager();
    ArchivalUnit au;
    try {
      au = getPluginManager().getAuFromId(psId);
      Poll poll = pm.stopPoll(au);
      if (poll != null) {
        return new ResponseEntity<>(HttpStatus.OK);
      }
    }
    catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("unable to locate poll with id " + psId);
        logger.error(e.getMessage(), e);
      }
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Return the current status of a poll.
   *
   * @param psId The poll service id of the called poll
   * @return A summary of the current Polls status.
   * @see PollsApi#getPollStatus
   */
  @Override
  public ResponseEntity<PollerSummary> getPollStatus(String psId) {
    if (logger.isDebugEnabled()) {
      logger.debug("request poll info for " + psId);
    }
    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    ArchivalUnit au;
    try {
      au = getPluginManager().getAuFromId(psId);
      if (au != null) {
        PollManager pm = getPollManager();
        Poll poll = pm.getPoll(au.getAuId());
        if (poll != null) {
          PollerSummary summary = summarizePollerPoll(poll);
          return new ResponseEntity<>(summary, HttpStatus.OK);
        }
      }
    }
    catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("unable to locate poll with id " + psId);
      }
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }


  /*  -------------------Poll Detail methods. --------------------------- */

  /**
   * Get the detailed description of a Poller poll.
   *
   * @param pollKey the PollKey assigned by the Poll Manager
   * @return A PollerPoll detail.
   * @see PollsApi#getPollerPollDetails
   */
  @Override
  public ResponseEntity<PollerDetail> getPollerPollDetails(String pollKey) {
    if (logger.isDebugEnabled()) {
      logger.debug("request poller details for poll with " + pollKey);
    }
    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);
    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);
    if (poll == null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Details for poll " + pollKey + " not found.");
      }
      return new ResponseEntity<>(new PollerDetail(), HttpStatus.NOT_FOUND);
    }
    PollerDetail detail = detailPoll(poll);
    return new ResponseEntity<>(detail, HttpStatus.OK);
  }

  /**
   * Get the detailed description of a Poller poll.
   *
   * @param pollKey the PollKey assigned by the Poll Manager
   * @return A VoterPoll detail.
   * @see PollsApi#getVoterPollDetails
   */
  @Override
  public ResponseEntity<VoterDetail> getVoterPollDetails(String pollKey) {
    if (logger.isDebugEnabled()) {
      logger.debug("request voter details for poll with " + pollKey);
    }
    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);
    if (poll == null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Details for poll " + pollKey + " not found.");
      }
      return new ResponseEntity<>(new VoterDetail(), HttpStatus.NOT_FOUND);
    }
    VoterDetail detail = detailVoterPoll(poll);
    return new ResponseEntity<>(detail, HttpStatus.OK);
  }

  /**
   * Get a Participant peers's urls for a Poller
   *
   * @param pollKey the PollKey assigned by the Poll Manager
   * @param peerId the id of the peer
   * @param urls the type of urls to return
   * @param page the page number of the paged results
   * @param size the size of the page.
   * @return A UrlPager of paged urls.
   * @see PollsApi#getPollPeerVoteUrls
   */
  @Override
  public ResponseEntity<UrlPageInfo> getPollPeerVoteUrls(String pollKey, String peerId, VoterUrlsEnum urls,
                                                          Integer limit, String continuationToken) {
    String parsedRequest = String.format("pollKey: %s, peerId: %s, urls: %s, limit: %s, continuationToken: %s, requestUrl: %s",
        pollKey, peerId, urls, limit, continuationToken, request.getRequestURI());

    if (logger.isDebugEnabled()) {
      logger.debug("getPollPeerVoteUrls called with request {}", parsedRequest);
    }

    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    Integer requestLimit = limit;
    limit = validateLimit(requestLimit, defaultPollPageSize, maxPollPageSize, parsedRequest);

    ContinuationToken requestTct = new ContinuationToken(continuationToken);
    String requestLastUrl = requestTct.getKey();
    String requestIteratorId = requestTct.getIteratorId();

    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);

    if (!(poll instanceof V3Poller)) {
      UrlPageInfo emptyResult = new UrlPageInfo();
      emptyResult.setUrls(Collections.emptyList());
      PageInfo pageInfo = new PageInfo();
      pageInfo.setItemsInPage(0);
      emptyResult.setPageInfo(pageInfo);
      return new ResponseEntity<>(emptyResult, HttpStatus.NOT_FOUND);
    }

    final List<ParticipantUserData> participants = ((V3Poller) poll).getParticipants();
    ParticipantUserData userData = userDataForPeer(peerId, participants);

    if (userData == null || !userData.hasVoted()) {
      UrlPageInfo emptyResult = new UrlPageInfo();
      emptyResult.setUrls(Collections.emptyList());
      PageInfo pageInfo = new PageInfo();
      pageInfo.setItemsInPage(0);
      emptyResult.setPageInfo(pageInfo);
      return new ResponseEntity<>(emptyResult, HttpStatus.NOT_FOUND);
    }

    VoteCounts voteCounts = userData.getVoteCounts();
    if (!voteCounts.hasPeerUrlLists()) {
      UrlPageInfo emptyResult = new UrlPageInfo();
      emptyResult.setUrls(Collections.emptyList());
      PageInfo pageInfo = new PageInfo();
      pageInfo.setItemsInPage(0);
      emptyResult.setPageInfo(pageInfo);
      return new ResponseEntity<>(emptyResult, HttpStatus.NOT_FOUND);
    }

    Collection<String> counts;
    switch (urls) {
      case AGREED:
        counts = voteCounts.getAgreedUrls();
        break;
      case DISAGREED:
        counts = voteCounts.getDisagreedUrls();
        break;
      case POLLERONLY:
        counts = voteCounts.getPollerOnlyUrls();
        break;
      case VOTERONLY:
        counts = voteCounts.getVoterOnlyUrls();
        break;
      default:
        counts = Collections.emptyList();
    }

    Iterator<String> iterator = null;
    if (requestIteratorId != null) {
      iterator = peerIterators.get(requestIteratorId);
      logger.trace("Retrieved iterator from map: iteratorId={}, iterator={}", requestIteratorId, iterator);
    }

    if (iterator == null) {
      iterator = new ArrayList<>(counts).iterator();
      logger.trace("Created new iterator");

      if (requestLastUrl != null) {
        logger.trace("Skipping to last URL: {}", requestLastUrl);
        boolean found = false;
        while (iterator.hasNext()) {
          String url = iterator.next();
          if (url.equals(requestLastUrl)) {
            found = true;
            break;
          }
        }
        if (!found) {
          logger.warn("Continuation URL not found in iterator: {}", requestLastUrl);
        }
      }
    }

    List<String> urlsList = populateTallyUrls(iterator, limit);
    logger.trace("Populated {} URLs", urlsList.size());

    ContinuationToken responseTct = null;
    if (iterator.hasNext()) {
      String lastUrl = urlsList.get(urlsList.size() - 1);
      String iteratorId = UUID.randomUUID().toString();
      peerIterators.put(iteratorId, iterator);
      responseTct = new ContinuationToken(lastUrl, iteratorId);
      logger.trace("Stored iterator and created response continuation token: {}", responseTct);
    }

    PageInfo pageInfo = new PageInfo();
    pageInfo.setItemsInPage(urlsList.size());
    pageInfo.setCurLink(request.getRequestURI());

    if (responseTct != null) {
      String token = responseTct.toWebResponseContinuationToken();
      pageInfo.setContinuationToken(token);

      String nextLink = String.format("%s?limit=%d&continuationToken=%s",
          request.getRequestURI(), limit, UrlUtil.encodeUrl(token));
      pageInfo.setNextLink(nextLink);
      logger.trace("Set nextLink: {}", nextLink);
    }

    UrlPageInfo result = new UrlPageInfo();
    result.setUrls(urlsList);
    result.setPageInfo(pageInfo);

    if (logger.isDebugEnabled()) {
      logger.debug("getPollPeerVoteUrls returning {} URLs", urlsList.size());
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }


  /**
   * Return details of form the RepairQueue of a called poll.
   *
   * @param pollKey the PollKey assigned by the Poll Manager
   * @param repair the kind of repair data to return.
   * @param page the page number of the paged results
   * @param size the size of the page.
   * @return A RepairPager of the current page of urls.
   * @see PollsApi#getRepairQueueData
   */
  @Override
  public ResponseEntity<RepairPageInfo> getRepairQueueData(String pollKey, RepairTypeEnum repair, Integer limit,
                                                            String continuationToken) {
    String parsedRequest = String.format("pollKey: %s, repair: %s, limit: %s, continuationToken: %s, requestUrl: %s",
        pollKey, repair, limit, continuationToken, getFullRequestUrl(request));

    if (logger.isDebugEnabled()) {
      logger.debug("Parsed request: {}", parsedRequest);
    }

    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    Integer requestLimit = limit;
    limit = validateLimit(requestLimit, defaultPollPageSize, maxPollPageSize, parsedRequest);

    // Parse the request continuation token.
    ContinuationToken requestRct = null;

    try {
      requestRct = new ContinuationToken(continuationToken);
      logger.trace("requestRct = {}", requestRct);
    } catch (IllegalArgumentException iae) {
      String message = "Invalid continuation token '" + continuationToken + "'";
      logger.warn(message);

      throw new LockssRestServiceException(
          LockssRestHttpException.ServerErrorType.NONE,
          HttpStatus.BAD_REQUEST,
          message,
          parsedRequest);
    }

    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);

    if (!(poll instanceof V3Poller)) {
      return new ResponseEntity<>(new RepairPageInfo(), HttpStatus.NOT_FOUND);
    }

    PollerStateBean.RepairQueue repairQueue = ((V3Poller) poll).getPollerStateBean().getRepairQueue();
    List<Repair> repairList;
    switch (repair) {
      case ACTIVE:
        repairList = repairQueue.getActiveRepairs();
        break;
      case PENDING:
        repairList = repairQueue.getPendingRepairs();
        break;
      case COMPLETED:
        repairList = repairQueue.getCompletedRepairs();
        break;
      default:
        repairList = new ArrayList<>();
    }

    if (repairList == null) {
      return new ResponseEntity<>(new RepairPageInfo(), HttpStatus.NOT_FOUND);
    }

    List<RepairData> repairs = new ArrayList<>();
    ContinuationToken responseRct = null;
    Iterator<Repair> iterator = null;
    boolean missingIterator = false;

    // Get the iterator ID (if any) used to provide a previous page of results.
    String iteratorId = requestRct.getIteratorId();
    logger.trace("iteratorId = {}", iteratorId);

    // Check whether a previous page of results exists.
    if (iteratorId != null) {
      // Yes: Get the iterator for it.
      iterator = repairIterators.get(iteratorId);
      logger.trace("iterator = {}", iterator);

      // Check whether the iterator was not found.
      if (iterator == null) {
        // Yes: Report the problem.
        missingIterator = true;
        logger.warn("No existing iterator for iteratorId = {}", iteratorId);
      }
    }

    // Check whether an iterator exists for a previous page of results.
    if (iterator == null) {
      // No: Get the repair items.
      iterator = repairList.iterator();
      logger.trace("Created new iterator");

      // Check whether there was a request continuation token.
      if (requestRct.getIteratorId() != null) {
        // Yes: Loop through the repairs until the repair after the last one in the previous
        // page of results is reached.
        while (iterator.hasNext()) {
          Repair repairItem = iterator.next();
          logger.trace("repairUrl = {}", repairItem.getUrl());

          // Check whether this is the repair after the last one in the previous page of results.
          if (requestRct.getKey().equals(repairItem.getUrl())) {
            // Yes: Finish the loop.
            logger.trace("Found last repair URL from previous request");
            break;
          }
        }
      }
    }

    // Populate the page of repairs.
    repairs = populateRepairData(iterator, limit, repair);
    logger.trace("repairs.size() = {}", repairs.size());

    // Check whether there are repairs to be linked to the next page of results.
    if (iterator.hasNext()) {
      // Yes: Peek to get the first repair for the next page.
      String lastRepairUrl = repairs.get(repairs.size() - 1).getRepairUrl();

      // Save the iterator for the next page of results.
      String newIteratorId = UUID.randomUUID().toString();
      repairIterators.put(newIteratorId, iterator);
      logger.trace("Populated repairIterators.size() = {}", repairIterators.size());

      // Create the response continuation token.
      responseRct = new ContinuationToken(lastRepairUrl, newIteratorId);
      logger.trace("responseRct = {}", responseRct);
    }

    // Create the response PageInfo.
    PageInfo pageInfo = new PageInfo();
    pageInfo.setItemsInPage(repairs.size());

    // Get the current link.
    StringBuffer curLinkBuffer = request.getRequestURL();

    if (request.getQueryString() != null
        && !request.getQueryString().trim().isEmpty()) {
      curLinkBuffer.append("?").append(request.getQueryString());
    }

    String curLink = curLinkBuffer.toString();
    logger.trace("curLink = {}", curLink);

    pageInfo.setCurLink(curLink);

    // Check whether there is a response continuation token.
    if (responseRct != null) {
      // Yes.
      String continuationTokenValue = responseRct.toWebResponseContinuationToken();
      pageInfo.setContinuationToken(continuationTokenValue);

      // Start building the next link.
      StringBuffer nextLinkBuffer = request.getRequestURL();
      boolean hasQueryParameters = false;

      // Check if limit parameter exists
      if (curLink.indexOf("limit=") > 0) {
        nextLinkBuffer.append("?limit=").append(requestLimit);
        hasQueryParameters = true;
      }

      // Check if repair parameter exists
      if (curLink.indexOf("repair=") > 0) {
        if (!hasQueryParameters) {
          nextLinkBuffer.append("?");
        } else {
          nextLinkBuffer.append("&");
        }
        nextLinkBuffer.append("repair=").append(repair);
        hasQueryParameters = true;
      }

      if (continuationTokenValue != null) {
        if (!hasQueryParameters) {
          nextLinkBuffer.append("?");
        } else {
          nextLinkBuffer.append("&");
        }

        nextLinkBuffer.append("continuationToken=")
            .append(UrlUtil.encodeUrl(continuationTokenValue));
      }

      String nextLink = nextLinkBuffer.toString();
      logger.trace("nextLink = {}", nextLink);

      pageInfo.setNextLink(nextLink);
    }

    // Create the response RepairPageInfo.
    RepairPageInfo result = new RepairPageInfo();
    result.setRepairs(repairs);
    result.setPageInfo(pageInfo);

    logger.debug("result = {}", result);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Return the Tallied Urls.
   *
   * @param pollKey the PollKey assigned by the Poll Manager
   * @param tally the kind of tally data to return.
   * @param page the page number of the paged results
   * @param size the size of the page.
   * @return A UrlPager of paged urls.
   * @see PollsApi#getTallyUrls
   */
  @Override
  public ResponseEntity<UrlPageInfo> getTallyUrls(String pollKey, TallyTypeEnum tally, Integer limit,
                                                   String continuationToken) {
    String parsedRequest = String.format("pollKey: %s, tally: %s, limit: %s, continuationToken: %s, requestUrl: %s",
        pollKey, tally, limit, continuationToken, getFullRequestUrl(request));

    if (logger.isDebugEnabled()) {
      logger.debug("Parsed request: {}", parsedRequest);
    }

    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    Integer requestLimit = limit;
    limit = validateLimit(requestLimit, defaultPollPageSize, maxPollPageSize, parsedRequest);

    // Parse the request continuation token.
    ContinuationToken requestTct = null;

    try {
      requestTct = new ContinuationToken(continuationToken);
      logger.trace("requestTct = {}", requestTct);
    } catch (IllegalArgumentException iae) {
      String message = "Invalid continuation token '" + continuationToken + "'";
      logger.warn(message);

      throw new LockssRestServiceException(
          LockssRestHttpException.ServerErrorType.NONE,
          HttpStatus.BAD_REQUEST,
          message,
          parsedRequest);
    }

    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);

    if (!(poll instanceof V3Poller)) {
      return new ResponseEntity<>(new UrlPageInfo(), HttpStatus.NOT_FOUND);
    }

    PollerStateBean.TallyStatus tallyStatus = ((V3Poller) poll).getPollerStateBean().getTallyStatus();
    Set<String> tallySet;
    switch (tally) {
      case AGREE:
        tallySet = tallyStatus.getAgreedUrls();
        break;
      case DISAGREE:
        tallySet = tallyStatus.getDisagreedUrls();
        break;
      case ERROR:
        tallySet = tallyStatus.getErrorUrls().keySet();
        break;
      case NOQUORUM:
        tallySet = tallyStatus.getNoQuorumUrls();
        break;
      case TOOCLOSE:
        tallySet = tallyStatus.getTooCloseUrls();
        break;
      default:
        tallySet = new HashSet<>();
    }

    if (tallySet == null) {
      return new ResponseEntity<>(new UrlPageInfo(), HttpStatus.NOT_FOUND);
    }

    List<String> urls = new ArrayList<>();
    ContinuationToken responseTct = null;
    Iterator<String> iterator = null;
    boolean missingIterator = false;

    // Get the iterator ID (if any) used to provide a previous page of results.
    String iteratorId = requestTct.getIteratorId();
    logger.trace("iteratorId = {}", iteratorId);

    // Check whether a previous page of results exists.
    if (iteratorId != null) {
      // Yes: Get the iterator for it.
      iterator = tallyIterators.get(iteratorId);
      logger.trace("iterator = {}", iterator);

      // Check whether the iterator was not found.
      if (iterator == null) {
        // Yes: Report the problem.
        missingIterator = true;
        logger.warn("No existing iterator for iteratorId = {}", iteratorId);
      }
    }

    // Check whether an iterator exists for a previous page of results.
    if (iterator == null) {
      // No: Get the tally URLs.
      iterator = new ArrayList<>(tallySet).iterator();
      logger.trace("Created new iterator");

      // Check whether there was a request continuation token.
      if (requestTct.getIteratorId() != null) {
        // Yes: Loop through the URLs until the URL after the last one in the previous
        // page of results is reached.
        while (iterator.hasNext()) {
          String url = iterator.next();
          logger.trace("url = {}", url);

          // Check whether this is the URL after the last one in the previous page of results.
          if (requestTct.getKey().equals(url)) {
            // Yes: Finish the loop.
            logger.trace("Found last URL from previous request");
            break;
          }
        }
      }
    }

    // Populate the page of URLs.
    urls = populateTallyUrls(iterator, limit);
    logger.trace("urls.size() = {}", urls.size());

    // Check whether there are URLs to be linked to the next page of results.
    if (iterator.hasNext()) {
      // Yes: Peek to get the first URL for the next page.
      String lastUrl = urls.get(urls.size() - 1);

      // Save the iterator for the next page of results.
      String newIteratorId = UUID.randomUUID().toString();
      tallyIterators.put(newIteratorId, iterator);
      logger.trace("Populated tallyIterators.size() = {}", tallyIterators.size());

      // Create the response continuation token.
      responseTct = new ContinuationToken(lastUrl, newIteratorId);
      logger.trace("responseTct = {}", responseTct);
    }

    // Create the response PageInfo.
    PageInfo pageInfo = new PageInfo();
    pageInfo.setItemsInPage(urls.size());

    // Get the current link.
    StringBuffer curLinkBuffer = request.getRequestURL();

    if (request.getQueryString() != null
        && !request.getQueryString().trim().isEmpty()) {
      curLinkBuffer.append("?").append(request.getQueryString());
    }

    String curLink = curLinkBuffer.toString();
    logger.trace("curLink = {}", curLink);

    pageInfo.setCurLink(curLink);

    // Check whether there is a response continuation token.
    if (responseTct != null) {
      // Yes.
      String continuationTokenValue = responseTct.toWebResponseContinuationToken();
      pageInfo.setContinuationToken(continuationTokenValue);

      // Start building the next link.
      StringBuffer nextLinkBuffer = request.getRequestURL();
      boolean hasQueryParameters = false;

      // Check if limit parameter exists
      if (curLink.indexOf("limit=") > 0) {
        nextLinkBuffer.append("?limit=").append(requestLimit);
        hasQueryParameters = true;
      }

      // Check if tally parameter exists
      if (curLink.indexOf("tally=") > 0) {
        if (!hasQueryParameters) {
          nextLinkBuffer.append("?");
        } else {
          nextLinkBuffer.append("&");
        }
        nextLinkBuffer.append("tally=").append(tally);
        hasQueryParameters = true;
      }

      if (continuationTokenValue != null) {
        if (!hasQueryParameters) {
          nextLinkBuffer.append("?");
        } else {
          nextLinkBuffer.append("&");
        }

        nextLinkBuffer.append("continuationToken=")
            .append(UrlUtil.encodeUrl(continuationTokenValue));
      }

      String nextLink = nextLinkBuffer.toString();
      logger.trace("nextLink = {}", nextLink);

      pageInfo.setNextLink(nextLink);
    }

    // Create the response UrlPageInfo.
    UrlPageInfo result = new UrlPageInfo();
    result.setUrls(urls);
    result.setPageInfo(pageInfo);

    logger.debug("result = {}", result);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
  /*  -------------------Poller methods. --------------------------- */

  /**
   * Get the Polls for which we are the poller.
   *
   * @param limit             The requested maximum number of poll summaries per response
   * @param continuationToken The continuation token of the next page of poll summaries to be returned
   * @return A PollerPageInfo used to page in the PollerSummary objects.
   * @see PollsApi#getPollsAsPoller
   */
  @Override
  public ResponseEntity<PollerPageInfo> getPollsAsPoller(Integer limit, String continuationToken) {
    String parsedRequest = String.format("limit: %s, continuationToken: %s, requestUrl: %s",
        limit, continuationToken, getFullRequestUrl(request));

    if (logger.isDebugEnabled()) {
      logger.debug("Parsed request: {}", parsedRequest);
    }

    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    Integer requestLimit = limit;
    limit = validateLimit(requestLimit, defaultPollPageSize, maxPollPageSize, parsedRequest);

    // Parse the request continuation token.
    ContinuationToken requestPct = null;

    try {
      requestPct = new ContinuationToken(continuationToken);
      logger.trace("requestPct = {}", requestPct);
    } catch (IllegalArgumentException iae) {
      String message = "Invalid continuation token '" + continuationToken + "'";
      logger.warn(message);

      throw new LockssRestServiceException(
          LockssRestHttpException.ServerErrorType.NONE,
          HttpStatus.BAD_REQUEST,
          message,
          parsedRequest);
    }

    List<PollerSummary> polls = new ArrayList<>();
    ContinuationToken responsePct = null;
    Iterator<V3Poller> iterator = null;
    boolean missingIterator = false;

    // Get the iterator ID (if any) used to provide a previous page of results.
    String iteratorId = requestPct.getIteratorId();

    // Check whether this request is for a previous page of results.
    if (iteratorId != null) {
      // Yes: Get the iterator (if any) used to provide a previous page of results.
      iterator = pollerIterators.get(iteratorId);
      missingIterator = iterator == null;
    }

    if (iterator == null) {
      // Get the iterator pointing to the first page of results.
      PollManager pm = getPollManager();
      Collection<V3Poller> pollers = pm.getV3Pollers();
      iterator = pollers.iterator();

      // Check whether we need to skip polls already returned.
      if (missingIterator) {
        // Yes: Get the last poll key from the continuation token.
        String lastPollKey = requestPct.getKey();

        // Loop through the polls skipping those already returned.
        while (iterator.hasNext()) {
          V3Poller poll = iterator.next();

          // Check whether this poll comes after the last one returned.
          if (poll.getKey().compareTo(lastPollKey) > 0) {
            // Yes: Add this poll to the results.
            polls.add(summarizePollerPoll(poll));
            // Add the rest of the polls separately.
            break;
          }
        }
      }
    }

    // Populate the rest of the results for this response.
    populatePolls(iterator, limit, polls);

    // Handle iterator storage/removal and create continuation token if needed
    if (iterator.hasNext()) {
      // More results exist: Store iterator and create continuation token
      // If iteratorId is null, this is a new iterator (first page), so generate a new UUID.
      // If iteratorId exists, this is an existing iterator (subsequent page), so reuse
      // the same ID to maintain continuity for the client.
      if (iteratorId == null) {
        iteratorId = UUID.randomUUID().toString();
      }
      pollerIterators.put(iteratorId, iterator);

      // Create the response continuation token.
      PollerSummary lastPoll = polls.get(polls.size() - 1);
      responsePct = new ContinuationToken(lastPoll.getPollKey(), iteratorId);
      logger.trace("responsePct = {}", responsePct);
    } else {
      // No more results: Remove the iterator if it exists
      if (iteratorId != null) {
        pollerIterators.remove(iteratorId);
      }
    }

    logger.trace("polls.size() = {}", polls.size());

    PageInfo pageInfo = new PageInfo();
    pageInfo.setItemsInPage(polls.size());

    // Get the current link.
    StringBuffer curLinkBuffer = request.getRequestURL();

    if (request.getQueryString() != null
        && !request.getQueryString().trim().isEmpty()) {
      curLinkBuffer.append("?").append(request.getQueryString());
    }

    String curLink = curLinkBuffer.toString();
    logger.trace("curLink = {}", curLink);

    pageInfo.setCurLink(curLink);

    // Check whether there is a response continuation token.
    if (responsePct != null) {
      // Yes.
      continuationToken = responsePct.toWebResponseContinuationToken();
      pageInfo.setContinuationToken(continuationToken);

      // Start building the next link.
      StringBuffer nextLinkBuffer = request.getRequestURL();
      boolean hasQueryParameters = false;

      if (curLink.indexOf("limit=") > 0) {
        nextLinkBuffer.append("?limit=").append(requestLimit);
        hasQueryParameters = true;
      }

      if (continuationToken != null) {
        if (!hasQueryParameters) {
          nextLinkBuffer.append("?");
        } else {
          nextLinkBuffer.append("&");
        }

        nextLinkBuffer.append("continuationToken=")
            .append(UrlUtil.encodeUrl(continuationToken));
      }

      String nextLink = nextLinkBuffer.toString();
      logger.trace("nextLink = {}", nextLink);

      pageInfo.setNextLink(nextLink);
    }

    PollerPageInfo pageInfoResponse = new PollerPageInfo();
    pageInfoResponse.setPolls(polls);
    pageInfoResponse.setPageInfo(pageInfo);
    logger.trace("pageInfoResponse = {}", pageInfoResponse);

    logger.debug("Returning OK.");
    return new ResponseEntity<>(pageInfoResponse, HttpStatus.OK);
  }


  /*  -------------------Voter methods. --------------------------- */

  /**
   * Get the Polls for which we are only a voter.
   *
   * @param limit             The requested maximum number of poll summaries per response
   * @param continuationToken The continuation token of the next page of poll summaries to be returned
   * @return A VoterPageInfo used to page in the VoterSummary objects.
   * @see PollsApi#getPollsAsVoter
   */
  @Override
  public ResponseEntity<VoterPageInfo> getPollsAsVoter(Integer limit, String continuationToken) {
    String parsedRequest = String.format("limit: %s, continuationToken: %s, requestUrl: %s",
        limit, continuationToken, getFullRequestUrl(request));

    if (logger.isDebugEnabled()) {
      logger.debug("Parsed request: {}", parsedRequest);
    }

    // Check whether the service has not been fully initialized.
    if (!isReady()) {
      logger.error(NOT_INITIALIZED_MESSAGE);
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    // are we authorized
    AuthUtil.checkHasRole(Roles.ROLE_AU_ADMIN);

    Integer requestLimit = limit;
    limit = validateLimit(requestLimit, defaultPollPageSize, maxPollPageSize, parsedRequest);

    // Parse the request continuation token.
    ContinuationToken requestPct = null;

    try {
      requestPct = new ContinuationToken(continuationToken);
      logger.trace("requestPct = {}", requestPct);
    } catch (IllegalArgumentException iae) {
      String message = "Invalid continuation token '" + continuationToken + "'";
      logger.warn(message);

      throw new LockssRestServiceException(
          LockssRestHttpException.ServerErrorType.NONE,
          HttpStatus.BAD_REQUEST,
          message,
          parsedRequest);
    }

    List<VoterSummary> polls = new ArrayList<>();
    ContinuationToken responsePct = null;
    Iterator<V3Voter> iterator = null;
    boolean missingIterator = false;

    // Get the iterator ID (if any) used to provide a previous page of results.
    String iteratorId = requestPct.getIteratorId();

    // Check whether this request is for a previous page of results.
    if (iteratorId != null) {
      // Yes: Get the iterator (if any) used to provide a previous page of results.
      iterator = voterIterators.remove(iteratorId);
      missingIterator = iterator == null;
    }

    if (iterator == null) {
      // Get the iterator pointing to the first page of results.
      PollManager pm = getPollManager();
      Collection<V3Voter> voters = pm.getV3Voters();
      iterator = voters.iterator();

      // Check whether we need to skip polls already returned.
      if (missingIterator) {
        // Yes: Get the last poll key from the continuation token.
        String lastPollKey = requestPct.getKey();

        // Loop through the polls skipping those already returned.
        while (iterator.hasNext()) {
          V3Voter poll = iterator.next();

          // Check whether this poll comes after the last one returned.
          if (poll.getKey().compareTo(lastPollKey) > 0) {
            // Yes: Add this poll to the results.
            polls.add(summarizeVoterPoll(poll));
            // Add the rest of the polls separately.
            break;
          }
        }
      }
    }

    // Populate the rest of the results for this response.
    populateVoterPolls(iterator, limit, polls);

    // Check whether the iterator may be used in the future to provide more results.
    if (iterator.hasNext()) {
      // Yes: Store it locally.
      String newIteratorId = UUID.randomUUID().toString();
      voterIterators.put(newIteratorId, iterator);

      // Create the response continuation token.
      VoterSummary lastPoll = polls.get(polls.size() - 1);
      responsePct = new ContinuationToken(lastPoll.getPollKey(), newIteratorId);
      logger.trace("responsePct = {}", responsePct);
    }

    logger.trace("polls.size() = {}", polls.size());

    PageInfo pageInfo = new PageInfo();
    pageInfo.setItemsInPage(polls.size());

    // Get the current link.
    StringBuffer curLinkBuffer = request.getRequestURL();

    if (request.getQueryString() != null
        && !request.getQueryString().trim().isEmpty()) {
      curLinkBuffer.append("?").append(request.getQueryString());
    }

    String curLink = curLinkBuffer.toString();
    logger.trace("curLink = {}", curLink);

    pageInfo.setCurLink(curLink);

    // Check whether there is a response continuation token.
    if (responsePct != null) {
      // Yes.
      continuationToken = responsePct.toWebResponseContinuationToken();
      pageInfo.setContinuationToken(continuationToken);

      // Start building the next link.
      StringBuffer nextLinkBuffer = request.getRequestURL();
      boolean hasQueryParameters = false;

      if (curLink.indexOf("limit=") > 0) {
        nextLinkBuffer.append("?limit=").append(requestLimit);
        hasQueryParameters = true;
      }

      if (continuationToken != null) {
        if (!hasQueryParameters) {
          nextLinkBuffer.append("?");
        } else {
          nextLinkBuffer.append("&");
        }

        nextLinkBuffer.append("continuationToken=")
            .append(UrlUtil.encodeUrl(continuationToken));
      }

      String nextLink = nextLinkBuffer.toString();
      logger.trace("nextLink = {}", nextLink);

      pageInfo.setNextLink(nextLink);
    }

    VoterPageInfo pageInfoResponse = new VoterPageInfo();
    pageInfoResponse.setPolls(polls);
    pageInfoResponse.setPageInfo(pageInfo);
    logger.trace("pageInfoResponse = {}", pageInfoResponse);

    logger.debug("Returning OK.");
    return new ResponseEntity<>(pageInfoResponse, HttpStatus.OK);
  }

  /* ------------------ DTO Mappings ---------------------- */

  /**
   * Convert a Poller Service  DTO into a PollManager PollSpec.
   *
   * @param au the ArchivalUnit for the auId in request
   * @param spec the CachedUriSetSpec that defines the polls scope.
   * @return a PollManager PollSpec.
   */
  private PollSpec pollSpecFromDesc(ArchivalUnit au, CachedUriSetSpec spec) {
    if (spec == null) {
      CachedUrlSet cus = au.getAuCachedUrlSet();
      return new PollSpec(cus, org.lockss.poller.Poll.V3_POLL);
    }
    else {
      return new PollSpec(au.getAuId(), spec.getUrlPrefix(), spec.getLowerBound(),
        spec.getUpperBound(), org.lockss.poller.Poll.V3_POLL);
    }
  }

  /**
   * Covert a PollManager PollSpec to a PollDesc
   *
   * @param pollSpec the PollSpec to convert.
   * @return a PollDesc DTO.
   */
  private PollDesc pollDescFromSpec(PollSpec pollSpec) {
    PollDesc pollDesc = new PollDesc();
    pollDesc.setAuId(pollSpec.getAuId());
    CachedUriSetSpec cuss = new CachedUriSetSpec();
    cuss.setUrlPrefix(pollSpec.getUrl());
    cuss.setUpperBound(pollSpec.getUprBound());
    cuss.setLowerBound(pollSpec.getLwrBound());
    pollDesc.setCuSetSpec(cuss);
    pollDesc.setPollType(pollSpec.getPollType());
    pollDesc.setProtocol(pollSpec.getProtocolVersion());
    pollDesc.setVariant(PollVariantEnum.fromValue(pollSpec.getPollVariant().shortName()));
    return pollDesc;
  }

  /**
   * Summarize a PollManager Poller Poll as a PollerSummary DTO
   *
   * @param inPoll the poll to summarize
   * @return a new PollerSummary DTO
   */
  private PollerSummary summarizePollerPoll(Poll inPoll) {
    PollerSummary summary = new PollerSummary();
    if (inPoll instanceof V3Poller v3poller) {
      PollerStateBean psb = v3poller.getPollerStateBean();
      summary.setPollKey(v3poller.getKey());
      summary.setAuId(v3poller.getAu().getAuId());
      summary.setStatus(V3Poller.POLLER_STATUS_STRINGS[v3poller.getStatus()]);
      summary.setStart(v3poller.getCreateTime());
      summary.setVariant(v3poller.getPollVariant().shortName());
      summary.setDeadline(v3poller.getDuration());
      summary.setPollEnd(psb.getPollEnd());
      summary.setParticipants(v3poller.getParticipants().size());
      PollerStateBean.TallyStatus ts = psb.getTallyStatus();
      if (null != ts) {
        summary.setNumTalliedUrls(ts.getTalliedUrlCount());
        summary.setNumAgreeUrls(ts.getAgreedUrlCount());
        summary.setNumHashErrors(ts.getErrorUrlCount());
      }
      summary.setNumCompletedRepairs(v3poller.getCompletedRepairs().size());
      summary.setDetailLink(makeDetailLink("poller/" + psb.getPollKey()));
    }
    return summary;
  }

  /**
   * Summarize a PollManager Voter Poll as a VoterSummary DTO
   *
   * @param inPoll the poll to summarize
   * @return a new VoterSummary DTO
   */
  private VoterSummary summarizeVoterPoll(Poll inPoll) {
    VoterSummary summary = new VoterSummary();
    if (inPoll instanceof V3Voter v3Voter) {
      VoterUserData userData = v3Voter.getVoterUserData();
      summary.setAuId(v3Voter.getAu().getAuId());
      summary.setCaller(v3Voter.getPollerId().getIdString());
      summary.setDeadline(v3Voter.getDeadline().getExpirationTime());
      summary.setPollKey(v3Voter.getKey());
      summary.setStart(v3Voter.getCreateTime());
      summary.setStatus(v3Voter.getStatusString());
      summary.setDetailLink(makeDetailLink("voter/" + userData.getPollKey()));
    }
    return summary;
  }


  /**
   * Convert a PollManager Poll object into a long PollDetail DTO.
   *
   * @param poll the poll to convert.
   * @return a PollDetail DTO
   */
  private PollerDetail detailPoll(Poll poll) {
    PollerDetail detail = new PollerDetail();
    if (poll instanceof V3Poller v3poller) {
      PollerStateBean psb = v3poller.getPollerStateBean();
      // object
      PollDesc desc = pollDescFromSpec(poll.getPollSpec());
      desc.setModulus(psb.getModulus());
      detail.setPollDesc(desc);
      detail.setPollerId(psb.getPollerId().getIdString());
      detail.setStatus(V3Poller.POLLER_STATUS_STRINGS[psb.getStatus()]);
      detail.setPollKey(psb.getPollKey());
      detail.setCreateTime(psb.getCreateTime());
      detail.setDuration(psb.getDuration());
      detail.setDeadline(psb.getPollDeadline());
      detail.setOuterCircleTarget(psb.getOuterCircleTarget());
      detail.setHashAlgorithm(psb.getHashAlgorithm());
      detail.setVoteMargin(psb.getVoteMargin());
      detail.setVoteDeadline(psb.getVoteDeadline());
      detail.setPollEnd(psb.getPollEnd());
      detail.setQuorum(psb.getQuorum());
      detail.setErrorDetails(psb.getErrorDetail());
      detail.setVoteDuration(psb.getVoteDuration());
      for (PeerIdentity peerId : psb.getNoAuPeers()) {
        detail.addNoAuPeersItem(peerId.getIdString());
      }
      for (ParticipantUserData participantData : v3poller.getParticipants()) {
        detail.addVotedPeersItem(
          peerDataFromParticipantData(participantData, psb.getPollKey()));
      }
      TallyData tallyData = tallyDataFromTallyStatus(psb.getTallyStatus(), psb.getPollKey());
      detail.setTally(tallyData);
      RepairQueue repairQueue = repairQueueFromDataRepairQueue(psb.getRepairQueue(),
        psb.getPollKey());
      detail.setRepairQueue(repairQueue);
    }
    return detail;
  }

  /**
   * Convert a PollManager V3Voter Poll into a VoterDetail
   *
   * @param poll the poll to convert
   * @return the VoterDetail.
   */
  private VoterDetail detailVoterPoll(Poll poll) {
    VoterDetail detail = new VoterDetail();
    if (poll instanceof V3Voter v3voter) {
      PollDesc desc = pollDescFromSpec(v3voter.getPollSpec());
      VoterUserData vud = v3voter.getVoterUserData();
      desc.setModulus(vud.getModulus());
      detail.setPollDesc(desc);
      detail.setPollerId(v3voter.getPollerId().getIdString());
      detail.setCallerId(v3voter.getCallerID().getIdString());
      detail.setStatus(vud.getStatusString());
      detail.setPollKey(vud.getPollKey());
      detail.setCreateTime(vud.getCreateTime());
      detail.setDuration(vud.getDuration());
      detail.setDeadline(vud.getDeadline());
      detail.setHashAlgorithm(vud.getHashAlgorithm());
      detail.setVoteDeadline(vud.getVoteDeadline());
      detail.setErrorDetails(vud.getErrorDetail());
      if (v3voter.getStatus() == V3Voter.STATUS_COMPLETE) {
        if (vud.hasReceivedHint()) {
          detail.setAgreement(vud.getAgreementHint());
        }
        if (vud.hasReceivedWeightedHint()) {
          detail.setWtAgreement(vud.getWeightedAgreementHint());
        }
        if (vud.hasReceivedSymmetricAgreement()) {
          detail.setSymmetricAgreement(vud.getSymmetricAgreement());
        }
        if (vud.hasReceivedSymmetricWeightedAgreement()) {
          detail.setWtSymmetricAgreement(vud.getSymmetricAgreement());
        }
      }
      detail.setPollerNonce(ByteArray.toBase64(vud.getPollerNonce()));
      detail.setVoterNonce(ByteArray.toBase64((vud.getVoterNonce())));
      if (vud.isSymmetricPoll()) {
        detail.setVoter2Nonce(ByteArray.toBase64(vud.getVoterNonce2()));
        if (v3voter.getStatus() == V3Voter.STATUS_COMPLETE) {
          detail.setNumAgree(vud.getNumAgreeUrl());
          detail.setNumDisagree(vud.getNumDisagreeUrl());
          detail.setNumPollerOnly(vud.getNumPollerOnlyUrl());
          detail.setNumVoterOnly(vud.getNumVoterOnlyUrl());

        }
      }
    }
    return detail;
  }

  /**
   * Convert a PollManager's ParticipantUserData object to a PeerData DTO.
   *
   * @param voter the voter data to convert
   * @param pollKey the poll key to use for lin construction.
   * @return a PeerData data transfer object.
   */
  private PeerData peerDataFromParticipantData(ParticipantUserData voter, String pollKey) {
    PeerData peerData = new PeerData();
    peerData.setPeerId(voter.getVoterId().getIdString());
    peerData.setStatus(voter.getStatusString());
    peerData.setAgreement(voter.getPercentAgreement());
    ParticipantUserData.VoteCounts voteCounts = voter.getVoteCounts();
    peerData.setNumAgree(voteCounts.getAgreedVotes());
    peerData.setNumDisagree(voteCounts.getDisagreedVotes());
    peerData.setNumPollerOnly(voteCounts.getPollerOnlyVotes());
    peerData.setNumVoterOnly(voteCounts.getVoterOnlyVotes());
    peerData.setBytesHashed(voter.getBytesHashed());
    peerData.setBytesRead(voter.getBytesRead());
    peerData.setWtAgreement(voteCounts.getWeightedPercentAgreement());
    peerData.setWtNumDisagree(voteCounts.getWeightedDisagreedVotes());
    peerData.setWtNumPollerOnly(voteCounts.getWeightedPollerOnlyVotes());
    peerData.setWtNumVoterOnly(voteCounts.getWeightedVoterOnlyVotes());
    PsmInterp interp = voter.getPsmInterp();
    if (interp != null) {
      PsmState state = interp.getCurrentState();
      if (state != null) {
        peerData.setState(state.getName());
        long when = interp.getLastStateChange();
        if (when > 0) {
          peerData.setLastStateChange(when);
        }
      }
    }
    String peerId = peerData.getPeerId();
    peerData.setAgreeLink(makePeerLink(pollKey, peerId, "agree"));
    peerData.setDisagreeLink(makePeerLink(pollKey, peerId, "disagree"));
    peerData.setPollerOnlyLink(makePeerLink(pollKey, peerId, "pollerOnly"));
    peerData.setVoterOnlyLink(makePeerLink(pollKey, peerId, "voterOnly"));
    return peerData;
  }

  /**
   * Convert a PollManager's RepairQueue object into a RepairQueue DTO
   *
   * @param inQueue the RepairQueue to convert
   * @param pollKey the key of the repair queue.
   * @return a PollService Repair Queue
   */
  private RepairQueue repairQueueFromDataRepairQueue(PollerStateBean.RepairQueue inQueue,
    String pollKey) {
    RepairQueue outQueue = new RepairQueue();
    outQueue.setNumActive(inQueue.getActiveRepairs().size());
    outQueue.setNumPending(inQueue.getPendingRepairs().size());
    outQueue.setNumCompleted(inQueue.getCompletedRepairs().size());
    outQueue.setActiveLink(makeRepairQLink(pollKey, "active"));
    outQueue.setCompletedLink(makeRepairQLink(pollKey, "completed"));
    outQueue.setPendingLink(makeRepairQLink(pollKey, "pending"));
    return outQueue;
  }

  /**
   * Convert a  PollManager's TallyStatus object into a TallyData DTO
   *
   * @param tallyStatus The TallyStatus object to convert
   * @param pollKey the key to use for the links.
   * @return a new TallyData object
   */
  private TallyData tallyDataFromTallyStatus(PollerStateBean.TallyStatus tallyStatus,
    String pollKey) {
    TallyData tallyData = new TallyData();
    tallyData.setNumAgree(tallyStatus.getAgreedUrlCount());
    tallyData.setNumDisagree(tallyStatus.getDisgreedUrlCount());
    tallyData.setNumNoQuorum(tallyStatus.getNoQuorumUrlCount());
    tallyData.setNumTooClose(tallyStatus.getTooCloseUrlCount());
    tallyData.setNumError(tallyStatus.getErrorUrlCount());
    tallyData.setWtAgreed(tallyStatus.getWeightedAgreedCount());
    tallyData.setWtDisagreed(tallyStatus.getWeightedDisagreedCount());
    tallyData.setWtNoQuorum(tallyStatus.getWeightedNoQuorumCount());
    tallyData.setWtTooClose(tallyStatus.getWeightedTooCloseCount());
    tallyData.setAgreeLink(makeTallyLink(pollKey, "agree"));
    tallyData.setDisagreeLink(makeTallyLink(pollKey, "disagree"));
    tallyData.setNoQuorumLink(makeTallyLink(pollKey, "noQuorum"));
    tallyData.setTooCloseLink(makeTallyLink(pollKey, "tooClose"));
    tallyData.setErrorLink(makeTallyLink(pollKey, "error"));
    return tallyData;
  }

  /**
   * Make a link to the details for a poll.
   *
   * @param pollKey the key to the poll.
   * @return a link to the polls detailed data.
   */
  private LinkDesc makeDetailLink(String pollKey) {
    try {
      // build a path element: "/polls/{pollKey}/"
      String prefix = UrlUtil.getUrlPrefix(request.getRequestURI());
      LinkDesc ldesc = new LinkDesc();
      ldesc.setLink(prefix + "/polls/" + pollKey + "/details");
      return ldesc;
    }
    catch (MalformedURLException e) {
      logger.error(DETAIL_UNAVAILABLE);
      // throw or ErrorDesc.
    }
    return null;
  }

  /**
   * Make a link to the tally details for a poll.
   *
   * @param pollKey the key for the poll
   * @param tallyType the type of tally data to return.
   * @return a new Link description object.
   */
  private LinkDesc makeTallyLink(String pollKey, String tallyType) {
    try {
      // build a path element: "/polls/{pollKey}/tally?tally=type"
      String prefix = UrlUtil.getUrlPrefix(request.getRequestURI());
      LinkDesc ldesc = new LinkDesc();
      ldesc.setLink(prefix + "/polls/" + pollKey + "/tally?tally=" + tallyType);
      return ldesc;
    }
    catch (MalformedURLException e) {
      logger.error(DETAIL_UNAVAILABLE);
      // throw or ErrorDesc.
    }
    return null;
  }

  /**
   * Make a link to the repair queue details for a poll.
   *
   * @param pollKey the key for the poll.
   * @param repairType the repair data to provide.
   * @return a new Link description object.
   */
  private LinkDesc makeRepairQLink(String pollKey, String repairType) {
    try {
      //  build a path element: /polls/{pollKey}/repairs?repair=type
      String prefix = UrlUtil.getUrlPrefix(request.getRequestURI());
      LinkDesc ldesc = new LinkDesc();
      ldesc.setLink(prefix + "/polls/" + pollKey + "/repairs?repair=" + repairType);
      return ldesc;
    }
    catch (MalformedURLException e) {
      logger.error(DETAIL_UNAVAILABLE);
    }
    return null;
  }

  /**
   * Make a link to the tally details for a peer.
   *
   * @param pollKey the pollKey for the link.
   * @param peerId the peerId for the link.
   * @param tallyType the tally data to provide.
   * @return a new Link description object.
   */
  private LinkDesc makePeerLink(String pollKey, String peerId, String tallyType) {
    // /polls/{pollKey}/peer/{peerId}?tally=
    try {
      String prefix = UrlUtil.getUrlPrefix(request.getRequestURI());
      LinkDesc ldesc = new LinkDesc();
      ldesc.setLink(prefix + "/polls/" + pollKey + "/peer/" + peerId + "?tally=" + tallyType);
      return ldesc;
    }
    catch (MalformedURLException e) {
      logger.error(DETAIL_UNAVAILABLE);
    }
    return null;
  }

  /**
   * Find the user data for a voting peer.
   *
   * @param peerId the Peer Id of the voting peer
   * @param participants the list of voting peers
   * @return the ParticipantUserData for the Peer or null if not found.
   */
  private ParticipantUserData userDataForPeer(String peerId,
    List<ParticipantUserData> participants) {
    if (peerId == null || participants == null || participants.isEmpty()) {
      return null;
    }
    for (ParticipantUserData userData : participants) {
      if (userData.getVoterId().getIdString().equals(peerId)) {
        return userData;
      }
    }
    return null;
  }


  /* ------------------------------------------------------------------------
      Lockss App integration Support
     ------------------------------------------------------------------------
  */

  /**
   * Provides the Lockss daemon instance
   *
   * @return the LockssDaemon
   */
  LockssDaemon getLockssDaemon() {
    if (theDaemon == null) {
      theDaemon = LockssDaemon.getLockssDaemon();
    }
    return theDaemon;
  }

  /**
   * Provides the poll manager.
   *
   * @return the current Lockss PollManager.
   */
  PollManager getPollManager() {
    if (pollManager == null) {
      pollManager = getLockssDaemon().getPollManager();
    }
    return pollManager;
  }
  void setPollManager(PollManager pollManager) {
    this.pollManager = pollManager;
  }
  /**
   * Provides the plugin manager.
   *
   * @return the current Lockss PluginManager.
   */
  public PluginManager getPluginManager() {
    if (pluginManager == null) {
      pluginManager = getLockssDaemon().getPluginManager();
    }
    return pluginManager;
  }

  protected boolean isReady() {
    return waitReady();
  }

  /* ------------------------------------------------------------------------
      Utility methods for paging
     ------------------------------------------------------------------------
  */

  /**
   * Provides the full URL of the request.
   *
   * @param request An HttpServletRequest with the HTTP request.
   * @return a String with the full URL of the request.
   */
  static String getFullRequestUrl(HttpServletRequest request) {
    if (request == null) {
      logger.warn("request = null");
      return "";
    }

    if (request.getQueryString() == null
        || request.getQueryString().trim().isEmpty()) {
      return "'" + request.getMethod() + " " + request.getRequestURL() + "'";
    }

    return "'" + request.getMethod() + " " + request.getRequestURL() + "?"
        + request.getQueryString() + "'";
  }

  /**
   * Validates the page size specified in the request.
   *
   * @param requestLimit An Integer with the page size specified in the request.
   * @param defaultValue An int with the value to be used when no page size is
   *                     specified in the request.
   * @param maxValue     An int with the maximum allowed value for the page
   *                     size.
   * @param parsedRequest A String with the parsed request for diagnostic purposes.
   * @return an int with the validated value for the page size.
   */
  static int validateLimit(Integer requestLimit, int defaultValue, int maxValue,
                           String parsedRequest) {
    logger.debug("requestLimit = {}, defaultValue = {}, maxValue = {}",
        requestLimit, defaultValue, maxValue);

    // Check whether it's not a positive integer.
    if (requestLimit != null && requestLimit.intValue() <= 0) {
      // Yes: Report the problem.
      String message =
          "Limit of requested items must be a positive integer; it was '"
              + requestLimit + "'";
      logger.warn(message);

      throw new LockssRestServiceException(
          LockssRestHttpException.ServerErrorType.NONE, HttpStatus.BAD_REQUEST,
          message, parsedRequest);
    }

    // No: Get the result.
    int result = requestLimit == null ?
        Math.min(defaultValue, maxValue) : Math.min(requestLimit, maxValue);
    logger.debug("result = {}", result);
    return result;
  }

  /**
   * Populates the polls to be included in the response.
   *
   * @param iterator An Iterator with the poll source iterator.
   * @param limit    An Integer with the maximum number of polls to be
   *                 included in the response.
   * @param polls    A List with the poll summaries to be included in the
   *                 response.
   */
  private void populatePolls(Iterator<V3Poller> iterator, Integer limit,
                             List<PollerSummary> polls) {
    logger.debug("limit = {}, polls = {}", limit, polls);
    int pollCount = polls.size();

    // Loop through as many polls that exist and are requested.
    while (pollCount < limit && iterator.hasNext()) {
      // Add this poll to the results.
      polls.add(summarizePollerPoll(iterator.next()));
      pollCount++;
    }
  }

  /**
   * Populates the voter polls to be included in the response.
   *
   * @param iterator An Iterator with the voter poll source iterator.
   * @param limit    An Integer with the maximum number of polls to be
   *                 included in the response.
   * @param polls    A List with the voter poll summaries to be included in the
   *                 response.
   */
  private void populateVoterPolls(Iterator<V3Voter> iterator, Integer limit,
                                   List<VoterSummary> polls) {
    logger.debug("limit = {}, polls = {}", limit, polls);
    int pollCount = polls.size();

    // Loop through as many polls that exist and are requested.
    while (pollCount < limit && iterator.hasNext()) {
      // Add this poll to the results.
      polls.add(summarizeVoterPoll(iterator.next()));
      pollCount++;
    }
  }

  /**
   * Populates the tally URLs to be included in the response.
   *
   * @param iterator An Iterator with the URL source iterator.
   * @param limit    An Integer with the maximum number of URLs to be
   *                 included in the response.
   * @return A List with the URLs to be included in the response.
   */
  private List<String> populateTallyUrls(Iterator<String> iterator, Integer limit) {
    logger.debug("limit = {}", limit);
    List<String> urls = new ArrayList<>();

    // Loop through as many URLs that exist and are requested.
    while (urls.size() < limit && iterator.hasNext()) {
      // Add this URL to the results.
      urls.add(iterator.next());
    }

    logger.debug("urls.size() = {}", urls.size());
    return urls;
  }

  /**
   * Populates the repair data to be included in the response.
   *
   * @param iterator An Iterator with the Repair source iterator.
   * @param limit    An Integer with the maximum number of repair items to be
   *                 included in the response.
   * @param repair   A RepairTypeEnum indicating the repair type (COMPLETED, ACTIVE, PENDING)
   * @return A List with the RepairData items to be included in the response.
   */
  private List<RepairData> populateRepairData(Iterator<Repair> iterator, Integer limit, RepairTypeEnum repair) {
    logger.debug("limit = {}, repair = {}", limit, repair);
    List<RepairData> repairs = new ArrayList<>();

    // Loop through as many repair items that exist and are requested.
    while (repairs.size() < limit && iterator.hasNext()) {
      Repair repairItem = iterator.next();
      RepairData rdata = new RepairData();
      rdata.setRepairUrl(repairItem.getUrl());
      rdata.setRepairFrom(repairItem.getRepairFrom().getIdString());
      if (RepairTypeEnum.COMPLETED.equals(repair)) {
        rdata.setResult(RepairData.ResultEnum.fromValue(repairItem.getTallyResult().toString()));
      }
      repairs.add(rdata);
    }

    logger.debug("repairs.size() = {}", repairs.size());
    return repairs;
  }

}
