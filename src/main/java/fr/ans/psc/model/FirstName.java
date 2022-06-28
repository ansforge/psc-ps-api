package fr.ans.psc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "First Name")
public class FirstName {
  @JsonProperty("firstName")
  private String firstName;

  @JsonProperty("order")
  private Integer order;

    public FirstName() {
    }

    public FirstName(String firstName, Integer order) {
        this.firstName = firstName;
        this.order = order;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
