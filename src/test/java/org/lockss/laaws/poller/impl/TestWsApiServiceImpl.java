package org.lockss.laaws.poller.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;
import static org.mockito.MockitoAnnotations.*;

import java.io.*;
import java.util.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.lockss.importer.*;
import org.lockss.spring.auth.*;
import org.lockss.spring.error.*;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.mock.web.*;

public class TestWsApiServiceImpl {

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private Importer mockImporter;

  @InjectMocks
  private WsApiServiceImpl wsApiService;

  public TestWsApiServiceImpl() {
    openMocks(this);
  }

  @Test
  public void testPutImportFileSuccess() throws Exception {
    MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain",
        new byte[10]);
    String targetBaseUrlPath = "target/baseUrl";
    String targetUrl = "target/url";
    List<String> userProperties = new ArrayList<>();

    mockStatic(AuthUtil.class);
    doNothing().when(mockImporter)
        .importFile(any(), eq(targetBaseUrlPath), eq(targetUrl), eq(userProperties));

    wsApiService = spy(new WsApiServiceImpl(mockRequest));
    doReturn(true).when(wsApiService).waitReady();

    ResponseEntity<Void> response = wsApiService.putImportFile(targetBaseUrlPath, targetUrl,
        mockFile, userProperties);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(mockImporter).importFile(any(), eq(targetBaseUrlPath), eq(targetUrl),
        eq(userProperties));
    AuthUtil.checkHasRole("AU_ADMIN");
  }

  @Test
  public void testPutImportFileServiceUnavailable() {
    MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain",
        new byte[10]);
    String targetBaseUrlPath = "target/baseUrl";
    String targetUrl = "target/url";
    List<String> userProperties = new ArrayList<>();

    wsApiService = spy(new WsApiServiceImpl(mockRequest));
    doReturn(false).when(wsApiService).waitReady();

    ResponseEntity<Void> response = wsApiService.putImportFile(targetBaseUrlPath, targetUrl,
        mockFile, userProperties);

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
  }

  @Test
  public void testPutImportFileBadRequest() throws Exception {
    MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain",
        new byte[10]);
    String targetBaseUrlPath = "target/baseUrl";
    String targetUrl = "target/url";
    List<String> userProperties = new ArrayList<>();

    mockStatic(AuthUtil.class);
    doNothing().when(mockImporter)
        .importFile(any(), eq(targetBaseUrlPath), eq(targetUrl), eq(userProperties));
    doThrow(new IllegalArgumentException("Test Exception")).when(mockImporter)
        .importFile(any(), any(), any(), any());

    wsApiService = spy(new WsApiServiceImpl(mockRequest));
    doReturn(true).when(wsApiService).waitReady();

    LockssRestServiceException exception = assertThrows(LockssRestServiceException.class, () ->
        wsApiService.putImportFile(targetBaseUrlPath, targetUrl, mockFile, userProperties));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    AuthUtil.checkHasRole("AU_ADMIN");
  }

  @Test
  public void testPutImportFileInternalServerError() throws Exception {
    MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain",
        new byte[10]);
    String targetBaseUrlPath = "target/baseUrl";
    String targetUrl = "target/url";
    List<String> userProperties = new ArrayList<>();

    mockStatic(AuthUtil.class);
    doNothing().when(mockImporter)
        .importFile(any(), eq(targetBaseUrlPath), eq(targetUrl), eq(userProperties));
    doThrow(new IOException("Test Exception")).when(mockImporter)
        .importFile(any(), any(), any(), any());

    wsApiService = spy(new WsApiServiceImpl(mockRequest));
    doReturn(true).when(wsApiService).waitReady();

    LockssRestServiceException exception = assertThrows(LockssRestServiceException.class, () ->
        wsApiService.putImportFile(targetBaseUrlPath, targetUrl, mockFile, userProperties));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    AuthUtil.checkHasRole("AU_ADMIN");
  }

}