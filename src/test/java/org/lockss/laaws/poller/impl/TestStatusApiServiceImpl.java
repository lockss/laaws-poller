/*
 * Copyright (c) 2019 Board of Trustees of Leland Stanford Jr. University,
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

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.app.*;
import org.lockss.log.L4JLogger;
import org.lockss.util.rest.status.ApiStatus;
import org.lockss.protocol.IdentityManager;
import org.lockss.protocol.PeerIdentity;
import org.lockss.protocol.V3LcapMessage;
import org.lockss.test.LockssTestCase4;
import org.lockss.test.MockArchivalUnit;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** @noinspection TestShouldMockAllTestedDependenciesInspection*/
public class TestStatusApiServiceImpl extends LockssTestCase4 {
  private static L4JLogger log = L4JLogger.getLogger();

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private StatusApiServiceImpl statusApiServiceImpl;

  protected static MockArchivalUnit mTestAu;

  protected PeerIdentity testID;
  protected V3LcapMessage[] v3Testmsg;
  protected IdentityManager mIdManager;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Runs the status-related test.
   * 
   * @throws JsonProcessingException
   *           if there are problems getting the expected status in JSON format.
   */
  @Test
  public void testGetApiStatus() throws JsonProcessingException {
    ApiStatus result = statusApiServiceImpl.getApiStatus();

    // Get the expected result.
    ApiStatus expected = new ApiStatus("swagger/swagger.yaml");
    expected.setReady(false);
    expected.setReadyTime(LockssApp.getLockssApp().getReadyTime());
    if (LockssDaemon.getLockssDaemon().areLoadablePluginsReady()) {
      expected.setStartupStatus(ApiStatus.StartupStatus.AUS_STARTED);
    } else {
      expected.setStartupStatus(ApiStatus.StartupStatus.NONE);
    }

    Assert.assertEquals(expected.toJson(), result.toJson());
  }
}

