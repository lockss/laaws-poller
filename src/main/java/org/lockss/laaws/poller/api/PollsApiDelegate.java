package org.lockss.laaws.poller.api;

import org.lockss.laaws.poller.model.ErrorDesc;
import org.lockss.laaws.poller.model.PollDetail;
import org.lockss.laaws.poller.model.PollerPager;
import org.lockss.laaws.poller.model.PollerSummary;
import org.lockss.laaws.poller.model.RepairPager;
import org.lockss.laaws.poller.model.UrlPager;
import org.lockss.laaws.poller.model.VoterPager;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

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
    default ResponseEntity<String> callPoll( String  body) {
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
    default ResponseEntity<Void> cancelPoll( String  psId) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default PollsApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * @see PollsApi#getPollDetails
     */
    default ResponseEntity<PollDetail> getPollDetails( String  pollKey) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pollerId\" : \"pollerId\",  \"pollDesc\" : {    \"auId\" : \"auId\",    \"cuSetSpec\" : {      \"upperBound\" : \"upperBound\",      \"urlPrefix\" : \"urlPrefix\",      \"lowerBound\" : \"lowerBound\"    },    \"protocol\" : 6,    \"pollType\" : 3,    \"variant\" : \"PoR\",    \"pluginPollVersion\" : \"pluginPollVersion\",    \"modulus\" : 1  },  \"quorum\" : 4,  \"pollKey\" : \"pollKey\",  \"votedPeers\" : [ {    \"peerId\" : \"peerId\",    \"lastStateChange\" : 8,    \"agreement\" : 7.386282,    \"pollerOnlyLink\" : \"pollerOnlyLink\",    \"bytesRead\" : 1,    \"bytesHashed\" : 7,    \"wtNumDisagree\" : 9.965781,    \"voterOnlyLink\" : \"voterOnlyLink\",    \"wtNumVoterOnly\" : 6.6835623,    \"disagreeLink\" : \"disagreeLink\",    \"numVoterOnly\" : 6,    \"numAgree\" : 1,    \"state\" : \"state\",    \"agreeLink\" : \"agreeLink\",    \"numPollerOnly\" : 1,    \"wtAgreement\" : 4.9652185,    \"wtNumAgree\" : 5.025005,    \"wtNumPollerOnly\" : 9.36931,    \"status\" : \"status\",    \"numDisagree\" : 1  }, {    \"peerId\" : \"peerId\",    \"lastStateChange\" : 8,    \"agreement\" : 7.386282,    \"pollerOnlyLink\" : \"pollerOnlyLink\",    \"bytesRead\" : 1,    \"bytesHashed\" : 7,    \"wtNumDisagree\" : 9.965781,    \"voterOnlyLink\" : \"voterOnlyLink\",    \"wtNumVoterOnly\" : 6.6835623,    \"disagreeLink\" : \"disagreeLink\",    \"numVoterOnly\" : 6,    \"numAgree\" : 1,    \"state\" : \"state\",    \"agreeLink\" : \"agreeLink\",    \"numPollerOnly\" : 1,    \"wtAgreement\" : 4.9652185,    \"wtNumAgree\" : 5.025005,    \"wtNumPollerOnly\" : 9.36931,    \"status\" : \"status\",    \"numDisagree\" : 1  } ],  \"voteMargin\" : 9,  \"duration\" : 5,  \"voteDeadline\" : 3,  \"repairQueue\" : {    \"numActive\" : 3,    \"activeLink\" : \"activeLink\",    \"numCompleted\" : 3,    \"pendingLink\" : \"pendingLink\",    \"numPending\" : 6,    \"completedLink\" : \"completedLink\"  },  \"createTime\" : 5,  \"outerCircleTarget\" : 7,  \"pollEnd\" : 2,  \"deadline\" : 2,  \"tally\" : {    \"errorLink\" : \"errorLink\",    \"numNoQuorum\" : 6,    \"wtTooClose\" : 6.878052,    \"noQuorumLink\" : \"noQuorumLink\",    \"wtNoQuorum\" : 5.9448957,    \"disagreeLink\" : \"disagreeLink\",    \"tooCloseLink\" : \"tooCloseLink\",    \"numTooClose\" : 3,    \"numAgree\" : 9,    \"wtDisagreed\" : 6.778325,    \"agreeLink\" : \"agreeLink\",    \"numError\" : 1,    \"numDisagree\" : 6,    \"wtAgreed\" : 2.8841622  },  \"hashAlgorithm\" : \"hashAlgorithm\",  \"status\" : \"status\"}", PollDetail.class), HttpStatus.NOT_IMPLEMENTED);
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
     * @see PollsApi#getPollPeerVoteUrls
     */
    default ResponseEntity<UrlPager> getPollPeerVoteUrls( String  pollKey,
         String  peerId,
         String  urls,
         Integer  page,
         Integer  size) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  },  \"urls\" : [ {    \"link\" : \"http:www.example.com/v1/element\",    \"desc\" : \"pollerOnly\"  }, {    \"link\" : \"http:www.example.com/v1/element\",    \"desc\" : \"pollerOnly\"  } ]}", UrlPager.class), HttpStatus.NOT_IMPLEMENTED);
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
    default ResponseEntity<PollerSummary> getPollStatus( String  psId) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"numCompletedRepairs\" : 5,  \"auId\" : \"auId\",  \"numHashErrors\" : 1,  \"numTalliedUrls\" : 6,  \"start\" : 2,  \"pollKey\" : \"pollKey\",  \"variant\" : \"variant\",  \"numAgreeUrls\" : 5.637377,  \"pollEnd\" : 9,  \"deadline\" : 7,  \"detailLink\" : \"detailLink\",  \"status\" : \"status\",  \"participants\" : 0}", PollerSummary.class), HttpStatus.NOT_IMPLEMENTED);
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
    default ResponseEntity<PollerPager> getPollsAsPoller( Integer  size,
         Integer  page) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  },  \"polls\" : [ {    \"numCompletedRepairs\" : 5,    \"auId\" : \"auId\",    \"numHashErrors\" : 1,    \"numTalliedUrls\" : 6,    \"start\" : 2,    \"pollKey\" : \"pollKey\",    \"variant\" : \"variant\",    \"numAgreeUrls\" : 5.637377,    \"pollEnd\" : 9,    \"deadline\" : 7,    \"detailLink\" : \"detailLink\",    \"status\" : \"status\",    \"participants\" : 0  }, {    \"numCompletedRepairs\" : 5,    \"auId\" : \"auId\",    \"numHashErrors\" : 1,    \"numTalliedUrls\" : 6,    \"start\" : 2,    \"pollKey\" : \"pollKey\",    \"variant\" : \"variant\",    \"numAgreeUrls\" : 5.637377,    \"pollEnd\" : 9,    \"deadline\" : 7,    \"detailLink\" : \"detailLink\",    \"status\" : \"status\",    \"participants\" : 0  } ]}", PollerPager.class), HttpStatus.NOT_IMPLEMENTED);
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
    default ResponseEntity<VoterPager> getPollsAsVoter( Integer  size,
         Integer  page) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  },  \"polls\" : [ {    \"auId\" : \"auId\",    \"caller\" : \"caller\",    \"start\" : 0,    \"pollKey\" : \"pollKey\",    \"deadline\" : 6,    \"detailLink\" : \"detailLink\"  }, {    \"auId\" : \"auId\",    \"caller\" : \"caller\",    \"start\" : 0,    \"pollKey\" : \"pollKey\",    \"deadline\" : 6,    \"detailLink\" : \"detailLink\"  } ]}", VoterPager.class), HttpStatus.NOT_IMPLEMENTED);
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
    default ResponseEntity<RepairPager> getRepairQueueData( String  pollKey,
         String  repair,
         Integer  page,
         Integer  size) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"repairs\" : [ {    \"repairUrl\" : \"repairUrl\",    \"repairFrom\" : \"repairFrom\",    \"status\" : \"active\"  }, {    \"repairUrl\" : \"repairUrl\",    \"repairFrom\" : \"repairFrom\",    \"status\" : \"active\"  } ],  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  }}", RepairPager.class), HttpStatus.NOT_IMPLEMENTED);
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
    default ResponseEntity<UrlPager> getTallyUrls( String  pollKey,
         String  tally,
         Integer  page,
         Integer  size) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageDesc\" : {    \"total\" : 150,    \"size\" : 5,    \"nextPage\" : \"nextPage\",    \"prevPage\" : \"prevPage\",    \"page\" : 10  },  \"urls\" : [ {    \"link\" : \"http:www.example.com/v1/element\",    \"desc\" : \"pollerOnly\"  }, {    \"link\" : \"http:www.example.com/v1/element\",    \"desc\" : \"pollerOnly\"  } ]}", UrlPager.class), HttpStatus.NOT_IMPLEMENTED);
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

}
