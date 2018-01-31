package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A Poll Request object for used to define a poll. If If lower &#x3D; “.” this is a singleContentNode.
 */
@ApiModel(description = "A Poll Request object for used to define a poll. If If lower = “.” this is a singleContentNode.")
@Validated

public class PollReq   {
  @JsonProperty("auid")
  private String auid = null;

  @JsonProperty("url")
  private String url = "null";

  @JsonProperty("upper")
  private String upper = "null";

  @JsonProperty("lower")
  private String lower = "null";

  @JsonProperty("pollType")
  private Integer pollType = 3;

  public PollReq auid(String auid) {
    this.auid = auid;
    return this;
  }

  /**
   * The auid which defines the poll
   * @return auid
  **/
  @ApiModelProperty(required = true, value = "The auid which defines the poll")
  @NotNull


  public String getAuid() {
    return auid;
  }

  public void setAuid(String auid) {
    this.auid = auid;
  }

  public PollReq url(String url) {
    this.url = url;
    return this;
  }

  /**
   * The url from which this poll starts, if null this is the au's base url.
   * @return url
  **/
  @ApiModelProperty(required = true, value = "The url from which this poll starts, if null this is the au's base url.")
  @NotNull


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public PollReq upper(String upper) {
    this.upper = upper;
    return this;
  }

  /**
   * upper boundary of the prefix range, inclusive.  If null, the range is unbounded at the top.
   * @return upper
  **/
  @ApiModelProperty(required = true, value = "upper boundary of the prefix range, inclusive.  If null, the range is unbounded at the top.")
  @NotNull


  public String getUpper() {
    return upper;
  }

  public void setUpper(String upper) {
    this.upper = upper;
  }

  public PollReq lower(String lower) {
    this.lower = lower;
    return this;
  }

  /**
   * lower boundary of the prefix range, inclusive.  If null, the range is unbounded at the bottom
   * @return lower
  **/
  @ApiModelProperty(required = true, value = "lower boundary of the prefix range, inclusive.  If null, the range is unbounded at the bottom")
  @NotNull


  public String getLower() {
    return lower;
  }

  public void setLower(String lower) {
    this.lower = lower;
  }

  public PollReq pollType(Integer pollType) {
    this.pollType = pollType;
    return this;
  }

  /**
   * The type of poll to run
   * minimum: 3
   * @return pollType
  **/
  @ApiModelProperty(example = "3", value = "The type of poll to run")

@Min(3)
  public Integer getPollType() {
    return pollType;
  }

  public void setPollType(Integer pollType) {
    this.pollType = pollType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PollReq pollReq = (PollReq) o;
    return Objects.equals(this.auid, pollReq.auid) &&
        Objects.equals(this.url, pollReq.url) &&
        Objects.equals(this.upper, pollReq.upper) &&
        Objects.equals(this.lower, pollReq.lower) &&
        Objects.equals(this.pollType, pollReq.pollType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(auid, url, upper, lower, pollType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PollReq {\n");
    
    sb.append("    auid: ").append(toIndentedString(auid)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    upper: ").append(toIndentedString(upper)).append("\n");
    sb.append("    lower: ").append(toIndentedString(lower)).append("\n");
    sb.append("    pollType: ").append(toIndentedString(pollType)).append("\n");
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

