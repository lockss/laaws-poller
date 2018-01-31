package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.lockss.laaws.poller.model.PageInfo;
import org.lockss.laaws.poller.model.Poll;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A page of poll resutls used to page in the poller info.
 */
@ApiModel(description = "A page of poll resutls used to page in the poller info.")
@Validated

public class PollPageInfo   {
  @JsonProperty("polls")
  @Valid
  private List<Poll> polls = new ArrayList<>();

  @JsonProperty("pageInfo")
  private PageInfo pageInfo = null;

  public PollPageInfo polls(List<Poll> polls) {
    this.polls = polls;
    return this;
  }

  public PollPageInfo addPollsItem(Poll pollsItem) {
    this.polls.add(pollsItem);
    return this;
  }

  /**
   * The list of polls for the current page.
   * @return polls
  **/
  @ApiModelProperty(required = true, value = "The list of polls for the current page.")
  @NotNull

  @Valid

  public List<Poll> getPolls() {
    return polls;
  }

  public void setPolls(List<Poll> polls) {
    this.polls = polls;
  }

  public PollPageInfo pageInfo(PageInfo pageInfo) {
    this.pageInfo = pageInfo;
    return this;
  }

  /**
   * Get pageInfo
   * @return pageInfo
  **/
  @ApiModelProperty(value = "")

  @Valid

  public PageInfo getPageInfo() {
    return pageInfo;
  }

  public void setPageInfo(PageInfo pageInfo) {
    this.pageInfo = pageInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PollPageInfo pollPageInfo = (PollPageInfo) o;
    return Objects.equals(this.polls, pollPageInfo.polls) &&
        Objects.equals(this.pageInfo, pollPageInfo.pageInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(polls, pageInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PollPageInfo {\n");
    
    sb.append("    polls: ").append(toIndentedString(polls)).append("\n");
    sb.append("    pageInfo: ").append(toIndentedString(pageInfo)).append("\n");
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

