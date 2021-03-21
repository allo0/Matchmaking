package io.swagger.model;

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
 * UserCollaborationSpec
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-10T13:05:51.960Z[GMT]")
public class UserCollaborationSpec   {
  @JsonProperty("userId")
  private String userId = null;

  /**
   * Gets or Sets intention
   */
  public enum IntentionEnum {
    WANT("want"),
    
    DWANT("dwant"),
    
    IDC("idc");

    private String value;

    IntentionEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static IntentionEnum fromValue(String text) {
      for (IntentionEnum b : IntentionEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("intention")
  private IntentionEnum intention = null;

  public UserCollaborationSpec userId(String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Get userId
   * @return userId
  **/
  @ApiModelProperty(value = "")
  
    public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public UserCollaborationSpec intention(IntentionEnum intention) {
    this.intention = intention;
    return this;
  }

  /**
   * Get intention
   * @return intention
  **/
  @ApiModelProperty(value = "")
  
    public IntentionEnum getIntention() {
    return intention;
  }

  public void setIntention(IntentionEnum intention) {
    this.intention = intention;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserCollaborationSpec userCollaborationSpec = (UserCollaborationSpec) o;
    return Objects.equals(this.userId, userCollaborationSpec.userId) &&
        Objects.equals(this.intention, userCollaborationSpec.intention);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, intention);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserCollaborationSpec {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    intention: ").append(toIndentedString(intention)).append("\n");
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
