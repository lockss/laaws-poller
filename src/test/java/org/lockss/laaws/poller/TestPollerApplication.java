package org.lockss.laaws.poller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import org.lockss.test.SpringLockssTestCase;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestPollerApplication extends SpringLockssTestCase {

	private static final Logger logger =
			LoggerFactory.getLogger(TestPollerApplication.class);
	// The port that Tomcat is using during this test.
	@LocalServerPort
	private int port;

	/* The identifier of an AU that exists in the test system. */
	String goodAuid = "org|lockss|plugin|pensoft|oai|PensoftOaiPlugin"
			+ "&au_oai_date~2014&au_oai_set~biorisk"
			+ "&base_url~http%3A%2F%2Fbiorisk%2Epensoft%2Enet%2F";

	/* The name of an AU that exists in the test system. */
	String goodAuName = "BioRisk Volume 2014";

	// The application Context used to specify the command line arguments to be
	// used for the tests.
	@Autowired
	ApplicationContext appCtx;

	/**
	 * Set up code to be run before each test.
	 *
	 * @throws IOException if there are problems.
	 */
	@Before
	public void setUpBeforeEachTest() throws Exception {
		if (logger.isDebugEnabled()) logger.debug("port = " + port);

		// Set up the temporary directory where the test data will reside.
		setUpTempDirectory(PollerApplication.class.getCanonicalName());

		// Copy the necessary files to the test temporary directory.
		File srcTree = new File(new File("test"), "cache");
		if (logger.isDebugEnabled())
			logger.debug("srcTree = " + srcTree.getAbsolutePath());

		copyToTempDir(srcTree);

		srcTree = new File(new File("test"), "tdbxml");
		if (logger.isDebugEnabled())
			logger.debug("srcTree = " + srcTree.getAbsolutePath());

		copyToTempDir(srcTree);
    runAuthenticated();
	}

	@Test
	public void contextLoads() {
	}

  /**
   * Runs the Swagger-related tests.
   *
   * @throws Exception
   *           if there are problems.
   */
  @Test public void testGetSwaggerDocs() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");
    ResponseEntity<String> successResponse = new TestRestTemplate().exchange(
        getTestUrlTemplate("/v2/api-docs"),
        HttpMethod.GET, null, String.class);

    HttpStatus statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);
    logger.info(successResponse.getBody());
    String expectedBody = "{'swagger':'2.0',"
        + "'info':{'description':'REST API for handling poller tasks '}}";
    JSONAssert.assertEquals(expectedBody, successResponse.getBody(), false);
    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the status-related tests.
   *
   * @throws Exception
   *           if there are problems.
   */

  @Test public void testGetStatus() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    ResponseEntity<String> successResponse = new TestRestTemplate().exchange(
        getTestUrlTemplate("/status"), HttpMethod.GET, null, String.class);

    HttpStatus statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    String expectedBody = "{\"version\":\"1.0.0\",\"ready\":true}}";

    JSONAssert.assertEquals(expectedBody, successResponse.getBody(), false);
    if (logger.isDebugEnabled()) logger.debug("Done.");
  }


  private void runUnauthenticated () throws Exception {
    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/pollerApiControllerTestAuthOff.opt");

    CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the tests with authentication turned on.
   *
   * @throws Exception
   *           if there are problems.
   */
  private void runAuthenticated() throws Exception {
    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/pollerApiControllerTestAuthOn.opt");

    CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));
    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Provides the standard command line arguments to start the server.
   *
   * @return a List<String> with the command line arguments.
   */
  private List<String> getCommandLineArguments() {
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

    return cmdLineArgs;
  }

  /**
   * Provides the URL template to be tested.
   *
   * @param pathAndQueryParams
   *          A String with the path and query parameters of the URL template to
   *          be tested.
   * @return a String with the URL template to be tested.
   */
  private String getTestUrlTemplate(String pathAndQueryParams) {
    return "http://localhost:" + port + pathAndQueryParams;
  }

}
