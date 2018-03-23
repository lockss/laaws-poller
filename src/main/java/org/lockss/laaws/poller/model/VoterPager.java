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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * A page of voter poll summary.
 */
@ApiModel(description = "A page of voter poll summary.")
@Validated

public class VoterPager {

  @JsonProperty("pageDesc")
  private PageDesc pageDesc = null;

  @JsonProperty("polls")
  @Valid
  private List<VoterSummary> polls = new ArrayList<>();

  public VoterPager pageDesc(PageDesc pageDesc) {
    this.pageDesc = pageDesc;
    return this;
  }

  /**
   * Get pageDesc
   *
   * @return pageDesc
   **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public PageDesc getPageDesc() {
    return pageDesc;
  }

  public void setPageDesc(PageDesc pageDesc) {
    this.pageDesc = pageDesc;
  }

  public VoterPager polls(List<VoterSummary> polls) {
    this.polls = polls;
    return this;
  }

  public VoterPager addPollsItem(VoterSummary pollsItem) {
    if (this.polls == null) {
      this.polls = new ArrayList<>();
    }
    this.polls.add(pollsItem);
    return this;
  }

  /**
   * The list of polls for the current page or null
   *
   * @return polls
   **/
  @ApiModelProperty(value = "The list of polls for the current page or null")

  @Valid

  public List<VoterSummary> getPolls() {
    return polls;
  }

  public void setPolls(List<VoterSummary> polls) {
    this.polls = polls;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VoterPager voterPager = (VoterPager) o;
    return Objects.equals(this.pageDesc, voterPager.pageDesc) &&
        Objects.equals(this.polls, voterPager.polls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pageDesc, polls);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VoterPager {\n");

    sb.append("    pageDesc: ").append(toIndentedString(pageDesc)).append("\n");
    sb.append("    polls: ").append(toIndentedString(polls)).append("\n");
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

