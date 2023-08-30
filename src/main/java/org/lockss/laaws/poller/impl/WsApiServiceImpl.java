package org.lockss.laaws.poller.impl;

import org.josql.Query;
import org.josql.QueryExecutionException;
import org.josql.QueryResults;
import org.lockss.hasher.HasherParams;
import org.lockss.hasher.HasherResult;
import org.lockss.hasher.SimpleHasher;
import org.lockss.importer.Importer;
import org.lockss.laaws.poller.api.WsApiDelegate;
import org.lockss.util.rest.repo.util.NamedInputStreamResource;
import org.lockss.log.L4JLogger;
import org.lockss.spring.base.BaseSpringApiServiceImpl;
import org.lockss.spring.error.LockssRestServiceException;
import org.lockss.util.StringUtil;
import org.lockss.util.io.FileUtil;
import org.lockss.util.josql.JosqlUtil;
import org.lockss.util.os.PlatformUtil;
import org.lockss.util.time.TimeBase;
import org.lockss.ws.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static org.lockss.ws.entities.HasherWsResult.BLOCK_FILE_TYPE;
import static org.lockss.ws.entities.HasherWsResult.RECORD_FILE_TYPE;

@Service
public class WsApiServiceImpl extends BaseSpringApiServiceImpl implements WsApiDelegate {
  private static L4JLogger log = L4JLogger.getLogger();

  // TODO: Move the following endpoint handler to some other service. It is here
  // only for expediency.

  private final HttpServletRequest request;

  @Autowired
  public WsApiServiceImpl(HttpServletRequest request) {
    this.request = request;
  }

  /**
   * PUT /aus/import: Import a file as an artifact in an Archival Unit.
   *
   * @param targetBaseUrlPath A String with the base URL path of the target
   *                          Archival Unit.
   * @param targetUrl         A String with the target Archival Unit URL.
   * @param file              A MultipartFile with the content of the file to be
   *                          imported.
   * @param userProperties    A {@code List<String>} with the user-specified
   *                          properties.
   * @return a {@code ResponseEntity<Void>}.
   */
  @Override
  public ResponseEntity<Void> putImportFile(String targetBaseUrlPath,
                                            String targetUrl, MultipartFile file, List<String> userProperties) {
    String parsedRequest = String.format("targetBaseUrlPath: %s, "
            + "targetUrl: %s, content.getName(): %s, content.getSize(): %s, "
            + "userProperties: %s, requestUrl: %s", targetBaseUrlPath, targetUrl,
        file.getName(), file.getSize(), userProperties,
        getFullRequestUrl(request));
    log.debug2("Parsed request: {}", parsedRequest);

    try {
      new Importer().importFile(file.getInputStream(), targetBaseUrlPath,
          targetUrl, userProperties);
      return new ResponseEntity<Void>(null, null, HttpStatus.OK);
    } catch (IllegalArgumentException | IllegalStateException
             | NoSuchAlgorithmException e) {
      String errorMessage = "Exception caught trying to import file";
      log.warn(errorMessage, e);
      log.warn("Parsed request: {}", parsedRequest);

      throw new LockssRestServiceException(HttpStatus.BAD_REQUEST, errorMessage,
          e, parsedRequest);
    } catch (Exception e) {
      String errorMessage = "Unexpected exception caught trying to import file";
      log.warn(errorMessage, e);
      log.warn("Parsed request: {}", parsedRequest);

      throw new LockssRestServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
          errorMessage, e, parsedRequest);
    }
  }

  /**
   * Provides the full URL of the request.
   *
   * @param request An HttpServletRequest with the HTTP request.
   * @return a String with the full URL of the request.
   */
  private String getFullRequestUrl(HttpServletRequest request) {
    if (request.getQueryString() == null
        || request.getQueryString().trim().isEmpty()) {
      return "'" + request.getMethod() + " " + request.getRequestURL() + "'";
    }

    return "'" + request.getMethod() + " " + request.getRequestURL() + "?"
        + request.getQueryString() + "'";
  }

  /**
   * GET /aurepositories?query={repositoryQuery}: Provides the selected properties
   * of selected repositories in the system.
   *
   * @param repositoryQuery A String with the
   *                        <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                        query</a> used to specify what properties to
   *                        retrieve from which repository.
   * @return a {@code List<RepositoryWsResult>} with the results.
   */
  @Override
  public ResponseEntity getRepositories(String repositoryQuery) {
    log.debug2("repositoryQuery = {}", repositoryQuery);

    RepositoryHelper repositoryHelper = new RepositoryHelper();
    List<RepositoryWsResult> results = null;

    try {
      // Create the full query.
      String fullQuery = JosqlUtil.createFullQuery(repositoryQuery,
          RepositoryHelper.SOURCE_FQCN, RepositoryHelper.PROPERTY_NAMES,
          RepositoryHelper.RESULT_FQCN);
      log.trace("fullQuery = {}", fullQuery);

      // Create a new JoSQL query.
      Query q = new Query();

      try {
        // Parse the SQL-like query.
        q.parse(fullQuery);

        // Execute the query.
        QueryResults qr = q.execute(repositoryHelper.createUniverse());

        // Get the query results.
        results = (List<RepositoryWsResult>) qr.getResults();
        log.trace("results.size() = {}", results.size());
        log.trace("results = {}", repositoryHelper.nonDefaultToString(results));
        return new ResponseEntity<>(results, HttpStatus.OK);
      } catch (QueryExecutionException qee) {
        String message = "Cannot getRepositories() for repositoryQuery = '"
            + repositoryQuery + "'";
        log.error(message, qee);
        return new ResponseEntity<String>(message,
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      String message = "Cannot getRepositories() for repositoryQuery = '"
          + repositoryQuery + "'";
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private static final Map<String, SimpleHasher.ParamsAndResult> HASH_REQUESTS =
      new LinkedHashMap<String, SimpleHasher.ParamsAndResult>();

  // Dummy request identifier used for synchronous hash operations to use code
  // that is common to asynchronous hash operations.
  private static final String DEFAULT_REQUEST_ID = "noRequestId";

  /**
   * Removes from the system an asynchronous hashing operation, terminating it
   * if it's still running.
   *
   * @param requestId A String with the identifier of the requested asynchronous
   *                  hashing operation.
   * @return a {@code ResponseEntity<String>} with the result of the removal of
   * the hashing operation.
   */
  @Override
  public ResponseEntity<String> deleteHash(String requestId) {
    log.debug2("requestId = {}", requestId);
    String message = null;

    try {
      // Handle a missing request identifier.
      if (StringUtil.isNullString(requestId)) {
        message = "Must supply request identifier";
        log.warn(message);
        return new ResponseEntity<String>(message, HttpStatus.BAD_REQUEST);
      }

      // Get the hash request data.
      SimpleHasher.ParamsAndResult paramsAndResult;
      synchronized (HASH_REQUESTS) {
        paramsAndResult = HASH_REQUESTS.get(requestId);
      }

      // Handle a missing request.
      if (paramsAndResult == null || paramsAndResult.result == null) {
        message = "Cannot find asynchronous hash request '" + requestId + "'";
        log.warn(message);
        return new ResponseEntity<String>(message, HttpStatus.NOT_FOUND);
      }

      // Get the result.
      HasherResult result = paramsAndResult.result;
      log.trace("result = {}", result);

      switch (result.getRunnerStatus()) {
        case NotStarted:
        case Init:
        case Starting:
        case Running:
          Future<Void> future = result.getFuture();

          if (future != null) {
            future.cancel(true);
          }

          break;
        default:
      }

      FileUtil.safeDeleteFile(result.getBlockFile());
      FileUtil.safeDeleteFile(result.getRecordFile());

      synchronized (HASH_REQUESTS) {
        HASH_REQUESTS.remove(requestId);
      }

      message = SimpleHasher.HasherStatus.Done.toString();
      log.debug2("message = {}", message);
      return new ResponseEntity<String>(message, HttpStatus.OK);
    } catch (Exception e) {
      message = "Cannot deleteHash() for requestId = '" + requestId + "'";
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  /**
   * Provides the results of all the asynchronous hashing operations.
   *
   * @return a {@code ResponseEntity<MultiValueMap<String, Object>>} with the
   *         results of all the asynchronous hashing operations.
   */
  public ResponseEntity getAllHashes() {
    log.debug2("Invoked.");

    try {
      // Initialize the response.
      MultiValueMap<String, HttpEntity<?>> parts = new LinkedMultiValueMap<>();

      // Loop through all the existing requests.
      synchronized (HASH_REQUESTS) {
        for (String requestId : HASH_REQUESTS.keySet()) {
          // Get the result.
          HasherResult result = HASH_REQUESTS.get(requestId).result;
          log.trace("result = {}", result);

          // Add to the response the parts for this result.
          populateResultParts(requestId, result, parts);
        }
      }

      log.trace("parts.size() = {}", parts.size());

      // Build and return the response.
      return buildResponse(parts);
    } catch (Exception e) {
      String message = "Cannot getAllHashes()";
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Provides the result of an asynchronous hashing operation.
   *
   * @param requestId A String with the identifier of the requested asynchronous
   *                  hashing operation.
   * @return a {@code ResponseEntity<MultiValueMap<String, Object>>} with the
   * result of the hashing operation.
   */
  @Override
  public ResponseEntity getHash(String requestId) {
    log.debug2("requestId = {}", requestId);
    String message = null;

    try {
      // Handle a missing request identifier.
      if (StringUtil.isNullString(requestId)) {
        message = "Must supply request identifier";
        log.warn(message);
        return new ResponseEntity<String>(message, HttpStatus.BAD_REQUEST);
      }

      // Get the hash request data.
      SimpleHasher.ParamsAndResult paramsAndResult;
      synchronized (HASH_REQUESTS) {
        paramsAndResult = HASH_REQUESTS.get(requestId);
      }

      // Handle a missing request.
      if (paramsAndResult == null || paramsAndResult.result == null) {
        message = "Cannot find asynchronous hash request '" + requestId + "'";
        log.warn(message);
        return new ResponseEntity<String>(message, HttpStatus.NOT_FOUND);
      }

      // Get the result.
      HasherResult result = paramsAndResult.result;
      log.trace("result = {}", result);

      // Initialize the response.
      MultiValueMap<String, HttpEntity<?>> parts = new LinkedMultiValueMap<>();

      // Populate the response with the parts for this result.
      populateResultParts(requestId, result, parts);

      log.trace("parts.size() = {}", parts.size());

      // Build and return the response.
      return buildResponse(parts);
    } catch (Exception e) {
      message = "Cannot getHash() for requestId = '" + requestId + "'";
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  /**
   * Performs the hashing of an AU or a URL.
   *
   * @param hasherWsParams A HasherWsParams with the parameters of the hashing
   *                       operation.
   * @param isAsynchronous A Boolean with the indication of whether the hashing
   *                       parameters is to be performed asynchronously.
   * @return a {@code ResponseEntity<MultiValueMap<String, Object>>} with the
   *         result of the hashing operation.
   */
  public ResponseEntity putHash(HasherWsParams hasherWsParams,
                                Boolean isAsynchronous) {
    log.debug2("hasherWsParams = {}", hasherWsParams);
    log.debug2("isAsynchronous = {}", isAsynchronous);

    try {
      // Prepare the hash parameters.
      HasherParams params =
          new HasherParams(PlatformUtil.getLocalHostname(), isAsynchronous);

      params.setAlgorithm(hasherWsParams.getAlgorithm());
      params.setAuId(hasherWsParams.getAuId());
      params.setChallenge(hasherWsParams.getChallenge());

      Boolean excludeSuspectVersions =
          hasherWsParams.isExcludeSuspectVersions();

      if (excludeSuspectVersions == null) {
        params.setExcludeSuspectVersions(false);
      } else {
        params.setExcludeSuspectVersions(excludeSuspectVersions.booleanValue());
      }

      Boolean includeWeight = hasherWsParams.isIncludeWeight();
      if (includeWeight == null) {
        params.setIncludeWeight(false);
      } else {
        params.setIncludeWeight(includeWeight.booleanValue());
      }

      params.setHashType(hasherWsParams.getHashType());
      params.setLower(hasherWsParams.getLower());

      Boolean recordFilteredStream = hasherWsParams.isRecordFilteredStream();
      if (recordFilteredStream == null) {
        params.setRecordFilteredStream(false);
      } else {
        params.setRecordFilteredStream(recordFilteredStream.booleanValue());
      }

      params.setResultEncoding(hasherWsParams.getResultEncoding());
      params.setUpper(hasherWsParams.getUpper());
      params.setUrl(hasherWsParams.getUrl());
      params.setVerifier(hasherWsParams.getVerifier());
      log.trace("params = {}", params);

      HasherResult result = new HasherResult();
      String requestId = DEFAULT_REQUEST_ID;

      // Check whether the hashing operation needs to be done synchronously.
      if (!isAsynchronous) {
        // Yes: Perform the hash synchronously.
        new SimpleHasher(null).hash(params, result);
      } else {
        try {
          // No: Initialize the request time.
          long requestTime = TimeBase.nowMs();
          result.setRequestTime(requestTime);

          // Obtain a request identifier.
          synchronized (HASH_REQUESTS) {
            requestId = SimpleHasher.getReqId(params, result, HASH_REQUESTS);
            log.trace("requestId = {}", requestId);
          }

          // Perform the hash asynchronously.
          new SimpleHasher(null).startHashingThread(params, result);
        } catch (RuntimeException re) {
          String errorMessage =
              "Error starting asynchronous hash thread: " + re.toString();
          log.warn(errorMessage);
          log.warn(re);
          return new ResponseEntity<String>(errorMessage,
              HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }

      log.trace("result = {}", result);

      // Initialize the response.
      MultiValueMap<String, HttpEntity<?>> parts = new LinkedMultiValueMap<>();

      // Populate the response with the parts for this result.
      populateResultParts(requestId, result, parts);

      log.trace("parts.size() = {}", parts.size());

      // Build and return the response.
      return buildResponse(parts);
    } catch (Exception e) {
      String message = "Cannot putHash() for hasherWsParams = '"
          + hasherWsParams + "', isAsynchronous = " + isAsynchronous;
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Populates a map of parts with the results of a hashing operation.
   *
   * @param requestId A String with the identifier of the hashing operation.
   * @param result    A HasherResult with the results of the hashing operation.
   * @param parts     A {@code MultiValueMap<String, HttpEntity<?>>} where to
   *                  add the hashing operation result parts.
   * @throws IOException if there are problems getting the contents of any of
   *                     the parts.
   */
  private void populateResultParts(String requestId, HasherResult result,
                                   MultiValueMap<String, HttpEntity<?>> parts) throws IOException {
    log.debug2("requestId = {}", requestId);
    log.debug2("result = {}", result);
    log.debug2("parts = {}", parts);

    addResultPropertiesPart(requestId, result, parts);

    File recordFile = result.getRecordFile();
    if (recordFile != null && recordFile.exists()
        && recordFile.length() > 0) {
      addFilePart(requestId + "-" + RECORD_FILE_TYPE, recordFile, parts);
    }

    File blockFile = result.getBlockFile();
    if (blockFile != null && blockFile.exists()
        && blockFile.length() > 0) {
      addFilePart(requestId + "-" + BLOCK_FILE_TYPE, blockFile, parts);
    }

    log.debug2("Done");
  }

  /**
   * Adds to the parts map a part with the properties of a hashing operation.
   *
   * @param partName A String with the name of the part to be added.
   * @param result   A HasherResult with the results of the hashing operation.
   * @param parts    A {@code MultiValueMap<String, HttpEntity<?>>} where to add
   *                 the hashing operation result parts.
   */
  private void addResultPropertiesPart(String partName, HasherResult result,
                                       MultiValueMap<String, HttpEntity<?>> parts) {
    log.debug2("partName = {}", partName);
    log.debug2("result = {}", result);
    log.debug2("parts = {}", parts);

    Map<String, Object> resultProperties = new HashMap<>();

    resultProperties.put("requestId", result.getRequestId());
    resultProperties.put("startTime", result.getStartTime());

    File recordFile = result.getRecordFile();
    if (recordFile != null && recordFile.exists() && recordFile.length() > 0) {
      resultProperties.put("recordFileName", recordFile.getName());
    }

    File blockFile = result.getBlockFile();
    if (blockFile != null && blockFile.exists() && blockFile.length() > 0) {
      resultProperties.put("blockFileName", blockFile.getName());
    }

    resultProperties.put("errorMessage", result.getRunnerError());
    resultProperties.put("status", result.getRunnerStatus().toString());

    if (result.getHashResult() != null) {
      resultProperties.put("hashResult", result.getHashResult());
    }

    resultProperties.put("bytesHashed", result.getBytesHashed());
    resultProperties.put("filesHashed", result.getFilesHashed());
    resultProperties.put("elapsedTime", result.getElapsedTime());

    // Add the part to the map.
    parts.add(partName, new HttpEntity<>(resultProperties, new HttpHeaders()));
  }

  /**
   * Adds to the parts map a part with a hashing operation result file.
   *
   * @param partName   A String with the name of the part to be added.
   * @param sourceFile A File with the result file used as the part contents
   *                   source.
   * @param parts      A {@code MultiValueMap<String, HttpEntity<?>>} where to
   *                   add the hashing operation result parts.
   * @throws IOException if there are problems getting the contents of the file.
   */
  void addFilePart(String partName, File sourceFile,
                   MultiValueMap<String, HttpEntity<?>> parts) throws IOException {
    log.debug2("partName = {}", partName);
    log.debug2("sourceFile = {}", sourceFile);

    FileDataSource fileDS = new FileDataSource(sourceFile);

    String name = fileDS.getName();
    log.trace("name = {}", name);

    long size = fileDS.getFile().length();
    log.trace("size = {}", size);

    // Save the version unique identifier header in the part of the response.
    HttpHeaders partHeaders = new HttpHeaders();

    // This must be set or else AbstractResource#contentLength will read the
    // entire InputStream to determine the content length, which will exhaust
    // the InputStream.
    partHeaders.setContentLength(size);

    log.trace("partHeaders = {}", () -> partHeaders);

    Resource resource =
        new NamedInputStreamResource(name, fileDS.getInputStream());

    // Add the part to the map.
    parts.add(partName, new HttpEntity<>(resource, partHeaders));
  }

  /**
   * Build the response with a map of parts.
   *
   * @param parts A {@code MultiValueMap<String, Object>} with the parts to be
   *              included in the response.
   * @return a {@code ResponseEntity<MultiValueMap<String, HttpEntity<?>>>} with
   * the result of one or more hashing operations.
   */
  private ResponseEntity<MultiValueMap<String, HttpEntity<?>>> buildResponse(
      MultiValueMap<String, HttpEntity<?>> parts) {
    log.debug2("parts = {}", parts);

    // Specify the response content type.
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    log.trace("responseHeaders = {}", () -> responseHeaders);

    // Specify the success.
    HttpStatus status = HttpStatus.OK;
    log.trace("status = {}", () -> status);

    return new ResponseEntity<MultiValueMap<String, HttpEntity<?>>>(parts,
        responseHeaders, status);
  }

  /**
   * Provides the selected properties of selected peers.
   *
   * @param peerQuery A String with the
   *                  <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                  query</a> used to specify what properties to retrieve
   *                  from which peers.
   * @return a {@code ResponseEntity<List<PeerWsResult>>} with the results.
   */
  @Override
  public ResponseEntity getPeers(String peerQuery) {
    log.debug2("peerQuery = {}", peerQuery);

    PeerHelper peerHelper = new PeerHelper();
    List<PeerWsResult> results = null;

    try {
      // Create the full query.
      String fullQuery = JosqlUtil.createFullQuery(peerQuery,
          PeerHelper.SOURCE_FQCN, PeerHelper.PROPERTY_NAMES,
          PeerHelper.RESULT_FQCN);
      log.trace("fullQuery = {}", fullQuery);

      // Create a new JoSQL query.
      Query q = new Query();

      try {
        // Parse the SQL-like query.
        q.parse(fullQuery);

        // Execute the query.
        QueryResults qr = q.execute(peerHelper.createUniverse());

        // Get the query results.
        results = (List<PeerWsResult>) qr.getResults();
        log.trace("results.size() = {}" + results.size());
        log.trace("results = {}", peerHelper.nonDefaultToString(results));
        return new ResponseEntity<List<PeerWsResult>>(results,
            HttpStatus.OK);
      } catch (QueryExecutionException qee) {
        String message =
            "Cannot getPeers() for peerQuery = '" + peerQuery + "'";
        log.error(message, qee);
        return new ResponseEntity<String>(message,
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      String message = "Cannot getPeers() for peerQuery = '" + peerQuery + "'";
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Provides the selected properties of selected polls.
   *
   * @param pollQuery A String with the
   *                  <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                  query</a> used to specify what properties to retrieve
   *                  from which polls.
   * @return a {@code ResponseEntity<List<PollWsResult>>} with the results.
   */
  @Override
  public ResponseEntity getPolls(String pollQuery) {
    log.debug("pollQuery = {}", pollQuery);

    PollHelper pollHelper = new PollHelper();
    List<PollWsResult> results = null;

    try {
      // Create the full query.
      String fullQuery = JosqlUtil.createFullQuery(pollQuery,
          PollHelper.SOURCE_FQCN, PollHelper.PROPERTY_NAMES,
          PollHelper.RESULT_FQCN);
      log.trace("fullQuery = {}", fullQuery);

      // Create a new JoSQL query.
      Query q = new Query();

      try {
        // Parse the SQL-like query.
        q.parse(fullQuery);

        // Execute the query.
        QueryResults qr = q.execute(pollHelper.createUniverse());

        // Get the query results.
        results = (List<PollWsResult>) qr.getResults();
        log.trace("results.size() = {}" + results.size());
        log.trace("results = {}", pollHelper.nonDefaultToString(results));
        return new ResponseEntity<List<PollWsResult>>(results,
            HttpStatus.OK);
      } catch (QueryExecutionException qee) {
        String message =
            "Cannot getPolls() for pollQuery = '" + pollQuery + "'";
        log.error(message, qee);
        return new ResponseEntity<String>(message,
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      String message = "Cannot getPolls() for pollQuery = '" + pollQuery + "'";
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * GET /repositoryspaces?query={repositoryQuery}: Provides the selected
   * properties of selected repository spaces in the system.
   *
   * @param repositorySpaceQuery A String with the
   *                             <a href= "package-summary.html#SQL-Like_Query">SQL-like
   *                             query</a> used to specify what properties to
   *                             retrieve from which repository space.
   * @return a {@code List<RepositorySpaceWsResult>} with the results.
   */
  @Override
  public ResponseEntity getRepositorySpaces(String repositorySpaceQuery) {
    log.debug2("repositorySpaceQuery = {}", repositorySpaceQuery);

    RepositorySpaceHelper repositorySpaceHelper = new RepositorySpaceHelper();
    List<RepositorySpaceWsResult> results = null;

    try {
      // Create the full query.
      String fullQuery = JosqlUtil.createFullQuery(repositorySpaceQuery,
          RepositorySpaceHelper.SOURCE_FQCN, RepositorySpaceHelper.PROPERTY_NAMES,
          RepositorySpaceHelper.RESULT_FQCN);
      log.trace("fullQuery = {}", fullQuery);

      // Create a new JoSQL query.
      Query q = new Query();

      try {
        // Parse the SQL-like query.
        q.parse(fullQuery);

        // Execute the query.
        QueryResults qr = q.execute(repositorySpaceHelper.createUniverse());

        // Get the query results.
        results = (List<RepositorySpaceWsResult>) qr.getResults();
        log.trace("results.size() = {}", results.size());
        log.trace("results = {}",
            repositorySpaceHelper.nonDefaultToString(results));
        return new ResponseEntity<>(results, HttpStatus.OK);
      } catch (QueryExecutionException qee) {
        String message = "Cannot getRepositorySpaces() for "
            + "repositorySpaceQuery = '" + repositorySpaceQuery + "'";
        log.error(message, qee);
        return new ResponseEntity<String>(message,
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      String message = "Cannot getRepositorySpaces() for "
          + "repositorySpaceQuery = '" + repositorySpaceQuery + "'";
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Provides the selected properties of selected votes.
   *
   * @param voteQuery A String with the
   *                  <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                  query</a> used to specify what properties to retrieve
   *                  from which votes.
   * @return a {@code ResponseEntity<List<VoteWsResult>>} with the results.
   */
  @Override
  public ResponseEntity getVotes(String voteQuery) {
    log.debug2("voteQuery = {}", voteQuery);

    VoteHelper voteHelper = new VoteHelper();
    List<VoteWsResult> results = null;

    try {
      // Create the full query.
      String fullQuery = JosqlUtil.createFullQuery(voteQuery,
          VoteHelper.SOURCE_FQCN, VoteHelper.PROPERTY_NAMES,
          VoteHelper.RESULT_FQCN);
      log.trace("fullQuery = {}", fullQuery);

      // Create a new JoSQL query.
      Query q = new Query();

      try {
        // Parse the SQL-like query.
        q.parse(fullQuery);

        // Execute the query.
        QueryResults qr = q.execute(voteHelper.createUniverse());

        // Get the query results.
        results = (List<VoteWsResult>) qr.getResults();
        log.trace("results.size() = {}" + results.size());
        log.trace("results = {}", voteHelper.nonDefaultToString(results));
        return new ResponseEntity<List<VoteWsResult>>(results,
            HttpStatus.OK);
      } catch (QueryExecutionException qee) {
        String message =
            "Cannot getVotes() for voteQuery = '" + voteQuery + "'";
        log.error(message, qee);
        return new ResponseEntity<String>(message,
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      String message = "Cannot getVotes() for voteQuery = '" + voteQuery + "'";
      log.error(message, e);
      return new ResponseEntity<String>(message,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
