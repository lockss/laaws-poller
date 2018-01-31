package org.lockss.laaws.poller.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * The current poll tally
 */
@ApiModel(description = "The current poll tally")
@Validated

public class PollTallyStatus   {
  @JsonProperty("agreedUrls")
  @Valid
  private List<String> agreedUrls = new ArrayList<>();

  @JsonProperty("disagreedUrls")
  @Valid
  private List<String> disagreedUrls = new ArrayList<>();

  @JsonProperty("tooCloseUrls")
  @Valid
  private List<String> tooCloseUrls = new ArrayList<>();

  @JsonProperty("noQuorumUrls")
  @Valid
  private List<String> noQuorumUrls = new ArrayList<>();

  @JsonProperty("errorUrls")
  @Valid
  private List<String> errorUrls = new ArrayList<>();

  @JsonProperty("weightedAgreedSum")
  private Float weightedAgreedSum = 0.0f;

  @JsonProperty("weightedDisagreedSum")
  private Float weightedDisagreedSum = 0.0f;

  @JsonProperty("weightedTooCloseSum")
  private Float weightedTooCloseSum = 0.0f;

  @JsonProperty("weightedNoQuorumSum")
  private Float weightedNoQuorumSum = 0.0f;

  public PollTallyStatus agreedUrls(List<String> agreedUrls) {
    this.agreedUrls = agreedUrls;
    return this;
  }

  public PollTallyStatus addAgreedUrlsItem(String agreedUrlsItem) {
    this.agreedUrls.add(agreedUrlsItem);
    return this;
  }

  /**
   * Urls with agreement.
   * @return agreedUrls
  **/
  @ApiModelProperty(required = true, value = "Urls with agreement.")
  @NotNull


  public List<String> getAgreedUrls() {
    return agreedUrls;
  }

  public void setAgreedUrls(List<String> agreedUrls) {
    this.agreedUrls = agreedUrls;
  }

  public PollTallyStatus disagreedUrls(List<String> disagreedUrls) {
    this.disagreedUrls = disagreedUrls;
    return this;
  }

  public PollTallyStatus addDisagreedUrlsItem(String disagreedUrlsItem) {
    this.disagreedUrls.add(disagreedUrlsItem);
    return this;
  }

  /**
   * Urls for which there is disagreement
   * @return disagreedUrls
  **/
  @ApiModelProperty(required = true, value = "Urls for which there is disagreement")
  @NotNull


  public List<String> getDisagreedUrls() {
    return disagreedUrls;
  }

  public void setDisagreedUrls(List<String> disagreedUrls) {
    this.disagreedUrls = disagreedUrls;
  }

  public PollTallyStatus tooCloseUrls(List<String> tooCloseUrls) {
    this.tooCloseUrls = tooCloseUrls;
    return this;
  }

  public PollTallyStatus addTooCloseUrlsItem(String tooCloseUrlsItem) {
    this.tooCloseUrls.add(tooCloseUrlsItem);
    return this;
  }

  /**
   * Urls which are too close to call.
   * @return tooCloseUrls
  **/
  @ApiModelProperty(required = true, value = "Urls which are too close to call.")
  @NotNull


  public List<String> getTooCloseUrls() {
    return tooCloseUrls;
  }

  public void setTooCloseUrls(List<String> tooCloseUrls) {
    this.tooCloseUrls = tooCloseUrls;
  }

  public PollTallyStatus noQuorumUrls(List<String> noQuorumUrls) {
    this.noQuorumUrls = noQuorumUrls;
    return this;
  }

  public PollTallyStatus addNoQuorumUrlsItem(String noQuorumUrlsItem) {
    this.noQuorumUrls.add(noQuorumUrlsItem);
    return this;
  }

  /**
   * Urls for which there are not enough votes.
   * @return noQuorumUrls
  **/
  @ApiModelProperty(required = true, value = "Urls for which there are not enough votes.")
  @NotNull


  public List<String> getNoQuorumUrls() {
    return noQuorumUrls;
  }

  public void setNoQuorumUrls(List<String> noQuorumUrls) {
    this.noQuorumUrls = noQuorumUrls;
  }

  public PollTallyStatus errorUrls(List<String> errorUrls) {
    this.errorUrls = errorUrls;
    return this;
  }

  public PollTallyStatus addErrorUrlsItem(String errorUrlsItem) {
    this.errorUrls.add(errorUrlsItem);
    return this;
  }

  /**
   * Urls for which there are errors.
   * @return errorUrls
  **/
  @ApiModelProperty(required = true, value = "Urls for which there are errors.")
  @NotNull


  public List<String> getErrorUrls() {
    return errorUrls;
  }

  public void setErrorUrls(List<String> errorUrls) {
    this.errorUrls = errorUrls;
  }

  public PollTallyStatus weightedAgreedSum(Float weightedAgreedSum) {
    this.weightedAgreedSum = weightedAgreedSum;
    return this;
  }

  /**
   * The weighted sum agreed uris.
   * @return weightedAgreedSum
  **/
  @ApiModelProperty(value = "The weighted sum agreed uris.")


  public Float getWeightedAgreedSum() {
    return weightedAgreedSum;
  }

  public void setWeightedAgreedSum(Float weightedAgreedSum) {
    this.weightedAgreedSum = weightedAgreedSum;
  }

  public PollTallyStatus weightedDisagreedSum(Float weightedDisagreedSum) {
    this.weightedDisagreedSum = weightedDisagreedSum;
    return this;
  }

  /**
   * The weighted sum of disagree uris.
   * @return weightedDisagreedSum
  **/
  @ApiModelProperty(value = "The weighted sum of disagree uris.")


  public Float getWeightedDisagreedSum() {
    return weightedDisagreedSum;
  }

  public void setWeightedDisagreedSum(Float weightedDisagreedSum) {
    this.weightedDisagreedSum = weightedDisagreedSum;
  }

  public PollTallyStatus weightedTooCloseSum(Float weightedTooCloseSum) {
    this.weightedTooCloseSum = weightedTooCloseSum;
    return this;
  }

  /**
   * The sum of the tooClose uris.
   * @return weightedTooCloseSum
  **/
  @ApiModelProperty(value = "The sum of the tooClose uris.")


  public Float getWeightedTooCloseSum() {
    return weightedTooCloseSum;
  }

  public void setWeightedTooCloseSum(Float weightedTooCloseSum) {
    this.weightedTooCloseSum = weightedTooCloseSum;
  }

  public PollTallyStatus weightedNoQuorumSum(Float weightedNoQuorumSum) {
    this.weightedNoQuorumSum = weightedNoQuorumSum;
    return this;
  }

  /**
   * The weighted sum of NoQuorum uris.
   * @return weightedNoQuorumSum
  **/
  @ApiModelProperty(value = "The weighted sum of NoQuorum uris.")


  public Float getWeightedNoQuorumSum() {
    return weightedNoQuorumSum;
  }

  public void setWeightedNoQuorumSum(Float weightedNoQuorumSum) {
    this.weightedNoQuorumSum = weightedNoQuorumSum;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PollTallyStatus pollTallyStatus = (PollTallyStatus) o;
    return Objects.equals(this.agreedUrls, pollTallyStatus.agreedUrls) &&
        Objects.equals(this.disagreedUrls, pollTallyStatus.disagreedUrls) &&
        Objects.equals(this.tooCloseUrls, pollTallyStatus.tooCloseUrls) &&
        Objects.equals(this.noQuorumUrls, pollTallyStatus.noQuorumUrls) &&
        Objects.equals(this.errorUrls, pollTallyStatus.errorUrls) &&
        Objects.equals(this.weightedAgreedSum, pollTallyStatus.weightedAgreedSum) &&
        Objects.equals(this.weightedDisagreedSum, pollTallyStatus.weightedDisagreedSum) &&
        Objects.equals(this.weightedTooCloseSum, pollTallyStatus.weightedTooCloseSum) &&
        Objects.equals(this.weightedNoQuorumSum, pollTallyStatus.weightedNoQuorumSum);
  }

  @Override
  public int hashCode() {
    return Objects.hash(agreedUrls, disagreedUrls, tooCloseUrls, noQuorumUrls, errorUrls, weightedAgreedSum, weightedDisagreedSum, weightedTooCloseSum, weightedNoQuorumSum);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PollTallyStatus {\n");
    
    sb.append("    agreedUrls: ").append(toIndentedString(agreedUrls)).append("\n");
    sb.append("    disagreedUrls: ").append(toIndentedString(disagreedUrls)).append("\n");
    sb.append("    tooCloseUrls: ").append(toIndentedString(tooCloseUrls)).append("\n");
    sb.append("    noQuorumUrls: ").append(toIndentedString(noQuorumUrls)).append("\n");
    sb.append("    errorUrls: ").append(toIndentedString(errorUrls)).append("\n");
    sb.append("    weightedAgreedSum: ").append(toIndentedString(weightedAgreedSum)).append("\n");
    sb.append("    weightedDisagreedSum: ").append(toIndentedString(weightedDisagreedSum)).append("\n");
    sb.append("    weightedTooCloseSum: ").append(toIndentedString(weightedTooCloseSum)).append("\n");
    sb.append("    weightedNoQuorumSum: ").append(toIndentedString(weightedNoQuorumSum)).append("\n");
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

