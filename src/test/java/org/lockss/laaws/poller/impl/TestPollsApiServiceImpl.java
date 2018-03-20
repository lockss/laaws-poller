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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import org.lockss.laaws.poller.model.*;
import org.lockss.plugin.PluginManager;
import org.lockss.poller.PollManager;
import org.lockss.poller.PollSpec;
import org.lockss.rs.status.ApiStatus;

class TestPollsApiServiceImpl {

  @Mock
  PollManager pollManager;
  @Mock
  PluginManager pluginManager;
  @Mock
  HashMap<String, PollSpec> requestMap;
  @Mock
  HttpServletRequest request;
  @InjectMocks
  PollsApiServiceImpl pollsApiServiceImpl;


  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testGetApiStatus() {
    ApiStatus expected = new ApiStatus();
    expected.setReady(false);
    expected.setVersion("1.0.0");
    ApiStatus result = pollsApiServiceImpl.getApiStatus();

    Assertions.assertEquals(expected, result);
  }

  @Test
  void testCallPoll() {
    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(new PollDesc());
    Assertions.assertEquals(null, result);
  }

  @Test
  void testCancelPoll() {
    ResponseEntity<Void> result = pollsApiServiceImpl.cancelPoll("psId");
    Assertions.assertEquals(null, result);
  }

  @Test
  void testGetPollStatus() {
    ResponseEntity<PollerSummary> result = pollsApiServiceImpl.getPollStatus("psId");
    Assertions.assertEquals(null, result);
  }


  @Test
  void testGetPollPeerVoteUrls() {
    ResponseEntity<UrlPager> result = pollsApiServiceImpl
        .getPollPeerVoteUrls("pollKey", "peerId", "urls", Integer.valueOf(0), Integer.valueOf(0));
    Assertions.assertEquals(null, result);
  }

  @Test
  void testGetRepairQueueData() {
    ResponseEntity<RepairPager> result = pollsApiServiceImpl
        .getRepairQueueData("pollKey", "repair", Integer.valueOf(0), Integer.valueOf(0));
    Assertions.assertEquals(null, result);
  }

  @Test
  void testGetTallyUrls() {
    ResponseEntity<UrlPager> result = pollsApiServiceImpl
        .getTallyUrls("pollKey", "tally", Integer.valueOf(0), Integer.valueOf(0));
    Assertions.assertEquals(null, result);
  }

  @Test
  void testGetPollsAsPoller() {
    ResponseEntity<PollerPager> result = pollsApiServiceImpl
        .getPollsAsPoller(Integer.valueOf(0), Integer.valueOf(0));
    Assertions.assertEquals(null, result);
  }

  @Test
  void testGetPollsAsVoter() {
    ResponseEntity<VoterPager> result = pollsApiServiceImpl
        .getPollsAsVoter(Integer.valueOf(0), Integer.valueOf(0));
    Assertions.assertEquals(null, result);
  }
}

