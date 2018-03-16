package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.lockss.laaws.poller.model.CachedUriSetSpec;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * The Poller Services poll spec used to define a poll.
 */
@ApiModel(description = "The Poller Services poll spec used to define a poll.")
@Validated

public class PollDesc   {
  @JsonProperty("auId")
  private String auId = null;

  @JsonProperty("cuSetSpec")
  private CachedUriSetSpec cuSetSpec = null;

  @JsonProperty("pollType")
  private Integer pollType = null;

  @JsonProperty("protocol")
  private Integer protocol = null;

  @JsonProperty("pluginPollVersion")
  private String pluginPollVersion = null;

  /**
   * The V3 poll variation.
   */
  public enum VariantEnum {
    POR("PoR"),
    
    POP("PoP"),
    
    LOCAL("Local"),
    
    NOPOLL("NoPoll");

    private String value;

    VariantEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static VariantEnum fromValue(String text) {
      for (VariantEnum b : VariantEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("variant")
  private VariantEnum variant = null;

  @JsonProperty("modulus")
  private Integer modulus = null;

  public PollDesc auId(String auId) {
    this.auId = auId;
    return this;
  }

  /**
   * The id which defines the poll
   * @return auId
  **/
  @ApiModelProperty(required = true, value = "The id which defines the poll")
  @NotNull


  public String getAuId() {
    return auId;
  }

  public void setAuId(String auId) {
    this.auId = auId;
  }

  public PollDesc cuSetSpec(CachedUriSetSpec cuSetSpec) {
    this.cuSetSpec = cuSetSpec;
    return this;
  }

  /**
   * Get cuSetSpec
   * @return cuSetSpec
  **/
  @ApiModelProperty(value = "")

  @Valid

  public CachedUriSetSpec getCuSetSpec() {
    return cuSetSpec;
  }

  public void setCuSetSpec(CachedUriSetSpec cuSetSpec) {
    this.cuSetSpec = cuSetSpec;
  }

  public PollDesc pollType(Integer pollType) {
    this.pollType = pollType;
    return this;
  }

  /**
   * The type of poll to run. Only V3 is supported.
   * minimum: 3
   * @return pollType
  **/
  @ApiModelProperty(value = "The type of poll to run. Only V3 is supported.")

@Min(3)
  public Integer getPollType() {
    return pollType;
  }

  public void setPollType(Integer pollType) {
    this.pollType = pollType;
  }

  public PollDesc protocol(Integer protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * The version of polling protocol.
   * @return protocol
  **/
  @ApiModelProperty(value = "The version of polling protocol.")


  public Integer getProtocol() {
    return protocol;
  }

  public void setProtocol(Integer protocol) {
    this.protocol = protocol;
  }

  public PollDesc pluginPollVersion(String pluginPollVersion) {
    this.pluginPollVersion = pluginPollVersion;
    return this;
  }

  /**
   * The version of the polling features needed by the plugin.
   * @return pluginPollVersion
  **/
  @ApiModelProperty(value = "The version of the polling features needed by the plugin.")


  public String getPluginPollVersion() {
    return pluginPollVersion;
  }

  public void setPluginPollVersion(String pluginPollVersion) {
    this.pluginPollVersion = pluginPollVersion;
  }

  public PollDesc variant(VariantEnum variant) {
    this.variant = variant;
    return this;
  }

  /**
   * The V3 poll variation.
   * @return variant
  **/
  @ApiModelProperty(value = "The V3 poll variation.")


  public VariantEnum getVariant() {
    return variant;
  }

  public void setVariant(VariantEnum variant) {
    this.variant = variant;
  }

  public PollDesc modulus(Integer modulus) {
    this.modulus = modulus;
    return this;
  }

  /**
   * Poll on every 'n'th url.
   * @return modulus
  **/
  @ApiModelProperty(value = "Poll on every 'n'th url.")


  public Integer getModulus() {
    return modulus;
  }

  public void setModulus(Integer modulus) {
    this.modulus = modulus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PollDesc pollDesc = (PollDesc) o;
    return Objects.equals(this.auId, pollDesc.auId) &&
        Objects.equals(this.cuSetSpec, pollDesc.cuSetSpec) &&
        Objects.equals(this.pollType, pollDesc.pollType) &&
        Objects.equals(this.protocol, pollDesc.protocol) &&
        Objects.equals(this.pluginPollVersion, pollDesc.pluginPollVersion) &&
        Objects.equals(this.variant, pollDesc.variant) &&
        Objects.equals(this.modulus, pollDesc.modulus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(auId, cuSetSpec, pollType, protocol, pluginPollVersion, variant, modulus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PollDesc {\n");
    
    sb.append("    auId: ").append(toIndentedString(auId)).append("\n");
    sb.append("    cuSetSpec: ").append(toIndentedString(cuSetSpec)).append("\n");
    sb.append("    pollType: ").append(toIndentedString(pollType)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    pluginPollVersion: ").append(toIndentedString(pluginPollVersion)).append("\n");
    sb.append("    variant: ").append(toIndentedString(variant)).append("\n");
    sb.append("    modulus: ").append(toIndentedString(modulus)).append("\n");
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

