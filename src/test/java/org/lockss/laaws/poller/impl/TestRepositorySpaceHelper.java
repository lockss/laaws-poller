package org.lockss.laaws.poller.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.lockss.repository.*;
import org.lockss.test.*;
import org.lockss.util.os.*;
import org.mockito.*;

public class TestRepositorySpaceHelper {

  /**
   * Tests the createUniverse method of the RepositorySpaceHelper class. The createUniverse method
   * retrieves all repository spaces and their associated data from the RepositoryManager and wraps
   * them in RepositorySpaceWsSource objects.
   */
  @Test
  public void testCreateUniverseWithEmptyRepositoryManager() {
    // Mock the LockssDaemon and its RepositoryManager
    MockLockssDaemon mockDaemon = mock(MockLockssDaemon.class);
    RepositoryManager mockRepoManager = mock(RepositoryManager.class);

    try (MockedStatic<MockLockssDaemon> daemonStaticMock = mockStatic(MockLockssDaemon.class)) {
      daemonStaticMock.when(MockLockssDaemon::getLockssDaemon).thenReturn(mockDaemon);
      when(mockDaemon.getRepositoryManager()).thenReturn(mockRepoManager);
      when(mockRepoManager.getRepositoryDFMap()).thenReturn(new HashMap<>());

      RepositorySpaceHelper helper = new RepositorySpaceHelper();
      List<RepositorySpaceWsSource> result = helper.createUniverse();

      // Assertions
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Test
  public void testCreateUniverseWithSingleRepositorySpace() {
    // Mock the LockssDaemon and its RepositoryManager
    MockLockssDaemon mockDaemon = mock(MockLockssDaemon.class);
    RepositoryManager mockRepoManager = mock(RepositoryManager.class);
    Map<String, PlatformUtil.DF> mockDfMap = new HashMap<>();
    PlatformUtil.DF mockDf = mock(PlatformUtil.DF.class);

    mockDfMap.put("repo1", mockDf);

    try (MockedStatic<MockLockssDaemon> daemonStaticMock = mockStatic(MockLockssDaemon.class)) {
      daemonStaticMock.when(MockLockssDaemon::getLockssDaemon).thenReturn(mockDaemon);
      when(mockDaemon.getRepositoryManager()).thenReturn(mockRepoManager);
      when(mockRepoManager.getRepositoryDFMap()).thenReturn(mockDfMap);

      RepositorySpaceHelper helper = new RepositorySpaceHelper();
      List<RepositorySpaceWsSource> result = helper.createUniverse();

      // Assertions
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("repo1", result.get(0).getRepositorySpaceId());
    }
  }

  @Test
  public void testCreateUniverseWithMultipleRepositorySpaces() {
    // Mock the LockssDaemon and its RepositoryManager
    MockLockssDaemon mockDaemon = mock(MockLockssDaemon.class);
    RepositoryManager mockRepoManager = mock(RepositoryManager.class);
    Map<String, PlatformUtil.DF> mockDfMap = new HashMap<>();
    PlatformUtil.DF mockDf1 = mock(PlatformUtil.DF.class);
    PlatformUtil.DF mockDf2 = mock(PlatformUtil.DF.class);

    mockDfMap.put("repo1", mockDf1);
    mockDfMap.put("repo2", mockDf2);

    try (MockedStatic<MockLockssDaemon> daemonStaticMock = mockStatic(MockLockssDaemon.class)) {
      daemonStaticMock.when(MockLockssDaemon::getLockssDaemon).thenReturn(mockDaemon);
      when(mockDaemon.getRepositoryManager()).thenReturn(mockRepoManager);
      when(mockRepoManager.getRepositoryDFMap()).thenReturn(mockDfMap);

      RepositorySpaceHelper helper = new RepositorySpaceHelper();
      List<RepositorySpaceWsSource> result = helper.createUniverse();

      // Assertions
      assertNotNull(result);
      assertEquals(2, result.size());
      assertTrue(result.stream().anyMatch(source -> "repo1".equals(source.getRepositorySpaceId())));
      assertTrue(result.stream().anyMatch(source -> "repo2".equals(source.getRepositorySpaceId())));
    }
  }
}