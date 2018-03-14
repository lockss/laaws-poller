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
import org.springframework.validation.annotation.Validated;

/**
 * Description of the RepairQueue.
 */
@ApiModel(description = "Description of the RepairQueue.")
@Validated

public class RepairQueue {

  @JsonProperty("numPending")
  private Integer numPending = null;

  @JsonProperty("pendingLink")
  private String pendingLink = null;

  @JsonProperty("numActive")
  private Integer numActive = null;

  @JsonProperty("activeLink")
  private String activeLink = null;

  @JsonProperty("numCompleted")
  private Integer numCompleted = null;

  @JsonProperty("completedLink")
  private String completedLink = null;

  public RepairQueue numPending(Integer numPending) {
    this.numPending = numPending;
    return this;
  }

  /**
   * The number of pending repairs
   *
   * @return numPending
   **/
  @ApiModelProperty(value = "The number of pending repairs")

  public Integer getNumPending() {
    return numPending;
  }

  public void setNumPending(Integer numPending) {
    this.numPending = numPending;
  }

  public RepairQueue pendingLink(String pendingLink) {
    this.pendingLink = pendingLink;
    return this;
  }

  /**
   * A link to the pending repairs.
   *
   * @return pendingLink
   **/
  @ApiModelProperty(value = "A link to the pending repairs.")

  public String getPendingLink() {
    return pendingLink;
  }

  public void setPendingLink(String pendingLink) {
    this.pendingLink = pendingLink;
  }

  public RepairQueue numActive(Integer numActive) {
    this.numActive = numActive;
    return this;
  }

  /**
   * The number of active repairs
   *
   * @return numActive
   **/
  @ApiModelProperty(value = "The number of active repairs")

  public Integer getNumActive() {
    return numActive;
  }

  public void setNumActive(Integer numActive) {
    this.numActive = numActive;
  }

  public RepairQueue activeLink(String activeLink) {
    this.activeLink = activeLink;
    return this;
  }

  /**
   * A link to the active repairs.
   *
   * @return activeLink
   **/
  @ApiModelProperty(value = "A link to the active repairs.")

  public String getActiveLink() {
    return activeLink;
  }

  public void setActiveLink(String activeLink) {
    this.activeLink = activeLink;
  }

  public RepairQueue numCompleted(Integer numCompleted) {
    this.numCompleted = numCompleted;
    return this;
  }

  /**
   * The number of completed repairs
   *
   * @return numCompleted
   **/
  @ApiModelProperty(value = "The number of completed repairs")

  public Integer getNumCompleted() {
    return numCompleted;
  }

  public void setNumCompleted(Integer numCompleted) {
    this.numCompleted = numCompleted;
  }

  public RepairQueue completedLink(String completedLink) {
    this.completedLink = completedLink;
    return this;
  }

  /**
   * A link to the  completed repairs.
   *
   * @return completedLink
   **/
  @ApiModelProperty(value = "A link to the  completed repairs.")

  public String getCompletedLink() {
    return completedLink;
  }

  public void setCompletedLink(String completedLink) {
    this.completedLink = completedLink;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RepairQueue repairQueue = (RepairQueue) o;
    return Objects.equals(this.numPending, repairQueue.numPending) &&
        Objects.equals(this.pendingLink, repairQueue.pendingLink) &&
        Objects.equals(this.numActive, repairQueue.numActive) &&
        Objects.equals(this.activeLink, repairQueue.activeLink) &&
        Objects.equals(this.numCompleted, repairQueue.numCompleted) &&
        Objects.equals(this.completedLink, repairQueue.completedLink);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(numPending, pendingLink, numActive, activeLink, numCompleted, completedLink);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RepairQueue {\n");

    sb.append("    numPending: ").append(toIndentedString(numPending)).append("\n");
    sb.append("    pendingLink: ").append(toIndentedString(pendingLink)).append("\n");
    sb.append("    numActive: ").append(toIndentedString(numActive)).append("\n");
    sb.append("    activeLink: ").append(toIndentedString(activeLink)).append("\n");
    sb.append("    numCompleted: ").append(toIndentedString(numCompleted)).append("\n");
    sb.append("    completedLink: ").append(toIndentedString(completedLink)).append("\n");
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

