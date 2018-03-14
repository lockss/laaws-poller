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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * This general error structure is used throughout this API.
 */
@ApiModel(description = "This general error structure is used throughout this API.")
@Validated

public class ErrorDesc {

  @JsonProperty("code")
  private Integer code = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("reasonPhrase")
  private String reasonPhrase = null;

  public ErrorDesc code(Integer code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * minimum: 400
   * maximum: 599
   *
   * @return code
   **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Min(400)
  @Max(599)
  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public ErrorDesc description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   *
   * @return description
   **/
  @ApiModelProperty(example = "Bad query parameter [$size]: Invalid integer value [abc]", value = "")

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ErrorDesc reasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
    return this;
  }

  /**
   * Get reasonPhrase
   *
   * @return reasonPhrase
   **/
  @ApiModelProperty(example = "Bad Request", value = "")

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public void setReasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErrorDesc errorDesc = (ErrorDesc) o;
    return Objects.equals(this.code, errorDesc.code) &&
        Objects.equals(this.description, errorDesc.description) &&
        Objects.equals(this.reasonPhrase, errorDesc.reasonPhrase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, description, reasonPhrase);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorDesc {\n");

    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    reasonPhrase: ").append(toIndentedString(reasonPhrase)).append("\n");
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

