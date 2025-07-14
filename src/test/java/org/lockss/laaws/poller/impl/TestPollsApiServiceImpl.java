/*
 * Copyright (c) 2018-2020 Board of Trustees of Leland Stanford Jr. University,
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
import org.lockss.laaws.poller.model.*;
import org.lockss.plugin.*;
import org.lockss.poller.*;
import org.lockss.poller.PollTestPlugin.*;
import org.lockss.poller.v3.*;
import org.lockss.protocol.*;
import org.lockss.protocol.IdentityManager.*;
import org.lockss.repository.*;
import org.lockss.test.*;
import org.lockss.util.*;
import org.lockss.util.rest.poller.*;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.security.test.context.support.WithMockUser;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestPollsApiServiceImpl extends LockssTestCase4 {

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private PollsApiServiceImpl pollsApiServiceImpl;

  private static String[] rooturls = {"http://www.test.org",
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
    String urlstr = "http://www.test3.org";
    when(request.getRequestURI()).thenReturn(urlstr);
  }

  @After
  public void tearDown() throws Exception {
    TimeBase.setReal();
    pollManager.stopService();
    idManager.stopService();
    theDaemon.getHashService().stopService();
    theDaemon.getRouterManager().stopService();
    super.tearDown();
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testCallPoll() {
    PollDesc desc = new PollDesc();
    String auId = testAu.getAuId();
    // straight forward request to start a poll.
    desc.setAuId(auId);
    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
    Assert.assertEquals(auId, result.getBody());
    Assert.assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
  }

  @Test(expected = SecurityException.class)
  @WithMockUser(roles = "USER")
  public void testCallPoll_Unauthorized() {
    // Test logic where the user does NOT have AU_ADMIN role
    // Should throw SecurityException or whatever your method does when access denied
    // ... your test code ...
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetPollStatus() throws Exception {
    ResponseEntity<PollerSummary> summaryResponse;
    String auId = "bogus";
    // check the status of bogus au name.
    pollsApiServiceImpl.localReady = true;
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
  @WithMockUser(roles = "AU_ADMIN")
  public void testCancelPoll() throws Exception {
    String auId = testAu.getAuId();
    pollsApiServiceImpl.localReady = true;
    addV3Polls();
    ResponseEntity<Void> result = pollsApiServiceImpl.cancelPoll(auId);
    Assert.assertNull(result.getBody());
    // todo: check the poll queue for the poll
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetPollAndDetails() {
    // no  poll
    // get details of the non-existent poll.
    pollsApiServiceImpl.localReady = true;
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
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetPollsAsPoller() throws Exception {
    pollsApiServiceImpl.localReady = true;
    ResponseEntity<PollerPager> result = pollsApiServiceImpl
        .getPollsAsPoller(20, 1);
    PollerPager pager = result.getBody();
    Assert.assertEquals(null, pager.getPolls());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    //todo: Add polls to poll queue
  }

  
  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetPollsAsVoter() {
    pollsApiServiceImpl.localReady = true;
    ResponseEntity<VoterPager> result = pollsApiServiceImpl
        .getPollsAsVoter(20, 1);
    VoterPager pager = result.getBody();
    Assert.assertEquals(null, pager.getPolls());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    //todo: Add polls to poll queue
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testCallPollServiceNotInitialized() {
    // Create a service that's not initialized
    PollsApiServiceImpl notInitializedService = new PollsApiServiceImpl();
    PollDesc desc = new PollDesc();
    desc.setAuId("test-au-id");

    ResponseEntity<String> result = notInitializedService.callPoll(desc);
    Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testCallPollWithInvalidAuId() {
    PollDesc desc = new PollDesc();
    desc.setAuId("invalid-au-id");

    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
    Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    Assert.assertTrue(result.getBody().contains("No valid au"));
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testCallPollNotEligibleException() throws Exception {
    PollDesc desc = new PollDesc();
    String auId = testAu.getAuId();
    desc.setAuId(auId);

    // Mock the poll manager to throw NotEligibleException
    MyPollManager mockPollManager = new MyPollManager() {
      @Override
      public void requestPoll(PollSpec pollSpec) throws NotEligibleException {
        throw new NotEligibleException("Test not eligible exception");
      }
    };

    // Replace the poll manager temporarily
    PollManager originalPollManager = theDaemon.getPollManager();
    theDaemon.setPollManager(mockPollManager);

    try {
      ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
      Assert.assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
      Assert.assertEquals("Test not eligible exception", result.getBody());
    } finally {
      // Restore original poll manager
      theDaemon.setPollManager(originalPollManager);
    }
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetPollerPollDetails() throws Exception {
    addV3Polls();
    String pollKey = "akeyforthispoll";

    ResponseEntity<PollerDetail> result = pollsApiServiceImpl.getPollerPollDetails(pollKey);
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetPollerPollDetailsNotFound() {
    String pollKey = "nonexistent-poll-key";

    ResponseEntity<PollerDetail> result = pollsApiServiceImpl.getPollerPollDetails(pollKey);
    Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetVoterPollDetails() throws Exception {
    addV3Polls();
    String pollKey = "akeyforthispoll";

    ResponseEntity<VoterDetail> result = pollsApiServiceImpl.getVoterPollDetails(pollKey);
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetVoterPollDetailsNotFound() {
    String pollKey = "nonexistent-poll-key";

    ResponseEntity<VoterDetail> result = pollsApiServiceImpl.getVoterPollDetails(pollKey);
    Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetTallyUrls() throws Exception {
    addV3Polls();
    String pollKey = "akeyforthispoll";

    ResponseEntity<UrlPager> result = pollsApiServiceImpl.getTallyUrls(pollKey, "agree", 1, 20);
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assert.assertNotNull(result.getBody());
  }

  @Test
  @WithMockUser(roles = "AU_ADMIN")
  public void testGetTallyUrlsNotFound() {
    String pollKey = "nonexistent-poll-key";

    ResponseEntity<UrlPager> result = pollsApiServiceImpl.getTallyUrls(pollKey, "agree", 1, 20);
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
    testAu = PTArchivalUnit.createFromListOfRootUrls(rooturls);
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
    pollsApiServiceImpl.localReady=true;

  }

  private void addRequiredConfig(Properties p) {
    String tempDirPath = null;

    try {
      tempDirPath =getTempDir().getAbsolutePath() + File.separator;
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
      this.testID =theDaemon.getIdentityManager().stringToPeerIdentity("127.0.0.1");
    } catch (MalformedIdentityKeyException var2) {
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

  private void addCompletedV3Poll(long timestamp,
      float agreement) throws Exception {
    String lwrbnd = "test1.doc";
    String uprbnd = "test3.doc";
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