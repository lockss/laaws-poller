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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.lockss.laaws.poller.model.*;
import org.lockss.config.Tdb;
import org.lockss.plugin.Plugin;
import org.lockss.poller.PollSpec;
import org.lockss.poller.TestPollManager;
import org.lockss.rs.status.ApiStatus;

class TestPollsApiServiceImpl extends TestPollManager {

  @Mock
  HashMap<String, PollSpec> requestMap;
  @Mock
  HttpServletRequest request;
  @InjectMocks
  PollsApiServiceImpl pollsApiServiceImpl;

  private static String[] rooturls = {"http://www.test.org",
      "http://www.test1.org",
      "http://www.test2.org"};

  private static String urlstr = "http://www.test3.org";
  private static String lwrbnd = "test1.doc";
  private static String uprbnd = "test3.doc";

  private Tdb tdb;
  private Plugin plugin;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  void testGetApiStatus() {
    ApiStatus result = pollsApiServiceImpl.getApiStatus();
    Assertions.assertFalse(result.isReady());
    Assertions.assertEquals("1.0.0", result.getVersion());
  }

  @Test
  void testCallPoll() {
    PollDesc desc = new PollDesc();
    String auId = testau.getAuId();
    // straight forward request to start a poll.
    desc.setAuId(testau.getAuId());
    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
    Assertions.assertEquals(auId, result.getBody());
    Assertions.assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
  }

  @Test
  void testGetPollStatus() throws Exception {
    ResponseEntity<PollerSummary> summaryResponse;
    String auId = "bogus";
    // check the status of bogus au name.
    summaryResponse =  pollsApiServiceImpl.getPollStatus(auId);
    PollerSummary summary = summaryResponse.getBody();
    Assertions.assertNull(summary);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, summaryResponse.getStatusCode());
    testGetV3PollStatus();
    auId = testau.getAuId();
    summaryResponse = pollsApiServiceImpl.getPollStatus(auId);
    summary = summaryResponse.getBody();
  }

  @Test
  void testCancelPoll() throws Exception {
    String auId = testau.getAuId();
    super.testGetV3PollStatus();
    ResponseEntity<Void> result = pollsApiServiceImpl.cancelPoll(auId);
    Assertions.assertNull(result.getBody());
  }

  @Test
  void testGetPollAndDetails() {
    // no  poll
    // get details of the non-existent poll.
    ResponseEntity<UrlPager> peersVoteUrls = pollsApiServiceImpl
        .getPollPeerVoteUrls("pollKey", "peerId", "urls", 1, 20);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, peersVoteUrls.getStatusCode());
    ResponseEntity<RepairPager> repairData = pollsApiServiceImpl
        .getRepairQueueData("pollKey", "repair", 1, 20);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, repairData.getStatusCode());
    ResponseEntity<UrlPager> tallyUrls = pollsApiServiceImpl
        .getTallyUrls("pollKey", "tally", 1, 20);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, repairData.getStatusCode());
  }

  @Test
  void testGetPollsAsPoller() throws Exception {
    ResponseEntity<PollerPager> result = pollsApiServiceImpl
        .getPollsAsPoller(20, 1);
    PollerPager pager = result.getBody();
    Assertions.assertEquals(null, pager.getPolls());
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    testPollQueue();
    result = pollsApiServiceImpl.getPollsAsPoller(20, 1);
    pager = result.getBody();
  }

  @Test
  void testGetPollsAsVoter() {
    ResponseEntity<VoterPager> result = pollsApiServiceImpl
        .getPollsAsVoter(20, 1);
    VoterPager pager = result.getBody();
    Assertions.assertEquals(null, pager.getPolls());
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
  }


}

