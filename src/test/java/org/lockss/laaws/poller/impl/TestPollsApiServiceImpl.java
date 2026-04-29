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

import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;
import jakarta.servlet.http.*;
import org.junit.*;
import org.junit.runner.*;
import org.lockss.config.*;
import org.lockss.laaws.poller.*;
import org.lockss.laaws.poller.model.*;
import org.lockss.plugin.*;
import org.lockss.poller.*;
import org.lockss.poller.v3.*;
import org.lockss.protocol.*;
import org.lockss.repository.*;
import org.lockss.spring.auth.*;
import org.lockss.test.*;
import org.lockss.util.*;
import org.lockss.util.rest.poller.*;
import org.lockss.util.rest.poller.model.RepairTypeEnum;
import org.lockss.util.rest.poller.model.TallyTypeEnum;
import org.lockss.util.rest.repo.model.*;
import org.mockito.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.http.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPollsApiServiceImpl extends LockssTestCase4 {

  @Mock
  private HttpServletRequest request;

  private MockedStatic<AuthUtil> authUtilMock;

  @MockitoSpyBean
  PollsApiServiceImpl pollsApiServiceImpl;

  private static final String[] rooturls = {"http://www.test.org",
      "http://www.test1.org",
      "http://www.test2.org"};

  protected static MockArchivalUnit testAu;
  private MockLockssDaemon theDaemon;

  protected PeerIdentity testID;
  protected V3LcapMessage[] v3Testmsg;
  protected MyPollManager pollManager;
  protected IdentityManager idManager;
  private File tempDir;
  private Tdb tdb;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.openMocks(this);
    authUtilMock = mockStatic(AuthUtil.class);
    authUtilMock.when(() -> AuthUtil.checkHasRole(anyString())).thenAnswer(invocation -> null); // if intended to do nothing
    String tempDirPath = setUpDiskSpace();
    ConfigurationUtil.addFromArgs(
        IdentityManager.PARAM_IDDB_DIR, tempDirPath + "iddb",
        IdentityManager.PARAM_LOCAL_IP, "127.1.2.3");
    TimeBase.setSimulated();
    initRequiredServices();
    initTestAddr();
    initTestMsg();
    setErrorIfTimerThrows(false);
    tdb = new Tdb();
    final String urlstr = "http://www.test3.org";
    when(request.getRequestURI()).thenReturn(urlstr);
    doReturn(true).when(pollsApiServiceImpl).isReady();
    doReturn(pollManager).when(pollsApiServiceImpl).getPollManager();
    doReturn(theDaemon).when(pollsApiServiceImpl).getLockssDaemon();
  }

  @After
  public void tearDown() throws Exception {
    TimeBase.setReal();
    pollManager.stopService();
    idManager.stopService();
    theDaemon.getHashService().stopService();
    theDaemon.getRouterManager().stopService();
    if (null != authUtilMock) {
      authUtilMock.close();
    }
    super.tearDown();
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
    auId = testAu.getAuId();
    summaryResponse = pollsApiServiceImpl.getPollStatus(auId);
    summary = summaryResponse.getBody();

  }

  @Test
  public void testCancelPoll() throws Exception {
    String auId = testAu.getAuId();
    addV3Polls();
    ResponseEntity<Void> result = pollsApiServiceImpl.cancelPoll(auId);
    Assert.assertNull(result.getBody());
    // todo: check the poll queue for the poll
  }

  @Test
  public void testGetPollAndDetails() {
    // no  poll
    // get details of the non-existent poll.
    ResponseEntity<UrlPageInfo> peersVoteUrls = pollsApiServiceImpl
        .getPollPeerVoteUrls("pollKey", "peerId", null, 20, null);
    Assert.assertEquals(HttpStatus.NOT_FOUND, peersVoteUrls.getStatusCode());
    ResponseEntity<RepairPageInfo> repairData = pollsApiServiceImpl
        .getRepairQueueData("pollKey", RepairTypeEnum.PENDING, 20, null);
    Assert.assertEquals(HttpStatus.NOT_FOUND, repairData.getStatusCode());
    ResponseEntity<UrlPageInfo> tallyUrls = pollsApiServiceImpl
        .getTallyUrls("pollKey", TallyTypeEnum.AGREE, 20, null);
    Assert.assertEquals(HttpStatus.NOT_FOUND, repairData.getStatusCode());
  }

  @Test
  public void testGetPollsAsPoller() throws Exception {
    ResponseEntity<PollerPageInfo> result = pollsApiServiceImpl
        .getPollsAsPoller(20, null);
    PollerPageInfo pageInfo = result.getBody();
    Assert.assertNotNull(pageInfo.getPolls());
    Assert.assertTrue(pageInfo.getPolls().isEmpty());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    //todo: Add polls to poll queue
  }

  
  @Test
  public void testGetPollsAsVoter() {
    ResponseEntity<VoterPageInfo> result = pollsApiServiceImpl
        .getPollsAsVoter(20, null);
    VoterPageInfo pageInfo = result.getBody();
    Assert.assertNotNull(pageInfo.getPolls());
    Assert.assertTrue(pageInfo.getPolls().isEmpty());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    //todo: Add polls to poll queue
  }

  @Test
  public void testCallPollServiceNotInitialized() {
    // Create a service that's not initialized
    when(pollsApiServiceImpl.isReady()).thenReturn(false);
    PollDesc desc = new PollDesc();
    desc.setAuId("test-au-id");

    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
    Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
  }

  @Test
  public void testCallPollWithInvalidAuId() {
    PollDesc desc = new PollDesc();
    desc.setAuId("invalid-au-id");

    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
    Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    Assert.assertTrue(result.getBody().contains("No valid au"));
  }

  @Test
  public void testGetPollerPollDetailsNotFound() {
    final String pollKey = "nonexistent-poll-key";

    ResponseEntity<PollerDetail> result = pollsApiServiceImpl.getPollerPollDetails(pollKey);
    Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  public void testGetVoterPollDetails() throws Exception {
    final String pollKey = "akeyforthispoll";
    addRunningV3Poll(100000L, 0.99f);
    ResponseEntity<VoterDetail> result = pollsApiServiceImpl.getVoterPollDetails(pollKey);
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  public void testGetVoterPollDetailsNotFound() {
    final String pollKey = "nonexistent-poll-key";

    ResponseEntity<VoterDetail> result = pollsApiServiceImpl.getVoterPollDetails(pollKey);
    Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  public void testGetTallyUrls() throws Exception {
    final String pollKey = "akeyforthispoll";
    addRunningV3Poll(100000L, 0.99f);

    ResponseEntity<UrlPageInfo> result = pollsApiServiceImpl.getTallyUrls(pollKey, TallyTypeEnum.AGREE, 20, null);
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  public void testGetTallyUrlsNotFound() {
    final String pollKey = "nonexistent-poll-key";

    ResponseEntity<UrlPageInfo> result = pollsApiServiceImpl.getTallyUrls(pollKey, TallyTypeEnum.AGREE, 20, null);
    Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  private void initRequiredServices() {
    theDaemon = getMockLockssDaemon();
    pollManager = new MyPollManager();
    pollManager.initService(theDaemon);
    theDaemon.setPollManager(pollManager);
    idManager = theDaemon.getIdentityManager();
    theDaemon.getPluginManager();
    testAu = PollTestPlugin.PTArchivalUnit.createFromListOfRootUrls(rooturls);
    testAu.setPlugin(new MockPlugin(theDaemon));
    PluginTestUtil.registerArchivalUnit(testAu);
    RepositoryManager repoMgr = theDaemon.getRepositoryManager();
    repoMgr.startService();
    Properties p = new Properties();
    addRequiredConfig(p);
    ConfigurationUtil.setCurrentConfigFromProps(p);
    theDaemon.getSchedService().startService();
    theDaemon.getHashService().startService();
    theDaemon.getRouterManager().startService();
    pollManager.startService();
    idManager.startService();

  }

  private void addRequiredConfig(Properties p) {
    String tempDirPath = null;

    try {
      tempDirPath = getTempDir().getAbsolutePath() + File.separator;
    } catch (IOException ioe) {
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
      testID = theDaemon.getIdentityManager().stringToPeerIdentity("127.0.0.1");
    } catch (IdentityManager.MalformedIdentityKeyException mikE) {
      fail("can't open test host");
    }

  }

  private void initTestMsg() throws Exception {
    v3Testmsg = new V3LcapMessage[1];
    v3Testmsg[0] = new V3LcapMessage(testAu.getAuId(), "testpollid",
        "2", ByteArray.makeRandomBytes(20),
        ByteArray.makeRandomBytes(20), 10, 12345678L,
        testID, getTempDir(), theDaemon);
    v3Testmsg[0].setArchivalId(testAu.getAuId());
  }

  public void addV3Polls() throws Exception {
    addCompletedV3Poll(100000L, 0.99f);
    addCompletedV3Poll(987654321L, 1.0f);
    addCompletedV3Poll(1000L, 0.25f);
  }

  public void addRunningV3Polls() throws Exception {
    addRunningV3Poll(100000L, 0.99f);
    addRunningV3Poll(987654321L, 1.0f);
    addRunningV3Poll(987654321L, 1.0f);
  }

  private void addRunningV3Poll(long timestamp,
      float agreement) throws Exception {
    final String lwrbnd = "test1.doc";
    final String uprbnd = "test3.doc";
    PollSpec spec = new MockPollSpec(testAu, rooturls[0], lwrbnd, uprbnd,
        Poll.V3_POLL);
    V3Poller poll = new V3Poller(spec, theDaemon, testID, "akeyforthispoll",
        1234567, "SHA-1");
    pollManager.addPoll(poll);
    PollManager.V3PollStatusAccessor v3status =
        pollManager.getV3Status();

    v3status.incrementNumPolls(testAu.getAuId());
    v3status.setAgreement(testAu.getAuId(), agreement);
    v3status.setLastPollTime(testAu.getAuId(), timestamp);
  }

  private void addCompletedV3Poll(long timestamp,
      float agreement) throws Exception {
    final String lwrbnd = "test1.doc";
    final String uprbnd = "test3.doc";
    PollSpec spec = new MockPollSpec(testAu, rooturls[0], lwrbnd, uprbnd,
        Poll.V3_POLL);
    V3Poller poll = new V3Poller(spec, theDaemon, testID, "akeyforthispoll",
        1234567, "SHA-1");
    pollManager.addPoll(poll);
    //poll.stopPoll();
    pollManager.finishPoll(poll);
    PollManager.V3PollStatusAccessor v3status =
        pollManager.getV3Status();
    v3status.incrementNumPolls(testAu.getAuId());
    v3status.setAgreement(testAu.getAuId(), agreement);
    v3status.setLastPollTime(testAu.getAuId(), timestamp);
  }

  static class MyPollManager extends PollManager {
    private final HashMap<String, Poll> thePolls = new HashMap<>();
    private final HashMap<String, Poll> theRecentPolls = new HashMap<>();
    MyPollManager() {
    }

    public Poll getPoll(String key)  {
      return thePolls.get(key);
    }

    void addPoll(Poll poll) throws NotEligibleException{
      if (thePolls.containsKey(poll.getKey())) {
        throw new NotEligibleException("Poll " + poll.getAu().getAuId() +
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