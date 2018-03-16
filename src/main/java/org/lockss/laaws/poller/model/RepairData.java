package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * structure used to define a repair source for url. if the source is null than repair from publisher
 */
@ApiModel(description = "structure used to define a repair source for url. if the source is null than repair from publisher")
@Validated

public class RepairData   {
  @JsonProperty("repairUrl")
  private String repairUrl = null;

  @JsonProperty("repairFrom")
  private String repairFrom = null;

  /**
   * The status of this repair
   */
  public enum ResultEnum {
    NO_QUORUM("No Quorum"),
    
    TOO_CLOSE("Too Close"),
    
    LOST("Lost"),
    
    LOST_POLLER_ONLY_BLOCK("Lost - Poller-only Block"),
    
    LOST_VOTER_ONLY_BLOCK("Lost - Voter-only Block"),
    
    WON("Won");

    private String value;

    ResultEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ResultEnum fromValue(String text) {
      for (ResultEnum b : ResultEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("result")
  private ResultEnum result = null;

  public RepairData repairUrl(String repairUrl) {
    this.repairUrl = repairUrl;
    return this;
  }

  /**
   * The url to repair
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

  public RepairData result(ResultEnum result) {
    this.result = result;
    return this;
  }

  /**
   * The status of this repair
   * @return result
  **/
  @ApiModelProperty(value = "The status of this repair")


  public ResultEnum getResult() {
    return result;
  }

  public void setResult(ResultEnum result) {
    this.result = result;
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
        Objects.equals(this.result, repairData.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repairUrl, repairFrom, result);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RepairData {\n");
    
    sb.append("    repairUrl: ").append(toIndentedString(repairUrl)).append("\n");
    sb.append("    repairFrom: ").append(toIndentedString(repairFrom)).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
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

