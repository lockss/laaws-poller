package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.lockss.laaws.poller.model.CachedUriSet;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * The poll spec used to define this poll.
 */
@ApiModel(description = "The poll spec used to define this poll.")
@Validated

public class PollSpec   {
  @JsonProperty("cachedUriSet")
  private CachedUriSet cachedUriSet = null;

  @JsonProperty("pollType")
  private Integer pollType = null;

  @JsonProperty("protocolVersion")
  private Integer protocolVersion = null;

  @JsonProperty("pluginPollVersion")
  private String pluginPollVersion = null;

  /**
   * The V3 poll variation.
   */
  public enum PollVariantEnum {
    POR("PoR"),
    
    POP("PoP"),
    
    LOCAL("Local"),
    
    NOPOLL("NoPoll");

    private String value;

    PollVariantEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static PollVariantEnum fromValue(String text) {
      for (PollVariantEnum b : PollVariantEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("pollVariant")
  private PollVariantEnum pollVariant = null;

  @JsonProperty("modulus")
  private Integer modulus = null;

  public PollSpec cachedUriSet(CachedUriSet cachedUriSet) {
    this.cachedUriSet = cachedUriSet;
    return this;
  }

  /**
   * Get cachedUriSet
   * @return cachedUriSet
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public CachedUriSet getCachedUriSet() {
    return cachedUriSet;
  }

  public void setCachedUriSet(CachedUriSet cachedUriSet) {
    this.cachedUriSet = cachedUriSet;
  }

  public PollSpec pollType(Integer pollType) {
    this.pollType = pollType;
    return this;
  }

  /**
   * The type of poll being requested.
   * @return pollType
  **/
  @ApiModelProperty(required = true, value = "The type of poll being requested.")
  @NotNull


  public Integer getPollType() {
    return pollType;
  }

  public void setPollType(Integer pollType) {
    this.pollType = pollType;
  }

  public PollSpec protocolVersion(Integer protocolVersion) {
    this.protocolVersion = protocolVersion;
    return this;
  }

  /**
   * The version of polling protocol.
   * @return protocolVersion
  **/
  @ApiModelProperty(value = "The version of polling protocol.")


  public Integer getProtocolVersion() {
    return protocolVersion;
  }

  public void setProtocolVersion(Integer protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  public PollSpec pluginPollVersion(String pluginPollVersion) {
    this.pluginPollVersion = pluginPollVersion;
    return this;
  }

  /**
   * The version of the polling features of the poll.
   * @return pluginPollVersion
  **/
  @ApiModelProperty(value = "The version of the polling features of the poll.")


  public String getPluginPollVersion() {
    return pluginPollVersion;
  }

  public void setPluginPollVersion(String pluginPollVersion) {
    this.pluginPollVersion = pluginPollVersion;
  }

  public PollSpec pollVariant(PollVariantEnum pollVariant) {
    this.pollVariant = pollVariant;
    return this;
  }

  /**
   * The V3 poll variation.
   * @return pollVariant
  **/
  @ApiModelProperty(value = "The V3 poll variation.")


  public PollVariantEnum getPollVariant() {
    return pollVariant;
  }

  public void setPollVariant(PollVariantEnum pollVariant) {
    this.pollVariant = pollVariant;
  }

  public PollSpec modulus(Integer modulus) {
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
    PollSpec pollSpec = (PollSpec) o;
    return Objects.equals(this.cachedUriSet, pollSpec.cachedUriSet) &&
        Objects.equals(this.pollType, pollSpec.pollType) &&
        Objects.equals(this.protocolVersion, pollSpec.protocolVersion) &&
        Objects.equals(this.pluginPollVersion, pollSpec.pluginPollVersion) &&
        Objects.equals(this.pollVariant, pollSpec.pollVariant) &&
        Objects.equals(this.modulus, pollSpec.modulus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cachedUriSet, pollType, protocolVersion, pluginPollVersion, pollVariant, modulus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PollSpec {\n");
    
    sb.append("    cachedUriSet: ").append(toIndentedString(cachedUriSet)).append("\n");
    sb.append("    pollType: ").append(toIndentedString(pollType)).append("\n");
    sb.append("    protocolVersion: ").append(toIndentedString(protocolVersion)).append("\n");
    sb.append("    pluginPollVersion: ").append(toIndentedString(pluginPollVersion)).append("\n");
    sb.append("    pollVariant: ").append(toIndentedString(pollVariant)).append("\n");
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

