/*
 * Copyright (c) 2018 Board of Trustees of Leland Stanford Jr. University,
 * all rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Stanford University shall not
 * be used in advertising or otherwise to promote the sale, use or other dealings
 * in this Software without prior written authorization from Stanford University.
 */

package org.lockss.laaws.poller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * The details of a poll being performed or queued as the Voter.
 */
@ApiModel(description = "The details of a poll being performed or queued as the Voter.")
@Validated

public class VoterDetail {

  @JsonProperty("pollDesc")
  private PollDesc pollDesc = null;

  @JsonProperty("pollerId")
  private String pollerId = null;

  @JsonProperty("callerId")
  private String callerId = null;

  @JsonProperty("status")
  private String status = null;

  @JsonProperty("pollKey")
  private String pollKey = null;

  @JsonProperty("createTime")
  private Long createTime = null;

  @JsonProperty("duration")
  private Long duration = null;

  @JsonProperty("deadline")
  private Long deadline = null;

  @JsonProperty("hashAlgorithm")
  private String hashAlgorithm = null;

  @JsonProperty("agreement")
  private Double agreement = null;

  @JsonProperty("wtAgreement")
  private Double wtAgreement = null;

  @JsonProperty("symmetricAgreement")
  private Double symmetricAgreement = null;

  @JsonProperty("wtSymmetricAgreement")
  private Double wtSymmetricAgreement = null;

  @JsonProperty("pollerNonce")
  private String pollerNonce = null;

  @JsonProperty("voterNonce")
  private String voterNonce = null;

  @JsonProperty("voter2Nonce")
  private String voter2Nonce = null;

  @JsonProperty("voteDeadline")
  private Long voteDeadline = null;

  @JsonProperty("numAgree")
  private Integer numAgree = null;

  @JsonProperty("numDisagree")
  private Integer numDisagree = null;

  @JsonProperty("numPollerOnly")
  private Integer numPollerOnly = null;

  @JsonProperty("numVoterOnly")
  private Integer numVoterOnly = null;

  @JsonProperty("errorDetails")
  private String errorDetails = null;

  public VoterDetail pollDesc(PollDesc pollDesc) {
    this.pollDesc = pollDesc;
    return this;
  }

  /**
   * Get pollDesc
   *
   * @return pollDesc
   **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public PollDesc getPollDesc() {
    return pollDesc;
  }

  public void setPollDesc(PollDesc pollDesc) {
    this.pollDesc = pollDesc;
  }

  public VoterDetail pollerId(String pollerId) {
    this.pollerId = pollerId;
    return this;
  }

  /**
   * The id of the voter in the poll
   *
   * @return pollerId
   **/
  @ApiModelProperty(required = true, value = "The id of the voter in the poll")
  @NotNull

  public String getPollerId() {
    return pollerId;
  }

  public void setPollerId(String pollerId) {
    this.pollerId = pollerId;
  }

  public VoterDetail callerId(String callerId) {
    this.callerId = callerId;
    return this;
  }

  /**
   * The id of the poller who called the poll
   *
   * @return callerId
   **/
  @ApiModelProperty(value = "The id of the poller who called the poll")

  public String getCallerId() {
    return callerId;
  }

  public void setCallerId(String callerId) {
    this.callerId = callerId;
  }

  public VoterDetail status(String status) {
    this.status = status;
    return this;
  }

  /**
   * The current status of the poll.
   *
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

  public VoterDetail pollKey(String pollKey) {
    this.pollKey = pollKey;
    return this;
  }

  /**
   * Key generated by poll manager when poll is created.
   *
   * @return pollKey
   **/
  @ApiModelProperty(value = "Key generated by poll manager when poll is created.")

  public String getPollKey() {
    return pollKey;
  }

  public void setPollKey(String pollKey) {
    this.pollKey = pollKey;
  }

  public VoterDetail createTime(Long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * The timestamp  at which the poll was created.
   *
   * @return createTime
   **/
  @ApiModelProperty(value = "The timestamp  at which the poll was created.")

  public Long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }

  public VoterDetail duration(Long duration) {
    this.duration = duration;
    return this;
  }

  /**
   * The estimated duration for the poll.
   *
   * @return duration
   **/
  @ApiModelProperty(value = "The estimated duration for the poll.")

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public VoterDetail deadline(Long deadline) {
    this.deadline = deadline;
    return this;
  }

  /**
   * The time by which the poll must have completed
   *
   * @return deadline
   **/
  @ApiModelProperty(value = "The time by which the poll must have completed")

  public Long getDeadline() {
    return deadline;
  }

  public void setDeadline(Long deadline) {
    this.deadline = deadline;
  }

  public VoterDetail hashAlgorithm(String hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
    return this;
  }

  /**
   * The algorithm used by the hasher for this poll.
   *
   * @return hashAlgorithm
   **/
  @ApiModelProperty(value = "The algorithm used by the hasher for this poll.")

  public String getHashAlgorithm() {
    return hashAlgorithm;
  }

  public void setHashAlgorithm(String hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  public VoterDetail agreement(Double agreement) {
    this.agreement = agreement;
    return this;
  }

  /**
   * The percentage agreement.
   *
   * @return agreement
   **/
  @ApiModelProperty(value = "The percentage agreement.")

  public Double getAgreement() {
    return agreement;
  }

  public void setAgreement(Double agreement) {
    this.agreement = agreement;
  }

  public VoterDetail wtAgreement(Double wtAgreement) {
    this.wtAgreement = wtAgreement;
    return this;
  }

  /**
   * The weighted percentage agreement.
   *
   * @return wtAgreement
   **/
  @ApiModelProperty(value = "The weighted percentage agreement.")

  public Double getWtAgreement() {
    return wtAgreement;
  }

  public void setWtAgreement(Double wtAgreement) {
    this.wtAgreement = wtAgreement;
  }

  public VoterDetail symmetricAgreement(Double symmetricAgreement) {
    this.symmetricAgreement = symmetricAgreement;
    return this;
  }

  /**
   * The percentage of symmetric agreement.
   *
   * @return symmetricAgreement
   **/
  @ApiModelProperty(value = "The percentage of symmetric agreement.")

  public Double getSymmetricAgreement() {
    return symmetricAgreement;
  }

  public void setSymmetricAgreement(Double symmetricAgreement) {
    this.symmetricAgreement = symmetricAgreement;
  }

  public VoterDetail wtSymmetricAgreement(Double wtSymmetricAgreement) {
    this.wtSymmetricAgreement = wtSymmetricAgreement;
    return this;
  }

  /**
   * The weighted percentage of symmetric agreement.
   *
   * @return wtSymmetricAgreement
   **/
  @ApiModelProperty(value = "The weighted percentage of symmetric agreement.")

  public Double getWtSymmetricAgreement() {
    return wtSymmetricAgreement;
  }

  public void setWtSymmetricAgreement(Double wtSymmetricAgreement) {
    this.wtSymmetricAgreement = wtSymmetricAgreement;
  }

  public VoterDetail pollerNonce(String pollerNonce) {
    this.pollerNonce = pollerNonce;
    return this;
  }

  /**
   * the poller nonce
   *
   * @return pollerNonce
   **/
  @ApiModelProperty(value = "the poller nonce")

  public String getPollerNonce() {
    return pollerNonce;
  }

  public void setPollerNonce(String pollerNonce) {
    this.pollerNonce = pollerNonce;
  }

  public VoterDetail voterNonce(String voterNonce) {
    this.voterNonce = voterNonce;
    return this;
  }

  /**
   * the voter nonce
   *
   * @return voterNonce
   **/
  @ApiModelProperty(value = "the voter nonce")

  public String getVoterNonce() {
    return voterNonce;
  }

  public void setVoterNonce(String voterNonce) {
    this.voterNonce = voterNonce;
  }

  public VoterDetail voter2Nonce(String voter2Nonce) {
    this.voter2Nonce = voter2Nonce;
    return this;
  }

  /**
   * the voter2 nonce
   *
   * @return voter2Nonce
   **/
  @ApiModelProperty(value = "the voter2 nonce")

  public String getVoter2Nonce() {
    return voter2Nonce;
  }

  public void setVoter2Nonce(String voter2Nonce) {
    this.voter2Nonce = voter2Nonce;
  }

  public VoterDetail voteDeadline(Long voteDeadline) {
    this.voteDeadline = voteDeadline;
    return this;
  }

  /**
   * The  time by which all voters must have voted.
   *
   * @return voteDeadline
   **/
  @ApiModelProperty(value = "The  time by which all voters must have voted.")

  public Long getVoteDeadline() {
    return voteDeadline;
  }

  public void setVoteDeadline(Long voteDeadline) {
    this.voteDeadline = voteDeadline;
  }

  public VoterDetail numAgree(Integer numAgree) {
    this.numAgree = numAgree;
    return this;
  }

  /**
   * Get numAgree
   *
   * @return numAgree
   **/
  @ApiModelProperty(value = "")

  public Integer getNumAgree() {
    return numAgree;
  }

  public void setNumAgree(Integer numAgree) {
    this.numAgree = numAgree;
  }

  public VoterDetail numDisagree(Integer numDisagree) {
    this.numDisagree = numDisagree;
    return this;
  }

  /**
   * Get numDisagree
   *
   * @return numDisagree
   **/
  @ApiModelProperty(value = "")

  public Integer getNumDisagree() {
    return numDisagree;
  }

  public void setNumDisagree(Integer numDisagree) {
    this.numDisagree = numDisagree;
  }

  public VoterDetail numPollerOnly(Integer numPollerOnly) {
    this.numPollerOnly = numPollerOnly;
    return this;
  }

  /**
   * Get numPollerOnly
   *
   * @return numPollerOnly
   **/
  @ApiModelProperty(value = "")

  public Integer getNumPollerOnly() {
    return numPollerOnly;
  }

  public void setNumPollerOnly(Integer numPollerOnly) {
    this.numPollerOnly = numPollerOnly;
  }

  public VoterDetail numVoterOnly(Integer numVoterOnly) {
    this.numVoterOnly = numVoterOnly;
    return this;
  }

  /**
   * Get numVoterOnly
   *
   * @return numVoterOnly
   **/
  @ApiModelProperty(value = "")

  public Integer getNumVoterOnly() {
    return numVoterOnly;
  }

  public void setNumVoterOnly(Integer numVoterOnly) {
    this.numVoterOnly = numVoterOnly;
  }

  public VoterDetail errorDetails(String errorDetails) {
    this.errorDetails = errorDetails;
    return this;
  }

  /**
   * The error which caused the poll to fail.
   *
   * @return errorDetails
   **/
  @ApiModelProperty(value = "The error which caused the poll to fail.")

  public String getErrorDetails() {
    return errorDetails;
  }

  public void setErrorDetails(String errorDetails) {
    this.errorDetails = errorDetails;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VoterDetail voterDetail = (VoterDetail) o;
    return Objects.equals(this.pollDesc, voterDetail.pollDesc) &&
        Objects.equals(this.pollerId, voterDetail.pollerId) &&
        Objects.equals(this.callerId, voterDetail.callerId) &&
        Objects.equals(this.status, voterDetail.status) &&
        Objects.equals(this.pollKey, voterDetail.pollKey) &&
        Objects.equals(this.createTime, voterDetail.createTime) &&
        Objects.equals(this.duration, voterDetail.duration) &&
        Objects.equals(this.deadline, voterDetail.deadline) &&
        Objects.equals(this.hashAlgorithm, voterDetail.hashAlgorithm) &&
        Objects.equals(this.agreement, voterDetail.agreement) &&
        Objects.equals(this.wtAgreement, voterDetail.wtAgreement) &&
        Objects.equals(this.symmetricAgreement, voterDetail.symmetricAgreement) &&
        Objects.equals(this.wtSymmetricAgreement, voterDetail.wtSymmetricAgreement) &&
        Objects.equals(this.pollerNonce, voterDetail.pollerNonce) &&
        Objects.equals(this.voterNonce, voterDetail.voterNonce) &&
        Objects.equals(this.voter2Nonce, voterDetail.voter2Nonce) &&
        Objects.equals(this.voteDeadline, voterDetail.voteDeadline) &&
        Objects.equals(this.numAgree, voterDetail.numAgree) &&
        Objects.equals(this.numDisagree, voterDetail.numDisagree) &&
        Objects.equals(this.numPollerOnly, voterDetail.numPollerOnly) &&
        Objects.equals(this.numVoterOnly, voterDetail.numVoterOnly) &&
        Objects.equals(this.errorDetails, voterDetail.errorDetails);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(pollDesc, pollerId, callerId, status, pollKey, createTime, duration, deadline,
            hashAlgorithm, agreement, wtAgreement, symmetricAgreement, wtSymmetricAgreement,
            pollerNonce, voterNonce, voter2Nonce, voteDeadline, numAgree, numDisagree,
            numPollerOnly, numVoterOnly, errorDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VoterDetail {\n");

    sb.append("    pollDesc: ").append(toIndentedString(pollDesc)).append("\n");
    sb.append("    pollerId: ").append(toIndentedString(pollerId)).append("\n");
    sb.append("    callerId: ").append(toIndentedString(callerId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    pollKey: ").append(toIndentedString(pollKey)).append("\n");
    sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
    sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
    sb.append("    deadline: ").append(toIndentedString(deadline)).append("\n");
    sb.append("    hashAlgorithm: ").append(toIndentedString(hashAlgorithm)).append("\n");
    sb.append("    agreement: ").append(toIndentedString(agreement)).append("\n");
    sb.append("    wtAgreement: ").append(toIndentedString(wtAgreement)).append("\n");
    sb.append("    symmetricAgreement: ").append(toIndentedString(symmetricAgreement)).append("\n");
    sb.append("    wtSymmetricAgreement: ").append(toIndentedString(wtSymmetricAgreement))
        .append("\n");
    sb.append("    pollerNonce: ").append(toIndentedString(pollerNonce)).append("\n");
    sb.append("    voterNonce: ").append(toIndentedString(voterNonce)).append("\n");
    sb.append("    voter2Nonce: ").append(toIndentedString(voter2Nonce)).append("\n");
    sb.append("    voteDeadline: ").append(toIndentedString(voteDeadline)).append("\n");
    sb.append("    numAgree: ").append(toIndentedString(numAgree)).append("\n");
    sb.append("    numDisagree: ").append(toIndentedString(numDisagree)).append("\n");
    sb.append("    numPollerOnly: ").append(toIndentedString(numPollerOnly)).append("\n");
    sb.append("    numVoterOnly: ").append(toIndentedString(numVoterOnly)).append("\n");
    sb.append("    errorDetails: ").append(toIndentedString(errorDetails)).append("\n");
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

