
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Professionnel de santé
 */
@Document(collection = "ps")
@ApiModel(description = "Professionnel de santé")
public class Ps implements Cloneable {

	@Id
	private String _id;

	@JsonProperty("idType")
	private String idType;

	@JsonProperty("id")
	private String id;

	@JsonProperty("nationalId")
	@Indexed(unique = true)
	@NotNull(message = "nationalId should not be null")
	private String nationalId;

	@JsonProperty("lastName")
	private String lastName;

	@JsonProperty("firstNames")
	private List<FirstName> firstNames;

	@JsonProperty("dateOfBirth")
	private String dateOfBirth;

	@JsonProperty("birthAddressCode")
	private String birthAddressCode;

	@JsonProperty("birthCountryCode")
	private String birthCountryCode;

	@JsonProperty("birthAddress")
	private String birthAddress;

	@JsonProperty("genderCode")
	private String genderCode;

	// Champs pour optimisation de la recherche (lowercase, non exposés dans l'API)
	@JsonIgnore
	@Indexed
	private String lastNameLower;

	@JsonIgnore
	@Indexed
	private String birthAddressLower;

	@JsonIgnore
	@Indexed
	private List<String> firstNamesLowerArray;

	@JsonProperty("phone")
	private String phone;

	@JsonProperty("email")
	private String email;

	@JsonProperty("salutationCode")
	private String salutationCode;

	@JsonProperty("professions")
	@Valid
	private List<Profession> professions = null;

	@JsonProperty("ids")
	@Indexed(unique = true)
	private List<String> ids = new ArrayList<>();

	@JsonProperty("alternativeIds")
	private List<AlternativeIdentifier> alternativeIds = new ArrayList<>();

	@JsonProperty("activated")
	@Indexed
	private Long activated;

	@JsonProperty("deactivated")
	@Indexed
	private Long deactivated;

	@Override
	public Ps clone() {
		Ps psClone;
		try {
			psClone = (Ps) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		return psClone;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	/**
	 * Get idType
	 * 
	 * @return idType
	 */
	@ApiModelProperty(value = "")
	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
	}

	/**
	 * Get id
	 * 
	 * @return id
	 */
	@ApiModelProperty(value = "")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get nationalId
	 * 
	 * @return nationalId
	 */
	@ApiModelProperty(required = true, value = "")
	@NotNull
	@Size(min = 1)
	public String getNationalId() {
		return nationalId;
	}

	public void setNationalId(String nationalId) {
		this.nationalId = nationalId;
	}

	/**
	 * Get lastName
	 * 
	 * @return lastName
	 */
	@ApiModelProperty(value = "")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
		this.lastNameLower = lastName != null ? lastName.toLowerCase() : null;
	}

	/**
	 * Get firstNames
	 * 
	 * @return firstNames
	 */
	@ApiModelProperty(value = "")
	public List<FirstName> getFirstNames() {
		return firstNames;
	}

	public void setFirstNames(List<FirstName> firstNames) {
		this.firstNames = firstNames;
		this.firstNamesLowerArray = firstNames != null 
			? firstNames.stream()
				.map(FirstName::getFirstName)
				.filter(fn -> fn != null)
				.map(String::toLowerCase)
				.collect(Collectors.toList())
			: null;
	}

	/**
	 * Get dateOfBirth
	 * 
	 * @return dateOfBirth
	 */
	@ApiModelProperty(value = "")
	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	/**
	 * Get birthAddressCode
	 * 
	 * @return birthAddressCode
	 */
	@ApiModelProperty(value = "")
	public String getBirthAddressCode() {
		return birthAddressCode;
	}

	public void setBirthAddressCode(String birthAddressCode) {
		this.birthAddressCode = birthAddressCode;
	}

	/**
	 * Get birthCountryCode
	 * 
	 * @return birthCountryCode
	 */
	@ApiModelProperty(value = "")
	public String getBirthCountryCode() {
		return birthCountryCode;
	}

	public void setBirthCountryCode(String birthCountryCode) {
		this.birthCountryCode = birthCountryCode;
	}

	/**
	 * Get birthAddress
	 * 
	 * @return birthAddress
	 */
	@ApiModelProperty(value = "")
	public String getBirthAddress() {
		return birthAddress;
	}

	public void setBirthAddress(String birthAddress) {
		this.birthAddress = birthAddress;
		this.birthAddressLower = birthAddress != null ? birthAddress.toLowerCase() : null;
	}

	/**
	 * Get genderCode
	 * 
	 * @return genderCode
	 */
	@ApiModelProperty(value = "")
	public String getGenderCode() {
		return genderCode;
	}

	public void setGenderCode(String genderCode) {
		this.genderCode = genderCode;
	}

	// Getters pour les champs de recherche optimisée
	public String getLastNameLower() {
		return lastNameLower;
	}

	public void setLastNameLower(String lastNameLower) {
		this.lastNameLower = lastNameLower;
	}

	public String getBirthAddressLower() {
		return birthAddressLower;
	}

	public void setBirthAddressLower(String birthAddressLower) {
		this.birthAddressLower = birthAddressLower;
	}

	public List<String> getFirstNamesLowerArray() {
		return firstNamesLowerArray;
	}

	public void setFirstNamesLowerArray(List<String> firstNamesLowerArray) {
		this.firstNamesLowerArray = firstNamesLowerArray;
	}

	/**
	 * Get phone
	 * 
	 * @return phone
	 */
	@ApiModelProperty(value = "")
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Get email
	 * 
	 * @return email
	 */
	@ApiModelProperty(value = "")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Get salutationCode
	 * 
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
	 * Get professions
	 * 
	 * @return professions
	 */
	@ApiModelProperty(value = "")
	@Valid
	public List<Profession> getProfessions() {
		return professions;
	}

	public void setProfessions(List<Profession> professions) {
		this.professions = professions;
	}

	/**
	 * Get ids
	 * 
	 * @return ids
	 */
	@ApiModelProperty(value = "")
	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	/**
	 * Get activated
	 * 
	 * @return activated
	 */
	@ApiModelProperty(value = "")
	public Long getActivated() {
		return activated;
	}

	public void setActivated(Long activated) {
		this.activated = activated;
	}

	/**
	 * Get deactivated
	 * 
	 * @return deactivated
	 */
	@ApiModelProperty(value = "")
	public Long getDeactivated() {
		return deactivated;
	}

	public void setDeactivated(Long deactivated) {
		this.deactivated = deactivated;
	}

	/***
	 * Get alternativeIds
	 * 
	 * @return
	 */
	@ApiModelProperty(value = "")
	public List<AlternativeIdentifier> getAlternativeIds() {
		return alternativeIds;
	}

	public void setAlternativeIds(List<AlternativeIdentifier> alternativeIds) {
		this.alternativeIds = alternativeIds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Ps ps = (Ps) o;
		return Objects.equals(this.idType, ps.idType) && Objects.equals(this.id, ps.id)
				&& Objects.equals(this.nationalId, ps.nationalId) && Objects.equals(this.lastName, ps.lastName)
				&& Objects.equals(this.firstNames, ps.firstNames) && Objects.equals(this.dateOfBirth, ps.dateOfBirth)
				&& Objects.equals(this.birthAddressCode, ps.birthAddressCode)
				&& Objects.equals(this.birthCountryCode, ps.birthCountryCode)
				&& Objects.equals(this.birthAddress, ps.birthAddress) && Objects.equals(this.genderCode, ps.genderCode)
				&& Objects.equals(this.phone, ps.phone) && Objects.equals(this.email, ps.email)
				&& Objects.equals(this.salutationCode, ps.salutationCode)
				&& Objects.equals(this.professions, ps.professions) && Objects.equals(this.ids, ps.ids)
				&& Objects.equals(this.activated, ps.activated) && Objects.equals(this.deactivated, ps.deactivated);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idType, id, nationalId, lastName, firstNames, dateOfBirth, birthAddressCode,
				birthCountryCode, birthAddress, genderCode, phone, email, salutationCode, professions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Ps {\n");

		sb.append("    idType: ").append(toIndentedString(idType)).append("\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    nationalId: ").append(toIndentedString(nationalId)).append("\n");
		sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
		sb.append("    firstNames: ").append(toIndentedString(firstNames)).append("\n");
		sb.append("    dateOfBirth: ").append(toIndentedString(dateOfBirth)).append("\n");
		sb.append("    birthAddressCode: ").append(toIndentedString(birthAddressCode)).append("\n");
		sb.append("    birthCountryCode: ").append(toIndentedString(birthCountryCode)).append("\n");
		sb.append("    birthAddress: ").append(toIndentedString(birthAddress)).append("\n");
		sb.append("    genderCode: ").append(toIndentedString(genderCode)).append("\n");
		sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
		sb.append("    email: ").append(toIndentedString(email)).append("\n");
		sb.append("    salutationCode: ").append(toIndentedString(salutationCode)).append("\n");
		sb.append("    professions: ").append(toIndentedString(professions)).append("\n");
		sb.append("    ids: ").append(toIndentedString(ids)).append("\n");
		sb.append("    activated: ").append(toIndentedString(activated)).append("\n");
		sb.append("    deactivated: ").append(toIndentedString(deactivated)).append("\n");
		sb.append("    alternativeIds: ").append(toIndentedString(alternativeIds)).append("\n");
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
