/*
 * Copyright (c) 2020, Board of Trustees of Leland Stanford Jr. University,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.lockss.laaws.poller.impl;

import static org.lockss.config.RestConfigClient.CONFIG_PART_NAME;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.activation.FileDataSource;
import org.lockss.app.LockssDaemon;
import org.lockss.exporter.Exporter;
import org.lockss.exporter.Exporter.FilenameTranslation;
import org.lockss.exporter.Exporter.Type;
import org.lockss.importer.Importer;
import org.lockss.laaws.error.LockssRestServiceException;
import org.lockss.laaws.poller.api.AusApiDelegate;
import org.lockss.laaws.rs.util.NamedInputStreamResource;
import org.lockss.log.L4JLogger;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.spring.base.*;
import org.lockss.util.io.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for accessing the repository artifacts.
 */
@Service
public class AusApiServiceImpl extends BaseSpringApiServiceImpl
implements AusApiDelegate {
  private static L4JLogger log = L4JLogger.getLogger();

  // Constants for the configuration of the export directory.
  private static final String EXPORT_DIR_KEY = "repo.export.directory";
  private static final String EXPORT_DIR_UNSET_VALUE = "export";

  private final ObjectMapper objectMapper;
  private final HttpServletRequest request;

  // The export directory.
  @Value("${" + EXPORT_DIR_KEY + ":"
      + EXPORT_DIR_UNSET_VALUE + "}")
  private String configExportDir;

  /**
   * Constructor for autowiring.
   * 
   * @param objectMapper
   *          An ObjectMapper for JSON processing.
   * @param request
   *          An HttpServletRequest with the HTTP request.
   */
  @org.springframework.beans.factory.annotation.Autowired
  public AusApiServiceImpl(ObjectMapper objectMapper,
      HttpServletRequest request) {
    this.objectMapper = objectMapper;
    this.request = request;
  }

  // TODO: Move the following endpoint handler to some other service. It is here
  // only for expediency.
  /**
   * GET /aus/{auid}/export: Export the Archival Unit artifacts as a group of
   * archives.
   *
   * @param auid              A String with the Archival Unit ID (AUID).
   * @param fileType          A String with the type of archive to create.
   * @param isCompress        A Boolean with the indication of whether contents
   *                          should be compressed.
   * @param isExcludeDirNodes A Boolean with the indication of whether
   *                          directories should be excluded.
   * @param xlateFilenames    A String with the type of filename translation to
   *                          be done.
   * @param filePrefix        A String with the prefix to be used to name the
   *                          exported file.
   * @param maxSize           A Long with the maximum size in MB of the exported
   *                          file.
   * @param maxVersions       An Integer with the maximum number of versions of
   *                          an artifact to be exported.
   * @return a {@code ResponseEntity<MultiValueMap<String, Object>>} with the
   *         archives containing the Archival Unit artifacts.
   */
  @Override
  public ResponseEntity getExportFiles(String auid, String fileType,
      Boolean isCompress, Boolean isExcludeDirNodes, String xlateFilenames,
      String filePrefix, Long maxSize, Integer maxVersions) {
    String parsedRequest = String.format("auid: %s, fileType: %s, "
      + "isCompress: %s, isExcludeDirNodes: %s, xlateFilenames: %s, "
      + "filePrefix: %s, maxSize: %s, maxVersions: %s, requestUrl: %s",
      auid, fileType, isCompress, isExcludeDirNodes, xlateFilenames, filePrefix,
      maxSize, maxVersions, getFullRequestUrl(request));
    log.debug2("Parsed request: {}", parsedRequest);

    try {
      LockssDaemon daemon = LockssDaemon.getLockssDaemon();

      // Get the Archival Unit to have its metadata indexing disabled.
      ArchivalUnit au = daemon.getPluginManager().getAuFromId(auid);
      log.trace("au = {}", au);

      // Handle a missing Archival Unit.
      if (au == null) {
	String errorMessage = "The archival unit does not exist";
	log.warn(errorMessage);
	log.warn("Parsed request: {}", parsedRequest);
	throw new LockssRestServiceException(HttpStatus.BAD_REQUEST,
	    errorMessage, parsedRequest);
      }

      // Initialize the appropriate type of exporter.
      Exporter exp = null;

      switch (fileType) {
        case "WARC_RESPONSE":
          exp = Type.WARC_RESPONSE.makeExporter(daemon, au);
          break;
        case "ARC_RESPONSE":
          exp = Type.ARC_RESPONSE.makeExporter(daemon, au);
          break;
        case "WARC_RESOURCE":
          exp = Type.WARC_RESOURCE.makeExporter(daemon, au);
          break;
        case "ARC_RESOURCE":
          exp = Type.ARC_RESOURCE.makeExporter(daemon, au);
          break;
        case "ZIP":
          exp = Type.ZIP.makeExporter(daemon, au);
          break;
        default:
          String errorMessage = "Invalid fileType '" + fileType
              + "': It must be one of 'WARC_RESPONSE', 'ARC_RESPONSE', "
              + "'WARC_RESOURCE', 'ARC_RESOURCE' or 'ZIP'";
  	log.warn(errorMessage);
  	log.warn("Parsed request: {}", parsedRequest);
  	throw new LockssRestServiceException(HttpStatus.BAD_REQUEST,
  	    errorMessage, parsedRequest);
      }

      // Validate and specify the directory where to create the exported files.
      File exportdir = new File(configExportDir);
      log.trace("exportdir = {}", exportdir);

      if (!exportdir.exists()) {
        if (!FileUtil.ensureDirExists(exportdir)) {
  	throw new IOException("Could not create export directory "
  	    + exportdir);
        }
      }

      exp.setDir(exportdir);

      // Specify whether the exported files should be compressed.
      exp.setCompress(isCompress);

      // Specify whether the directory nodes should not be exported.
      exp.setExcludeDirNodes(isExcludeDirNodes);

      // Specify any filename translation.
      switch (xlateFilenames) {
	case "XLATE_MAC":
	  exp.setFilenameTranslation(FilenameTranslation.XLATE_MAC);
          break;
	case "XLATE_WINDOWS":
	  exp.setFilenameTranslation(FilenameTranslation.XLATE_WINDOWS);
          break;
	case "XLATE_NONE":
	  exp.setFilenameTranslation(FilenameTranslation.XLATE_NONE);
          break;
        default:
          String errorMessage = "Invalid xlateFilenames '" + xlateFilenames
              + "': It must be one of 'XLATE_MAC', 'XLATE_WINDOWS' or "
              + "'XLATE_NONE'";
  	log.warn(errorMessage);
  	log.warn("Parsed request: {}", parsedRequest);
  	throw new LockssRestServiceException(HttpStatus.BAD_REQUEST,
  	    errorMessage, parsedRequest);
      }

      // Specify the export filenames prefix.
      exp.setPrefix(filePrefix);

      // Specify any limit on the size of the exported files.
      if (maxSize > 0) {
        exp.setMaxSize((long)(maxSize * 1024 * 1024));
      }

      // Specify any limit on the number of artifact versions to be exported.
      if (maxVersions > 0) {
        exp.setMaxVersions(maxVersions);
      }

      // Export the files.
      exp.export();

      // Process the created export files.
      List<File> exportFiles = exp.getExportFiles();
      log.trace("exportFiles = {}", exportFiles);

      int exportFilesCount = exportFiles.size();
      log.trace("exportFilesCount = {}", exportFilesCount);

      // Build the response entity.
      MultiValueMap<String, Object> parts =
  	new LinkedMultiValueMap<String, Object>();

      for (int i = 0; i < exportFilesCount; i++) {
	log.trace("Processing export file {} of {}...", i + 1,
	    exportFilesCount);

        FileDataSource fileDS = new FileDataSource(exportFiles.get(i));

        String name = fileDS.getName();
        log.trace("name = {}", name);

        long size = fileDS.getFile().length();
        log.trace("size = {}", size);

        // Save the version unique identifier header in the part of the response.
        HttpHeaders partHeaders = new HttpHeaders();

        // This must be set or else AbstractResource#contentLength will read the
        // entire InputStream to determine the content length, which will
        // exhaust the InputStream.
        partHeaders.setContentLength(size);

        log.trace("partHeaders = {}", () -> partHeaders);

        Resource resource =
            new NamedInputStreamResource(name, fileDS.getInputStream());

        parts.add(CONFIG_PART_NAME, new HttpEntity<>(resource, partHeaders));
        log.trace("parts.size() = {}", parts.size());
      }

      log.debug2("Done exporting {} files", exportFilesCount);

      // Specify the response content type.
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
      log.trace("responseHeaders = {}", () -> responseHeaders);

      HttpStatus status = HttpStatus.OK;
      log.trace("status = {}", () -> status);

      return new ResponseEntity<MultiValueMap<String, Object>>(parts,
  	  responseHeaders, status);
    } catch (LockssRestServiceException lre) {
      // Let it cascade to the controller advice exception handler.
      throw lre;
    } catch (Exception e) {
      String errorMessage =
	  "Unexpected exception caught while attempting to export files";

      log.warn(errorMessage, e);
      log.warn("Parsed request: {}", parsedRequest);

      throw new LockssRestServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
	  errorMessage, e, parsedRequest);
    }
  }

  // TODO: Move the following endpoint handler to some other service. It is here
  // only for expediency.
  /**
   * PUT /aus/import: Import a file as an artifact in an Archival Unit.
   *
   * @param targetBaseUrlPath A String with the base URL path of the target
   *                          Archival Unit.
   * @param targetUrl         A String with the target Archival Unit URL.
   * @param file           A MultipartFile with the content of the file to be
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

  @Override
  public Optional<ObjectMapper> getObjectMapper() {
    return Optional.ofNullable(objectMapper);
  }

  @Override
  public Optional<HttpServletRequest> getRequest() {
    return Optional.ofNullable(request);
  }

  /**
   * Provides the full URL of the request.
   * 
   * @param request
   *          An HttpServletRequest with the HTTP request.
   * 
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
}
