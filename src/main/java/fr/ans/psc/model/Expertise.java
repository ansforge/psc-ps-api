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

/**
 * Expertise
 */
@ApiModel(description = "Expertise")
public class Expertise implements Cloneable {
  @JsonProperty("expertiseId")
  private String expertiseId;

  @JsonProperty("typeCode")
  private String typeCode;

  @JsonProperty("code")
  private String code;

  @Override
  public Expertise clone() {
    try {
      return (Expertise) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get expertiseId
   * @return expertiseId
  */
  @ApiModelProperty(value = "")
  public String getExpertiseId() {
    return expertiseId;
  }

  public void setExpertiseId(String expertiseId) {
    this.expertiseId = expertiseId;
  }

  /**
   * Get typeCode
   * @return typeCode
  */
  @ApiModelProperty(value = "")
  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Expertise expertise = (Expertise) o;
    return Objects.equals(this.expertiseId, expertise.expertiseId) &&
        Objects.equals(this.typeCode, expertise.typeCode) &&
        Objects.equals(this.code, expertise.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expertiseId, typeCode, code);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Expertise {\n");
    
    sb.append("    expertiseId: ").append(toIndentedString(expertiseId)).append("\n");
    sb.append("    typeCode: ").append(toIndentedString(typeCode)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
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

