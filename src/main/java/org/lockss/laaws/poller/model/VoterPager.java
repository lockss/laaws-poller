package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.lockss.laaws.poller.model.PageDesc;
import org.lockss.laaws.poller.model.VoterSummary;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A page of voter poll summary.
 */
@ApiModel(description = "A page of voter poll summary.")
@Validated

public class VoterPager   {
  @JsonProperty("pageDesc")
  private PageDesc pageDesc = null;

  @JsonProperty("polls")
  @Valid
  private List<VoterSummary> polls = null;

  public VoterPager pageDesc(PageDesc pageDesc) {
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

