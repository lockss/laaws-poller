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
 * LinkDesc
 */
@Validated

public class LinkDesc   {
  @JsonProperty("link")
  private String link = null;

  @JsonProperty("desc")
  private String desc = null;

  public LinkDesc link(String link) {
    this.link = link;
    return this;
  }

  /**
   * The actual link suitable for a standard GET request
   * @return link
  **/
  @ApiModelProperty(example = "http:www.example.com/v1/element", required = true, value = "The actual link suitable for a standard GET request")
  @NotNull


  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public LinkDesc desc(String desc) {
    this.desc = desc;
    return this;
  }

  /**
   * A description of this link
   * @return desc
  **/
  @ApiModelProperty(example = "pollerOnly", value = "A description of this link")


  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinkDesc linkDesc = (LinkDesc) o;
    return Objects.equals(this.link, linkDesc.link) &&
        Objects.equals(this.desc, linkDesc.desc);
  }

  @Override
  public int hashCode() {
    return Objects.hash(link, desc);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinkDesc {\n");
    
    sb.append("    link: ").append(toIndentedString(link)).append("\n");
    sb.append("    desc: ").append(toIndentedString(desc)).append("\n");
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

