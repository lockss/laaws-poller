package org.lockss.laaws.poller.api;

import org.lockss.laaws.poller.model.Error;
import org.lockss.laaws.poller.model.Poll;
import org.lockss.laaws.poller.model.PollPageInfo;
import org.lockss.laaws.poller.model.PollReq;
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
     * @see PollsApi#cancelPoll
     */
    default ResponseEntity<Poll> cancelPoll(String pollId) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pollerId\" : \"pollerId\",  \"pollSpec\" : {    \"pollType\" : 0,    \"protocolVersion\" : 6,    \"pluginPollVersion\" : \"pluginPollVersion\",    \"modulus\" : 1,    \"cachedUriSet\" : {      \"auId\" : \"auId\",      \"spec\" : {        \"upperBound\" : \"upperBound\",        \"urlPrefix\" : \"urlPrefix\",        \"lowerBound\" : \"lowerBound\"      }    },    \"pollVariant\" : \"PoR\"  },  \"quorum\" : 4,  \"pollKey\" : \"pollKey\",  \"votedPeers\" : 7,  \"tallyStatus\" : {    \"weightedDisagreedSum\" : 1.0246457,    \"weightedTooCloseSum\" : 1.4894159,    \"weightedAgreedSum\" : 1.2315135,    \"agreedUrls\" : [ \"agreedUrls\", \"agreedUrls\" ],    \"disagreedUrls\" : [ \"disagreedUrls\", \"disagreedUrls\" ],    \"errorUrls\" : [ \"errorUrls\", \"errorUrls\" ],    \"tooCloseUrls\" : [ \"tooCloseUrls\", \"tooCloseUrls\" ],    \"weightedNoQuorumSum\" : 6.846853,    \"noQuorumUrls\" : [ \"noQuorumUrls\", \"noQuorumUrls\" ]  },  \"pollDeadline\" : 2,  \"voteMargin\" : 9,  \"duration\" : 5,  \"voteDeadline\" : 3,  \"repairQueue\" : {    \"pendingRepairs\" : [ {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    }, {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    } ],    \"activeRepairs\" : [ {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    }, {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    } ],    \"completedRepairs\" : [ {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    }, {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    } ]  },  \"createTime\" : 5,  \"outerCircleTarget\" : 7,  \"pollEnd\" : 2,  \"hashAlgorithm\" : \"hashAlgorithm\",  \"status\" : \"status\"}", Poll.class), HttpStatus.NOT_IMPLEMENTED);
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
     * @see PollsApi#getPoll
     */
    default ResponseEntity<Poll> getPoll(String pollId) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pollerId\" : \"pollerId\",  \"pollSpec\" : {    \"pollType\" : 0,    \"protocolVersion\" : 6,    \"pluginPollVersion\" : \"pluginPollVersion\",    \"modulus\" : 1,    \"cachedUriSet\" : {      \"auId\" : \"auId\",      \"spec\" : {        \"upperBound\" : \"upperBound\",        \"urlPrefix\" : \"urlPrefix\",        \"lowerBound\" : \"lowerBound\"      }    },    \"pollVariant\" : \"PoR\"  },  \"quorum\" : 4,  \"pollKey\" : \"pollKey\",  \"votedPeers\" : 7,  \"tallyStatus\" : {    \"weightedDisagreedSum\" : 1.0246457,    \"weightedTooCloseSum\" : 1.4894159,    \"weightedAgreedSum\" : 1.2315135,    \"agreedUrls\" : [ \"agreedUrls\", \"agreedUrls\" ],    \"disagreedUrls\" : [ \"disagreedUrls\", \"disagreedUrls\" ],    \"errorUrls\" : [ \"errorUrls\", \"errorUrls\" ],    \"tooCloseUrls\" : [ \"tooCloseUrls\", \"tooCloseUrls\" ],    \"weightedNoQuorumSum\" : 6.846853,    \"noQuorumUrls\" : [ \"noQuorumUrls\", \"noQuorumUrls\" ]  },  \"pollDeadline\" : 2,  \"voteMargin\" : 9,  \"duration\" : 5,  \"voteDeadline\" : 3,  \"repairQueue\" : {    \"pendingRepairs\" : [ {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    }, {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    } ],    \"activeRepairs\" : [ {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    }, {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    } ],    \"completedRepairs\" : [ {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    }, {      \"uri\" : \"uri\",      \"repairFrom\" : \"repairFrom\"    } ]  },  \"createTime\" : 5,  \"outerCircleTarget\" : 7,  \"pollEnd\" : 2,  \"hashAlgorithm\" : \"hashAlgorithm\",  \"status\" : \"status\"}", Poll.class), HttpStatus.NOT_IMPLEMENTED);
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
     * @see PollsApi#getPolls
     */
    default ResponseEntity<PollPageInfo> getPolls(Integer size,
        Integer page) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"pageInfo\" : {    \"resultsPerPage\" : 20,    \"prevLink\" : \"prevLink\",    \"totalCount\" : 150,    \"currentPage\" : 2,    \"nextLink\" : \"nextLink\"  },  \"polls\" : [ {    \"pollerId\" : \"pollerId\",    \"pollSpec\" : {      \"pollType\" : 0,      \"protocolVersion\" : 6,      \"pluginPollVersion\" : \"pluginPollVersion\",      \"modulus\" : 1,      \"cachedUriSet\" : {        \"auId\" : \"auId\",        \"spec\" : {          \"upperBound\" : \"upperBound\",          \"urlPrefix\" : \"urlPrefix\",          \"lowerBound\" : \"lowerBound\"        }      },      \"pollVariant\" : \"PoR\"    },    \"quorum\" : 4,    \"pollKey\" : \"pollKey\",    \"votedPeers\" : 7,    \"tallyStatus\" : {      \"weightedDisagreedSum\" : 1.0246457,      \"weightedTooCloseSum\" : 1.4894159,      \"weightedAgreedSum\" : 1.2315135,      \"agreedUrls\" : [ \"agreedUrls\", \"agreedUrls\" ],      \"disagreedUrls\" : [ \"disagreedUrls\", \"disagreedUrls\" ],      \"errorUrls\" : [ \"errorUrls\", \"errorUrls\" ],      \"tooCloseUrls\" : [ \"tooCloseUrls\", \"tooCloseUrls\" ],      \"weightedNoQuorumSum\" : 6.846853,      \"noQuorumUrls\" : [ \"noQuorumUrls\", \"noQuorumUrls\" ]    },    \"pollDeadline\" : 2,    \"voteMargin\" : 9,    \"duration\" : 5,    \"voteDeadline\" : 3,    \"repairQueue\" : {      \"pendingRepairs\" : [ {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      }, {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      } ],      \"activeRepairs\" : [ {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      }, {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      } ],      \"completedRepairs\" : [ {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      }, {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      } ]    },    \"createTime\" : 5,    \"outerCircleTarget\" : 7,    \"pollEnd\" : 2,    \"hashAlgorithm\" : \"hashAlgorithm\",    \"status\" : \"status\"  }, {    \"pollerId\" : \"pollerId\",    \"pollSpec\" : {      \"pollType\" : 0,      \"protocolVersion\" : 6,      \"pluginPollVersion\" : \"pluginPollVersion\",      \"modulus\" : 1,      \"cachedUriSet\" : {        \"auId\" : \"auId\",        \"spec\" : {          \"upperBound\" : \"upperBound\",          \"urlPrefix\" : \"urlPrefix\",          \"lowerBound\" : \"lowerBound\"        }      },      \"pollVariant\" : \"PoR\"    },    \"quorum\" : 4,    \"pollKey\" : \"pollKey\",    \"votedPeers\" : 7,    \"tallyStatus\" : {      \"weightedDisagreedSum\" : 1.0246457,      \"weightedTooCloseSum\" : 1.4894159,      \"weightedAgreedSum\" : 1.2315135,      \"agreedUrls\" : [ \"agreedUrls\", \"agreedUrls\" ],      \"disagreedUrls\" : [ \"disagreedUrls\", \"disagreedUrls\" ],      \"errorUrls\" : [ \"errorUrls\", \"errorUrls\" ],      \"tooCloseUrls\" : [ \"tooCloseUrls\", \"tooCloseUrls\" ],      \"weightedNoQuorumSum\" : 6.846853,      \"noQuorumUrls\" : [ \"noQuorumUrls\", \"noQuorumUrls\" ]    },    \"pollDeadline\" : 2,    \"voteMargin\" : 9,    \"duration\" : 5,    \"voteDeadline\" : 3,    \"repairQueue\" : {      \"pendingRepairs\" : [ {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      }, {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      } ],      \"activeRepairs\" : [ {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      }, {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      } ],      \"completedRepairs\" : [ {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      }, {        \"uri\" : \"uri\",        \"repairFrom\" : \"repairFrom\"      } ]    },    \"createTime\" : 5,    \"outerCircleTarget\" : 7,    \"pollEnd\" : 2,    \"hashAlgorithm\" : \"hashAlgorithm\",    \"status\" : \"status\"  } ]}", PollPageInfo.class), HttpStatus.NOT_IMPLEMENTED);
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
     * @see PollsApi#requestPoll
     */
    default ResponseEntity<String> requestPoll(PollReq body) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("\"A id for this poll.\"", String.class), HttpStatus.NOT_IMPLEMENTED);
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
