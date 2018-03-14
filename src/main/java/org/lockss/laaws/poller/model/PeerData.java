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
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Detail for a single voter in a poll.
 */
@ApiModel(description = "Detail for a single voter in a poll.")
@Validated

public class PeerData {

  @JsonProperty("peerId")
  private String peerId = null;

  @JsonProperty("status")
  private String status = null;

  @JsonProperty("agreement")
  private Float agreement = null;

  @JsonProperty("numAgree")
  private Integer numAgree = null;

  @JsonProperty("agreeLink")
  private String agreeLink = null;

  @JsonProperty("numDisagree")
  private Integer numDisagree = null;

  @JsonProperty("disagreeLink")
  private String disagreeLink = null;

  @JsonProperty("numPollerOnly")
  private Integer numPollerOnly = null;

  @JsonProperty("pollerOnlyLink")
  private String pollerOnlyLink = null;

  @JsonProperty("numVoterOnly")
  private Integer numVoterOnly = null;

  @JsonProperty("voterOnlyLink")
  private String voterOnlyLink = null;

  @JsonProperty("bytesHashed")
  private Long bytesHashed = null;

  @JsonProperty("bytesRead")
  private Long bytesRead = null;

  @JsonProperty("wtAgreement")
  private Float wtAgreement = null;

  @JsonProperty("wtNumAgree")
  private Float wtNumAgree = null;

  @JsonProperty("wtNumDisagree")
  private Float wtNumDisagree = null;

  @JsonProperty("wtNumPollerOnly")
  private Float wtNumPollerOnly = null;

  @JsonProperty("wtNumVoterOnly")
  private Float wtNumVoterOnly = null;

  @JsonProperty("state")
  private String state = null;

  @JsonProperty("lastStateChange")
  private Long lastStateChange = null;

  public PeerData peerId(String peerId) {
    this.peerId = peerId;
    return this;
  }

  /**
   * the peer id for this participant
   *
   * @return peerId
   **/
  @ApiModelProperty(required = true, value = "the peer id for this participant")
  @NotNull

  public String getPeerId() {
    return peerId;
  }

  public void setPeerId(String peerId) {
    this.peerId = peerId;
  }

  public PeerData status(String status) {
    this.status = status;
    return this;
  }

  /**
   * the status of this peer
   *
   * @return status
   **/
  @ApiModelProperty(required = true, value = "the status of this peer")
  @NotNull

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public PeerData agreement(Float agreement) {
    this.agreement = agreement;
    return this;
  }

  /**
   * the percentage of vote agreement.
   *
   * @return agreement
   **/
  @ApiModelProperty(value = "the percentage of vote agreement.")

  public Float getAgreement() {
    return agreement;
  }

  public void setAgreement(Float agreement) {
    this.agreement = agreement;
  }

  public PeerData numAgree(Integer numAgree) {
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

  public PeerData agreeLink(String agreeLink) {
    this.agreeLink = agreeLink;
    return this;
  }

  /**
   * the url to list of agreement urls.
   *
   * @return agreeLink
   **/
  @ApiModelProperty(value = "the url to list of agreement urls.")

  public String getAgreeLink() {
    return agreeLink;
  }

  public void setAgreeLink(String agreeLink) {
    this.agreeLink = agreeLink;
  }

  public PeerData numDisagree(Integer numDisagree) {
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

  public PeerData disagreeLink(String disagreeLink) {
    this.disagreeLink = disagreeLink;
    return this;
  }

  /**
   * the url to list disagreed urls.
   *
   * @return disagreeLink
   **/
  @ApiModelProperty(value = "the url to list disagreed urls.")

  public String getDisagreeLink() {
    return disagreeLink;
  }

  public void setDisagreeLink(String disagreeLink) {
    this.disagreeLink = disagreeLink;
  }

  public PeerData numPollerOnly(Integer numPollerOnly) {
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

  public PeerData pollerOnlyLink(String pollerOnlyLink) {
    this.pollerOnlyLink = pollerOnlyLink;
    return this;
  }

  /**
   * the url to list of poller only urls.
   *
   * @return pollerOnlyLink
   **/
  @ApiModelProperty(value = "the url to list of poller only urls.")

  public String getPollerOnlyLink() {
    return pollerOnlyLink;
  }

  public void setPollerOnlyLink(String pollerOnlyLink) {
    this.pollerOnlyLink = pollerOnlyLink;
  }

  public PeerData numVoterOnly(Integer numVoterOnly) {
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

  public PeerData voterOnlyLink(String voterOnlyLink) {
    this.voterOnlyLink = voterOnlyLink;
    return this;
  }

  /**
   * the url to list of voter only urls.
   *
   * @return voterOnlyLink
   **/
  @ApiModelProperty(value = "the url to list of voter only urls.")

  public String getVoterOnlyLink() {
    return voterOnlyLink;
  }

  public void setVoterOnlyLink(String voterOnlyLink) {
    this.voterOnlyLink = voterOnlyLink;
  }

  public PeerData bytesHashed(Long bytesHashed) {
    this.bytesHashed = bytesHashed;
    return this;
  }

  /**
   * the number of bytes hashed.
   *
   * @return bytesHashed
   **/
  @ApiModelProperty(value = "the number of bytes hashed.")

  public Long getBytesHashed() {
    return bytesHashed;
  }

  public void setBytesHashed(Long bytesHashed) {
    this.bytesHashed = bytesHashed;
  }

  public PeerData bytesRead(Long bytesRead) {
    this.bytesRead = bytesRead;
    return this;
  }

  /**
   * the number of bytes read.
   *
   * @return bytesRead
   **/
  @ApiModelProperty(value = "the number of bytes read.")

  public Long getBytesRead() {
    return bytesRead;
  }

  public void setBytesRead(Long bytesRead) {
    this.bytesRead = bytesRead;
  }

  public PeerData wtAgreement(Float wtAgreement) {
    this.wtAgreement = wtAgreement;
    return this;
  }

  /**
   * the weight of vote percentage agreement.
   *
   * @return wtAgreement
   **/
  @ApiModelProperty(value = "the weight of vote percentage agreement.")

  public Float getWtAgreement() {
    return wtAgreement;
  }

  public void setWtAgreement(Float wtAgreement) {
    this.wtAgreement = wtAgreement;
  }

  public PeerData wtNumAgree(Float wtNumAgree) {
    this.wtNumAgree = wtNumAgree;
    return this;
  }

  /**
   * the weight of number agree votes.
   *
   * @return wtNumAgree
   **/
  @ApiModelProperty(value = "the weight of number agree votes.")

  public Float getWtNumAgree() {
    return wtNumAgree;
  }

  public void setWtNumAgree(Float wtNumAgree) {
    this.wtNumAgree = wtNumAgree;
  }

  public PeerData wtNumDisagree(Float wtNumDisagree) {
    this.wtNumDisagree = wtNumDisagree;
    return this;
  }

  /**
   * the weight of number of disagree votes.
   *
   * @return wtNumDisagree
   **/
  @ApiModelProperty(value = "the weight of number of disagree votes.")

  public Float getWtNumDisagree() {
    return wtNumDisagree;
  }

  public void setWtNumDisagree(Float wtNumDisagree) {
    this.wtNumDisagree = wtNumDisagree;
  }

  public PeerData wtNumPollerOnly(Float wtNumPollerOnly) {
    this.wtNumPollerOnly = wtNumPollerOnly;
    return this;
  }

  /**
   * the weight of number of poller only votes.
   *
   * @return wtNumPollerOnly
   **/
  @ApiModelProperty(value = "the weight of number of poller only votes.")

  public Float getWtNumPollerOnly() {
    return wtNumPollerOnly;
  }

  public void setWtNumPollerOnly(Float wtNumPollerOnly) {
    this.wtNumPollerOnly = wtNumPollerOnly;
  }

  public PeerData wtNumVoterOnly(Float wtNumVoterOnly) {
    this.wtNumVoterOnly = wtNumVoterOnly;
    return this;
  }

  /**
   * the weight of number of voter only votes.
   *
   * @return wtNumVoterOnly
   **/
  @ApiModelProperty(value = "the weight of number of voter only votes.")

  public Float getWtNumVoterOnly() {
    return wtNumVoterOnly;
  }

  public void setWtNumVoterOnly(Float wtNumVoterOnly) {
    this.wtNumVoterOnly = wtNumVoterOnly;
  }

  public PeerData state(String state) {
    this.state = state;
    return this;
  }

  /**
   * the state machine state.
   *
   * @return state
   **/
  @ApiModelProperty(value = "the state machine state.")

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public PeerData lastStateChange(Long lastStateChange) {
    this.lastStateChange = lastStateChange;
    return this;
  }

  /**
   * the time of last state change.
   *
   * @return lastStateChange
   **/
  @ApiModelProperty(value = "the time of last state change.")

  public Long getLastStateChange() {
    return lastStateChange;
  }

  public void setLastStateChange(Long lastStateChange) {
    this.lastStateChange = lastStateChange;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PeerData peerData = (PeerData) o;
    return Objects.equals(this.peerId, peerData.peerId) &&
        Objects.equals(this.status, peerData.status) &&
        Objects.equals(this.agreement, peerData.agreement) &&
        Objects.equals(this.numAgree, peerData.numAgree) &&
        Objects.equals(this.agreeLink, peerData.agreeLink) &&
        Objects.equals(this.numDisagree, peerData.numDisagree) &&
        Objects.equals(this.disagreeLink, peerData.disagreeLink) &&
        Objects.equals(this.numPollerOnly, peerData.numPollerOnly) &&
        Objects.equals(this.pollerOnlyLink, peerData.pollerOnlyLink) &&
        Objects.equals(this.numVoterOnly, peerData.numVoterOnly) &&
        Objects.equals(this.voterOnlyLink, peerData.voterOnlyLink) &&
        Objects.equals(this.bytesHashed, peerData.bytesHashed) &&
        Objects.equals(this.bytesRead, peerData.bytesRead) &&
        Objects.equals(this.wtAgreement, peerData.wtAgreement) &&
        Objects.equals(this.wtNumAgree, peerData.wtNumAgree) &&
        Objects.equals(this.wtNumDisagree, peerData.wtNumDisagree) &&
        Objects.equals(this.wtNumPollerOnly, peerData.wtNumPollerOnly) &&
        Objects.equals(this.wtNumVoterOnly, peerData.wtNumVoterOnly) &&
        Objects.equals(this.state, peerData.state) &&
        Objects.equals(this.lastStateChange, peerData.lastStateChange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(peerId, status, agreement, numAgree, agreeLink, numDisagree, disagreeLink,
        numPollerOnly, pollerOnlyLink, numVoterOnly, voterOnlyLink, bytesHashed, bytesRead,
        wtAgreement, wtNumAgree, wtNumDisagree, wtNumPollerOnly, wtNumVoterOnly, state,
        lastStateChange);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PeerData {\n");

    sb.append("    peerId: ").append(toIndentedString(peerId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    agreement: ").append(toIndentedString(agreement)).append("\n");
    sb.append("    numAgree: ").append(toIndentedString(numAgree)).append("\n");
    sb.append("    agreeLink: ").append(toIndentedString(agreeLink)).append("\n");
    sb.append("    numDisagree: ").append(toIndentedString(numDisagree)).append("\n");
    sb.append("    disagreeLink: ").append(toIndentedString(disagreeLink)).append("\n");
    sb.append("    numPollerOnly: ").append(toIndentedString(numPollerOnly)).append("\n");
    sb.append("    pollerOnlyLink: ").append(toIndentedString(pollerOnlyLink)).append("\n");
    sb.append("    numVoterOnly: ").append(toIndentedString(numVoterOnly)).append("\n");
    sb.append("    voterOnlyLink: ").append(toIndentedString(voterOnlyLink)).append("\n");
    sb.append("    bytesHashed: ").append(toIndentedString(bytesHashed)).append("\n");
    sb.append("    bytesRead: ").append(toIndentedString(bytesRead)).append("\n");
    sb.append("    wtAgreement: ").append(toIndentedString(wtAgreement)).append("\n");
    sb.append("    wtNumAgree: ").append(toIndentedString(wtNumAgree)).append("\n");
    sb.append("    wtNumDisagree: ").append(toIndentedString(wtNumDisagree)).append("\n");
    sb.append("    wtNumPollerOnly: ").append(toIndentedString(wtNumPollerOnly)).append("\n");
    sb.append("    wtNumVoterOnly: ").append(toIndentedString(wtNumVoterOnly)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    lastStateChange: ").append(toIndentedString(lastStateChange)).append("\n");
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

