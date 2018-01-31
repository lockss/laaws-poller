package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.lockss.laaws.poller.model.PollRepairQueue;
import org.lockss.laaws.poller.model.PollSpec;
import org.lockss.laaws.poller.model.PollTallyStatus;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A description of a poll being performed by the Poller
 */
@ApiModel(description = "A description of a poll being performed by the Poller")
@Validated

public class Poll   {
  @JsonProperty("pollerId")
  private String pollerId = null;

  @JsonProperty("pollKey")
  private String pollKey = null;

  @JsonProperty("pollSpec")
  private PollSpec pollSpec = null;

  @JsonProperty("createTime")
  private Long createTime = null;

  @JsonProperty("duration")
  private Long duration = null;

  @JsonProperty("pollDeadline")
  private Long pollDeadline = null;

  @JsonProperty("outerCircleTarget")
  private Integer outerCircleTarget = null;

  @JsonProperty("hashAlgorithm")
  private String hashAlgorithm = null;

  @JsonProperty("status")
  private String status = null;

  @JsonProperty("voteMargin")
  private Integer voteMargin = null;

  @JsonProperty("voteDeadline")
  private Long voteDeadline = null;

  @JsonProperty("pollEnd")
  private Long pollEnd = null;

  @JsonProperty("quorum")
  private Integer quorum = null;

  @JsonProperty("votedPeers")
  private Integer votedPeers = null;

  @JsonProperty("tallyStatus")
  private PollTallyStatus tallyStatus = null;

  @JsonProperty("repairQueue")
  private PollRepairQueue repairQueue = null;

  public Poll pollerId(String pollerId) {
    this.pollerId = pollerId;
    return this;
  }

  /**
   * The id of the poller who called the poll
   * @return pollerId
  **/
  @ApiModelProperty(required = true, value = "The id of the poller who called the poll")
  @NotNull


  public String getPollerId() {
    return pollerId;
  }

  public void setPollerId(String pollerId) {
    this.pollerId = pollerId;
  }

  public Poll pollKey(String pollKey) {
    this.pollKey = pollKey;
    return this;
  }

  /**
   * A randomly generated poll id
   * @return pollKey
  **/
  @ApiModelProperty(required = true, value = "A randomly generated poll id")
  @NotNull


  public String getPollKey() {
    return pollKey;
  }

  public void setPollKey(String pollKey) {
    this.pollKey = pollKey;
  }

  public Poll pollSpec(PollSpec pollSpec) {
    this.pollSpec = pollSpec;
    return this;
  }

  /**
   * Get pollSpec
   * @return pollSpec
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public PollSpec getPollSpec() {
    return pollSpec;
  }

  public void setPollSpec(PollSpec pollSpec) {
    this.pollSpec = pollSpec;
  }

  public Poll createTime(Long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * The timestamp  at which the poll was requested.
   * @return createTime
  **/
  @ApiModelProperty(required = true, value = "The timestamp  at which the poll was requested.")
  @NotNull


  public Long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }

  public Poll duration(Long duration) {
    this.duration = duration;
    return this;
  }

  /**
   * The estimated duration for the poll.
   * @return duration
  **/
  @ApiModelProperty(value = "The estimated duration for the poll.")


  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public Poll pollDeadline(Long pollDeadline) {
    this.pollDeadline = pollDeadline;
    return this;
  }

  /**
   * The time by which the poll must have completed
   * @return pollDeadline
  **/
  @ApiModelProperty(required = true, value = "The time by which the poll must have completed")
  @NotNull


  public Long getPollDeadline() {
    return pollDeadline;
  }

  public void setPollDeadline(Long pollDeadline) {
    this.pollDeadline = pollDeadline;
  }

  public Poll outerCircleTarget(Integer outerCircleTarget) {
    this.outerCircleTarget = outerCircleTarget;
    return this;
  }

  /**
   * The number of peers from the poller outer circle to taget.
   * @return outerCircleTarget
  **/
  @ApiModelProperty(value = "The number of peers from the poller outer circle to taget.")


  public Integer getOuterCircleTarget() {
    return outerCircleTarget;
  }

  public void setOuterCircleTarget(Integer outerCircleTarget) {
    this.outerCircleTarget = outerCircleTarget;
  }

  public Poll hashAlgorithm(String hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
    return this;
  }

  /**
   * The algorithm used by the hasher for this poll.
   * @return hashAlgorithm
  **/
  @ApiModelProperty(required = true, value = "The algorithm used by the hasher for this poll.")
  @NotNull


  public String getHashAlgorithm() {
    return hashAlgorithm;
  }

  public void setHashAlgorithm(String hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  public Poll status(String status) {
    this.status = status;
    return this;
  }

  /**
   * The current status of the poll.
   * @return status
  **/
  @ApiModelProperty(required = true, value = "The current status of the poll.")
  @NotNull


  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Poll voteMargin(Integer voteMargin) {
    this.voteMargin = voteMargin;
    return this;
  }

  /**
   * The required agreement need to decide landslide agreement or disagreement.
   * @return voteMargin
  **/
  @ApiModelProperty(value = "The required agreement need to decide landslide agreement or disagreement.")


  public Integer getVoteMargin() {
    return voteMargin;
  }

  public void setVoteMargin(Integer voteMargin) {
    this.voteMargin = voteMargin;
  }

  public Poll voteDeadline(Long voteDeadline) {
    this.voteDeadline = voteDeadline;
    return this;
  }

  /**
   * The  time by which all voters must have voted.
   * @return voteDeadline
  **/
  @ApiModelProperty(required = true, value = "The  time by which all voters must have voted.")
  @NotNull


  public Long getVoteDeadline() {
    return voteDeadline;
  }

  public void setVoteDeadline(Long voteDeadline) {
    this.voteDeadline = voteDeadline;
  }

  public Poll pollEnd(Long pollEnd) {
    this.pollEnd = pollEnd;
    return this;
  }

  /**
   * the time at which the poll ended or -1 if still running.
   * @return pollEnd
  **/
  @ApiModelProperty(value = "the time at which the poll ended or -1 if still running.")


  public Long getPollEnd() {
    return pollEnd;
  }

  public void setPollEnd(Long pollEnd) {
    this.pollEnd = pollEnd;
  }

  public Poll quorum(Integer quorum) {
    this.quorum = quorum;
    return this;
  }

  /**
   * The minimum required for a quroum
   * @return quorum
  **/
  @ApiModelProperty(value = "The minimum required for a quroum")


  public Integer getQuorum() {
    return quorum;
  }

  public void setQuorum(Integer quorum) {
    this.quorum = quorum;
  }

  public Poll votedPeers(Integer votedPeers) {
    this.votedPeers = votedPeers;
    return this;
  }

  /**
   * The number of peers who've voted.
   * @return votedPeers
  **/
  @ApiModelProperty(value = "The number of peers who've voted.")


  public Integer getVotedPeers() {
    return votedPeers;
  }

  public void setVotedPeers(Integer votedPeers) {
    this.votedPeers = votedPeers;
  }

  public Poll tallyStatus(PollTallyStatus tallyStatus) {
    this.tallyStatus = tallyStatus;
    return this;
  }

  /**
   * Get tallyStatus
   * @return tallyStatus
  **/
  @ApiModelProperty(value = "")

  @Valid

  public PollTallyStatus getTallyStatus() {
    return tallyStatus;
  }

  public void setTallyStatus(PollTallyStatus tallyStatus) {
    this.tallyStatus = tallyStatus;
  }

  public Poll repairQueue(PollRepairQueue repairQueue) {
    this.repairQueue = repairQueue;
    return this;
  }

  /**
   * Get repairQueue
   * @return repairQueue
  **/
  @ApiModelProperty(value = "")

  @Valid

  public PollRepairQueue getRepairQueue() {
    return repairQueue;
  }

  public void setRepairQueue(PollRepairQueue repairQueue) {
    this.repairQueue = repairQueue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Poll poll = (Poll) o;
    return Objects.equals(this.pollerId, poll.pollerId) &&
        Objects.equals(this.pollKey, poll.pollKey) &&
        Objects.equals(this.pollSpec, poll.pollSpec) &&
        Objects.equals(this.createTime, poll.createTime) &&
        Objects.equals(this.duration, poll.duration) &&
        Objects.equals(this.pollDeadline, poll.pollDeadline) &&
        Objects.equals(this.outerCircleTarget, poll.outerCircleTarget) &&
        Objects.equals(this.hashAlgorithm, poll.hashAlgorithm) &&
        Objects.equals(this.status, poll.status) &&
        Objects.equals(this.voteMargin, poll.voteMargin) &&
        Objects.equals(this.voteDeadline, poll.voteDeadline) &&
        Objects.equals(this.pollEnd, poll.pollEnd) &&
        Objects.equals(this.quorum, poll.quorum) &&
        Objects.equals(this.votedPeers, poll.votedPeers) &&
        Objects.equals(this.tallyStatus, poll.tallyStatus) &&
        Objects.equals(this.repairQueue, poll.repairQueue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pollerId, pollKey, pollSpec, createTime, duration, pollDeadline, outerCircleTarget, hashAlgorithm, status, voteMargin, voteDeadline, pollEnd, quorum, votedPeers, tallyStatus, repairQueue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Poll {\n");
    
    sb.append("    pollerId: ").append(toIndentedString(pollerId)).append("\n");
    sb.append("    pollKey: ").append(toIndentedString(pollKey)).append("\n");
    sb.append("    pollSpec: ").append(toIndentedString(pollSpec)).append("\n");
    sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
    sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
    sb.append("    pollDeadline: ").append(toIndentedString(pollDeadline)).append("\n");
    sb.append("    outerCircleTarget: ").append(toIndentedString(outerCircleTarget)).append("\n");
    sb.append("    hashAlgorithm: ").append(toIndentedString(hashAlgorithm)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    voteMargin: ").append(toIndentedString(voteMargin)).append("\n");
    sb.append("    voteDeadline: ").append(toIndentedString(voteDeadline)).append("\n");
    sb.append("    pollEnd: ").append(toIndentedString(pollEnd)).append("\n");
    sb.append("    quorum: ").append(toIndentedString(quorum)).append("\n");
    sb.append("    votedPeers: ").append(toIndentedString(votedPeers)).append("\n");
    sb.append("    tallyStatus: ").append(toIndentedString(tallyStatus)).append("\n");
    sb.append("    repairQueue: ").append(toIndentedString(repairQueue)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

