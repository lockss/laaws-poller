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
 * A summary of a poll in which we are the poller.
 */
@ApiModel(description = "A summary of a poll in which we are the poller.")
@Validated

public class PollerSummary {

  @JsonProperty("auId")
  private String auId = null;

  @JsonProperty("variant")
  private String variant = null;

  @JsonProperty("status")
  private String status = null;

  @JsonProperty("participants")
  private Integer participants = null;

  @JsonProperty("numTalliedUrls")
  private Integer numTalliedUrls = null;

  @JsonProperty("numHashErrors")
  private Integer numHashErrors = null;

  @JsonProperty("numCompletedRepairs")
  private Integer numCompletedRepairs = null;

  @JsonProperty("numAgreeUrls")
  private Integer numAgreeUrls = null;

  @JsonProperty("start")
  private Long start = null;

  @JsonProperty("deadline")
  private Long deadline = null;

  @JsonProperty("pollEnd")
  private Long pollEnd = null;

  @JsonProperty("pollKey")
  private String pollKey = null;

  @JsonProperty("detailLink")
  private LinkDesc detailLink = null;

  public PollerSummary auId(String auId) {
    this.auId = auId;
    return this;
  }

  /**
   * The id for the au being polled.
   *
   * @return auId
   **/
  @ApiModelProperty(required = true, value = "The id for the au being polled.")
  @NotNull

  public String getAuId() {
    return auId;
  }

  public void setAuId(String auId) {
    this.auId = auId;
  }

  public PollerSummary variant(String variant) {
    this.variant = variant;
    return this;
  }

  /**
   * The V3 Poll variant.
   *
   * @return variant
   **/
  @ApiModelProperty(required = true, value = "The V3 Poll variant.")
  @NotNull

  public String getVariant() {
    return variant;
  }

  public void setVariant(String variant) {
    this.variant = variant;
  }

  public PollerSummary status(String status) {
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

  public PollerSummary participants(Integer participants) {
    this.participants = participants;
    return this;
  }

  /**
   * Get participants
   *
   * @return participants
   **/
  @ApiModelProperty(value = "")

  public Integer getParticipants() {
    return participants;
  }

  public void setParticipants(Integer participants) {
    this.participants = participants;
  }

  public PollerSummary numTalliedUrls(Integer numTalliedUrls) {
    this.numTalliedUrls = numTalliedUrls;
    return this;
  }

  /**
   * Get numTalliedUrls
   *
   * @return numTalliedUrls
   **/
  @ApiModelProperty(value = "")

  public Integer getNumTalliedUrls() {
    return numTalliedUrls;
  }

  public void setNumTalliedUrls(Integer numTalliedUrls) {
    this.numTalliedUrls = numTalliedUrls;
  }

  public PollerSummary numHashErrors(Integer numHashErrors) {
    this.numHashErrors = numHashErrors;
    return this;
  }

  /**
   * Get numHashErrors
   *
   * @return numHashErrors
   **/
  @ApiModelProperty(value = "")

  public Integer getNumHashErrors() {
    return numHashErrors;
  }

  public void setNumHashErrors(Integer numHashErrors) {
    this.numHashErrors = numHashErrors;
  }

  public PollerSummary numCompletedRepairs(Integer numCompletedRepairs) {
    this.numCompletedRepairs = numCompletedRepairs;
    return this;
  }

  /**
   * Get numCompletedRepairs
   *
   * @return numCompletedRepairs
   **/
  @ApiModelProperty(value = "")

  public Integer getNumCompletedRepairs() {
    return numCompletedRepairs;
  }

  public void setNumCompletedRepairs(Integer numCompletedRepairs) {
    this.numCompletedRepairs = numCompletedRepairs;
  }

  public PollerSummary numAgreeUrls(Integer numAgreeUrls) {
    this.numAgreeUrls = numAgreeUrls;
    return this;
  }

  /**
   * Get numAgreeUrls
   *
   * @return numAgreeUrls
   **/
  @ApiModelProperty(value = "")

  public Integer getNumAgreeUrls() {
    return numAgreeUrls;
  }

  public void setNumAgreeUrls(Integer numAgreeUrls) {
    this.numAgreeUrls = numAgreeUrls;
  }

  public PollerSummary start(Long start) {
    this.start = start;
    return this;
  }

  /**
   * The timestamp for when the poll started.
   *
   * @return start
   **/
  @ApiModelProperty(required = true, value = "The timestamp for when the poll started.")
  @NotNull

  public Long getStart() {
    return start;
  }

  public void setStart(Long start) {
    this.start = start;
  }

  public PollerSummary deadline(Long deadline) {
    this.deadline = deadline;
    return this;
  }

  /**
   * The deadline for voting in this poll.
   *
   * @return deadline
   **/
  @ApiModelProperty(required = true, value = "The deadline for voting in this poll.")
  @NotNull

  public Long getDeadline() {
    return deadline;
  }

  public void setDeadline(Long deadline) {
    this.deadline = deadline;
  }

  public PollerSummary pollEnd(Long pollEnd) {
    this.pollEnd = pollEnd;
    return this;
  }

  /**
   * The time at which the poll ended.
   *
   * @return pollEnd
   **/
  @ApiModelProperty(value = "The time at which the poll ended.")

  public Long getPollEnd() {
    return pollEnd;
  }

  public void setPollEnd(Long pollEnd) {
    this.pollEnd = pollEnd;
  }

  public PollerSummary pollKey(String pollKey) {
    this.pollKey = pollKey;
    return this;
  }

  /**
   * Key generated by poll manager when poll was created.
   *
   * @return pollKey
   **/
  @ApiModelProperty(required = true, value = "Key generated by poll manager when poll was created.")
  @NotNull

  public String getPollKey() {
    return pollKey;
  }

  public void setPollKey(String pollKey) {
    this.pollKey = pollKey;
  }

  public PollerSummary detailLink(LinkDesc detailLink) {
    this.detailLink = detailLink;
    return this;
  }

  /**
   * Get detailLink
   *
   * @return detailLink
   **/
  @ApiModelProperty(value = "")

  @Valid

  public LinkDesc getDetailLink() {
    return detailLink;
  }

  public void setDetailLink(LinkDesc detailLink) {
    this.detailLink = detailLink;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PollerSummary pollerSummary = (PollerSummary) o;
    return Objects.equals(this.auId, pollerSummary.auId) &&
        Objects.equals(this.variant, pollerSummary.variant) &&
        Objects.equals(this.status, pollerSummary.status) &&
        Objects.equals(this.participants, pollerSummary.participants) &&
        Objects.equals(this.numTalliedUrls, pollerSummary.numTalliedUrls) &&
        Objects.equals(this.numHashErrors, pollerSummary.numHashErrors) &&
        Objects.equals(this.numCompletedRepairs, pollerSummary.numCompletedRepairs) &&
        Objects.equals(this.numAgreeUrls, pollerSummary.numAgreeUrls) &&
        Objects.equals(this.start, pollerSummary.start) &&
        Objects.equals(this.deadline, pollerSummary.deadline) &&
        Objects.equals(this.pollEnd, pollerSummary.pollEnd) &&
        Objects.equals(this.pollKey, pollerSummary.pollKey) &&
        Objects.equals(this.detailLink, pollerSummary.detailLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(auId, variant, status, participants, numTalliedUrls, numHashErrors,
        numCompletedRepairs, numAgreeUrls, start, deadline, pollEnd, pollKey, detailLink);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PollerSummary {\n");

    sb.append("    auId: ").append(toIndentedString(auId)).append("\n");
    sb.append("    variant: ").append(toIndentedString(variant)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    participants: ").append(toIndentedString(participants)).append("\n");
    sb.append("    numTalliedUrls: ").append(toIndentedString(numTalliedUrls)).append("\n");
    sb.append("    numHashErrors: ").append(toIndentedString(numHashErrors)).append("\n");
    sb.append("    numCompletedRepairs: ").append(toIndentedString(numCompletedRepairs))
        .append("\n");
    sb.append("    numAgreeUrls: ").append(toIndentedString(numAgreeUrls)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    deadline: ").append(toIndentedString(deadline)).append("\n");
    sb.append("    pollEnd: ").append(toIndentedString(pollEnd)).append("\n");
    sb.append("    pollKey: ").append(toIndentedString(pollKey)).append("\n");
    sb.append("    detailLink: ").append(toIndentedString(detailLink)).append("\n");
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

