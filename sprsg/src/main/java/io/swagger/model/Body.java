package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.model.UserCollaborationIntentions;
import io.swagger.model.UserPairwiseScore;
import io.swagger.model.UserScore;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Body
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-10T13:05:51.960Z[GMT]")
public class Body   {
  @JsonProperty("userGlobalScores")
  @Valid
  private List<UserScore> userGlobalScores = null;

  @JsonProperty("userPairwiseScore")
  @Valid
  private List<UserPairwiseScore> userPairwiseScore = null;

  @JsonProperty("userCollaborationIntentions")
  @Valid
  private List<UserCollaborationIntentions> userCollaborationIntentions = null;

  public Body userGlobalScores(List<UserScore> userGlobalScores) {
    this.userGlobalScores = userGlobalScores;
    return this;
  }

  public Body addUserGlobalScoresItem(UserScore userGlobalScoresItem) {
    if (this.userGlobalScores == null) {
      this.userGlobalScores = new ArrayList<UserScore>();
    }
    this.userGlobalScores.add(userGlobalScoresItem);
    return this;
  }

  /**
   * Get userGlobalScores
   * @return userGlobalScores
  **/
  @ApiModelProperty(value = "")
      @Valid
    public List<UserScore> getUserGlobalScores() {
    return userGlobalScores;
  }

  public void setUserGlobalScores(List<UserScore> userGlobalScores) {
    this.userGlobalScores = userGlobalScores;
  }

  public Body userPairwiseScore(List<UserPairwiseScore> userPairwiseScore) {
    this.userPairwiseScore = userPairwiseScore;
    return this;
  }

  public Body addUserPairwiseScoreItem(UserPairwiseScore userPairwiseScoreItem) {
    if (this.userPairwiseScore == null) {
      this.userPairwiseScore = new ArrayList<UserPairwiseScore>();
    }
    this.userPairwiseScore.add(userPairwiseScoreItem);
    return this;
  }

  /**
   * Get userPairwiseScore
   * @return userPairwiseScore
  **/
  @ApiModelProperty(value = "")
      @Valid
    public List<UserPairwiseScore> getUserPairwiseScore() {
    return userPairwiseScore;
  }

  public void setUserPairwiseScore(List<UserPairwiseScore> userPairwiseScore) {
    this.userPairwiseScore = userPairwiseScore;
  }

  public Body userCollaborationIntentions(List<UserCollaborationIntentions> userCollaborationIntentions) {
    this.userCollaborationIntentions = userCollaborationIntentions;
    return this;
  }

  public Body addUserCollaborationIntentionsItem(UserCollaborationIntentions userCollaborationIntentionsItem) {
    if (this.userCollaborationIntentions == null) {
      this.userCollaborationIntentions = new ArrayList<UserCollaborationIntentions>();
    }
    this.userCollaborationIntentions.add(userCollaborationIntentionsItem);
    return this;
  }

  /**
   * Get userCollaborationIntentions
   * @return userCollaborationIntentions
  **/
  @ApiModelProperty(value = "")
      @Valid
    public List<UserCollaborationIntentions> getUserCollaborationIntentions() {
    return userCollaborationIntentions;
  }

  public void setUserCollaborationIntentions(List<UserCollaborationIntentions> userCollaborationIntentions) {
    this.userCollaborationIntentions = userCollaborationIntentions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Body body = (Body) o;
    return Objects.equals(this.userGlobalScores, body.userGlobalScores) &&
        Objects.equals(this.userPairwiseScore, body.userPairwiseScore) &&
        Objects.equals(this.userCollaborationIntentions, body.userCollaborationIntentions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userGlobalScores, userPairwiseScore, userCollaborationIntentions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Body {\n");
    
    sb.append("    userGlobalScores: ").append(toIndentedString(userGlobalScores)).append("\n");
    sb.append("    userPairwiseScore: ").append(toIndentedString(userPairwiseScore)).append("\n");
    sb.append("    userCollaborationIntentions: ").append(toIndentedString(userCollaborationIntentions)).append("\n");
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
