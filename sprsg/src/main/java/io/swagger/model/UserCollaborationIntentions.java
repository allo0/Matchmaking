package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.model.UserCollaborationSpec;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * UserCollaborationIntentions
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-10T13:05:51.960Z[GMT]")
public class UserCollaborationIntentions   {
  @JsonProperty("gradingUser")
  private String gradingUser = null;

  @JsonProperty("intentions")
  @Valid
  private List<UserCollaborationSpec> intentions = null;

  public UserCollaborationIntentions gradingUser(String gradingUser) {
    this.gradingUser = gradingUser;
    return this;
  }

  /**
   * Get gradingUser
   * @return gradingUser
  **/
  @ApiModelProperty(value = "")
  
    public String getGradingUser() {
    return gradingUser;
  }

  public void setGradingUser(String gradingUser) {
    this.gradingUser = gradingUser;
  }

  public UserCollaborationIntentions intentions(List<UserCollaborationSpec> intentions) {
    this.intentions = intentions;
    return this;
  }

  public UserCollaborationIntentions addIntentionsItem(UserCollaborationSpec intentionsItem) {
    if (this.intentions == null) {
      this.intentions = new ArrayList<UserCollaborationSpec>();
    }
    this.intentions.add(intentionsItem);
    return this;
  }

  /**
   * Get intentions
   * @return intentions
  **/
  @ApiModelProperty(value = "")
      @Valid
    public List<UserCollaborationSpec> getIntentions() {
    return intentions;
  }

  public void setIntentions(List<UserCollaborationSpec> intentions) {
    this.intentions = intentions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserCollaborationIntentions userCollaborationIntentions = (UserCollaborationIntentions) o;
    return Objects.equals(this.gradingUser, userCollaborationIntentions.gradingUser) &&
        Objects.equals(this.intentions, userCollaborationIntentions.intentions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gradingUser, intentions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserCollaborationIntentions {\n");
    
    sb.append("    gradingUser: ").append(toIndentedString(gradingUser)).append("\n");
    sb.append("    intentions: ").append(toIndentedString(intentions)).append("\n");
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
