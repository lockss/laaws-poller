#!/bin/bash
#
# Copyright (c) 2018-2020 Board of Trustees of Leland Stanford Jr. University,
# all rights reserved.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
# STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
# IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#
# Except as contained in this notice, the name of Stanford University shall not
# be used in advertising or otherwise to promote the sale, use or other dealings
# in this Software without prior written authorization from Stanford University.
#

# Generates code using Swagger.
#
# Edit StatusApiDelegate.java.
STATUS_API_DELEGATE=src/generated/java/org/lockss/laaws/poller/api/StatusApiDelegate.java
sed -i.backup "s/import org.lockss.laaws.poller.model.ApiStatus/import org.lockss.util.rest.status.ApiStatus/" $STATUS_API_DELEGATE && rm $STATUS_API_DELEGATE.backup

# Edit StatusApi.java.
STATUS_API=src/generated/java/org/lockss/laaws/poller/api/StatusApi.java
sed -i.backup "s/import org.lockss.laaws.poller.model.ApiStatus/import org.lockss.util.rest.status.ApiStatus/" $STATUS_API && rm $STATUS_API.backup

# Edit PollsApiDelegate.java.
POLLS_API_DELEGATE=src/generated/java/org/lockss/laaws/poller/api/PollsApiDelegate.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PollDesc/import org.lockss.util.rest.poller.PollDesc/" $POLLS_API_DELEGATE && rm $POLLS_API_DELEGATE.backup
sed -i.backup "s/import org.lockss.laaws.poller.model.PollWsResult/import org.lockss.ws.entities.PollWsResult/" $POLLS_API_DELEGATE && rm $POLLS_API_DELEGATE.backup

# Edit PollsApi.java.
POLLS_API=src/generated/java/org/lockss/laaws/poller/api/PollsApi.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PollDesc/import org.lockss.util.rest.poller.PollDesc/" $POLLS_API && rm $POLLS_API.backup
sed -i.backup "s/import org.lockss.laaws.poller.model.PollWsResult/import org.lockss.ws.entities.PollWsResult/" $POLLS_API && rm $POLLS_API.backup

# Edit PollerDetail.java.
POLLER_DETAIL=src/generated/java/org/lockss/laaws/poller/model/PollerDetail.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PollDesc/import org.lockss.util.rest.poller.PollDesc/" $POLLER_DETAIL && rm $POLLER_DETAIL.backup

# Edit VoterDetail.java.
VOTER_DETAIL=src/generated/java/org/lockss/laaws/poller/model/VoterDetail.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PollDesc/import org.lockss.util.rest.poller.PollDesc/" $VOTER_DETAIL && rm $VOTER_DETAIL.backup

# Edit PeersApiDelegate.java.
PEERS_API_DELEGATE=src/generated/java/org/lockss/laaws/poller/api/PeersApiDelegate.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PeerWsResult/import org.lockss.ws.entities.PeerWsResult/" $PEERS_API_DELEGATE && rm $PEERS_API_DELEGATE.backup

# Edit PeersApi.java.
PEERS_API=src/generated/java/org/lockss/laaws/poller/api/PeersApi.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PeerWsResult/import org.lockss.ws.entities.PeerWsResult/" $PEERS_API && rm $PEERS_API.backup

# Edit VotesApiDelegate.java.
VOTES_API_DELEGATE=src/generated/java/org/lockss/laaws/poller/api/VotesApiDelegate.java
sed -i.backup "s/import org.lockss.laaws.poller.model.VoteWsResult/import org.lockss.ws.entities.VoteWsResult/" $VOTES_API_DELEGATE && rm $VOTES_API_DELEGATE.backup

# Edit VotesApi.java.
VOTES_API=src/generated/java/org/lockss/laaws/poller/api/VotesApi.java
sed -i.backup "s/import org.lockss.laaws.poller.model.VoteWsResult/import org.lockss.ws.entities.VoteWsResult/" $VOTES_API && rm $VOTES_API.backup

# Edit HashesApiDelegate.java.
HASHES_API_DELEGATE=src/generated/java/org/lockss/laaws/poller/api/HashesApiDelegate.java
sed -i.backup "s/import org.lockss.laaws.poller.model.HasherWsAsynchronousResult/import org.lockss.ws.entities.HasherWsAsynchronousResult/" $HASHES_API_DELEGATE && rm $HASHES_API_DELEGATE.backup
sed -i.backup "s/import org.lockss.laaws.poller.model.HasherWsParams/import org.lockss.ws.entities.HasherWsParams/" $HASHES_API_DELEGATE && rm $HASHES_API_DELEGATE.backup

# Edit HashesApi.java.
HASHES_API=src/generated/java/org/lockss/laaws/poller/api/HashesApi.java
sed -i.backup "s/import org.lockss.laaws.poller.model.HasherWsAsynchronousResult/import org.lockss.ws.entities.HasherWsAsynchronousResult/" $HASHES_API && rm $HASHES_API.backup
sed -i.backup "s/import org.lockss.laaws.poller.model.HasherWsParams/import org.lockss.ws.entities.HasherWsParams/" $HASHES_API && rm $HASHES_API.backup

# Edit RepositoriesApiDelegate.java.
REPOSITORIES_API_DELEGATE=src/generated/java/org/lockss/laaws/poller/api/RepositoriesApiDelegate.java
sed -i.backup "s/import org.lockss.laaws.poller.model.RepositoryWsResult/import org.lockss.ws.entities.RepositoryWsResult/" $REPOSITORIES_API_DELEGATE && rm $REPOSITORIES_API_DELEGATE.backup

# Edit RepositoriesApi.java.
REPOSITORIES_API=src/generated/java/org/lockss/laaws/poller/api/RepositoriesApi.java
sed -i.backup "s/import org.lockss.laaws.poller.model.RepositoryWsResult/import org.lockss.ws.entities.RepositoryWsResult/" $REPOSITORIES_API && rm $REPOSITORIES_API.backup

# Edit RepositoryspacesApiDelegate.java.
REPOSITORY_SPACES_API_DELEGATE=src/generated/java/org/lockss/laaws/poller/api/RepositoryspacesApiDelegate.java
sed -i.backup "s/import org.lockss.laaws.poller.model.RepositorySpaceWsResult/import org.lockss.ws.entities.RepositorySpaceWsResult/" $REPOSITORY_SPACES_API_DELEGATE && rm $REPOSITORY_SPACES_API_DELEGATE.backup

# Edit RepositoryspacesApi.java.
REPOSITORY_SPACES_API=src/generated/java/org/lockss/laaws/poller/api/RepositoryspacesApi.java
sed -i.backup "s/import org.lockss.laaws.poller.model.RepositorySpaceWsResult/import org.lockss.ws.entities.RepositorySpaceWsResult/" $REPOSITORY_SPACES_API && rm $REPOSITORY_SPACES_API.backup
