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

import static org.lockss.ws.entities.HasherWsResult.BLOCK_FILE_TYPE;
import static org.lockss.ws.entities.HasherWsResult.RECORD_FILE_TYPE;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import javax.activation.FileDataSource;
import org.lockss.hasher.HasherParams;
import org.lockss.hasher.HasherResult;
import org.lockss.hasher.SimpleHasher;
import org.lockss.hasher.SimpleHasher.HasherStatus;
import org.lockss.hasher.SimpleHasher.ParamsAndResult;
import org.lockss.laaws.poller.api.HashesApiDelegate;
import org.lockss.laaws.rs.util.NamedInputStreamResource;
import org.lockss.log.L4JLogger;
import org.lockss.spring.base.BaseSpringApiServiceImpl;
import org.lockss.util.io.FileUtil;
import org.lockss.util.StringUtil;
import org.lockss.util.os.PlatformUtil;
import org.lockss.util.time.TimeBase;
import org.lockss.ws.entities.HasherWsParams;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Service for hashing operations.
 */
@Service
public class HashesApiServiceImpl extends BaseSpringApiServiceImpl
implements HashesApiDelegate {
  private static L4JLogger log = L4JLogger.getLogger();

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
   *         the hashing operation.
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
      ParamsAndResult paramsAndResult;
      synchronized (HASH_REQUESTS) {
        paramsAndResult = HASH_REQUESTS.get(requestId);
      }

      // Handle a missing request.
      if (paramsAndResult == null || paramsAndResult.result == null) {
        message ="Cannot find asynchronous h ash request '" + requestId + "'";
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

      message = HasherStatus.Done.toString();
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
   *         result of the hashing operation.
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
      ParamsAndResult paramsAndResult;
      synchronized (HASH_REQUESTS) {
        paramsAndResult = HASH_REQUESTS.get(requestId);
      }

      // Handle a missing request.
      if (paramsAndResult == null || paramsAndResult.result == null) {
        message ="Cannot find asynchronous hash request '" + requestId + "'";
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
      MultiValueMap<String, HttpEntity<?>>  parts) {
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
   *         the result of one or more hashing operations.
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
}
