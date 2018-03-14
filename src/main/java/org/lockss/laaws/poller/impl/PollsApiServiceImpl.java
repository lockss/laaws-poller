/*
 * Copyright (c) 2018 Board of Trustees of Leland Stanford Jr. University,
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

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.mortbay.util.B64Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.lockss.laaws.poller.api.PollsApiDelegate;
import org.lockss.laaws.poller.model.*;
import org.lockss.app.LockssApp;
import org.lockss.app.LockssDaemon;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.plugin.PluginManager;
import org.lockss.poller.Poll;
import org.lockss.poller.PollManager;
import org.lockss.poller.PollManager.NotEligibleException;
import org.lockss.poller.PollSpec;
import org.lockss.rs.status.ApiStatus;
import org.lockss.rs.status.SpringLockssBaseApiController;
import org.lockss.util.ByteArray;

@Service
public class PollsApiServiceImpl extends SpringLockssBaseApiController implements PollsApiDelegate {

  private static Logger logger = LoggerFactory.getLogger(PollsApiServiceImpl.class);
  private static final String API_VERSION = "1.0.0";
  private PollManager pollManager;
  private PluginManager pluginManager;
  private ModelMapper modelMapper = new ModelMapper();
  private HashMap<String, PollSpec> requestMap = new HashMap<>();
  @Autowired
  private HttpServletRequest request;



  /* ------------------------------------------------------------------------
        SpringLockssBaseApiController implementation.
       ------------------------------------------------------------------------
    */
  @Override
  public ApiStatus getApiStatus() {
    return new ApiStatus()
        .setVersion(API_VERSION)
        .setReady(LockssApp.getLockssApp().isAppRunning());
  }

  /* ------------------------------------------------------------------------
      PollsApiDelegate implementation.
     ------------------------------------------------------------------------
  */

  /*  -------------------Poll Service methods. --------------------------- */

  /**
   * @see PollsApiDelegate#callPoll
   */
  @Override
  public ResponseEntity<String> callPoll(PollDesc  body) {
    String au_id = body.getAuId();
    if (logger.isDebugEnabled()) {
      logger.debug("request to start a poll for au: " + au_id);
    }
    PollSpec ps = pollSpecFromDesc(body);
    PollManager pm = getPollManager();
    try {
      pm.requestPoll(ps);
    } catch (NotEligibleException e) {
      logger.error(e.getMessage());
      return new ResponseEntity<>(e.getMessage(),HttpStatus.FORBIDDEN);
    }
    String auId = ps.getAuId();
    requestMap.put(auId, ps);
    return new ResponseEntity<>(auId, HttpStatus.ACCEPTED);
  }


  /**
   * @see PollsApiDelegate#cancelPoll
   */
  @Override
  public ResponseEntity<Void> cancelPoll(String psId) {
    if (logger.isDebugEnabled()) {
      logger.debug("request to cancel poll for " + psId);
    }
    PollManager pm = getPollManager();
    ArchivalUnit au;
    PollSpec spec = requestMap.remove(psId);
    if(spec != null) {
      au = spec.getCachedUrlSet().getArchivalUnit();
    }
    else {
      au = getPluginManager().getAuFromId(psId);
    }
    Poll poll = pm.stopPoll(au);
    if(poll != null) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    if(logger.isDebugEnabled()) {
      logger.debug("unable to locate poll with id " + psId);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * @see PollsApiDelegate#getPollStatus
   */
  @Override
  public ResponseEntity<PollerSummary> getPollStatus(String psId) {
    if (logger.isDebugEnabled()) {
      logger.debug("request poll info for " + psId);
    }
    PollSpec spec = requestMap.get(psId);
    ArchivalUnit au;
    if(spec != null) {
      au = spec.getCachedUrlSet().getArchivalUnit();
    }
    else {
      au = getPluginManager().getAuFromId(psId);
    }
    if(au != null) {
      PollManager pm = getPollManager();
      Poll poll = pm.getPoll(au.getAuId());
      if (poll != null) {
        PollerSummary summary = summarizePoll(poll);
        return new ResponseEntity<>(summary, HttpStatus.OK);
      }
    }
    if(logger.isDebugEnabled()) {
      logger.debug("unable to locate poll with id " + psId);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);

  }


  /*  -------------------Poll Detail methods. --------------------------- */

  /**
   * @see PollsApiDelegate#getPollDetails
   */
  @Override
  public ResponseEntity<PollDetail> getPollDetails(String pollKey) {
    if (logger.isDebugEnabled()) {
      logger.debug("request poll details for poll with " + pollKey);
    }
    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);
    if(poll == null)
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    PollDetail detail = detailFromPoll(poll);
    return new ResponseEntity<>(detail, HttpStatus.OK);
  }


  /**
   * @see PollsApiDelegate#getPollPeerVoteUrls
   */
  @Override
  public ResponseEntity<UrlPager> getPollPeerVoteUrls(String pollKey, String peerId, String urls,
      Integer page, Integer size) {
    return null;
  }


  /**
   * @see PollsApiDelegate#getRepairQueueData
   */
  @Override
  public ResponseEntity<RepairPager> getRepairQueueData(String pollKey, String repair, Integer page,
      Integer size) {
    return null;
  }

  /**
   * @see PollsApiDelegate#getTallyUrls
   */
  @Override
  public ResponseEntity<UrlPager> getTallyUrls(String pollKey, String tally, Integer page,
      Integer size) {
    return null;
  }

  /*  -------------------Poller methods. --------------------------- */

  /**
   * @see PollsApiDelegate#getPollsAsPoller
   */
  @Override
  public ResponseEntity<PollerPager> getPollsAsPoller(Integer size, Integer page) {
    return null;
  }

  /*  -------------------Voter methods. --------------------------- */

  /**
   * @see PollsApiDelegate#getPollsAsVoter
   */
  @Override
  public ResponseEntity<VoterPager> getPollsAsVoter(Integer size, Integer page) {
    return null;
  }

  /* ------------------ DTO Mappings ---------------------- */
  private PollSpec pollSpecFromDesc(PollDesc pollDesc) {
    PollSpec pspec = new PollSpec(
        pollDesc.getAuId(),
        pollDesc.getCuSetSpec().getUrlPrefix(),
        pollDesc.getCuSetSpec().getLowerBound(),
        pollDesc.getCuSetSpec().getUpperBound(),
        org.lockss.poller.Poll.V3_POLL);
    return pspec;
  }

  private PollerSummary summarizePoll(Poll poll) {
    PollerSummary summary = new PollerSummary();
    return summary;
  }

  private PollDetail detailFromPoll(Poll poll) {
    PollDetail detail = new PollDetail();
    return detail;
  }


  /* ------------------------------------------------------------------------
      Lockss App integration Support
     ------------------------------------------------------------------------
  */

  /**
   * Provides the poll manager.
   *
   * @return a MetadataManager with the metadata manager.
   */
  private PollManager getPollManager() {
    if (pollManager == null) {
      pollManager = LockssDaemon.getLockssDaemon().getPollManager();
    }
    return pollManager;
  }

  /**
   * Provides the plugin manager.
   *
   * @return a MetadataManager with the metadata manager.
   */
  private PluginManager getPluginManager() {
    if (pluginManager == null) {
      pluginManager = LockssDaemon.getLockssDaemon().getPluginManager();
    }
    return pluginManager;
  }
}
