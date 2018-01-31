package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.lockss.laaws.poller.model.Repair;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * The collection of repairers for this poll.
 */
@ApiModel(description = "The collection of repairers for this poll.")
@Validated

public class PollRepairQueue   {
  @JsonProperty("pendingRepairs")
  @Valid
  private List<Repair> pendingRepairs = new ArrayList<>();

  @JsonProperty("activeRepairs")
  @Valid
  private List<Repair> activeRepairs = new ArrayList<>();

  @JsonProperty("completedRepairs")
  @Valid
  private List<Repair> completedRepairs = new ArrayList<>();

  public PollRepairQueue pendingRepairs(List<Repair> pendingRepairs) {
    this.pendingRepairs = pendingRepairs;
    return this;
  }

  public PollRepairQueue addPendingRepairsItem(Repair pendingRepairsItem) {
    this.pendingRepairs.add(pendingRepairsItem);
    return this;
  }

  /**
   * The repairs which are pending
   * @return pendingRepairs
  **/
  @ApiModelProperty(required = true, value = "The repairs which are pending")
  @NotNull

  @Valid

  public List<Repair> getPendingRepairs() {
    return pendingRepairs;
  }

  public void setPendingRepairs(List<Repair> pendingRepairs) {
    this.pendingRepairs = pendingRepairs;
  }

  public PollRepairQueue activeRepairs(List<Repair> activeRepairs) {
    this.activeRepairs = activeRepairs;
    return this;
  }

  public PollRepairQueue addActiveRepairsItem(Repair activeRepairsItem) {
    this.activeRepairs.add(activeRepairsItem);
    return this;
  }

  /**
   * The repairs which are active
   * @return activeRepairs
  **/
  @ApiModelProperty(required = true, value = "The repairs which are active")
  @NotNull

  @Valid

  public List<Repair> getActiveRepairs() {
    return activeRepairs;
  }

  public void setActiveRepairs(List<Repair> activeRepairs) {
    this.activeRepairs = activeRepairs;
  }

  public PollRepairQueue completedRepairs(List<Repair> completedRepairs) {
    this.completedRepairs = completedRepairs;
    return this;
  }

  public PollRepairQueue addCompletedRepairsItem(Repair completedRepairsItem) {
    this.completedRepairs.add(completedRepairsItem);
    return this;
  }

  /**
   * The completed repairs
   * @return completedRepairs
  **/
  @ApiModelProperty(required = true, value = "The completed repairs")
  @NotNull

  @Valid

  public List<Repair> getCompletedRepairs() {
    return completedRepairs;
  }

  public void setCompletedRepairs(List<Repair> completedRepairs) {
    this.completedRepairs = completedRepairs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PollRepairQueue pollRepairQueue = (PollRepairQueue) o;
    return Objects.equals(this.pendingRepairs, pollRepairQueue.pendingRepairs) &&
        Objects.equals(this.activeRepairs, pollRepairQueue.activeRepairs) &&
        Objects.equals(this.completedRepairs, pollRepairQueue.completedRepairs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pendingRepairs, activeRepairs, completedRepairs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PollRepairQueue {\n");
    
    sb.append("    pendingRepairs: ").append(toIndentedString(pendingRepairs)).append("\n");
    sb.append("    activeRepairs: ").append(toIndentedString(activeRepairs)).append("\n");
    sb.append("    completedRepairs: ").append(toIndentedString(completedRepairs)).append("\n");
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

