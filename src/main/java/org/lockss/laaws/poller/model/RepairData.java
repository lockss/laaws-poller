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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * structure used to define a repair source for url. if the source is null than repair from publisher
 */
@ApiModel(description = "structure used to define a repair source for url. if the source is null than repair from publisher")
@Validated

public class RepairData {

  @JsonProperty("repairUrl")
  private String repairUrl = null;

  @JsonProperty("repairFrom")
  private String repairFrom = null;

  /**
   * The status of this repair
   */
  public enum StatusEnum {
    ACTIVE("active"),

    COMPLETED("completed"),

    PENDING("pending");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("status")
  private StatusEnum status = null;

  public RepairData repairUrl(String repairUrl) {
    this.repairUrl = repairUrl;
    return this;
  }

  /**
   * The url to repair
   *
   * @return repairUrl
   **/
  @ApiModelProperty(required = true, value = "The url to repair")
  @NotNull

  public String getRepairUrl() {
    return repairUrl;
  }

  public void setRepairUrl(String repairUrl) {
    this.repairUrl = repairUrl;
  }

  public RepairData repairFrom(String repairFrom) {
    this.repairFrom = repairFrom;
    return this;
  }

  /**
   * The peer to repair from
   *
   * @return repairFrom
   **/
  @ApiModelProperty(required = true, value = "The peer to repair from")
  @NotNull

  public String getRepairFrom() {
    return repairFrom;
  }

  public void setRepairFrom(String repairFrom) {
    this.repairFrom = repairFrom;
  }

  public RepairData status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * The status of this repair
   *
   * @return status
   **/
  @ApiModelProperty(value = "The status of this repair")

  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RepairData repairData = (RepairData) o;
    return Objects.equals(this.repairUrl, repairData.repairUrl) &&
        Objects.equals(this.repairFrom, repairData.repairFrom) &&
        Objects.equals(this.status, repairData.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repairUrl, repairFrom, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RepairData {\n");

    sb.append("    repairUrl: ").append(toIndentedString(repairUrl)).append("\n");
    sb.append("    repairFrom: ").append(toIndentedString(repairFrom)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

