package org.lockss.laaws.poller.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.lockss.laaws.poller.*;
import org.lockss.plugin.*;
import org.lockss.poller.v3.*;
import org.springframework.boot.test.context.*;

@SpringBootTest(classes = {PollerApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestPollWsSource {

  @Test
  public void testGetAuIdFromArchivalUnit() {
    // Arrange
    ArchivalUnit archivalUnitMock = mock(ArchivalUnit.class);
    when(archivalUnitMock.getAuId()).thenReturn("test-au-id");

    PollWsSource pollWsSource = new PollWsSource(archivalUnitMock);

    // Act
    String auId = pollWsSource.getAuId();

    // Assert
    assertEquals("test-au-id", auId);
  }

  @Test
  public void testGetAuIdFromV3Poller() {
    // Arrange
    ArchivalUnit archivalUnitMock = mock(ArchivalUnit.class);
    when(archivalUnitMock.getAuId()).thenReturn("test-au-id");

    V3Poller v3PollerMock = mock(V3Poller.class);
    when(v3PollerMock.getAu()).thenReturn(archivalUnitMock);

    PollWsSource pollWsSource = new PollWsSource(v3PollerMock);

    // Act
    String auId = pollWsSource.getAuId();

    // Assert
    assertEquals("test-au-id", auId);
  }

  @Test
  public void testGetAuIdPopulatedOnlyOnce() {
    // Arrange
    ArchivalUnit archivalUnitMock = mock(ArchivalUnit.class);
    when(archivalUnitMock.getAuId()).thenReturn("test-au-id");

    V3Poller v3PollerMock = mock(V3Poller.class);
    when(v3PollerMock.getAu()).thenReturn(archivalUnitMock);

    PollWsSource pollWsSource = new PollWsSource(v3PollerMock);

    // First Act / Assert
    String firstCall = pollWsSource.getAuId(); // Should invoke the logic
    assertEquals("test-au-id", firstCall);

    // Second Act / Assert
    String secondCall = pollWsSource.getAuId(); // Should not invoke the logic again
    assertEquals("test-au-id", secondCall);

    // Verify interaction with ArchivalUnit occurs once
    verify(archivalUnitMock, times(1)).getAuId();
  }
}