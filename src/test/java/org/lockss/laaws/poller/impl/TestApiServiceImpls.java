/*

Copyright (c) 2000-2020 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.lockss.laaws.poller.impl;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import org.junit.*;
import org.junit.runner.*;
import org.lockss.app.*;
import org.lockss.config.*;
import org.lockss.crawler.FuncNewContentCrawler.*;
import org.lockss.laaws.poller.*;
import org.lockss.laaws.poller.model.*;
import org.lockss.log.*;
import org.lockss.plugin.*;
import org.lockss.plugin.simulated.*;
import org.lockss.poller.*;
import org.lockss.poller.v3.*;
import org.lockss.spring.test.*;
import org.lockss.util.rest.*;
import org.lockss.util.rest.poller.*;
import org.lockss.util.rest.status.*;
import org.mockito.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;
import org.skyscreamer.jsonassert.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.boot.test.web.client.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.boot.web.client.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.junit4.*;
import org.springframework.test.util.*;
import org.springframework.web.util.*;

/**
 * Test class for org.lockss.laaws.md.api.MetadataApiServiceImpl and
 * org.lockss.laaws.md.api.UrlsApiServiceImpl.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PollerApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TestApiServiceImpls extends SpringLockssTestCase4 {

  private static final L4JLogger log = L4JLogger.getLogger();

  private static final String UI_PORT_CONFIGURATION_TEMPLATE = "UiPortConfigTemplate.txt";
  private static final String UI_PORT_CONFIGURATION_FILE = "UiPort.txt";

  private static final String EMPTY_STRING = "";
  // The identifier of an AU that does not exist in the test system.
  private static final String UNKNOWN_AUID = "unknown_auid";

  private static final String BOGUS_ID = "bogus";

  // Credentials.
  private final Credentials USER_ADMIN = new Credentials("lockss-u", "lockss-p");
  private final Credentials AU_ADMIN = new Credentials("au-admin", "I'mAuAdmin");
  private final Credentials CONTENT_ADMIN = new Credentials("content-admin", "I'mContentAdmin");
  private final Credentials ACCESS_CONTENT = new Credentials("access-content", "I'mAccessContent");
  private final Credentials ANYBODY = new Credentials("someUser", "somePassword");


  // The port that Tomcat is using during this test.
  @LocalServerPort
  private int port;

  // The application Context used to specify the command line arguments to be
  // used for the tests.
  @Autowired
  ApplicationContext appCtx;

// Spy the real PollManager bean instead of a pure mock
  @SpyBean
  private PollManager pollManagerSpy;



  @Autowired
  private PollsApiServiceImpl pollsApiService;

  // Mock PluginManager for testing
  private MySimulatedArchivalUnit sau;

  private MockEntryManager mockEntryManager;


  /**
   * Set up code to be run before each test.
   *
   * @throws Exception if there are problems.
   */
  @Before
  public void setUpBeforeEachTest() throws Exception {
    log.debug2("port = {}", port);
    // Set up the temporary directory where the test data will reside.
    setUpTempDirectory(TestPollerApplication.class.getCanonicalName());

    // Set up the UI port.
    setUpUiPort(UI_PORT_CONFIGURATION_TEMPLATE, UI_PORT_CONFIGURATION_FILE);

    sau =
        (MySimulatedArchivalUnit)
            PluginTestUtil.createAndStartSimAu(
                MySimulatedPlugin.class, simAuConfig(getTempDirPath()));

    log.trace("Generating tree of size 3x1x2 with 3000 byte files...");
    sau.generateContentTree();
    createEntryManagerMock();
    // Force the service to use our spy
    PollManager pollManager = LockssDaemon.getLockssDaemon().getPollManager();
    pollManagerSpy = spy(pollManager);

    log.debug2("Done");
  }
  @After
  public void tearDown() throws Exception {
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
    super.tearDown();
  }

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
   * If you need to swap in a different mock/impl at runtime,
   * use this helper:
   */
  private Object createEntryManagerMock() {
    Class<?> emClass = Arrays.stream(PollManager.class.getDeclaredClasses())
        .filter(c -> "EntryManager".equals(c.getSimpleName()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("EntryManager class not found"));

    // create a Mockito mock with a custom Answer
    return Mockito.mock(emClass, new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        Object[] args = invocation.getArguments();

        switch (method) {
          case "addPoll":
            mockEntryManager.addPoll((Poll)args[0]);
            break;
          case "getCurrentPoll":
            return mockEntryManager.getCurrentPoll((String)args[0]);
          case "isPollRunning":
            return mockEntryManager.isPollRunning((String)args[0]);
          case "forAu":
            return mockEntryManager.forAu((ArchivalUnit)args[0]);
          case "forAuId":
            return mockEntryManager.forAuId((String)args[0]);
          default:
            // if you want real behavior by reflection, you could do:
            // return invocation.callRealMethod();
            // otherwise:
            throw new UnsupportedOperationException("Unexpected method: " + method);
        }
        return null;
      }
    });
  }

  private void setSpyEntryManager() {
    ReflectionTestUtils.setField(pollManagerSpy, "entryManager", createEntryManagerMock());
  }

  /**
   * Runs the tests with authentication turned on.
   *
   * @throws Exception if there are problems.
   */
  @Test
  public void runAuthenticatedTests() throws Exception {
    log.debug2("Invoked");

    // Specify the command line parameters to be used for the tests.
    final List<String> cmdLineArgs = getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/testAuthOn.txt");

    final CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));
    startAllAusIfNecessary();

    runGetSwaggerDocsTest(getTestUrlTemplate("/v3/api-docs"));
    runStatusApiServiceImplTests();
    runPollsApiServiceImplTests();
    runWsApiServiceImplTests();
    log.debug2("Done");
  }



  /**
   * Provides the standard command line arguments to start the server.
   *
   * @return a List of type Swith the command line arguments.
   * @throws IOException if there are problems.
   */
  private List<String> getCommandLineArguments() throws IOException {
    log.debug2("Invoked");

    final List<String> cmdLineArgs = new ArrayList<String>();
    cmdLineArgs.add("-p");
    cmdLineArgs.add(getPlatformDiskSpaceConfigPath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/common.xml");
    cmdLineArgs.add("-p");
    cmdLineArgs.add(getUiPortConfigFile().getAbsolutePath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.txt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.opt");

    log.debug2("cmdLineArgs = {}", () -> cmdLineArgs);
    return cmdLineArgs;
  }

  /**
   * Runs the PollsApiServiceImpl-related authenticated-specific tests.
   *
   * @throws Exception if there are problems.
   */
  private void runPollsApiServiceImplTests() throws Exception {
    log.debug2("Invoked");
    runMethodsNotAllowedTest();
    runTestCallPoll();
    runTestCancelPoll();
    runTestGetPollStatus();
    runTestGetPollerPollDetails();
    runTestGetVoterPollDetails();
    runTestGetPollsAsPoller();
    runTestGetPollsAsVoter();
    log.debug2("Done");
  }

  private void runStatusApiServiceImplTests() throws Exception {
    log.debug2("Invoked");
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
    log.debug2("Done");
  }

  private void runWsApiServiceImplTests() throws Exception {
    log.debug2("Invoked");
    log.debug2("Done");
  }
  private void runMethodsNotAllowedTest() {
    log.debug2("Invoked");

    // Missing poll ID.
    runTestMethodNotAllowed(null, ANYBODY, HttpMethod.PUT, HttpStatus.UNAUTHORIZED);

    // Empty poll ID.
    runTestMethodNotAllowed(EMPTY_STRING, null, HttpMethod.PATCH, HttpStatus.UNAUTHORIZED);

    // Unknown poll ID.
    runTestMethodNotAllowed(BOGUS_ID, ANYBODY, HttpMethod.PUT, HttpStatus.UNAUTHORIZED);

    // No credentials.
    runTestMethodNotAllowed(BOGUS_ID, null, HttpMethod.PATCH, HttpStatus.UNAUTHORIZED);
    runTestMethodNotAllowed(null, USER_ADMIN, HttpMethod.PUT, HttpStatus.NOT_FOUND);

    // Empty poll ID.
    runTestMethodNotAllowed(EMPTY_STRING, CONTENT_ADMIN, HttpMethod.PATCH, HttpStatus.NOT_FOUND);

    // Unknown poll ID.
    runTestMethodNotAllowed(BOGUS_ID, ACCESS_CONTENT, HttpMethod.PUT,
        HttpStatus.METHOD_NOT_ALLOWED);

    runTestMethodNotAllowed(BOGUS_ID, USER_ADMIN, HttpMethod.PUT, HttpStatus.METHOD_NOT_ALLOWED);

    log.debug2("Done");
  }

  private void runTestMethodNotAllowed(final String pollId, final Credentials credentials,
      final HttpMethod method, final HttpStatus expectedStatus) {
    log.debug2("pollId = {}", pollId);
    log.debug2("credentials = {}", credentials);
    log.debug2("method = {}", method);
    log.debug2("expectedStatus = {}", expectedStatus);

    // Get the test URL template.
    final String template = getTestUrlTemplate("/polls/{pollId}");

    // Create the URI of the request to the REST service.
    final UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build()
        .expand(Collections.singletonMap("pollId", pollId));

    final URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode()
        .toUri();
    log.trace("uri = {}", uri);

    // Initialize the request to the REST service.
    final RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);

    HttpEntity<String> requestEntity = null;

    // Get the individual credentials elements.
    String user = null;
    String password = null;

    if (null != credentials) {
      user = credentials.getUser();
      password = credentials.getPassword();
    }

    // Check whether there are any custom headers to be specified in the
    // request.
    if (null != user || null != password) {

      // Initialize the request headers.
      final HttpHeaders headers = new HttpHeaders();

      // Set up the authentication credentials, if necessary.
      if (credentials != null) {
        credentials.setUpBasicAuthentication(headers);
      }

      log.trace("requestHeaders = {}", () -> headers.toSingleValueMap());

      // Create the request entity.
      requestEntity = new HttpEntity<>(null, headers);
    }

    // Make the request and get the response.
    final ResponseEntity<String> response = new TestRestTemplate(templateBuilder).exchange(uri,
        method, requestEntity, String.class);

    // Get the response status.
    final HttpStatusCode statusCode = response.getStatusCode();
    final HttpStatus status = HttpStatus.valueOf(statusCode.value());
    Assert.assertFalse(RestUtil.isSuccess(status));
    Assert.assertEquals(expectedStatus, status);
  }

  private String runTestCallPollWithStatus(final PollDesc body, final Credentials credentials,
      final HttpMethod method, final HttpStatus expectedStatus) throws Exception {
    String result = null;
    log.debug2("body = {}", body);
    log.debug2("credentials = {}", credentials);
    log.debug2("method = {}", method);
    log.debug2("expectedStatus = {}", expectedStatus);
    // Get the test URL template.
    final String template = getTestUrlTemplate("/polls");

    // Create the URI of the request to the REST service.
    final UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build();
    final URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode()
        .toUri();
    log.trace("uri = {}", uri);

    // Initialize the request to the REST service.
    final RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);

    HttpEntity<PollDesc> requestEntity = null;

    // Get the individual credentials elements.
    String user = null;
    String password = null;

    if (null != credentials) {
      user = credentials.getUser();
      password = credentials.getPassword();
    }

    // Check whether there are any custom headers to be specified in the
    // request.
    if (null != user || null != password) {

      // Initialize the request headers.
      final HttpHeaders headers = new HttpHeaders();

      // Set up the authentication credentials, if necessary.
      if (credentials != null) {
        credentials.setUpBasicAuthentication(headers);
      }

      log.trace("requestHeaders = {}", () -> headers.toSingleValueMap());

      // Create the request entity.
      requestEntity = new HttpEntity<>(body, headers);;
    }

    // Make the request and get the response.
    final ResponseEntity<String> response = new TestRestTemplate(templateBuilder).exchange(uri,
        method, requestEntity, String.class);

    // Get the response status.
    final HttpStatusCode statusCode = response.getStatusCode();
    final HttpStatus status = HttpStatus.valueOf(statusCode.value());
    Assert.assertEquals(expectedStatus, status);
    if (RestUtil.isSuccess(status)) {
      result = response.getBody();
    }

    log.debug2("result = {}", result);
    return result;
  }


  private PollerSummary runTestGetPollStatus(final String pollId, final Credentials credentials,
      final HttpMethod method, final HttpStatus expectedStatus) throws Exception {

    PollerSummary result = null;
    log.debug2("pollId = {}", pollId);
    log.debug2("credentials = {}", credentials);
    log.debug2("method = {}", method);
    log.debug2("expectedStatus = {}", expectedStatus);

    // Get the test URL template.
    final String template = getTestUrlTemplate("/polls/{pollId}");

    // Create the URI of the request to the REST service.
    final UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build()
        .expand(Collections.singletonMap("pollId", pollId));

    final URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode()
        .toUri();
    log.trace("uri = {}", uri);

    // Initialize the request to the REST service.
    final RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);

    HttpEntity<String> requestEntity = null;

    // Get the individual credentials elements.
    String user = null;
    String password = null;

    if (null != credentials) {
      user = credentials.getUser();
      password = credentials.getPassword();
    }

    // Check whether there are any custom headers to be specified in the
    // request.
    if (null != user || null != password) {

      // Initialize the request headers.
      final HttpHeaders headers = new HttpHeaders();

      // Set up the authentication credentials, if necessary.
      if (credentials != null) {
        credentials.setUpBasicAuthentication(headers);
      }

      log.trace("requestHeaders = {}", () -> headers.toSingleValueMap());

      // Create the request entity.
      requestEntity = new HttpEntity<>(null, headers);
    }

    // Make the request and get the response.
    final ResponseEntity<String> response = new TestRestTemplate(templateBuilder).exchange(uri,
        method, requestEntity, String.class);

    // Get the response status.
    final HttpStatusCode statusCode = response.getStatusCode();
    final HttpStatus status = HttpStatus.valueOf(statusCode.value());
    Assert.assertFalse(RestUtil.isSuccess(status));
    Assert.assertEquals(expectedStatus, status);

    if (RestUtil.isSuccess(status)) {
      result = new ObjectMapper().readValue(response.getBody(), PollerSummary.class);
    }

    log.debug2("result = {}", result);
    return result;
  }


  /**
   * Tests the callPoll endpoint using TestRestTemplate and mocked PollManager.
   */
  private void runTestCallPoll() throws Exception {
    log.debug2("Invoked");


    // Test with valid AU ID
    PollDesc pollDesc = new PollDesc();
    pollDesc.setAuId(sau.getAuId());
    runTestCallPollWithStatus(pollDesc, AU_ADMIN, HttpMethod.POST,HttpStatus.ACCEPTED);

    // Test with invalid AU ID
    PollDesc invalidPollDesc = new PollDesc();
    invalidPollDesc.setAuId(UNKNOWN_AUID);
    
    runTestCallPollWithStatus(invalidPollDesc, AU_ADMIN, HttpMethod.POST, HttpStatus.NOT_FOUND);

    // Test authorization - no credentials
    runTestCallPollWithStatus(pollDesc, null, HttpMethod.POST, HttpStatus.UNAUTHORIZED);

    // Test authorization - wrong role
    runTestCallPollWithStatus(pollDesc, ANYBODY,HttpMethod.POST, HttpStatus.UNAUTHORIZED);

    log.debug2("Done");
  }

  /**
   * Tests the cancelPoll endpoint.
   */
  private void runTestCancelPoll() throws Exception {
    log.debug2("Invoked");
    setSpyEntryManager();
   // Test successful cancel
    runTestCancelPollWithStatus(sau.getAuId(), AU_ADMIN, HttpStatus.OK);

    // Test cancel with unknown AU
    runTestCancelPollWithStatus(UNKNOWN_AUID, AU_ADMIN, HttpStatus.NOT_FOUND);
    // Test authorization
    runTestCancelPollWithStatus(sau.getAuId(), null, HttpStatus.UNAUTHORIZED);
    runTestCancelPollWithStatus(sau.getAuId(), ANYBODY, HttpStatus.FORBIDDEN);

    log.debug2("Done");
  }

  /**
   * Tests the getPollStatus endpoint.
   */
  private void runTestGetPollStatus() throws Exception {
    log.debug2("Invoked");

    // Setup mock poll
    V3Poller mockPoller = mock(V3Poller.class);
    when(mockPoller.getKey()).thenReturn("test-poll-key");
    when(mockPoller.getAu()).thenReturn(sau);
    when(mockPoller.getStatus()).thenReturn(1);
    when(mockPoller.getCreateTime()).thenReturn(System.currentTimeMillis());
    when(mockPoller.getPollVariant()).thenReturn(V3Poller.PollVariant.PoR);
    when(mockPoller.getDuration()).thenReturn(86400000L);
    when(mockPoller.getParticipants()).thenReturn(new ArrayList<>());
    when(mockPoller.getCompletedRepairs()).thenReturn(new ArrayList<>());
    
    PollerStateBean mockStateBean = mock(PollerStateBean.class);
    when(mockStateBean.getPollEnd()).thenReturn(System.currentTimeMillis());
    when(mockPoller.getPollerStateBean()).thenReturn(mockStateBean);

    when(pollManagerSpy.getPoll(sau.getAuId())).thenReturn(mockPoller);

    // Test successful get status
    runTestGetPollStatusWithExpectedResult(sau.getAuId(), AU_ADMIN, HttpStatus.OK);

    // Test with unknown AU
    when(pollManagerSpy.getPoll(UNKNOWN_AUID)).thenReturn(null);
    runTestGetPollStatusWithExpectedResult(UNKNOWN_AUID, AU_ADMIN, HttpStatus.NOT_FOUND);

    // Test authorization
    runTestGetPollStatusWithExpectedResult(sau.getAuId(), null, HttpStatus.UNAUTHORIZED);
    runTestGetPollStatusWithExpectedResult(sau.getAuId(), ANYBODY, HttpStatus.FORBIDDEN);

    log.debug2("Done");
  }

  /**
   * Tests the getPollerPollDetails endpoint.
   */
  private void runTestGetPollerPollDetails() throws Exception {
    log.debug2("Invoked");

    String pollKey = "test-poll-key";
    
    // Setup mock poll
    V3Poller mockPoller = mock(V3Poller.class);
    when(pollManagerSpy.getPoll(pollKey)).thenReturn(mockPoller);

    runTestGetPollerPollDetailsWithStatus(pollKey, AU_ADMIN, HttpStatus.OK);

    // Test with unknown poll key
    when(pollManagerSpy.getPoll("unknown-key")).thenReturn(null);
    runTestGetPollerPollDetailsWithStatus("unknown-key", AU_ADMIN, HttpStatus.NOT_FOUND);

    // Test authorization
    runTestGetPollerPollDetailsWithStatus(pollKey, null, HttpStatus.UNAUTHORIZED);
    runTestGetPollerPollDetailsWithStatus(pollKey, ANYBODY, HttpStatus.FORBIDDEN);

    log.debug2("Done");
  }

  /**
   * Tests the getVoterPollDetails endpoint.
   */
  private void runTestGetVoterPollDetails() throws Exception {
    log.debug2("Invoked");

    String pollKey = "test-voter-poll-key";
    
    // Setup mock poll
    V3Voter mockVoter = mock(V3Voter.class);
    when(pollManagerSpy.getPoll(pollKey)).thenReturn(mockVoter);

    runTestGetVoterPollDetailsWithStatus(pollKey, AU_ADMIN, HttpStatus.OK);

    // Test with unknown poll key
    when(pollManagerSpy.getPoll("unknown-voter-key")).thenReturn(null);
    runTestGetVoterPollDetailsWithStatus("unknown-voter-key", AU_ADMIN, HttpStatus.NOT_FOUND);

    // Test authorization
    runTestGetVoterPollDetailsWithStatus(pollKey, null, HttpStatus.UNAUTHORIZED);
    runTestGetVoterPollDetailsWithStatus(pollKey, ANYBODY, HttpStatus.FORBIDDEN);

    log.debug2("Done");
  }

  /**
   * Tests the getPollsAsPoller endpoint.
   */
  private void runTestGetPollsAsPoller() throws Exception {
    log.debug2("Invoked");

    // Setup mock pollers
    Collection<V3Poller> mockPollers = new ArrayList<>();
    when(pollManagerSpy.getV3Pollers()).thenReturn(mockPollers);

    runTestGetPollsAsPollerWithStatus(1, 20, AU_ADMIN, HttpStatus.OK);

    // Test authorization
    runTestGetPollsAsPollerWithStatus(1, 20, null, HttpStatus.UNAUTHORIZED);
    runTestGetPollsAsPollerWithStatus(1, 20, ANYBODY, HttpStatus.FORBIDDEN);

    log.debug2("Done");
  }

  /**
   * Tests the getPollsAsVoter endpoint.
   */
  private void runTestGetPollsAsVoter() throws Exception {
    log.debug2("Invoked");

    // Setup mock voters
    Collection<V3Voter> mockVoters = new ArrayList<>();
    when(pollManagerSpy.getV3Voters()).thenReturn(mockVoters);

    runTestGetPollsAsVoterWithStatus(1, 20, AU_ADMIN, HttpStatus.OK);

    // Test authorization
    runTestGetPollsAsVoterWithStatus(1, 20, null, HttpStatus.UNAUTHORIZED);
    runTestGetPollsAsVoterWithStatus(1, 20, ANYBODY, HttpStatus.FORBIDDEN);

    log.debug2("Done");
  }

  // Helper methods for the test calls


  private void runTestCancelPollWithStatus(String auId, Credentials credentials, HttpStatus expectedStatus) {
    log.debug2("Testing cancel poll with auId: {}", auId);
    
    String template = getTestUrlTemplate("/polls/{auId}");
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build()
        .expand(Collections.singletonMap("auId", auId));
    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode().toUri();
    
    RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);
    HttpEntity<String> requestEntity = null;
    
    if (credentials != null) {
      HttpHeaders headers = new HttpHeaders();
      credentials.setUpBasicAuthentication(headers);
      requestEntity = new HttpEntity<>(null, headers);
    }
    
    ResponseEntity<Void> response = new TestRestTemplate(templateBuilder)
        .exchange(uri, HttpMethod.DELETE, requestEntity, Void.class);
    
    Assert.assertEquals(expectedStatus, response.getStatusCode());
  }

  private void runTestGetPollStatusWithExpectedResult(String auId, Credentials credentials, HttpStatus expectedStatus) {
    log.debug2("Testing get poll status with auId: {}", auId);
    
    String template = getTestUrlTemplate("/polls/{auId}");
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build()
        .expand(Collections.singletonMap("auId", auId));
    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode().toUri();
    
    RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);
    HttpEntity<String> requestEntity = null;
    
    if (credentials != null) {
      HttpHeaders headers = new HttpHeaders();
      credentials.setUpBasicAuthentication(headers);
      requestEntity = new HttpEntity<>(null, headers);
    }
    
    ResponseEntity<PollerSummary> response = new TestRestTemplate(templateBuilder)
        .exchange(uri, HttpMethod.GET, requestEntity, PollerSummary.class);
    
    Assert.assertEquals(expectedStatus, response.getStatusCode());
  }

  private void runTestGetPollerPollDetailsWithStatus(String pollKey, Credentials credentials, HttpStatus expectedStatus) {
    log.debug2("Testing get poller poll details with pollKey: {}", pollKey);
    
    String template = getTestUrlTemplate("/polls/poller/{pollKey}");
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build()
        .expand(Collections.singletonMap("pollKey", pollKey));
    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode().toUri();
    
    RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);
    HttpEntity<String> requestEntity = null;
    
    if (credentials != null) {
      HttpHeaders headers = new HttpHeaders();
      credentials.setUpBasicAuthentication(headers);
      requestEntity = new HttpEntity<>(null, headers);
    }
    
    ResponseEntity<PollerDetail> response = new TestRestTemplate(templateBuilder)
        .exchange(uri, HttpMethod.GET, requestEntity, PollerDetail.class);
    
    Assert.assertEquals(expectedStatus, response.getStatusCode());
  }

  private void runTestGetVoterPollDetailsWithStatus(String pollKey, Credentials credentials, HttpStatus expectedStatus) {
    log.debug2("Testing get voter poll details with pollKey: {}", pollKey);
    
    String template = getTestUrlTemplate("/polls/voter/{pollKey}");
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build()
        .expand(Collections.singletonMap("pollKey", pollKey));
    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode().toUri();
    
    RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);
    HttpEntity<String> requestEntity = null;
    
    if (credentials != null) {
      HttpHeaders headers = new HttpHeaders();
      credentials.setUpBasicAuthentication(headers);
      requestEntity = new HttpEntity<>(null, headers);
    }
    
    ResponseEntity<VoterDetail> response = new TestRestTemplate(templateBuilder)
        .exchange(uri, HttpMethod.GET, requestEntity, VoterDetail.class);
    
    Assert.assertEquals(expectedStatus, response.getStatusCode());
  }

  private void runTestGetPollsAsPollerWithStatus(Integer page, Integer size, Credentials credentials, HttpStatus expectedStatus) {
    log.debug2("Testing get polls as poller");
    
    String template = getTestUrlTemplate("/polls/poller?page={page}&size={size}");
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build()
        .expand(Map.of("page", page, "size", size));
    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode().toUri();
    
    RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);
    HttpEntity<String> requestEntity = null;
    
    if (credentials != null) {
      HttpHeaders headers = new HttpHeaders();
      credentials.setUpBasicAuthentication(headers);
      requestEntity = new HttpEntity<>(null, headers);
    }
    
    ResponseEntity<PollerPager> response = new TestRestTemplate(templateBuilder)
        .exchange(uri, HttpMethod.GET, requestEntity, PollerPager.class);
    
    Assert.assertEquals(expectedStatus, response.getStatusCode());
  }

  private void runTestGetPollsAsVoterWithStatus(Integer page, Integer size, Credentials credentials, HttpStatus expectedStatus) {
    log.debug2("Testing get polls as voter");
    
    String template = getTestUrlTemplate("/polls/voter?page={page}&size={size}");
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template).build()
        .expand(Map.of("page", page, "size", size));
    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents).build().encode().toUri();
    
    RestTemplateBuilder templateBuilder = RestUtil.getRestTemplateBuilder(0, 0);
    HttpEntity<String> requestEntity = null;
    
    if (credentials != null) {
      HttpHeaders headers = new HttpHeaders();
      credentials.setUpBasicAuthentication(headers);
      requestEntity = new HttpEntity<>(null, headers);
    }
    
    ResponseEntity<VoterPager> response = new TestRestTemplate(templateBuilder)
        .exchange(uri, HttpMethod.GET, requestEntity, VoterPager.class);
    
    Assert.assertEquals(expectedStatus, response.getStatusCode());
  }

  /**
   * Provides the URL template to be tested.
   *
   * @param pathAndQueryParams A String with the path and query parameters of the URL template to be
   * tested.
   * @return a String with the URL template to be tested.
   */
  String getTestUrlTemplate(final String pathAndQueryParams) {
    return "http://localhost:%d%s".formatted(port, pathAndQueryParams);
  }

  /**
   * Simple in‐memory mock of EntryManager for testing.
   */
  public class MockEntryManager {
    private final Map<String, Poll> polls = new ConcurrentHashMap<>();

    public void addPoll(Poll poll) {
      if (polls.containsKey(poll.getKey())) {
        throw new IllegalArgumentException("Poll already registered: " + poll.getKey());
      }
      polls.put(poll.getKey(), poll);
    }

    public Poll getCurrentPoll(String key) {
      return polls.get(key);
    }

    public boolean isPollRunning(String key) {
      return polls.containsKey(key);
    }

    public List<Poll> forAu(ArchivalUnit au) {
      return polls.values().stream()
          .filter(p -> p.getAu().equals(au))
          .collect(Collectors.toList());
    }

    public Poll forAuId(String auId) {
      return polls.values().stream()
          .filter(p -> p.getAu().getAuId().equals(auId))
          .findFirst()
          .orElse(null);
    }

    /** Utility to clear all polls between tests */
    public void clear() {
      polls.clear();
    }
  }
}