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

import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.lockss.laaws.poller.model.*;
import org.lockss.poller.TestPollManager;
import org.lockss.laaws.status.model.ApiStatus;

public class TestPollsApiServiceImpl extends TestPollManager {

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private PollsApiServiceImpl pollsApiServiceImpl;

  private static String[] rooturls = {"http://www.test.org",
      "http://www.test1.org",
      "http://www.test2.org"};

  private static String urlstr = "http://www.test3.org";
  private static String lwrbnd = "test1.doc";
  private static String uprbnd = "test3.doc";

  @Before
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
    when(request.getRequestURI()).thenReturn(urlstr);
  }


  @org.junit.Test
  public void testGetApiStatus() {
    ApiStatus result = pollsApiServiceImpl.getApiStatus();
    Assert.assertFalse(result.isReady());
    Assert.assertEquals("1.0.0", result.getVersion());
  }

  @org.junit.Test
  public void testCallPoll() {
    PollDesc desc = new PollDesc();
    String auId = testau.getAuId();
    // straight forward request to start a poll.
    desc.setAuId(testau.getAuId());
    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
    Assert.assertEquals(auId, result.getBody());
    Assert.assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
  }

  @org.junit.Test
  public void testGetPollStatus() throws Exception {
    ResponseEntity<PollerSummary> summaryResponse;
    String auId = "bogus";
    // check the status of bogus au name.
    summaryResponse = pollsApiServiceImpl.getPollStatus(auId);
    PollerSummary summary = summaryResponse.getBody();
    Assert.assertNull(summary);
    Assert.assertEquals(HttpStatus.NOT_FOUND, summaryResponse.getStatusCode());
    testGetV3PollStatus();
    auId = testau.getAuId();
    summaryResponse = pollsApiServiceImpl.getPollStatus(auId);
    summary = summaryResponse.getBody();
  }

  @org.junit.Test
  public void testCancelPoll() throws Exception {
    String auId = testau.getAuId();
    super.testGetV3PollStatus();
    ResponseEntity<Void> result = pollsApiServiceImpl.cancelPoll(auId);
    Assert.assertNull(result.getBody());
  }

  @org.junit.Test
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

  @org.junit.Test
  public void testGetPollsAsPoller() throws Exception {
    ResponseEntity<PollerPager> result = pollsApiServiceImpl
        .getPollsAsPoller(20, 1);
    PollerPager pager = result.getBody();
    Assert.assertEquals(0, pager.getPolls().size());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    testGetV3PollStatus();
    result = pollsApiServiceImpl.getPollsAsPoller(20, 1);
    pager = result.getBody();
    Assert.assertNotNull(pager.getPolls());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
  }

  @org.junit.Test
  public void testGetPollsAsVoter() {
    ResponseEntity<VoterPager> result = pollsApiServiceImpl
        .getPollsAsVoter(20, 1);
    VoterPager pager = result.getBody();
    Assert.assertEquals(0, pager.getPolls().size());
    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
  }
}

