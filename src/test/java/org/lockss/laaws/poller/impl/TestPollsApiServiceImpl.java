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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.lockss.laaws.poller.PollerApplication;
import org.lockss.laaws.poller.model.*;
import org.lockss.plugin.PluginManager;
import org.lockss.poller.PollManager;
import org.lockss.poller.PollSpec;
import org.lockss.rs.status.ApiStatus;
import org.lockss.test.SpringLockssTestCase;

class TestPollsApiServiceImpl extends SpringLockssTestCase {

  @Spy
  PollManager pollManager;
  @Spy
  PluginManager pluginManager;
  @Mock
  HashMap<String, PollSpec> requestMap;
  @Mock
  HttpServletRequest request;
  @InjectMocks
  PollsApiServiceImpl pollsApiServiceImpl;

  /* The identifier of an AU that exists in the test system. */
  String goodAuid = "org|lockss|plugin|pensoft|oai|PensoftOaiPlugin"
      + "&au_oai_date~2014&au_oai_set~biorisk"
      + "&base_url~http%3A%2F%2Fbiorisk%2Epensoft%2Enet%2F";

  /* The name of an AU that exists in the test system. */
  String goodAuName = "BioRisk Volume 2014";

  @BeforeEach
  void initAll() {
    MockitoAnnotations.initMocks(this);

    initDirs();
    initDaemon();
    // set up the mock Request object to return the configured host:port.
    when(request.getRequestURI()).thenReturn("http://localhost:49520");
  }

  @Test
  void testGetApiStatus() {
    ApiStatus result = pollsApiServiceImpl.getApiStatus();
    Assertions.assertTrue(result.isReady());
    Assertions.assertEquals("1.0.0", result.getVersion());
  }

  @Test
  void testCallQueryCancelPoll() {
    PollDesc desc = new PollDesc();
    // straight forward request to start a poll.
    desc.setAuId(goodAuid);
    ResponseEntity<String> result = pollsApiServiceImpl.callPoll(desc);
    Assertions.assertEquals(goodAuid, result.getBody());
    Assertions.assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());

    // check the status of the poll.
    ResponseEntity<PollerSummary> summaryResponse = pollsApiServiceImpl.getPollStatus(goodAuid);
    PollerSummary summary = summaryResponse.getBody();
    Assertions.assertNotNull(summary);

    // now cancel it.
    pollsApiServiceImpl.cancelPoll(goodAuid);
    // check the status of the poll.
    summaryResponse = pollsApiServiceImpl.getPollStatus(goodAuid);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, summaryResponse.getStatusCode());
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
  void testGetPollsAsPoller() {
    ResponseEntity<PollerPager> result = pollsApiServiceImpl
        .getPollsAsPoller(20, 1);
    PollerPager pager = result.getBody();
    Assertions.assertEquals(0, pager.getPolls().size());
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
  }

  @Test
  void testGetPollsAsVoter() {
    ResponseEntity<VoterPager> result = pollsApiServiceImpl
        .getPollsAsVoter(20, 1);
    VoterPager pager = result.getBody();
    Assertions.assertEquals(null, pager.getPolls());
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
  }


  private void initDaemon() {
    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = new ArrayList<String>();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/common.xml");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/lockss.txt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.txt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.opt");
    cmdLineArgs.add("-b");
    cmdLineArgs.add(getPlatformDiskSpaceConfigPath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/pollerApiControllerTestAuthOn.opt");
    PollerApplication app = new PollerApplication();
    app.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));
  }

  private void initDirs() {
    // Set up the temporary directory where the test data will reside.

    try {
      setUpTempDirectory(PollsApiServiceImpl.class.getCanonicalName());
      // Copy the necessary files to the test temporary directory.
      File srcTree = new File(new File("test"), "cache");
      copyToTempDir(srcTree);
      srcTree = new File(new File("test"), "tdbxml");
      copyToTempDir(srcTree);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}

