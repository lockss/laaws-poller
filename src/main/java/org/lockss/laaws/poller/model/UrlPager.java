package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.lockss.laaws.poller.model.LinkDesc;
import org.lockss.laaws.poller.model.PageDesc;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A Pager for a list of urls found in poll details.
 */
@ApiModel(description = "A Pager for a list of urls found in poll details.")
@Validated

public class UrlPager   {
  @JsonProperty("pageDesc")
  private PageDesc pageDesc = null;

  @JsonProperty("urls")
  @Valid
  private List<LinkDesc> urls = null;

  public UrlPager pageDesc(PageDesc pageDesc) {
    this.pageDesc = pageDesc;
    return this;
  }

  /**
   * Get pageDesc
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

  public UrlPager urls(List<LinkDesc> urls) {
    this.urls = urls;
    return this;
  }

  public UrlPager addUrlsItem(LinkDesc urlsItem) {
    if (this.urls == null) {
      this.urls = new ArrayList<>();
    }
    this.urls.add(urlsItem);
    return this;
  }

  /**
   * The urls on this page.
   * @return urls
  **/
  @ApiModelProperty(value = "The urls on this page.")

  @Valid

  public List<LinkDesc> getUrls() {
    return urls;
  }

  public void setUrls(List<LinkDesc> urls) {
    this.urls = urls;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UrlPager urlPager = (UrlPager) o;
    return Objects.equals(this.pageDesc, urlPager.pageDesc) &&
        Objects.equals(this.urls, urlPager.urls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pageDesc, urls);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UrlPager {\n");
    
    sb.append("    pageDesc: ").append(toIndentedString(pageDesc)).append("\n");
    sb.append("    urls: ").append(toIndentedString(urls)).append("\n");
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

