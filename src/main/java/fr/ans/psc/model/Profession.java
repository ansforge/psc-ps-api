/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import javax.validation.Valid;

/**
 * Profession
 */
@ApiModel(description = "Profession")
public class Profession implements Cloneable{
  @JsonProperty("exProId")
  private String exProId;

  @JsonProperty("code")
  private String code;

  @JsonProperty("categoryCode")
  private String categoryCode;

  @JsonProperty("salutationCode")
  private String salutationCode;

  @JsonProperty("lastName")
  private String lastName;

  @JsonProperty("firstName")
  private String firstName;

  @JsonProperty("expertises")
  @Valid
  private List<Expertise> expertises = null;

  @JsonProperty("workSituations")
  @Valid
  private List<WorkSituation> workSituations = null;

  @Override
  public Profession clone() {
    try {
      return (Profession) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public Profession exProId(String exProId) {
    this.exProId = exProId;
    return this;
  }

  /**
   * Get exProId
   * @return exProId
  */
  @ApiModelProperty(value = "")
  public String getExProId() {
    return exProId;
  }

  public void setExProId(String exProId) {
    this.exProId = exProId;
  }

  /**
   * Get code
   * @return code
  */
  @ApiModelProperty(value = "")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Get categoryCode
   * @return categoryCode
  */
  @ApiModelProperty(value = "")
  public String getCategoryCode() {
    return categoryCode;
  }

  public void setCategoryCode(String categoryCode) {
    this.categoryCode = categoryCode;
  }

  /**
   * Get salutationCode
   * @return salutationCode
  */
  @ApiModelProperty(value = "")
  public String getSalutationCode() {
    return salutationCode;
  }

  public void setSalutationCode(String salutationCode) {
    this.salutationCode = salutationCode;
  }

  /**
   * Get lastName
   * @return lastName
  */
  @ApiModelProperty(value = "")
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Get firstName
   * @return firstName
  */
  @ApiModelProperty(value = "")
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Get expertises
   * @return expertises
  */
  @ApiModelProperty(value = "")
  @Valid
  public List<Expertise> getExpertises() {
    return expertises;
  }

  public void setExpertises(List<Expertise> expertises) {
    this.expertises = expertises;
  }

  /**
   * Get workSituations
   * @return workSituations
  */
  @ApiModelProperty(value = "")
  @Valid
  public List<WorkSituation> getWorkSituations() {
    return workSituations;
  }

  public void setWorkSituations(List<WorkSituation> workSituations) {
    this.workSituations = workSituations;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Profession profession = (Profession) o;
    return Objects.equals(this.exProId, profession.exProId) &&
        Objects.equals(this.code, profession.code) &&
        Objects.equals(this.categoryCode, profession.categoryCode) &&
        Objects.equals(this.salutationCode, profession.salutationCode) &&
        Objects.equals(this.lastName, profession.lastName) &&
        Objects.equals(this.firstName, profession.firstName) &&
        Objects.equals(this.expertises, profession.expertises) &&
        Objects.equals(this.workSituations, profession.workSituations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exProId, code, categoryCode, salutationCode, lastName, firstName, expertises, workSituations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Profession {\n");
    
    sb.append("    exProId: ").append(toIndentedString(exProId)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    categoryCode: ").append(toIndentedString(categoryCode)).append("\n");
    sb.append("    salutationCode: ").append(toIndentedString(salutationCode)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    expertises: ").append(toIndentedString(expertises)).append("\n");
    sb.append("    workSituations: ").append(toIndentedString(workSituations)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

