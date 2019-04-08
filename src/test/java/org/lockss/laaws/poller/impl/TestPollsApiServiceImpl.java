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

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.config.Tdb;
import org.lockss.laaws.poller.model.PollDesc;
import org.lockss.laaws.poller.model.PollerPager;
import org.lockss.laaws.poller.model.PollerSummary;
import org.lockss.laaws.poller.model.RepairPager;
import org.lockss.laaws.poller.model.UrlPager;
import org.lockss.laaws.poller.model.VoterPager;
import org.lockss.laaws.status.model.ApiStatus;
import org.lockss.plugin.PluginTestUtil;
import org.lockss.poller.Poll;
import org.lockss.poller.PollManager;
import org.lockss.poller.PollSpec;
import org.lockss.poller.PollTestPlugin.PTArchivalUnit;
import org.lockss.poller.v3.V3Poller;
import org.lockss.protocol.IdentityManager;
import org.lockss.protocol.IdentityManager.MalformedIdentityKeyException;
import org.lockss.protocol.LcapDatagramComm;
import org.lockss.protocol.PeerIdentity;
import org.lockss.protocol.V3LcapMessage;
import org.lockss.repository.RepositoryManager;
import org.lockss.test.ConfigurationUtil;
import org.lockss.test.LockssTestCase4;
import org.lockss.test.MockArchivalUnit;
import org.lockss.test.MockLockssDaemon;
import org.lockss.test.MockPlugin;
import org.lockss.test.MockPollSpec;
import org.lockss.util.ByteArray;
import org.lockss.util.TimeBase;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** @noinspection TestShouldMockAllTestedDependenciesInspection*/
public class TestPollsApiServiceImpl extends LockssTestCase4 {

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private PollsApiServiceImpl pollsApiServiceImpl;

  private static String[] rooturls = {"http://www.test.org",
      "http://www.test1.org",
      "http://www.test2.org"};

  protected static MockArchivalUnit mTestAu;
  private MockLockssDaemon theDaemon;

  protected PeerIdentity testID;
  protected V3LcapMessage[] v3Testmsg;
  protected MyPollManager mPollManager;
  protected IdentityManager mIdManager;
  private File tempDir;
  private Tdb tdb;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
    String tempDirPath = setUpDiskSpace();
    ConfigurationUtil.addFromArgs(
        IdentityManager.PARAM_IDDB_DIR, tempDirPath + "iddb",
        IdentityManager.PARAM_LOCAL_IP, "127.1.2.3",
        LcapDatagramComm.PARAM_ENABLED, "false");
    TimeBase.setSimulated();
    initRequiredServices();
    initTestAddr();
    initTestMsg();
    setErrorIfTimerThrows(false);
    tdb = new Tdb();
    String urlstr = "http://www.test3.org";
    when(request.getRequestURI()).thenReturn(urlstr);
  }

  @After
  public void tearDown() throws Exception {
    TimeBase.setReal();
    mPollManager.stopService();
    mIdManager.stopService();
    theDaemon.getLockssRepository(mTestAu).stopService();
    theDaemon.getHashService().stopService();
    theDaemon.getRouterManager().stopService();
    super.tearDown();
  }

  @Test
  public void testGetApiStatus() {
    ApiStatus result = pollsApiServiceImpl.getApiStatus();
    Assert.assertFalse(result.isReady());
    Assert.assertEquals("2.0.0", result.getApiVersion());
    Assert.assertEquals("laaws-poller", result.getComponentName());
    Assert.assertEquals("LOCKSS Poller Service REST API", result.getServiceName());
    Assert.assertEquals("1.75.0", result.getLockssVersion());
    Assert.assertEquals("1.0.0-SNAPSHOT", result.getComponentVersion());
  }

  @Test
  public void testCallPoll() {
    PollDesc desc = new PollDesc();
    String auId = mTestAu.getAuId();
    // straight forward request to start a poll.
    desc.setAuId(auId);
    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
    Assert.assertEquals(auId, result.getBody());
    Assert.assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
  }

  @Test
  public void testGetPollStatus() throws Exception {
    ResponseEntity<PollerSummary> summaryResponse;
    String auId = "bogus";
    // check the status of bogus au name.
    summaryResponse = pollsApiServiceImpl.getPollStatus(auId);
    PollerSummary summary = summaryResponse.getBody();
    Assert.assertNull(summary);
    Assert.assertEquals(HttpStatus.NOT_FOUND, summaryResponse.getStatusCode());
    addV3Polls();
    auId = mTestAu.getAuId();
    summaryResponse = pollsApiServiceImpl.getPollStatus(auId);
    summary = summaryResponse.getBody();

  }

  @Test
  public void testCancelPoll() throws Exception {
    String auId = mTestAu.getAuId();
    addV3Polls();
    ResponseEntity<Void> result = pollsApiServiceImpl.cancelPoll(auId);
    Assert.assertNull(result.getBody());
    // todo: check the poll queue for the poll
  }

  @Test
  public void testGetPollAndDetails() {
    // no  poll
    // get details of the non-existent poll.
    ResponseEntity<UrlPager> peersVoteUrls = pollsApiServiceImpl
        .getPollPeerVoteUrls("pollKey", "peerId", "urls", 1, 20);
    Assert.assertEquals(HttpStatus.NOT_FOUND, peersVoteUrls.getStatusCode());
    ResponseEntity<RepairPager> repairData = pollsApiServiceImpl
        .getRepairQueueData("pollKey", "repair", 1, 20);
    Assert.assertEquals(HttpStatus.NOT_FOUND, repairData.getStatusCode());
    ResponseEntity<UrlPager> tallyUrls = pollsApiServiceImpl
        .getTallyUrls("pollKey", "tally", 1, 20);
    Assert.assertEquals(HttpStatus.NOT_FOUND, repairData.getStatusCode());
  }

  @Test
  public void testGetPollsAsPoller() throws Exception {
    ResponseEntity<PollerPager> result = pollsApiServiceImpl
        .getPollsAsPoller(20, 1);
    PollerPager pager = result.getBody();
    Assert.assertEquals(null, pager.getPolls());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    //todo: Add polls to poll queue
  }

  
  @Test
  public void testGetPollsAsVoter() {
    ResponseEntity<VoterPager> result = pollsApiServiceImpl
        .getPollsAsVoter(20, 1);
    VoterPager pager = result.getBody();
    Assert.assertEquals(null, pager.getPolls());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    //todo: Add polls to poll queue
  }

  private void initRequiredServices() {
    theDaemon = getMockLockssDaemon();
    mPollManager = new MyPollManager();
    mPollManager.initService(theDaemon);
    theDaemon.setPollManager(mPollManager);
    mIdManager = theDaemon.getIdentityManager();
    theDaemon.getPluginManager();
    mTestAu = PTArchivalUnit.createFromListOfRootUrls(rooturls);
    mTestAu.setPlugin(new MockPlugin(theDaemon));
    PluginTestUtil.registerArchivalUnit(mTestAu);
    RepositoryManager repoMgr = theDaemon.getRepositoryManager();
    repoMgr.startService();
    Properties p = new Properties();
    addRequiredConfig(p);
    ConfigurationUtil.setCurrentConfigFromProps(p);
    theDaemon.getSchedService().startService();
    theDaemon.getHashService().startService();
    theDaemon.getRouterManager().startService();
    theDaemon.getActivityRegulator(mTestAu).startService();
    mPollManager.startService();
    mIdManager.startService();
  }

  private void addRequiredConfig(Properties p) {
    String tempDirPath = null;

    try {
      tempDirPath = this.getTempDir().getAbsolutePath() + File.separator;
    } catch (IOException var4) {
      fail("unable to create a temporary directory");
    }

    p.setProperty("org.lockss.id.database.dir", tempDirPath + "iddb");
    p.setProperty("org.lockss.platform.diskSpacePaths", tempDirPath);
    p.setProperty("org.lockss.localIPAddress", "127.0.0.1");
    p.setProperty("org.lockss.comm.enabled", "false");
    p.setProperty("org.lockss.repository.v2Repository", "volatile:foo");
  }

  private void initTestAddr() {
    try {
      this.testID = this.theDaemon.getIdentityManager().stringToPeerIdentity("127.0.0.1");
    } catch (MalformedIdentityKeyException var2) {
      fail("can't open test host");
    }

  }

  private void initTestMsg() throws Exception {
    this.v3Testmsg = new V3LcapMessage[1];
    this.v3Testmsg[0] = new V3LcapMessage(mTestAu.getAuId(),"testpollid",
        "2", ByteArray.makeRandomBytes(20),
        ByteArray.makeRandomBytes(20), 10, 12345678L,
        this.testID, this.tempDir, this.theDaemon);
    this.v3Testmsg[0].setArchivalId(mTestAu.getAuId());
  }

  public void addV3Polls() throws Exception {
    addCompletedV3Poll(100000L, 0.99f);
    addCompletedV3Poll(987654321L, 1.0f);
    addCompletedV3Poll(1000L, 0.25f);
  }

  private void addCompletedV3Poll(long timestamp,
      float agreement) throws Exception {
    String lwrbnd = "test1.doc";
    String uprbnd = "test3.doc";
    PollSpec spec = new MockPollSpec(mTestAu, rooturls[0], lwrbnd, uprbnd,
        Poll.V3_POLL);
    V3Poller poll = new V3Poller(spec, theDaemon, testID, "akeyforthispoll",
        1234567, "SHA-1");
    mPollManager.addPoll(poll);
    //poll.stopPoll();
    mPollManager.finishPoll(poll);
    PollManager.V3PollStatusAccessor v3status =
        mPollManager.getV3Status();
    v3status.incrementNumPolls(mTestAu.getAuId());
    v3status.setAgreement(mTestAu.getAuId(), agreement);
    v3status.setLastPollTime(mTestAu.getAuId(), timestamp);
  }

  static class MyPollManager extends PollManager {
    private HashMap<String, Poll> thePolls =
        new HashMap<>();
    private HashMap<String, Poll> theRecentPolls =
        new HashMap<>();
    MyPollManager() {
    }

    void addPoll(Poll poll) {
      if (thePolls.containsKey(poll.getKey())) {
        throw new IllegalArgumentException("Poll " + poll.getAu().getAuId() +
            " is already in the EntryManager.");
      }
      thePolls.put(poll.getKey(), poll);
    }

    void finishPoll(Poll poll)
    {
      String key = poll.getKey();
      if (thePolls.containsKey(key)) {
        theRecentPolls.put(key, thePolls.remove(key));
      }
    }

    boolean isPollActive(String key) {
      return (thePolls.containsKey(key));
    }

    boolean isPollClosed(String key) {
      return theRecentPolls.containsKey(key);
    }
  }

}

