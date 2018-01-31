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
 * structure used to define a repair source for url. if the source is null than repair from publisher
 */
@ApiModel(description = "structure used to define a repair source for url. if the source is null than repair from publisher")
@Validated

public class Repair   {
  @JsonProperty("uri")
  private String uri = null;

  @JsonProperty("repairFrom")
  private String repairFrom = null;

  public Repair uri(String uri) {
    this.uri = uri;
    return this;
  }

  /**
   * The uri to repair
   * @return uri
  **/
  @ApiModelProperty(required = true, value = "The uri to repair")
  @NotNull


  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public Repair repairFrom(String repairFrom) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Repair repair = (Repair) o;
    return Objects.equals(this.uri, repair.uri) &&
        Objects.equals(this.repairFrom, repair.repairFrom);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri, repairFrom);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Repair {\n");
    
    sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
    sb.append("    repairFrom: ").append(toIndentedString(repairFrom)).append("\n");
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

