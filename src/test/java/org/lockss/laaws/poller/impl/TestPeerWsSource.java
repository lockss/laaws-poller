/*

 Copyright (c) 2014-2020 Board of Trustees of Leland Stanford Jr. University,
 all rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Except as contained in this notice, the name of Stanford University shall not
 be used in advertising or otherwise to promote the sale, use or other dealings
 in this Software without prior written authorization from Stanford University.

 */
package org.lockss.laaws.poller.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.laaws.poller.*;
import org.lockss.protocol.PeerIdentity;
import org.lockss.protocol.PeerIdentityStatus;
import org.lockss.protocol.V3LcapMessage;
import org.lockss.protocol.V3LcapMessage.PollNak;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test class for PeerWsSource.
 */
@SpringBootTest(classes = {PollerApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestPeerWsSource extends SpringLockssTestCase4 {

  @Mock
  private PeerIdentityStatus mockPeerStatus;

  @Mock
  private PeerIdentity mockPeerIdentity;

  @Mock
  private PollNak mockPollNak;

  private PeerWsSource peerWsSource;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.openMocks(this);
    
    // Setup default mock behavior
    when(mockPeerStatus.getPeerIdentity()).thenReturn(mockPeerIdentity);
    when(mockPeerIdentity.getIdString()).thenReturn("test-peer-id");
    
    peerWsSource = new PeerWsSource(mockPeerStatus);
  }

  @Test
  public void testConstructorWithPeerIdentityStatus() {
    // Test that constructor accepts PeerIdentityStatus
    PeerWsSource source = new PeerWsSource(mockPeerStatus);
    assertNotNull(source);
  }

  @Test
  public void testGetPeerIdLazily() {
    // Setup mock
    when(mockPeerStatus.getPeerIdentity()).thenReturn(mockPeerIdentity);
    when(mockPeerIdentity.getIdString()).thenReturn("peer-123");

    // First call should retrieve and cache the peer ID
    String peerId = peerWsSource.getPeerId();
    assertEquals("peer-123", peerId);
    
    // Verify that getPeerIdentity was called
    verify(mockPeerStatus, times(1)).getPeerIdentity();
    verify(mockPeerIdentity, times(1)).getIdString();

    // Second call should return cached value without calling mock again
    String peerId2 = peerWsSource.getPeerId();
    assertEquals("peer-123", peerId2);
    
    // Verify that mock methods were not called again
    verify(mockPeerStatus, times(1)).getPeerIdentity();
    verify(mockPeerIdentity, times(1)).getIdString();
  }

  @Test
  public void testGetLastMessageTime() {
    // Setup mock
    when(mockPeerStatus.getLastMessageTime()).thenReturn(1672531200000L);

    // Test lazy loading
    Long lastMessage = peerWsSource.getLastMessage();
    assertEquals(Long.valueOf(1672531200000L), lastMessage);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getLastMessageTime();

    // Second call should return cached value
    Long lastMessage2 = peerWsSource.getLastMessage();
    assertEquals(Long.valueOf(1672531200000L), lastMessage2);
    
    // Verify mock was not called again
    verify(mockPeerStatus, times(1)).getLastMessageTime();
  }

  @Test
  public void testGetMessageTypeValidOpcode() {
    // Setup mock with valid opcode (within POLL_MESSAGES range)
    int validOpCode = V3LcapMessage.POLL_MESSAGES_BASE + 1;
    when(mockPeerStatus.getLastMessageOpCode()).thenReturn(validOpCode);

    // Test message type retrieval
    String messageType = peerWsSource.getMessageType();
    
    // Should return the message type with opcode
    assertNotNull(messageType);
    assertTrue(messageType.contains("(" + validOpCode + ")"));
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getLastMessageOpCode();
  }

  @Test
  public void testGetMessageTypeInvalidOpcode() {
    // Setup mock with invalid opcode (outside POLL_MESSAGES range)
    int invalidOpCode = -1;
    when(mockPeerStatus.getLastMessageOpCode()).thenReturn(invalidOpCode);

    // Test message type retrieval
    String messageType = peerWsSource.getMessageType();
    
    // Should return "n/a" for invalid opcode
    assertEquals("n/a", messageType);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getLastMessageOpCode();
  }

  @Test
  public void testGetPlatformGroupMatch() {
    // Setup mock with groups
    List<String> peerGroups = Arrays.asList("group1", "group2");
    when(mockPeerStatus.getGroups()).thenReturn(peerGroups);

    // Test platform group match
    Boolean match = peerWsSource.getPlatformGroupMatch();
    
    // Should return a boolean value
    assertNotNull(match);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getGroups();
  }

  @Test
  public void testLazyLoadingPreventsRecalculation() {
    // Setup mock
    when(mockPeerStatus.getTotalMessages()).thenReturn(42L);

    // First call
    Long messageCount1 = peerWsSource.getMessageCount();
    assertEquals(Long.valueOf(42L), messageCount1);
    
    // Change mock behavior
    when(mockPeerStatus.getTotalMessages()).thenReturn(100L);
    
    // Second call should still return cached value
    Long messageCount2 = peerWsSource.getMessageCount();
    assertEquals(Long.valueOf(42L), messageCount2);
    
    // Verify mock was only called once
    verify(mockPeerStatus, times(1)).getTotalMessages();
  }

  @Test
  public void testNullPollNakHandling() {
    // Setup mock to return null PollNak
    when(mockPeerStatus.getLastPollNak()).thenReturn(null);

    // Test NAK reason retrieval
    String nakReason = peerWsSource.getNakReason();
    
    // Should handle null gracefully
    assertNull(nakReason);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getLastPollNak();
  }

  @Test
  public void testGetLastPoll() {
    // Setup mock
    when(mockPeerStatus.getLastPollerTime()).thenReturn(1672617600000L);

    // Test lazy loading
    Long lastPoll = peerWsSource.getLastPoll();
    assertEquals(Long.valueOf(1672617600000L), lastPoll);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getLastPollerTime();
  }

  @Test
  public void testGetLastVote() {
    // Setup mock
    when(mockPeerStatus.getLastVoterTime()).thenReturn(1672704000000L);

    // Test lazy loading
    Long lastVote = peerWsSource.getLastVote();
    assertEquals(Long.valueOf(1672704000000L), lastVote);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getLastVoterTime();
  }

  @Test
  public void testGetLastInvitation() {
    // Setup mock
    when(mockPeerStatus.getLastPollInvitationTime()).thenReturn(1672790400000L);

    // Test lazy loading
    Long lastInvitation = peerWsSource.getLastInvitation();
    assertEquals(Long.valueOf(1672790400000L), lastInvitation);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getLastPollInvitationTime();
  }

  @Test
  public void testGetInvitationCount() {
    // Setup mock
    when(mockPeerStatus.getTotalPollInvitatioins()).thenReturn(5L);

    // Test lazy loading
    Long invitationCount = peerWsSource.getInvitationCount();
    assertEquals(Long.valueOf(5L), invitationCount);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getTotalPollInvitatioins();
  }

  @Test
  public void testGetPollsCalled() {
    // Setup mock
    when(mockPeerStatus.getTotalPollerPolls()).thenReturn(3);

    // Test lazy loading
    Long pollsCalled = peerWsSource.getPollsCalled();
    assertEquals(Long.valueOf(3), pollsCalled);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getTotalPollerPolls();
  }

  @Test
  public void testGetVotesCast() {
    // Setup mock
    when(mockPeerStatus.getTotalVoterPolls()).thenReturn(7);

    // Test lazy loading
    Long votesCast = peerWsSource.getVotesCast();
    assertEquals(Long.valueOf(7), votesCast);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getTotalVoterPolls();
  }

  @Test
  public void testGetPollsRejected() {
    // Setup mock
    when(mockPeerStatus.getTotalRejectedPolls()).thenReturn(2);

    // Test lazy loading
    Long pollsRejected = peerWsSource.getPollsRejected();
    assertEquals(Long.valueOf(2), pollsRejected);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getTotalRejectedPolls();
  }

  @Test
  public void testGetNakReasonWithPollNak() {
    // Setup mock with PollNak
    when(mockPeerStatus.getLastPollNak()).thenReturn(mockPollNak);
    when(mockPollNak.toString()).thenReturn("BUSY");

    // Test NAK reason retrieval
    String nakReason = peerWsSource.getNakReason();
    assertEquals("BUSY", nakReason);
    
    // Verify mock was called (but don't verify toString() as Mockito doesn't allow it)
    verify(mockPeerStatus, times(1)).getLastPollNak();
  }

  @Test
  public void testGetGroupsWithNullGroups() {
    // Setup mock to return null groups
    when(mockPeerStatus.getGroups()).thenReturn(null);

    // Test groups retrieval
    List<String> groups = peerWsSource.getGroups();
    assertNull(groups);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getGroups();
  }

  @Test
  public void testGetGroupsWithEmptyGroups() {
    // Setup mock to return empty groups
    List<String> emptyGroups = new ArrayList<>();
    when(mockPeerStatus.getGroups()).thenReturn(emptyGroups);

    // Test groups retrieval
    List<String> groups = peerWsSource.getGroups();
    assertNotNull(groups);
    assertTrue(groups.isEmpty());
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getGroups();
  }

  @Test
  public void testGetGroupsWithMultipleGroups() {
    // Setup mock to return multiple groups
    List<String> multipleGroups = Arrays.asList("group1", "group2", "group3");
    when(mockPeerStatus.getGroups()).thenReturn(multipleGroups);

    // Test groups retrieval
    List<String> groups = peerWsSource.getGroups();
    assertNotNull(groups);
    assertEquals(3, groups.size());
    assertEquals("group1", groups.get(0));
    assertEquals("group2", groups.get(1));
    assertEquals("group3", groups.get(2));
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getGroups();
  }

  @Test
  public void testGetPlatformGroupMatchWithEmptyGroups() {
    // Setup mock to return empty groups
    List<String> emptyGroups = new ArrayList<>();
    when(mockPeerStatus.getGroups()).thenReturn(emptyGroups);

    // Test platform group match - should return true for empty groups
    Boolean match = peerWsSource.getPlatformGroupMatch();
    assertTrue(match);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getGroups();
  }

  @Test
  public void testGetPlatformGroupMatchWithNullGroups() {
    // Setup mock to return null groups
    when(mockPeerStatus.getGroups()).thenReturn(null);

    // Test platform group match - should return true for null groups
    Boolean match = peerWsSource.getPlatformGroupMatch();
    assertTrue(match);
    
    // Verify mock was called
    verify(mockPeerStatus, times(1)).getGroups();
  }
}