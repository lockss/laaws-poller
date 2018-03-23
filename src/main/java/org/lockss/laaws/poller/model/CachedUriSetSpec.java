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
 * A set of urls bounded by  upper and lower restraints.  If lower &#x3D; \&quot;.\&quot; this is a singleContentNode. If lower is null then start at the root url, if upper is null end with the last.
 */
@ApiModel(description = "A set of urls bounded by  upper and lower restraints.  If lower = \".\" this is a singleContentNode. If lower is null then start at the root url, if upper is null end with the last.")
@Validated

public class CachedUriSetSpec {

  @JsonProperty("urlPrefix")
  private String urlPrefix = null;

  @JsonProperty("lowerBound")
  private String lowerBound = null;

  @JsonProperty("upperBound")
  private String upperBound = null;

  public CachedUriSetSpec urlPrefix(String urlPrefix) {
    this.urlPrefix = urlPrefix;
    return this;
  }

  /**
   * The base which roots the lower and upper bound
   *
   * @return urlPrefix
   **/
  @ApiModelProperty(required = true, value = "The base which roots the lower and upper bound")
  @NotNull

  public String getUrlPrefix() {
    return urlPrefix;
  }

  public void setUrlPrefix(String urlPrefix) {
    this.urlPrefix = urlPrefix;
  }

  public CachedUriSetSpec lowerBound(String lowerBound) {
    this.lowerBound = lowerBound;
    return this;
  }

  /**
   * lower bound of the prefix range, inclusive.
   *
   * @return lowerBound
   **/
  @ApiModelProperty(value = "lower bound of the prefix range, inclusive.")

  public String getLowerBound() {
    return lowerBound;
  }

  public void setLowerBound(String lowerBound) {
    this.lowerBound = lowerBound;
  }

  public CachedUriSetSpec upperBound(String upperBound) {
    this.upperBound = upperBound;
    return this;
  }

  /**
   * upper bound of prefix range, inclusive.
   *
   * @return upperBound
   **/
  @ApiModelProperty(value = "upper bound of prefix range, inclusive.")

  public String getUpperBound() {
    return upperBound;
  }

  public void setUpperBound(String upperBound) {
    this.upperBound = upperBound;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CachedUriSetSpec cachedUriSetSpec = (CachedUriSetSpec) o;
    return Objects.equals(this.urlPrefix, cachedUriSetSpec.urlPrefix) &&
        Objects.equals(this.lowerBound, cachedUriSetSpec.lowerBound) &&
        Objects.equals(this.upperBound, cachedUriSetSpec.upperBound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(urlPrefix, lowerBound, upperBound);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CachedUriSetSpec {\n");

    sb.append("    urlPrefix: ").append(toIndentedString(urlPrefix)).append("\n");
    sb.append("    lowerBound: ").append(toIndentedString(lowerBound)).append("\n");
    sb.append("    upperBound: ").append(toIndentedString(upperBound)).append("\n");
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

