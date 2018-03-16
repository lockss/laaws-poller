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

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.lockss.laaws.poller.api.PollsApiDelegate;
import org.lockss.laaws.poller.model.*;
import org.lockss.laaws.poller.model.PollDesc.VariantEnum;
import org.lockss.laaws.poller.model.RepairData.ResultEnum;
import org.lockss.app.LockssApp;
import org.lockss.app.LockssDaemon;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.plugin.PluginManager;
import org.lockss.plugin.UrlData;
import org.lockss.poller.Poll;
import org.lockss.poller.PollManager;
import org.lockss.poller.PollManager.NotEligibleException;
import org.lockss.poller.PollSpec;
import org.lockss.poller.v3.*;
import org.lockss.poller.v3.ParticipantUserData.VoteCounts;
import org.lockss.poller.v3.PollerStateBean.Repair;
import org.lockss.poller.v3.PollerStateBean.TallyStatus;
import org.lockss.protocol.PeerIdentity;
import org.lockss.protocol.psm.PsmInterp;
import org.lockss.protocol.psm.PsmState;
import org.lockss.rs.status.ApiStatus;
import org.lockss.rs.status.SpringLockssBaseApiController;

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
        PollerSummary summary = summarizePollerPoll(poll);
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
    PollDetail detail = detailPoll(poll);
    return new ResponseEntity<>(detail, HttpStatus.OK);
  }


  /**
   * @see PollsApiDelegate#getPollPeerVoteUrls
   */
  @Override
  public ResponseEntity<UrlPager> getPollPeerVoteUrls(String pollKey, String peerId, String urls,
      Integer page, Integer size) {
    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);
    String baseLink = request.getRequestURI();
    if (poll instanceof V3Poller) {
      final List<ParticipantUserData> participants = ((V3Poller) poll).getParticipants();
      ParticipantUserData userData = userDataForPeer(peerId, participants);
      if (userData != null) {
        VoteCounts voteCounts = userData.getVoteCounts();
        if (voteCounts.hasPeerUrlLists() && userData.hasVoted()) {
          Collection<String> counts = null;
          switch (urls) {
            case "agreed":
              counts = voteCounts.getAgreedUrls();
              break;
            case "disagreed":
              counts = voteCounts.getDisagreedUrls();
              break;
            case "pollerOnly":
              counts = voteCounts.getPollerOnlyUrls();
              break;
            case "voterOnly":
              counts = voteCounts.getVoterOnlyUrls();
              break;
          }
          if(counts != null && counts.size() > 0) {
            Page<String> urlpage = new Page<>(counts, page, size);
            // The page description.
            PageDesc desc = getPageDesc(baseLink, urlpage);
            UrlPager pager = new UrlPager();
            pager.setPageDesc(desc);
            // The page content.
            if (urlpage.hasContent()) {
              int offset = urlpage.getOffset();
              int last = offset + urlpage.getPageSize();
              last = last > urlpage.getTotal() ? last : urlpage.getTotal();
              List<String> urlList = urlpage.getPageContent();
              for(int idx = offset; idx < last; idx++ ) {
                pager.addUrlsItem(urlList.get(idx));
              }
            }
            return new ResponseEntity<>(pager,urlpage.getPageHeaders(),HttpStatus.OK);
          }
        }
      }
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }



  /**
   * @see PollsApiDelegate#getRepairQueueData
   */
  @Override
  public ResponseEntity<RepairPager> getRepairQueueData(String pollKey, String repair, Integer page,
      Integer size) {
    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);
    String baseLink = request.getRequestURI();
    if (poll instanceof V3Poller) {
      PollerStateBean.RepairQueue repairQueue = ((V3Poller) poll).getPollerStateBean().getRepairQueue();
      List<Repair> repairList = null;
      switch (repair) {
        case "active":
          repairList = repairQueue.getActiveRepairs();
          break;
        case "pending":
          repairList = repairQueue.getPendingRepairs();
          break;
        case "completed":
          repairList = repairQueue.getCompletedRepairs();
          break;
      }
      if(repairList != null && repairList.size() > 0) {
        Page<Repair> r_page = new Page<>(repairList, page, size);
        // The page description.
        PageDesc desc = getPageDesc(baseLink, r_page);
        RepairPager pager = new RepairPager();
        pager.setPageDesc(desc);
        // The page content.
        if (r_page.hasContent()) {
          int offset = r_page.getOffset();
          int last = offset + r_page.getPageSize();
          last = last > r_page.getTotal() ? last : r_page.getTotal();
          List<Repair> r_list = r_page.getPageContent();
          for(int idx = offset; idx < last; idx++ ) {
            RepairData repair_d = new RepairData();
            Repair rep = r_list.get(idx);
            repair_d.setRepairUrl(rep.getUrl());
            repair_d.setRepairFrom(rep.getRepairFrom().getIdString());
            if("completed".equals(repair))
              repair_d.setResult(ResultEnum.fromValue(rep.getTallyResult().toString()));
            pager.addRepairsItem(repair_d);
          }
        }
        return new ResponseEntity<>(pager,r_page.getPageHeaders(),HttpStatus.OK);
      }
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * @see PollsApiDelegate#getTallyUrls
   */
  @Override
  public ResponseEntity<UrlPager> getTallyUrls(String pollKey, String tally, Integer page,
      Integer size) {
    PollManager pm = getPollManager();
    Poll poll = pm.getPoll(pollKey);
    String baseLink = request.getRequestURI();
    if (poll instanceof V3Poller) {
      final TallyStatus tallyStatus = ((V3Poller) poll).getPollerStateBean().getTallyStatus();
      Set<String> tallySet = null;
      switch (tally) {
        case "agree":
          tallySet = tallyStatus.getAgreedUrls();
          break;
        case "disagree":
          tallySet = tallyStatus.getDisagreedUrls();
          break;
        case "error":
          tallySet = tallyStatus.getErrorUrls().keySet();
          break;
        case "noQuorum":
          tallySet = tallyStatus.getNoQuorumUrls();
          break;
        case "tooClose":
          tallySet = tallyStatus.getTooCloseUrls();
          break;
      }
      if(tallySet != null && tallySet.size() > 0) {
        Page<String> urlpage = new Page<>(tallySet, page, size);
        // The page description.
        PageDesc desc = getPageDesc(baseLink, urlpage);
        UrlPager pager = new UrlPager();
        pager.setPageDesc(desc);
        // The page content.
        if (urlpage.hasContent()) {
          int offset = urlpage.getOffset();
          int last = offset + urlpage.getPageSize();
          last = last > urlpage.getTotal() ? last : urlpage.getTotal();
          List<String> urlList = urlpage.getPageContent();
          for(int idx = offset; idx < last; idx++ ) {
              pager.addUrlsItem(urlList.get(idx));
          }
        }
        return new ResponseEntity<>(pager,urlpage.getPageHeaders(),HttpStatus.OK);
      }
     }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /*  -------------------Poller methods. --------------------------- */

  /**
   * @see PollsApiDelegate#getPollsAsPoller
   */
  @Override
  public ResponseEntity<PollerPager> getPollsAsPoller(Integer size, Integer page) {
    if (logger.isDebugEnabled()) {
      logger.debug("request for  a page " + page + " of voter polls with page size " + size);
    }
    PollManager pm = getPollManager();
    Collection<V3Poller> pollers = pm.getV3Pollers();
    String baseLink = request.getRequestURI();
    Page<V3Poller> ppage = new Page<>(pollers, size, page);
    PollerPager pager = new PollerPager();
    // The page description.
    PageDesc desc = getPageDesc(baseLink, ppage);
    pager.setPageDesc(desc);
    // The page content.
    if (ppage.hasContent()) {
      int offset = ppage.getOffset();
      int last = offset + ppage.getPageSize();
      last = last > ppage.getTotal() ? last : ppage.getTotal();
      List<V3Poller> pollerList = ppage.getPageContent();
      for(int idx = offset; idx < last; idx++ ) {
        pager.addPollsItem(summarizePollerPoll(pollerList.get(idx)));
      }
    }
    return new ResponseEntity<>(pager, HttpStatus.OK);
  }

  private PageDesc getPageDesc(String baseLink, Page page) {
    PageDesc desc = new PageDesc();
    desc.setTotal(page.getTotal());
    desc.setSize(page.getPageSize());
    desc.setPage(page.getPageNum());
    desc.setNextPage(page.getNextLink(baseLink));
    desc.setPrevPage(page.getPrevLink(baseLink));
    return desc;
  }

  /*  -------------------Voter methods. --------------------------- */

  /**
   * @see PollsApiDelegate#getPollsAsVoter
   */
  @Override
  public ResponseEntity<VoterPager> getPollsAsVoter(Integer size, Integer page) {
    if (logger.isDebugEnabled()) {
      logger.debug("request for  a page " + page + " of voter polls with page size " + size);
    }
    PollManager pm = getPollManager();
    Collection<V3Voter> voters = pm.getV3Voters();
    String baseLink = request.getRequestURI();
    Page<V3Voter> vpage = new Page<>(voters, size, page);
    VoterPager pager = new VoterPager();
    // The page description.
    PageDesc desc = new PageDesc();
    desc.setTotal(vpage.getTotal());
    desc.setSize(vpage.getPageSize());
    desc.setPage(vpage.getPageNum());
    desc.setNextPage(vpage.getNextLink(baseLink));
    desc.setPrevPage(vpage.getPrevLink(baseLink));
    pager.setPageDesc(desc);
    // The page content.
    if (vpage.hasContent()) {
      int offset = vpage.getOffset();
      int last = offset + vpage.getPageSize();
      last = last > vpage.getTotal() ? last : vpage.getTotal();
      List<V3Voter> voterList = vpage.getPageContent();
      for(int idx = offset; idx < last; idx++ ) {
        pager.addPollsItem(summarizeVoterPoll(voterList.get(idx)));
      }
    }
    return new ResponseEntity<>(pager, HttpStatus.OK);
  }

  /* ------------------ DTO Mappings ---------------------- */
  private PollSpec pollSpecFromDesc(PollDesc pollDesc) {
    return new PollSpec(
        pollDesc.getAuId(),
        pollDesc.getCuSetSpec().getUrlPrefix(),
        pollDesc.getCuSetSpec().getLowerBound(),
        pollDesc.getCuSetSpec().getUpperBound(),
        org.lockss.poller.Poll.V3_POLL);
  }

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
    pollDesc.setVariant(VariantEnum.fromValue(pollSpec.getPollVariant().shortName()));
    return pollDesc;
  }

  private PollerSummary summarizePollerPoll(Poll inPoll) {
    PollerSummary summary = new PollerSummary();
    if (inPoll instanceof V3Poller) {
      V3Poller v3poller = (V3Poller) inPoll;
      PollerStateBean psb = v3poller.getPollerStateBean();
      summary.setPollKey(psb.getPollKey());
      summary.setAuId(psb.getAuId());
      summary.setStatus(V3Poller.POLLER_STATUS_STRINGS[psb.getStatus()]);
      summary.setStart(psb.getCreateTime());
      summary.setVariant(psb.getPollVariant().shortName());
      summary.setDeadline(psb.getPollDeadline());
      summary.setPollEnd(psb.getPollEnd());
      TallyStatus ts = psb.getTallyStatus();
      summary.setNumTalliedUrls(ts.getTalliedUrlCount());
      summary.setParticipants(psb.votedPeerCount());
      summary.setNumAgreeUrls(ts.getAgreedUrlCount());
      summary.setNumHashErrors(ts.getErrorUrlCount());
      summary.getNumCompletedRepairs();
      //String baseUrl = request.getRequestURI();
      //summary.setDetailLink();
      // /polls/{pollKey}/details
    }
    return summary;
  }

  private VoterSummary summarizeVoterPoll(Poll inPoll) {
    VoterSummary summary = new VoterSummary();
    if(inPoll instanceof V3Voter) {
      V3Voter v3Voter = (V3Voter) inPoll;
      VoterUserData userData = v3Voter.getVoterUserData();
      summary.setAuId(userData.getAuId());
      summary.setCaller(userData.getPollerId().getIdString());
      summary.setDeadline(userData.getDeadline());
      summary.setPollKey(userData.getPollKey());
      summary.setStart(userData.getCreateTime());
      summary.setStatus(userData.getStatusString());
      String baseUrl = request.getRequestURI();
      //summary.setDetailLink();
    }
    return summary;
  }

  private PollDetail detailPoll(Poll poll) {
    PollDetail detail = new PollDetail();
    if (poll instanceof V3Poller) {
      V3Poller v3poller = (V3Poller) poll;
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
      for(PeerIdentity peerId : psb.getNoAuPeers()) {
        detail.addNoAuPeersItem(peerId.getIdString());
      }
      for(ParticipantUserData participantData: v3poller.getParticipants()) {
        detail.addVotedPeersItem(PeerDataFromParticipantData(participantData));
      }
      TallyData tallyData = tallyDataFromTallyStatus(psb.getTallyStatus());
      detail.setTally(tallyData);
      RepairQueue repairQueue = RepairQueueFromDataRepairQueue(psb.getRepairQueue());
      detail.setRepairQueue(repairQueue);
    }
    return detail;
  }

  private RepairQueue RepairQueueFromDataRepairQueue(PollerStateBean.RepairQueue inQueue) {
    RepairQueue outQueue = new RepairQueue();
    return outQueue;
  }

  private TallyData tallyDataFromTallyStatus(TallyStatus tallyStatus) {
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
    //tallyData.setAgreeLink();
    //tallyData.setDisagreeLink();
    //tallyData.setNoQuorumLink();
    //tallyData.setTooCloseLink();
    //tallyData.setErrorLink();
    return tallyData;
  }

  private ParticipantUserData userDataForPeer(String peerId,
      List<ParticipantUserData> participants) {
    if(peerId == null || participants == null || participants.size() == 0)
      return null;
    for(ParticipantUserData userData : participants) {
      if(userData.getVoterId().equals(peerId)) {
        return userData;
      }
    }
    return null;
  }

  private PeerData PeerDataFromParticipantData(ParticipantUserData voter) {
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
      //tallyData.setAgreeLink();
      //tallyData.setDisagreeLink();
      //tallyData.setPollerOnlyLink();
      //tallyData.setVoterOnlyLink();
    return peerData;
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
