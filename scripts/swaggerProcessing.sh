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

# Edit PollsApi.java.
POLLS_API=src/generated/java/org/lockss/laaws/poller/api/PollsApi.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PollDesc/import org.lockss.util.rest.poller.PollDesc/" $POLLS_API && rm $POLLS_API.backup

# Edit PollerDetail.java.
POLLER_DETAIL=src/generated/java/org/lockss/laaws/poller/model/PollerDetail.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PollDesc/import org.lockss.util.rest.poller.PollDesc/" $POLLER_DETAIL && rm $POLLER_DETAIL.backup

# Edit VoterDetail.java.
VOTER_DETAIL=src/generated/java/org/lockss/laaws/poller/model/VoterDetail.java
sed -i.backup "s/import org.lockss.laaws.poller.model.PollDesc/import org.lockss.util.rest.poller.PollDesc/" $VOTER_DETAIL && rm $VOTER_DETAIL.backup
