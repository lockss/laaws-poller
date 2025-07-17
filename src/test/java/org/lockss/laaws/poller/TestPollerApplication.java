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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.app.LockssDaemon;
import org.lockss.config.ConfigManager;
import org.lockss.config.Configuration;
import org.lockss.crawler.FuncNewContentCrawler.MySimulatedArchivalUnit;
import org.lockss.crawler.FuncNewContentCrawler.MySimulatedPlugin;
import org.lockss.laaws.poller.api.PollsApiController;
import org.lockss.log.L4JLogger;
import org.lockss.plugin.PluginTestUtil;
import org.lockss.plugin.simulated.SimulatedContentGenerator;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PollerApplication.class},
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TestPollerApplication extends SpringLockssTestCase4 {

  private static final String UI_PORT_CONFIGURATION_TEMPLATE = "UiPortConfigTemplate.txt";
  private static final String UI_PORT_CONFIGURATION_FILE = "UiPort.txt";
  private static final L4JLogger logger = L4JLogger.getLogger();

  // The port that Tomcat is using during this test.
  @LocalServerPort
  private int port;

  // The application Context used to specify the command line arguments.
  @Autowired
  private ApplicationContext appCtx;

  @Autowired
  private PollsApiController controller;

  private MySimulatedArchivalUnit sau;

  // Credentials.
  private final Credentials USER_ADMIN = this.new Credentials("lockss-u", "lockss-p");
  private final Credentials AU_ADMIN =
    new Credentials("au-admin", "I'mAuAdmin");
  private final Credentials CONTENT_ADMIN =
    this.new Credentials("content-admin", "I'mContentAdmin");
  private final Credentials ACCESS_CONTENT =
    this.new Credentials("access-content", "I'mAccessContent");
  private final Credentials ANYBODY = this.new Credentials("someUser", "somePassword");


  /**
   * Set up code to be run before each test.
   *
   * @throws IOException if there are problems.
   */
  /**
   * Set up code to be run before each test.
   *
   * @throws Exception if there are problems.
   */
  @Before
  public void setUpBeforeEachTest() throws Exception {
    logger.debug2("port = {}", port);

    // Set up the temporary directory where the test data will reside.
    setUpTempDirectory(TestPollerApplication.class.getCanonicalName());

    // Set up the UI port.
    setUpUiPort(UI_PORT_CONFIGURATION_TEMPLATE, UI_PORT_CONFIGURATION_FILE);

    sau =
      (MySimulatedArchivalUnit)
        PluginTestUtil.createAndStartSimAu(
          MySimulatedPlugin.class, simAuConfig(getTempDirPath()));

    logger.trace("Generating tree of size 3x1x2 with 3000 byte files...");
    sau.generateContentTree();

    logger.debug2("Done");
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

  @Test
  public void runAuthenticatedTests() throws Exception {
    logger.debug2("Invoked");

    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/testAuthOn.opt");

    CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));
    startAllAusIfNecessary();

    runGetSwaggerDocsTest(getTestUrlTemplate("/v3/api-docs"));

  }

  // Can't be part of setUpBeforeEachTest as daemon hasn't been started yet
  private void startAllAusIfNecessary() {
    startAuIfNecessary(sau.getAuId());
  }
  /**
   * Provides the configuration of a simulated Archival Unit.
   *
   * @param rootPath A String with the path where the simulated Archival Unit files will be stored.
   * @return a Configuration with the simulated Archival Unit configuration.
   */
  private Configuration simAuConfig(String rootPath) {
    Configuration conf = ConfigManager.newConfiguration();
    conf.put("root", rootPath);
    conf.put("depth", "3");
    conf.put("branch", "1");
    conf.put("numFiles", "2");
    conf.put("fileTypes", "" + SimulatedContentGenerator.FILE_TYPE_BIN);
    conf.put("binFileSize", "" + 3000);
    return conf;
  }


  /**
   * Provides the standard command line arguments to start the server.
   *
   * @return a List of type string with the command line arguments.
   * @throws IOException if there are problems.
   */
  private List<String> getCommandLineArguments() throws IOException {
    logger.debug2("Invoked");

    List<String> cmdLineArgs = new ArrayList<String>();
    cmdLineArgs.add("-p");
    cmdLineArgs.add(getPlatformDiskSpaceConfigPath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/common.xml");

    File folder = new File(new File(new File(getTempDirPath()), "crawler"), "prod");
    logger.info("folder = {}", () -> folder);

    cmdLineArgs.add("-x");
    cmdLineArgs.add(folder.getAbsolutePath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.txt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add(getUiPortConfigFile().getAbsolutePath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.opt");

    logger.debug2("cmdLineArgs = {}", () -> cmdLineArgs);
    return cmdLineArgs;
  }

  /**
   * Provides the URL template to be tested.
   *
   * @param pathAndQueryParams A String with the path and query parameters of the URL template to be
   * tested.
   * @return a String with the URL template to be tested.
   */
  private String getTestUrlTemplate(String pathAndQueryParams) {
    return "http://localhost:" + port + pathAndQueryParams;
  }


}