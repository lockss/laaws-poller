package org.lockss.laaws.poller.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.lockss.app.LockssApp;
import org.lockss.app.LockssDaemon;
import org.lockss.laaws.poller.api.PollsApiDelegate;
import org.lockss.laaws.poller.api.PollsApi;
import org.lockss.laaws.poller.model.CachedUriSet;
import org.lockss.laaws.poller.model.CachedUriSetSpec;
import org.lockss.laaws.poller.model.PageInfo;
import org.lockss.laaws.poller.model.Poll;
import org.lockss.laaws.poller.model.PollPageInfo;
import org.lockss.laaws.poller.model.PollRepairQueue;
import org.lockss.laaws.poller.model.PollReq;
import org.lockss.laaws.poller.model.PollSpec;
import org.lockss.laaws.poller.model.PollTallyStatus;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.plugin.PluginManager;
import org.lockss.poller.PollManager;
import org.lockss.poller.PollManager.NotEligibleException;
import org.lockss.poller.v3.PollerStateBean;
import org.lockss.poller.v3.PollerStateBean.RepairQueue;
import org.lockss.poller.v3.PollerStateBean.TallyStatus;
import org.lockss.poller.v3.V3Poller;
import org.lockss.poller.v3.V3Voter;
import org.lockss.poller.v3.VoterUserData;
import org.lockss.rs.status.ApiStatus;
import org.lockss.rs.status.SpringLockssBaseApiController;
import org.lockss.util.ByteArray;
import org.modelmapper.ModelMapper;
import org.mortbay.util.B64Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PollsApiServiceImpl extends SpringLockssBaseApiController implements PollsApiDelegate  {

  private static Logger logger = LoggerFactory.getLogger(PollsApiServiceImpl.class);
  private static final String API_VERSION = "1.0.0";
  private PollManager pollManager;
  private PluginManager pluginManager;
  private ModelMapper modelMapper = new ModelMapper();
  private HashMap<String,PollReq> requestMap = new HashMap<>();
  @Autowired
  private HttpServletRequest request;

  /**
   * @see PollsApi#requestPoll
   */
  @Override
  public ResponseEntity<String> requestPoll(PollReq req) {
    String au_id = req.getAuid();
    PollManager pm =getPollManager();
    ArchivalUnit au = getPluginManager().getAuFromId(au_id);
    org.lockss.poller.PollSpec spec = new org.lockss.poller.PollSpec(au_id,
        req.getUrl(),
        req.getLower(),
        req.getUpper(),
        req.getPollType());
    try {
      pm.enqueueHighPriorityPoll(au, spec);
      return new ResponseEntity<>(addPollJob(req),HttpStatus.ACCEPTED);
    } catch (NotEligibleException e) {
      logger.error(e.getMessage());
    }
    return new ResponseEntity<>(au_id, HttpStatus.NOT_ACCEPTABLE);
  }
  
  /**
   * @see PollsApi#cancelPoll
   */
  @Override
  public ResponseEntity<Poll> cancelPoll(String pollId) {
    if (logger.isDebugEnabled()) {
      logger.debug("request to cancel poll " + pollId);
    }
    PollReq req = requestMap.remove(pollId);
    if(req != null) {
      PollManager pm = getPollManager();
      ArchivalUnit au = getPluginManager().getAuFromId(req.getAuid());
      List<org.lockss.poller.Poll> polls = pm.stopAuPolls(au);
      Poll retPoll = findPollForReq(polls, req);
      if (retPoll != null) {
            return new ResponseEntity<>(retPoll, HttpStatus.OK);
      }
    }
    if(logger.isDebugEnabled()) {
      logger.debug("unable to locate poll with id " + pollId);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * @see PollsApi#getPoll
   */
  @Override
  public ResponseEntity<Poll> getPoll(String pollId) {
    if (logger.isDebugEnabled()) {
      logger.debug("request poll info for " + pollId);
    }
    PollReq req = requestMap.get(pollId);
    if(req != null) {
      PollManager pm = getPollManager();
      ArchivalUnit au = getPluginManager().getAuFromId(req.getAuid());
      List<org.lockss.poller.Poll> polls = pm.getAuPolls(au);
      Poll retPoll = findPollForReq(polls, req);
      if (retPoll != null) {
        return new ResponseEntity<>(retPoll, HttpStatus.OK);
      }
    }
    if(logger.isDebugEnabled()) {
      logger.debug("unable to locate poll with id " + pollId);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);

  }

  /**
   * @see PollsApi#getPolls
   */
  @Override
  public ResponseEntity<PollPageInfo> getPolls(Integer size, Integer page) {
    if (logger.isDebugEnabled()) {
      logger.debug("request for  a page " + page + " of poll queue with page size " + size);
    }
    PollPageInfo result = new PollPageInfo();
    try {
      PageInfo pi = new PageInfo();
      PollManager pm =getPollManager();
      String curLink = request.getRequestURI();
      String nextLink = curLink;

      if (page != null) {
        curLink = curLink + "?page=" + page;
        nextLink = nextLink + "?page=" + (page + 1);

        if (size != null) {
          curLink = curLink + "&size=" + size;
          nextLink = nextLink + "&size=" + size;
        }
      } else if (size != null) {
        curLink = curLink + "?size=" + size;
        nextLink = nextLink + "?size=" + size;
      }

      if (log.isDebugEnabled()) {
        log.debug("curLink = " + curLink);
        log.debug("nextLink = " + nextLink);
      }
      //todo: fix so this is correct
      pi.setPrevLink(curLink);
      pi.setNextLink(nextLink);
      pi.setCurrentPage(page);
      pi.setResultsPerPage(size);


      result.setPageInfo(pi);
      List<org.lockss.poller.Poll> pollList = new ArrayList<>();
      pollList.addAll(pm.getV3Pollers());
      pollList.addAll(pm.getV3Voters());
      int len = pollList.size();
      int index = 0;
      int endp = 10;
      if (page != null && size != null) {
        //find the start index
        if (len > size) {
          index = (page * size) - size;
          if (index >= len) {
            index = len - size;
          }
          if (index < 0) {
            index = 0;
          }
        }
        // find the endpoint
        if (size > 0 && size < len) {
          endp = Math.min(len, index + size);
        }
      }
      //add the next size elements
      for(int ix = index; ix<endp; ix++) {
        result.addPollsItem(makeSummaryPollFromPoll(pollList.get(ix)));
      }
    } catch (Exception e) {
      String message =
          "Cannot get polls for page = " + page + ", size = " + size;
      logger.error(message, e);
      throw new RuntimeException(message);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }


  @Override
  public ApiStatus getApiStatus() {

    return new ApiStatus()
        .setVersion(API_VERSION)
        .setReady(LockssApp.getLockssApp().isAppRunning());
  }

  protected org.lockss.laaws.poller.model.Poll makePollFromPoll(org.lockss.poller.Poll inPoll) {
    org.lockss.laaws.poller.model.Poll outPoll = new Poll();
    if(inPoll instanceof V3Poller) {
      V3Poller v3poller = (V3Poller)inPoll;
      pollerPollToPoll(v3poller, outPoll);
      outPoll.setQuorum(v3poller.getQuorum());
      outPoll.setVoteMargin(v3poller.getVoteMargin());
      PollerStateBean psb = v3poller.getPollerStateBean();
      outPoll.setVotedPeers(psb.votedPeerCount());
      outPoll.setRepairQueue(buildPollRepairQueue(psb.getRepairQueue()));
      outPoll.setTallyStatus(buildTallyStatus(psb.getTallyStatus()));
    }
    else if (inPoll instanceof V3Voter)
    {
      V3Voter v3Voter = (V3Voter)inPoll;
      voterPollToPoll(v3Voter, outPoll);
    }
    return outPoll;
  }


  protected org.lockss.laaws.poller.model.Poll makeSummaryPollFromPoll(org.lockss.poller.Poll inPoll) {
    org.lockss.laaws.poller.model.Poll outPoll = new Poll();
    if(inPoll instanceof V3Poller) {
      V3Poller v3poller = (V3Poller) inPoll;
      pollerPollToPoll(v3poller, outPoll);
    }
    else if (inPoll instanceof V3Voter) {
      V3Voter v3Voter = (V3Voter)inPoll;
      voterPollToPoll(v3Voter, outPoll);
    }
    return outPoll;
  }

  private PollRepairQueue buildPollRepairQueue(RepairQueue repairQueue) {
    PollRepairQueue outQueue = new PollRepairQueue();
    modelMapper.map(repairQueue, outQueue);
    return outQueue;
  }
  private PollTallyStatus buildTallyStatus(TallyStatus tallyStatus)
  {
    PollTallyStatus outTally = new PollTallyStatus();
    modelMapper.map(tallyStatus, outTally);
    return outTally;
  }

  private void pollerPollToPoll(V3Poller v3poller, Poll outPoll) {
    PollerStateBean bean = v3poller.getPollerStateBean();
    outPoll.setPollKey(bean.getPollKey());
    outPoll.setHashAlgorithm(bean.getHashAlgorithm());
    outPoll.setCreateTime(bean.getCreateTime());
    outPoll.setPollDeadline(bean.getPollDeadline());
    outPoll.setDuration(bean.getDuration());
    outPoll.setPollEnd(bean.getPollEnd());
    outPoll.setPollerId(bean.getPollerId().getIdString());
    outPoll.setStatus(V3Poller.POLLER_STATUS_STRINGS[bean.getStatus()]);
    PollSpec spec = new PollSpec();
    spec.setModulus(bean.getModulus());
    spec.setPollType(bean.getPollSpec().getPollType());
    spec.setPollVariant(PollSpec.PollVariantEnum.fromValue(bean.getPollVariant().shortName()));
    spec.setProtocolVersion(bean.getProtocolVersion());
    spec.setPluginPollVersion(bean.getPluginVersion());
    CachedUriSet cus = new CachedUriSet();
    cus.setAuId(bean.getAuId());
    CachedUriSetSpec cuss = new CachedUriSetSpec();
    cuss.setUrlPrefix(bean.getUrl());
    cuss.setLowerBound(bean.getPollSpec().getLwrBound());
    cuss.setUpperBound(bean.getPollSpec().getUprBound());
    cus.setSpec(cuss);
    spec.setCachedUriSet(cus);
    outPoll.setPollSpec(spec);
  }

  private void voterPollToPoll(V3Voter v3Voter, Poll outPoll) {
    VoterUserData userData = v3Voter.getVoterUserData();
    outPoll.setPollKey(userData.getPollKey());
    outPoll.setHashAlgorithm(userData.getHashAlgorithm());
    outPoll.setCreateTime(userData.getCreateTime());
    outPoll.setPollDeadline(userData.getDeadline());
    outPoll.setDuration(userData.getDuration());
    outPoll.setPollerId(userData.getPollerId().getIdString());
    outPoll.setStatus(userData.getStatusString());
    PollSpec spec = new PollSpec();
    spec.setModulus(userData.getModulus());
    spec.setPollType(userData.getPollSpec().getPollType());
    spec.setProtocolVersion(userData.getPollVersion());
    spec.setPluginPollVersion(userData.getPluginVersion());
    CachedUriSet cus = new CachedUriSet();
    cus.setAuId(userData.getAuId());
    CachedUriSetSpec cuss = new CachedUriSetSpec();
    cuss.setUrlPrefix(userData.getPollSpec().getUrl());
    cuss.setLowerBound(userData.getPollSpec().getLwrBound());
    cuss.setUpperBound(userData.getPollSpec().getUprBound());
    cus.setSpec(cuss);
    spec.setCachedUriSet(cus);
    outPoll.setPollSpec(spec);
  }

  /**
   * Provides the poll manager.
   *
   * @return a MetadataManager with the metadata manager.
   */
  private PollManager getPollManager() {
    if(pollManager == null) {
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
    if(pluginManager == null) {
      pluginManager = LockssDaemon.getLockssDaemon().getPluginManager();
    }
    return pluginManager;
  }

  /**
   * Add a Poll Request to the job queue
   * @param req add the request to our local queue
   * @return the local identifier for this poll
   */
  private String addPollJob(PollReq req) {
    String key =
        String.valueOf(B64Code.encode(ByteArray.makeRandomBytes(20)));
    requestMap.put(key, req);
    return key;
  }
  
  /**
   * Find a PollManager Poll that matches the request.
   * @param polls the list of polls for this au.
   * @param req the request used for this poll.
   * @return the Poll from the list which matches req.
   */
  private Poll findPollForReq(List<org.lockss.poller.Poll> polls, PollReq req)
  {
    if (polls != null && polls.size() > 0) {
      for(org.lockss.poller.Poll poll : polls) {
        org.lockss.poller.PollSpec spec = poll.getPollSpec();
        if( req.getAuid().equals(spec.getAuId()) &&
            req.getLower().equals(spec.getLwrBound())&&
            req.getUpper().equals(spec.getUprBound()) &&
            req.getUrl().equals(spec.getUrl()) &&
            req.getPollType().equals(spec.getPollType())) {
          return makePollFromPoll(poll);
        }
      }
    }
    return null;
  }
}
