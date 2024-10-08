/*
 * Copyright (c) 2018-2019 Board of Trustees of Leland Stanford Jr. University,
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

package org.lockss.laaws.poller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.app.LockssDaemon;
import org.lockss.laaws.poller.api.PollsApiController;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PollerApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestPollerApplication extends SpringLockssTestCase4 {

  // The port that Tomcat is using during this test.
  @LocalServerPort
  private int port;

  // The application Context used to specify the command line arguments.
  @Autowired
  private ApplicationContext appCtx;

  @Autowired
  private PollsApiController controller;


  private static final Logger logger =
      LoggerFactory.getLogger(TestPollerApplication.class);


  /**
   * Set up code to be run before each test.
   *
   * @throws IOException if there are problems.
   */
  @Before
  public void setup() throws Exception {
    super.setUp();
    if (logger.isDebugEnabled()) {
      logger.debug("port = " + port);
    }
    // Set up the temporary directory where the test data will reside.
    setUpTempDirectory(TestPollerApplication.class.getCanonicalName());
    // Copy the necessary files to the test temporary directory.
    File srcTree = new File(new File("test"), "cache");
    if (logger.isDebugEnabled()) {
      logger.debug("srcTree = " + srcTree.getAbsolutePath());
    }

    copyToTempDir(srcTree);

    // Set up the UI port.
    setUpUiPort(UI_PORT_CONFIGURATION_TEMPLATE, UI_PORT_CONFIGURATION_FILE);

    runAuthenticated();
  }

  @After
  public void tearDown() throws Exception {
    if (controller != null) {
      LockssDaemon daemon = LockssDaemon.getLockssDaemon();
      if (daemon != null) {
        if (daemon.getIdentityManager() != null) {
          daemon.getIdentityManager().stopService();
        }
        if (daemon.getHashService() != null) {
          daemon.getHashService().stopService();
        }
        if (daemon.getRouterManager() != null) {
          daemon.getRouterManager().stopService();
        }
      }
    }
    super.tearDown();
  }


  @Test
  public void contextLoads() {
    logger.info("context-loaded");
    assertNotNull(controller);
  }

  /**
   * Runs the Swagger-related tests.
   *
   * @throws Exception if there are problems.
   */
  @Test
  public void testGetSwaggerDocs() throws Exception {
    runGetSwaggerDocsTest(getTestUrlTemplate("/v3/api-docs"));
  }

  @Test
  public void testGetPollerPolls() throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("Get Poller Polls...");
    }

  }


  /**
   * Runs the tests with authentication turned on.
   *
   * @throws Exception if there are problems.
   */
  private void runAuthenticated() throws Exception {
    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = new ArrayList<String>();

    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/common.xml");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/lockss.txt");
    cmdLineArgs.add("-b");
    cmdLineArgs.add(getPlatformDiskSpaceConfigPath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/pollerApiControllerTestAuthOn.opt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add(getUiPortConfigFile().getAbsolutePath());

    CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));

    if (logger.isDebugEnabled()) {
      logger.debug("Done.");
    }
  }

  /**
   * Provides the URL template to be tested.
   *
   * @param pathAndQueryParams A String with the path and query parameters of the URL template to
   * be tested.
   * @return a String with the URL template to be tested.
   */
  private String getTestUrlTemplate(String pathAndQueryParams) {
    return "http://localhost:" + port + pathAndQueryParams;
  }


}
