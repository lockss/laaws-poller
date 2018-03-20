package org.lockss.laaws.poller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.lockss.laaws.poller.model.*;
import org.lockss.rs.status.ApiStatus;

/**
 * A delegate to be called by the {@link PollsApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */

public interface PollsApiDelegate {

    Logger log = LoggerFactory.getLogger(PollsApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    /**
     * @see PollsApi#callPoll
     */
    default ResponseEntity<String> callPoll(PollDesc body) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("\"\"", String.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#cancelPoll
     */
    default ResponseEntity<Void> cancelPoll(String psId) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getPollPeerVoteUrls
     */
    default ResponseEntity<UrlPager> getPollPeerVoteUrls(String pollKey,
        String peerId,
        String urls,
        Integer page,
        Integer size) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  },  \"urls\" : [ \"urls\", \"urls\" ]}", UrlPager.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getPollStatus
     */
    default ResponseEntity<PollerSummary> getPollStatus(String psId) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"numCompletedRepairs\" : 5,  \"auId\" : \"auId\",  \"numHashErrors\" : 1,  \"numTalliedUrls\" : 6,  \"start\" : 2,  \"pollKey\" : \"pollKey\",  \"variant\" : \"variant\",  \"numAgreeUrls\" : 5,  \"pollEnd\" : 9,  \"deadline\" : 7,  \"detailLink\" : {    \"link\" : \"http:www.example.com/v1/element\",    \"desc\" : \"pollerOnly\"  },  \"status\" : \"status\",  \"participants\" : 0}", PollerSummary.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getPollerPollDetails
     */
    default ResponseEntity<PollerDetail> getPollerPollDetails(String pollKey) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"noAuPeers\" : [ \"noAuPeers\", \"noAuPeers\" ],  \"pollerId\" : \"pollerId\",  \"pollDesc\" : {    \"auId\" : \"auId\",    \"cuSetSpec\" : {      \"upperBound\" : \"upperBound\",      \"urlPrefix\" : \"urlPrefix\",      \"lowerBound\" : \"lowerBound\"    },    \"protocol\" : 6,    \"pollType\" : 3,    \"variant\" : \"PoR\",    \"pluginPollVersion\" : \"pluginPollVersion\",    \"modulus\" : 1  },  \"quorum\" : 3,  \"pollKey\" : \"pollKey\",  \"votedPeers\" : [ {    \"peerId\" : \"peerId\",    \"lastStateChange\" : 9,    \"agreement\" : 2.027123,    \"pollerOnlyLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"bytesRead\" : 6,    \"bytesHashed\" : 1,    \"wtNumDisagree\" : 4.9652185,    \"voterOnlyLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"wtNumVoterOnly\" : 9.965781,    \"disagreeLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numVoterOnly\" : 1,    \"numAgree\" : 4,    \"state\" : \"state\",    \"agreeLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numPollerOnly\" : 1,    \"wtAgreement\" : 7.4577446,    \"wtNumAgree\" : 1.1730742,    \"wtNumPollerOnly\" : 5.025005,    \"status\" : \"status\",    \"numDisagree\" : 7  }, {    \"peerId\" : \"peerId\",    \"lastStateChange\" : 9,    \"agreement\" : 2.027123,    \"pollerOnlyLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"bytesRead\" : 6,    \"bytesHashed\" : 1,    \"wtNumDisagree\" : 4.9652185,    \"voterOnlyLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"wtNumVoterOnly\" : 9.965781,    \"disagreeLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numVoterOnly\" : 1,    \"numAgree\" : 4,    \"state\" : \"state\",    \"agreeLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numPollerOnly\" : 1,    \"wtAgreement\" : 7.4577446,    \"wtNumAgree\" : 1.1730742,    \"wtNumPollerOnly\" : 5.025005,    \"status\" : \"status\",    \"numDisagree\" : 7  } ],  \"voteMargin\" : 5,  \"voteDuration\" : 7,  \"duration\" : 6,  \"voteDeadline\" : 2,  \"repairQueue\" : {    \"numActive\" : 5,    \"activeLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numCompleted\" : 6,    \"pendingLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numPending\" : 6,    \"completedLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    }  },  \"createTime\" : 0,  \"outerCircleTarget\" : 5,  \"pollEnd\" : 9,  \"deadline\" : 1,  \"tally\" : {    \"errorLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numNoQuorum\" : 6,    \"wtTooClose\" : 2.8841622,    \"noQuorumLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"wtNoQuorum\" : 6.778325,    \"disagreeLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"tooCloseLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numTooClose\" : 9,    \"numAgree\" : 6,    \"wtDisagreed\" : 1.284659,    \"agreeLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"numError\" : 3,    \"numDisagree\" : 8,    \"wtAgreed\" : 6.965118  },  \"hashAlgorithm\" : \"hashAlgorithm\",  \"status\" : \"status\",  \"errorDetails\" : \"errorDetails\"}", PollerDetail.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getPollsAsPoller
     */
    default ResponseEntity<PollerPager> getPollsAsPoller(Integer size,
        Integer page) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  },  \"polls\" : [ {    \"numCompletedRepairs\" : 5,    \"auId\" : \"auId\",    \"numHashErrors\" : 1,    \"numTalliedUrls\" : 6,    \"start\" : 2,    \"pollKey\" : \"pollKey\",    \"variant\" : \"variant\",    \"numAgreeUrls\" : 5,    \"pollEnd\" : 9,    \"deadline\" : 7,    \"detailLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"status\" : \"status\",    \"participants\" : 0  }, {    \"numCompletedRepairs\" : 5,    \"auId\" : \"auId\",    \"numHashErrors\" : 1,    \"numTalliedUrls\" : 6,    \"start\" : 2,    \"pollKey\" : \"pollKey\",    \"variant\" : \"variant\",    \"numAgreeUrls\" : 5,    \"pollEnd\" : 9,    \"deadline\" : 7,    \"detailLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"status\" : \"status\",    \"participants\" : 0  } ]}", PollerPager.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getPollsAsVoter
     */
    default ResponseEntity<VoterPager> getPollsAsVoter(Integer size,
        Integer page) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  },  \"polls\" : [ {    \"auId\" : \"auId\",    \"caller\" : \"caller\",    \"start\" : 0,    \"pollKey\" : \"pollKey\",    \"deadline\" : 6,    \"detailLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"status\" : \"status\"  }, {    \"auId\" : \"auId\",    \"caller\" : \"caller\",    \"start\" : 0,    \"pollKey\" : \"pollKey\",    \"deadline\" : 6,    \"detailLink\" : {      \"link\" : \"http:www.example.com/v1/element\",      \"desc\" : \"pollerOnly\"    },    \"status\" : \"status\"  } ]}", VoterPager.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getRepairQueueData
     */
    default ResponseEntity<RepairPager> getRepairQueueData(String pollKey,
        String repair,
        Integer page,
        Integer size) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"repairs\" : [ {    \"result\" : \"No Quorum\",    \"repairUrl\" : \"repairUrl\",    \"repairFrom\" : \"repairFrom\"  }, {    \"result\" : \"No Quorum\",    \"repairUrl\" : \"repairUrl\",    \"repairFrom\" : \"repairFrom\"  } ],  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  }}", RepairPager.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getTallyUrls
     */
    default ResponseEntity<UrlPager> getTallyUrls(String pollKey,
        String tally,
        Integer page,
        Integer size) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  },  \"urls\" : [ \"urls\", \"urls\" ]}", UrlPager.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getVoterPollDetails
     */
    default ResponseEntity<VoterDetail> getVoterPollDetails(String pollKey) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pollerNonce\" : \"pollerNonce\",  \"pollerId\" : \"pollerId\",  \"agreement\" : 5.962133916683182,  \"pollDesc\" : {    \"auId\" : \"auId\",    \"cuSetSpec\" : {      \"upperBound\" : \"upperBound\",      \"urlPrefix\" : \"urlPrefix\",      \"lowerBound\" : \"lowerBound\"    },    \"protocol\" : 6,    \"pollType\" : 3,    \"variant\" : \"PoR\",    \"pluginPollVersion\" : \"pluginPollVersion\",    \"modulus\" : 1  },  \"wtSymmetricAgreement\" : 7.061401241503109,  \"pollKey\" : \"pollKey\",  \"voterNonce\" : \"voterNonce\",  \"duration\" : 6,  \"voteDeadline\" : 9,  \"createTime\" : 0,  \"voter2Nonce\" : \"voter2Nonce\",  \"numVoterOnly\" : 7,  \"symmetricAgreement\" : 2.3021358869347655,  \"numAgree\" : 3,  \"callerId\" : \"callerId\",  \"deadline\" : 1,  \"wtAgreement\" : 5.637376656633329,  \"numPollerOnly\" : 4,  \"hashAlgorithm\" : \"hashAlgorithm\",  \"status\" : \"status\",  \"numDisagree\" : 2,  \"errorDetails\" : \"errorDetails\"}", VoterDetail.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getStatus
     */
    default ResponseEntity<ApiStatus> getStatus() {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"ready\" : true,  \"version\" : \"version\"}", ApiStatus.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default StatusApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
