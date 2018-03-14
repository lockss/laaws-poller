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
 * The tally for the current poll.
 */
@ApiModel(description = "The tally for the current poll.")
@Validated

public class TallyData {

  @JsonProperty("numAgree")
  private Integer numAgree = null;

  @JsonProperty("agreeLink")
  private String agreeLink = null;

  @JsonProperty("numDisagree")
  private Integer numDisagree = null;

  @JsonProperty("disagreeLink")
  private String disagreeLink = null;

  @JsonProperty("numTooClose")
  private Integer numTooClose = null;

  @JsonProperty("tooCloseLink")
  private String tooCloseLink = null;

  @JsonProperty("numNoQuorum")
  private Integer numNoQuorum = null;

  @JsonProperty("noQuorumLink")
  private String noQuorumLink = null;

  @JsonProperty("numError")
  private Integer numError = null;

  @JsonProperty("errorLink")
  private String errorLink = null;

  @JsonProperty("wtAgreed")
  private Float wtAgreed = 0.0f;

  @JsonProperty("wtDisagreed")
  private Float wtDisagreed = 0.0f;

  @JsonProperty("wtTooClose")
  private Float wtTooClose = 0.0f;

  @JsonProperty("wtNoQuorum")
  private Float wtNoQuorum = 0.0f;

  public TallyData numAgree(Integer numAgree) {
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

  public TallyData agreeLink(String agreeLink) {
    this.agreeLink = agreeLink;
    return this;
  }

  /**
   * Link to urls with agreement.
   *
   * @return agreeLink
   **/
  @ApiModelProperty(value = "Link to urls with agreement.")

  public String getAgreeLink() {
    return agreeLink;
  }

  public void setAgreeLink(String agreeLink) {
    this.agreeLink = agreeLink;
  }

  public TallyData numDisagree(Integer numDisagree) {
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

  public TallyData disagreeLink(String disagreeLink) {
    this.disagreeLink = disagreeLink;
    return this;
  }

  /**
   * Link to urls for which there is disagreement
   *
   * @return disagreeLink
   **/
  @ApiModelProperty(value = "Link to urls for which there is disagreement")

  public String getDisagreeLink() {
    return disagreeLink;
  }

  public void setDisagreeLink(String disagreeLink) {
    this.disagreeLink = disagreeLink;
  }

  public TallyData numTooClose(Integer numTooClose) {
    this.numTooClose = numTooClose;
    return this;
  }

  /**
   * Get numTooClose
   *
   * @return numTooClose
   **/
  @ApiModelProperty(value = "")

  public Integer getNumTooClose() {
    return numTooClose;
  }

  public void setNumTooClose(Integer numTooClose) {
    this.numTooClose = numTooClose;
  }

  public TallyData tooCloseLink(String tooCloseLink) {
    this.tooCloseLink = tooCloseLink;
    return this;
  }

  /**
   * Link to urls which are too close to call.
   *
   * @return tooCloseLink
   **/
  @ApiModelProperty(value = "Link to urls which are too close to call.")

  public String getTooCloseLink() {
    return tooCloseLink;
  }

  public void setTooCloseLink(String tooCloseLink) {
    this.tooCloseLink = tooCloseLink;
  }

  public TallyData numNoQuorum(Integer numNoQuorum) {
    this.numNoQuorum = numNoQuorum;
    return this;
  }

  /**
   * Get numNoQuorum
   *
   * @return numNoQuorum
   **/
  @ApiModelProperty(value = "")

  public Integer getNumNoQuorum() {
    return numNoQuorum;
  }

  public void setNumNoQuorum(Integer numNoQuorum) {
    this.numNoQuorum = numNoQuorum;
  }

  public TallyData noQuorumLink(String noQuorumLink) {
    this.noQuorumLink = noQuorumLink;
    return this;
  }

  /**
   * Link to urls for which there is no quorum.
   *
   * @return noQuorumLink
   **/
  @ApiModelProperty(value = "Link to urls for which there is no quorum.")

  public String getNoQuorumLink() {
    return noQuorumLink;
  }

  public void setNoQuorumLink(String noQuorumLink) {
    this.noQuorumLink = noQuorumLink;
  }

  public TallyData numError(Integer numError) {
    this.numError = numError;
    return this;
  }

  /**
   * Get numError
   *
   * @return numError
   **/
  @ApiModelProperty(value = "")

  public Integer getNumError() {
    return numError;
  }

  public void setNumError(Integer numError) {
    this.numError = numError;
  }

  public TallyData errorLink(String errorLink) {
    this.errorLink = errorLink;
    return this;
  }

  /**
   * Link to urls for which there are errors.
   *
   * @return errorLink
   **/
  @ApiModelProperty(value = "Link to urls for which there are errors.")

  public String getErrorLink() {
    return errorLink;
  }

  public void setErrorLink(String errorLink) {
    this.errorLink = errorLink;
  }

  public TallyData wtAgreed(Float wtAgreed) {
    this.wtAgreed = wtAgreed;
    return this;
  }

  /**
   * The weighted sum agreed uris.
   *
   * @return wtAgreed
   **/
  @ApiModelProperty(value = "The weighted sum agreed uris.")

  public Float getWtAgreed() {
    return wtAgreed;
  }

  public void setWtAgreed(Float wtAgreed) {
    this.wtAgreed = wtAgreed;
  }

  public TallyData wtDisagreed(Float wtDisagreed) {
    this.wtDisagreed = wtDisagreed;
    return this;
  }

  /**
   * The weighted sum of disagree uris.
   *
   * @return wtDisagreed
   **/
  @ApiModelProperty(value = "The weighted sum of disagree uris.")

  public Float getWtDisagreed() {
    return wtDisagreed;
  }

  public void setWtDisagreed(Float wtDisagreed) {
    this.wtDisagreed = wtDisagreed;
  }

  public TallyData wtTooClose(Float wtTooClose) {
    this.wtTooClose = wtTooClose;
    return this;
  }

  /**
   * The sum of the tooClose uris.
   *
   * @return wtTooClose
   **/
  @ApiModelProperty(value = "The sum of the tooClose uris.")

  public Float getWtTooClose() {
    return wtTooClose;
  }

  public void setWtTooClose(Float wtTooClose) {
    this.wtTooClose = wtTooClose;
  }

  public TallyData wtNoQuorum(Float wtNoQuorum) {
    this.wtNoQuorum = wtNoQuorum;
    return this;
  }

  /**
   * The weighted sum of NoQuorum uris.
   *
   * @return wtNoQuorum
   **/
  @ApiModelProperty(value = "The weighted sum of NoQuorum uris.")

  public Float getWtNoQuorum() {
    return wtNoQuorum;
  }

  public void setWtNoQuorum(Float wtNoQuorum) {
    this.wtNoQuorum = wtNoQuorum;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TallyData tallyData = (TallyData) o;
    return Objects.equals(this.numAgree, tallyData.numAgree) &&
        Objects.equals(this.agreeLink, tallyData.agreeLink) &&
        Objects.equals(this.numDisagree, tallyData.numDisagree) &&
        Objects.equals(this.disagreeLink, tallyData.disagreeLink) &&
        Objects.equals(this.numTooClose, tallyData.numTooClose) &&
        Objects.equals(this.tooCloseLink, tallyData.tooCloseLink) &&
        Objects.equals(this.numNoQuorum, tallyData.numNoQuorum) &&
        Objects.equals(this.noQuorumLink, tallyData.noQuorumLink) &&
        Objects.equals(this.numError, tallyData.numError) &&
        Objects.equals(this.errorLink, tallyData.errorLink) &&
        Objects.equals(this.wtAgreed, tallyData.wtAgreed) &&
        Objects.equals(this.wtDisagreed, tallyData.wtDisagreed) &&
        Objects.equals(this.wtTooClose, tallyData.wtTooClose) &&
        Objects.equals(this.wtNoQuorum, tallyData.wtNoQuorum);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numAgree, agreeLink, numDisagree, disagreeLink, numTooClose, tooCloseLink,
        numNoQuorum, noQuorumLink, numError, errorLink, wtAgreed, wtDisagreed, wtTooClose,
        wtNoQuorum);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TallyData {\n");

    sb.append("    numAgree: ").append(toIndentedString(numAgree)).append("\n");
    sb.append("    agreeLink: ").append(toIndentedString(agreeLink)).append("\n");
    sb.append("    numDisagree: ").append(toIndentedString(numDisagree)).append("\n");
    sb.append("    disagreeLink: ").append(toIndentedString(disagreeLink)).append("\n");
    sb.append("    numTooClose: ").append(toIndentedString(numTooClose)).append("\n");
    sb.append("    tooCloseLink: ").append(toIndentedString(tooCloseLink)).append("\n");
    sb.append("    numNoQuorum: ").append(toIndentedString(numNoQuorum)).append("\n");
    sb.append("    noQuorumLink: ").append(toIndentedString(noQuorumLink)).append("\n");
    sb.append("    numError: ").append(toIndentedString(numError)).append("\n");
    sb.append("    errorLink: ").append(toIndentedString(errorLink)).append("\n");
    sb.append("    wtAgreed: ").append(toIndentedString(wtAgreed)).append("\n");
    sb.append("    wtDisagreed: ").append(toIndentedString(wtDisagreed)).append("\n");
    sb.append("    wtTooClose: ").append(toIndentedString(wtTooClose)).append("\n");
    sb.append("    wtNoQuorum: ").append(toIndentedString(wtNoQuorum)).append("\n");
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

