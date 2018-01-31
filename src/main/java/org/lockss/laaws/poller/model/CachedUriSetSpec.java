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
 * A set of urls bounded by  upper and lower restraints.  If lower &#x3D; \&quot;.\&quot; this is a singleContentNode. If lower is null then start at the root url, if upper is null end with the last.
 */
@ApiModel(description = "A set of urls bounded by  upper and lower restraints.  If lower = \".\" this is a singleContentNode. If lower is null then start at the root url, if upper is null end with the last.")
@Validated

public class CachedUriSetSpec   {
  @JsonProperty("urlPrefix")
  private String urlPrefix = null;

  @JsonProperty("lowerBound")
  private String lowerBound = null;

  @JsonProperty("upperBound")
  private String upperBound = null;

  public CachedUriSetSpec urlPrefix(String urlPrefix) {
    this.urlPrefix = urlPrefix;
    return this;
  }

  /**
   * The base which roots the lower and upper bound
   * @return urlPrefix
  **/
  @ApiModelProperty(required = true, value = "The base which roots the lower and upper bound")
  @NotNull


  public String getUrlPrefix() {
    return urlPrefix;
  }

  public void setUrlPrefix(String urlPrefix) {
    this.urlPrefix = urlPrefix;
  }

  public CachedUriSetSpec lowerBound(String lowerBound) {
    this.lowerBound = lowerBound;
    return this;
  }

  /**
   * lower bound of the prefix range, inclusive.
   * @return lowerBound
  **/
  @ApiModelProperty(value = "lower bound of the prefix range, inclusive.")


  public String getLowerBound() {
    return lowerBound;
  }

  public void setLowerBound(String lowerBound) {
    this.lowerBound = lowerBound;
  }

  public CachedUriSetSpec upperBound(String upperBound) {
    this.upperBound = upperBound;
    return this;
  }

  /**
   * upper bound of prefix range, inclusive.
   * @return upperBound
  **/
  @ApiModelProperty(value = "upper bound of prefix range, inclusive.")


  public String getUpperBound() {
    return upperBound;
  }

  public void setUpperBound(String upperBound) {
    this.upperBound = upperBound;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CachedUriSetSpec cachedUriSetSpec = (CachedUriSetSpec) o;
    return Objects.equals(this.urlPrefix, cachedUriSetSpec.urlPrefix) &&
        Objects.equals(this.lowerBound, cachedUriSetSpec.lowerBound) &&
        Objects.equals(this.upperBound, cachedUriSetSpec.upperBound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(urlPrefix, lowerBound, upperBound);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CachedUriSetSpec {\n");
    
    sb.append("    urlPrefix: ").append(toIndentedString(urlPrefix)).append("\n");
    sb.append("    lowerBound: ").append(toIndentedString(lowerBound)).append("\n");
    sb.append("    upperBound: ").append(toIndentedString(upperBound)).append("\n");
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

