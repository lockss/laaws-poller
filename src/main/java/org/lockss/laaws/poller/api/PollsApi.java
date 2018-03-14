/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.0-SNAPSHOT).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.lockss.laaws.poller.api;

import org.lockss.laaws.poller.model.ErrorDesc;
import org.lockss.laaws.poller.model.PollDesc;
import org.lockss.laaws.poller.model.PollDetail;
import org.lockss.laaws.poller.model.PollerPager;
import org.lockss.laaws.poller.model.PollerSummary;
import org.lockss.laaws.poller.model.RepairPager;
import org.lockss.laaws.poller.model.UrlPager;
import org.lockss.laaws.poller.model.VoterPager;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Api(value = "polls", description = "the polls API")
public interface PollsApi {

    PollsApiDelegate getDelegate();

    @ApiOperation(value = "Send a request to call a poll to the poller", nickname = "callPoll", notes = "Use the information found in the descriptor object to initiate a  poll.", response = String.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 202, message = "The Poll request has been accepted and added to the queue.", response = String.class),
        @ApiResponse(code = 401, message = "The Request is unauthorized", response = ErrorDesc.class),
        @ApiResponse(code = 403, message = "The Au is not eligible for polling", response = ErrorDesc.class),
        @ApiResponse(code = 404, message = "The descriptor (au) can not be found.", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    default ResponseEntity<String> callPoll(@ApiParam(value = "" ,required=true )  @Valid @RequestBody PollDesc body) {
        return getDelegate().callPoll(body);
    }


    @ApiOperation(value = "Stop a poll and remove from queue.", nickname = "cancelPoll", notes = "Stop a running poll and delete any schecduled polls for poll with the poll service id.", authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Poll successfully stopped"),
        @ApiResponse(code = 401, message = "Unauthorized request", response = ErrorDesc.class),
        @ApiResponse(code = 404, message = "No poll found with that id", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls/{psId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    default ResponseEntity<Void> cancelPoll(@ApiParam(value = "",required=true) @PathVariable("psId") String psId) {
        return getDelegate().cancelPoll(psId);
    }


    @ApiOperation(value = "PollDetails", nickname = "getPollDetails", notes = "Return the detailed information about a poll.", response = PollDetail.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "poll", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Detailed poll info returned.", response = PollDetail.class),
        @ApiResponse(code = 401, message = "Unauthorized request", response = ErrorDesc.class),
        @ApiResponse(code = 404, message = "No such poll key.", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls/{pollKey}/details",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    default ResponseEntity<PollDetail> getPollDetails(@ApiParam(value = "",required=true) @PathVariable("pollKey") String pollKey) {
        return getDelegate().getPollDetails(pollKey);
    }


    @ApiOperation(value = "Poll Peer Data", nickname = "getPollPeerVoteUrls", notes = "", response = UrlPager.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "poll", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A pagable list of voter urls with a given status.", response = UrlPager.class),
        @ApiResponse(code = 404, message = "Poll or Voter ID not found.", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls/{pollKey}/peer/{peerId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    default ResponseEntity<UrlPager> getPollPeerVoteUrls(@ApiParam(value = "The pollKey from the PollDetail",required=true) @PathVariable("pollKey") String pollKey,@ApiParam(value = "The peerId from the Poll Detail.PeerData",required=true) @PathVariable("peerId") String peerId,@NotNull @ApiParam(value = "The voter urls to return.", required = true, allowableValues = "agreed, disagreed, pollerOnly, voterOnly") @Valid @RequestParam(value = "urls", required = true) String urls,@ApiParam(value = "The page number") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The page size") @Valid @RequestParam(value = "size", required = false) Integer size) {
        return getDelegate().getPollPeerVoteUrls(pollKey, peerId, urls, page, size);
    }


    @ApiOperation(value = "Get queued poll status", nickname = "getPollStatus", notes = "Get the status of a previously queued poll.", response = PollerSummary.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Poll info returned.", response = PollerSummary.class),
        @ApiResponse(code = 401, message = "Unauthorized request", response = ErrorDesc.class),
        @ApiResponse(code = 404, message = "No such poll id.", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls/{psId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    default ResponseEntity<PollerSummary> getPollStatus(@ApiParam(value = "",required=true) @PathVariable("psId") String psId) {
        return getDelegate().getPollStatus(psId);
    }


    @ApiOperation(value = "Get the list of recent polls as poller.", nickname = "getPollsAsPoller", notes = "Get the list of recent polls as poller from the poll queue. if size and page are passed in use those arguments to limit return data.", response = PollerPager.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "poller", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A pagable list has been returned.", response = PollerPager.class),
        @ApiResponse(code = 401, message = "Unauthorized request", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls/poller",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    default ResponseEntity<PollerPager> getPollsAsPoller(@ApiParam(value = "Size of the page to retrieve.") @Valid @RequestParam(value = "size", required = false) Integer size,@ApiParam(value = "Number of the page to retrieve.") @Valid @RequestParam(value = "page", required = false) Integer page) {
        return getDelegate().getPollsAsPoller(size, page);
    }


    @ApiOperation(value = "Get the list of recent voter only polls.", nickname = "getPollsAsVoter", notes = "Get the list of recent polls as voter from the poll queue. if size and page are passed in use those arguments to limit return data.", response = VoterPager.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "voter", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A pagable list has been returned.", response = VoterPager.class),
        @ApiResponse(code = 401, message = "Unauthorized request", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls/voter",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    default ResponseEntity<VoterPager> getPollsAsVoter(@ApiParam(value = "Size of the page to retrieve.") @Valid @RequestParam(value = "size", required = false) Integer size,@ApiParam(value = "Number of the page to retrieve.") @Valid @RequestParam(value = "page", required = false) Integer page) {
        return getDelegate().getPollsAsVoter(size, page);
    }


    @ApiOperation(value = "Poll Repairs", nickname = "getRepairQueueData", notes = "", response = RepairPager.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "poll", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A pagable list of repair queue elements.", response = RepairPager.class),
        @ApiResponse(code = 404, message = "Poll ID not found.", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls/{pollKey}/repairs",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    default ResponseEntity<RepairPager> getRepairQueueData(@ApiParam(value = "The pollKey as listed in the PollDetail object.",required=true) @PathVariable("pollKey") String pollKey,@NotNull @ApiParam(value = "The repair queue elements to return.", required = true, allowableValues = "pending, active, completed") @Valid @RequestParam(value = "repair", required = true) String repair,@ApiParam(value = "The page number.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the page.") @Valid @RequestParam(value = "size", required = false) Integer size) {
        return getDelegate().getRepairQueueData(pollKey, repair, page, size);
    }


    @ApiOperation(value = "Page Tally", nickname = "getTallyUrls", notes = "", response = UrlPager.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={ "poll", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A pagable list of urls for given tally type.", response = UrlPager.class),
        @ApiResponse(code = 404, message = "Poll Key not found.", response = ErrorDesc.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDesc.class) })
    @RequestMapping(value = "/polls/{pollKey}/tallies",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    default ResponseEntity<UrlPager> getTallyUrls(@ApiParam(value = "The pollKey as listed in the PollDetail object.",required=true) @PathVariable("pollKey") String pollKey,@NotNull @ApiParam(value = "The kind of tally element to return.", required = true, allowableValues = "agree, disagree, error, noQuorum, tooClose") @Valid @RequestParam(value = "tally", required = true) String tally,@ApiParam(value = "The page number.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the page.") @Valid @RequestParam(value = "size", required = false) Integer size) {
        return getDelegate().getTallyUrls(pollKey, tally, page, size);
    }

}
