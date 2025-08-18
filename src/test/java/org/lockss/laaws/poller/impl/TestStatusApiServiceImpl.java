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

import com.fasterxml.jackson.core.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.*;
import org.lockss.app.*;
import org.lockss.laaws.poller.*;
import org.lockss.log.*;
import org.lockss.spring.test.*;
import org.lockss.util.rest.status.*;
import org.skyscreamer.jsonassert.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.client.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.test.context.junit4.*;

/**
 * This annotation tells JUnit to run using Spring's testing support.
 * It provides functionality of the Spring TestContext Framework to standard JUnit tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PollerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestStatusApiServiceImpl extends SpringLockssTestCase4 {
  private static final L4JLogger log = L4JLogger.getLogger();

  // The port that Tomcat is using during this test.
  @LocalServerPort
  private int port;

  // The application Context used to specify the command line arguments to be
  // used for the tests.
  @Autowired
  ApplicationContext appCtx;

  /**
   * Set up code to be run before each test.
   *
   * @throws Exception
   *           if there are problems.
   */
  @Before
  public void setUpBeforeEachTest() throws Exception {
    TestStatusApiServiceImpl.log.debug2("port = {}", () -> this.port);

    // Set up the temporary directory where the test data will reside.
    this.setUpTempDirectory(TestStatusApiServiceImpl.class.getCanonicalName());

    // Set up the UI port.
    this.setUpUiPort(SpringLockssTestCase4.UI_PORT_CONFIGURATION_TEMPLATE, SpringLockssTestCase4.UI_PORT_CONFIGURATION_FILE);

    TestStatusApiServiceImpl.log.debug2("Done");
  }

  /**
   * Runs the tests with authentication turned off.
   *
   * @throws Exception
   *           if there are problems.
   */
  @Test
  public void runUnAuthenticatedTests() throws Exception {
    TestStatusApiServiceImpl.log.debug2("Invoked");

    // Specify the command line parameters to be used for the tests.
    final List<String> cmdLineArgs = this.getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/testAuthOff.txt");

    final CommandLineRunner runner = this.appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));

    this.runGetSwaggerDocsTest(this.getTestUrlTemplate("/v3/api-docs"));
    this.getStatusTest();

    TestStatusApiServiceImpl.log.debug2("Done");
  }

  /**
   * Runs the tests with authentication turned on.
   *
   * @throws Exception
   *           if there are problems.
   */
  @Test
  public void runAuthenticatedTests() throws Exception {
    TestStatusApiServiceImpl.log.debug2("Invoked");

    // Specify the command line parameters to be used for the tests.
    final List<String> cmdLineArgs = this.getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/testAuthOn.txt");

    final CommandLineRunner runner = this.appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));

    this.runGetSwaggerDocsTest(this.getTestUrlTemplate("/v3/api-docs"));
    this.getStatusTest();

    TestStatusApiServiceImpl.log.debug2("Done");
  }

  /**
   * Provides the standard command line arguments to start the server.
   *
   * @return a List<String> with the command line arguments.
   * @throws IOException
   *           if there are problems.
   */
  private List<String> getCommandLineArguments() throws IOException {
    TestStatusApiServiceImpl.log.debug2("Invoked");

    final List<String> cmdLineArgs = new ArrayList<String>();
    cmdLineArgs.add("-p");
    cmdLineArgs.add(this.getPlatformDiskSpaceConfigPath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add(this.getUiPortConfigFile().getAbsolutePath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.txt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.opt");

    TestStatusApiServiceImpl.log.debug2("cmdLineArgs = {}", () -> cmdLineArgs);
    return cmdLineArgs;
  }

  /**
   * Runs the status-related tests.
   *
   * @throws JsonProcessingException
   *           if there are problems getting the expected status in JSON format.
   */
  private void getStatusTest() throws JsonProcessingException {
    TestStatusApiServiceImpl.log.debug2("Invoked");

    final ResponseEntity<String> successResponse = new TestRestTemplate().exchange(
        this.getTestUrlTemplate("/status"), HttpMethod.GET, null, String.class);

    final HttpStatusCode statusCode = successResponse.getStatusCode();
    final HttpStatus status = HttpStatus.valueOf(statusCode.value());
    Assert.assertEquals(HttpStatus.OK, status);

    // Get the expected result.
    final ApiStatus expected = new ApiStatus("swagger/swagger.yaml");
    expected.setReady(true);
    expected.setReadyTime(LockssApp.getLockssApp().getReadyTime());
    if (LockssDaemon.getLockssDaemon().areLoadablePluginsReady()) {
      expected.setStartupStatus(ApiStatus.StartupStatus.AUS_STARTED);
    } else {
      expected.setStartupStatus(ApiStatus.StartupStatus.NONE);
    }

    JSONAssert.assertEquals(expected.toJson(), successResponse.getBody(),
        false);

    TestStatusApiServiceImpl.log.debug2("Done");
  }

  /**
   * Provides the URL template to be tested.
   *
   * @param pathAndQueryParams
   *          A String with the path and query parameters of the URL template to
   *          be tested.
   * @return a String with the URL template to be tested.
   */
  private String getTestUrlTemplate(final String pathAndQueryParams) {
    return "http://localhost:" + this.port + pathAndQueryParams;
  }
}