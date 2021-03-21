package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * UserPairAssignment
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-10T13:05:51.960Z[GMT]")
public class UserPairAssignment   {
  @JsonProperty("user1")
  private String user1 = null;

  @JsonProperty("user2")
  private String user2 = null;

  public UserPairAssignment user1(String user1) {
    this.user1 = user1;
    return this;
  }

  /**
   * Get user1
   * @return user1
  **/
  @ApiModelProperty(value = "")
  
    public String getUser1() {
    return user1;
  }

  public void setUser1(String user1) {
    this.user1 = user1;
  }

  public UserPairAssignment user2(String user2) {
    this.user2 = user2;
    return this;
  }

  /**
   * Get user2
   * @return user2
  **/
  @ApiModelProperty(value = "")
  
    public String getUser2() {
    return user2;
  }

  public void setUser2(String user2) {
    this.user2 = user2;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserPairAssignment userPairAssignment = (UserPairAssignment) o;
    return Objects.equals(this.user1, userPairAssignment.user1) &&
        Objects.equals(this.user2, userPairAssignment.user2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user1, user2);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserPairAssignment {\n");
    
    sb.append("    user1: ").append(toIndentedString(user1)).append("\n");
    sb.append("    user2: ").append(toIndentedString(user2)).append("\n");
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
