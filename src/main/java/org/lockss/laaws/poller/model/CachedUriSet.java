package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.lockss.laaws.poller.model.CachedUriSetSpec;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * The set of files whose URIs match a list to be polled after filtering based on set of url-regex pairs within an AU.
 */
@ApiModel(description = "The set of files whose URIs match a list to be polled after filtering based on set of url-regex pairs within an AU.")
@Validated

public class CachedUriSet   {
  @JsonProperty("auId")
  private String auId = null;

  @JsonProperty("spec")
  private CachedUriSetSpec spec = null;

  public CachedUriSet auId(String auId) {
    this.auId = auId;
    return this;
  }

  /**
   * The owning archival unit string
   * @return auId
  **/
  @ApiModelProperty(required = true, value = "The owning archival unit string")
  @NotNull


  public String getAuId() {
    return auId;
  }

  public void setAuId(String auId) {
    this.auId = auId;
  }

  public CachedUriSet spec(CachedUriSetSpec spec) {
    this.spec = spec;
    return this;
  }

  /**
   * Get spec
   * @return spec
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public CachedUriSetSpec getSpec() {
    return spec;
  }

  public void setSpec(CachedUriSetSpec spec) {
    this.spec = spec;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CachedUriSet cachedUriSet = (CachedUriSet) o;
    return Objects.equals(this.auId, cachedUriSet.auId) &&
        Objects.equals(this.spec, cachedUriSet.spec);
  }

  @Override
  public int hashCode() {
    return Objects.hash(auId, spec);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CachedUriSet {\n");
    
    sb.append("    auId: ").append(toIndentedString(auId)).append("\n");
    sb.append("    spec: ").append(toIndentedString(spec)).append("\n");
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

