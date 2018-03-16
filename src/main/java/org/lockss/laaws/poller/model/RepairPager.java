package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.lockss.laaws.poller.model.PageDesc;
import org.lockss.laaws.poller.model.RepairData;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A Pager for a list of urls found in poll details.
 */
@ApiModel(description = "A Pager for a list of urls found in poll details.")
@Validated

public class RepairPager   {
  @JsonProperty("pageDesc")
  private PageDesc pageDesc = null;

  @JsonProperty("repairs")
  @Valid
  private List<RepairData> repairs = null;

  public RepairPager pageDesc(PageDesc pageDesc) {
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

  public RepairPager repairs(List<RepairData> repairs) {
    this.repairs = repairs;
    return this;
  }

  public RepairPager addRepairsItem(RepairData repairsItem) {
    if (this.repairs == null) {
      this.repairs = new ArrayList<>();
    }
    this.repairs.add(repairsItem);
    return this;
  }

  /**
   * The list of repair data on this page or null.
   * @return repairs
  **/
  @ApiModelProperty(value = "The list of repair data on this page or null.")

  @Valid

  public List<RepairData> getRepairs() {
    return repairs;
  }

  public void setRepairs(List<RepairData> repairs) {
    this.repairs = repairs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RepairPager repairPager = (RepairPager) o;
    return Objects.equals(this.pageDesc, repairPager.pageDesc) &&
        Objects.equals(this.repairs, repairPager.repairs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pageDesc, repairs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RepairPager {\n");
    
    sb.append("    pageDesc: ").append(toIndentedString(pageDesc)).append("\n");
    sb.append("    repairs: ").append(toIndentedString(repairs)).append("\n");
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

