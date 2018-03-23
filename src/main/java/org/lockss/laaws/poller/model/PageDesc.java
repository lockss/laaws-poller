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
 * The information needed to page in a long list of data
 */
@ApiModel(description = "The information needed to page in a long list of data")
@Validated

public class PageDesc {

  @JsonProperty("page")
  private Integer page = null;

  @JsonProperty("size")
  private Integer size = null;

  @JsonProperty("total")
  private Integer total = null;

  @JsonProperty("prevPage")
  private String prevPage = null;

  @JsonProperty("nextPage")
  private String nextPage = null;

  public PageDesc page(Integer page) {
    this.page = page;
    return this;
  }

  /**
   * The page number
   *
   * @return page
   **/
  @ApiModelProperty(example = "10", required = true, value = "The page number")
  @NotNull

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public PageDesc size(Integer size) {
    this.size = size;
    return this;
  }

  /**
   * The size or number of elements on a page
   *
   * @return size
   **/
  @ApiModelProperty(example = "5", required = true, value = "The size or number of elements on a page")
  @NotNull

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public PageDesc total(Integer total) {
    this.total = total;
    return this;
  }

  /**
   * The mTotal number of elements.
   *
   * @return total
   **/
  @ApiModelProperty(example = "150", required = true, value = "The mTotal number of elements.")
  @NotNull

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public PageDesc prevPage(String prevPage) {
    this.prevPage = prevPage;
    return this;
  }

  /**
   * The url of the prev page of results or null
   *
   * @return prevPage
   **/
  @ApiModelProperty(value = "The url of the prev page of results or null")

  public String getPrevPage() {
    return prevPage;
  }

  public void setPrevPage(String prevPage) {
    this.prevPage = prevPage;
  }

  public PageDesc nextPage(String nextPage) {
    this.nextPage = nextPage;
    return this;
  }

  /**
   * The url to the next page of results or null.
   *
   * @return nextPage
   **/
  @ApiModelProperty(value = "The url to the next page of results or null.")

  public String getNextPage() {
    return nextPage;
  }

  public void setNextPage(String nextPage) {
    this.nextPage = nextPage;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PageDesc pageDesc = (PageDesc) o;
    return Objects.equals(this.page, pageDesc.page) &&
        Objects.equals(this.size, pageDesc.size) &&
        Objects.equals(this.total, pageDesc.total) &&
        Objects.equals(this.prevPage, pageDesc.prevPage) &&
        Objects.equals(this.nextPage, pageDesc.nextPage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(page, size, total, prevPage, nextPage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PageDesc {\n");

    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    prevPage: ").append(toIndentedString(prevPage)).append("\n");
    sb.append("    nextPage: ").append(toIndentedString(nextPage)).append("\n");
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

