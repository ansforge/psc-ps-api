package fr.ans.psc.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * WorkSituation
 */
@ApiModel(description = "Situation d'exercice")
public class WorkSituation {
    @JsonProperty("situId")
    private String situId;

    @JsonProperty("modeCode")
    private String modeCode;

    @JsonProperty("activitySectorCode")
    private String activitySectorCode;

    @JsonProperty("pharmacistTableSectionCode")
    private String pharmacistTableSectionCode;

    @JsonProperty("roleCode")
    private String roleCode;

    @JsonProperty("registrationAuthority")
    private String registrationAuthority;

    @JsonProperty("siteSIRET")
    private String siteSIRET;

    @JsonProperty("siteSIREN")
    private String siteSIREN;

    @JsonProperty("siteFINESS")
    private String siteFINESS;

    @JsonProperty("legalEstablishmentFINESS")
    private String legalEstablishmentFINESS;

    @JsonProperty("structureTechnicalId")
    @Indexed(unique = true)
    @NotNull(message = "structure technical id should not be null")
    private String structureTechnicalId;

    @JsonProperty("legalCommercialName")
    private String legalCommercialName;

    @JsonProperty("publicCommercialName")
    private String publicCommercialName;

    @JsonProperty("recipientAdditionalInfo")
    private String recipientAdditionalInfo;

    @JsonProperty("geoLocationAdditionalInfo")
    private String geoLocationAdditionalInfo;

    @JsonProperty("streetNumber")
    private String streetNumber;

    @JsonProperty("streetNumberRepetitionIndex")
    private String streetNumberRepetitionIndex;

    @JsonProperty("streetCategoryCode")
    private String streetCategoryCode;

    @JsonProperty("streetLabel")
    private String streetLabel;

    @JsonProperty("distributionMention")
    private String distributionMention;

    @JsonProperty("cedexOffice")
    private String cedexOffice;

    @JsonProperty("postalCode")
    private String postalCode;

    @JsonProperty("communeCode")
    private String communeCode;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("phone2")
    private String phone2;

    @JsonProperty("fax")
    private String fax;

    @JsonProperty("email")
    private String email;

    @JsonProperty("departmentCode")
    private String departmentCode;

    @JsonProperty("oldStructureId")
    private String oldStructureId;

    /**
     * Get situId
     *
     * @return situId
     */
    @ApiModelProperty(value = "")
    public String getSituId() {
        return situId;
    }

    public void setSituId(String situId) {
        this.situId = situId;
    }

    /**
     * Get modeCode
     *
     * @return modeCode
     */
    @ApiModelProperty(value = "")
    public String getModeCode() {
        return modeCode;
    }

    public void setModeCode(String modeCode) {
        this.modeCode = modeCode;
    }

    /**
     * Get activitySectorCode
     *
     * @return activitySectorCode
     */
    @ApiModelProperty(value = "")
    public String getActivitySectorCode() {
        return activitySectorCode;
    }

    public void setActivitySectorCode(String activitySectorCode) {
        this.activitySectorCode = activitySectorCode;
    }

    /**
     * Get pharmacistTableSectionCode
     *
     * @return pharmacistTableSectionCode
     */
    @ApiModelProperty(value = "")
    public String getPharmacistTableSectionCode() {
        return pharmacistTableSectionCode;
    }

    public void setPharmacistTableSectionCode(String pharmacistTableSectionCode) {
        this.pharmacistTableSectionCode = pharmacistTableSectionCode;
    }

    /**
     * Get roleCode
     *
     * @return roleCode
     */
    @ApiModelProperty(value = "")
    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    /**
     * Get registrationAuthority
     *
     * @return registrationAuthority
     */
    @ApiModelProperty(value = "")
    public String getRegistrationAuthority() {
        return registrationAuthority;
    }

    public void setRegistrationAuthority(String registrationAuthority) {
        this.registrationAuthority = registrationAuthority;
    }

    /**
     * Get siteSIRET
     * @return siteSIRET
     */
    @ApiModelProperty(value = "")
    public String getSiteSIRET() {
        return siteSIRET;
    }

    public void setSiteSIRET(String siteSIRET) {
        this.siteSIRET = siteSIRET;
    }

    /**
     * Get siteSIREN
     * @return siteSIREN
     */
    @ApiModelProperty(value = "")
    public String getSiteSIREN() {
        return siteSIREN;
    }

    public void setSiteSIREN(String siteSIREN) {
        this.siteSIREN = siteSIREN;
    }

    /**
     * Get siteFINESS
     * @return siteFINESS
     */
    @ApiModelProperty(value = "")
    public String getSiteFINESS() {
        return siteFINESS;
    }

    public void setSiteFINESS(String siteFINESS) {
        this.siteFINESS = siteFINESS;
    }

    /**
     * Get legalEstablishmentFINESS
     * @return legalEstablishmentFINESS
     */
    @ApiModelProperty(value = "")
    public String getLegalEstablishmentFINESS() {
        return legalEstablishmentFINESS;
    }

    public void setLegalEstablishmentFINESS(String legalEstablishmentFINESS) {
        this.legalEstablishmentFINESS = legalEstablishmentFINESS;
    }

    /**
     * Get structureTechnicalId
     * @return structureTechnicalId
     */
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Size(min = 1)
    public String getStructureTechnicalId() {
        return structureTechnicalId;
    }

    public void setStructureTechnicalId(String structureTechnicalId) {
        this.structureTechnicalId = structureTechnicalId;
    }

    /**
     * Get legalCommercialName
     * @return legalCommercialName
     */
    @ApiModelProperty(value = "")
    public String getLegalCommercialName() {
        return legalCommercialName;
    }

    public void setLegalCommercialName(String legalCommercialName) {
        this.legalCommercialName = legalCommercialName;
    }

    /**
     * Get publicCommercialName
     * @return publicCommercialName
     */
    @ApiModelProperty(value = "")
    public String getPublicCommercialName() {
        return publicCommercialName;
    }

    public void setPublicCommercialName(String publicCommercialName) {
        this.publicCommercialName = publicCommercialName;
    }

    /**
     * Get recipientAdditionalInfo
     * @return recipientAdditionalInfo
     */
    @ApiModelProperty(value = "")
    public String getRecipientAdditionalInfo() {
        return recipientAdditionalInfo;
    }

    public void setRecipientAdditionalInfo(String recipientAdditionalInfo) {
        this.recipientAdditionalInfo = recipientAdditionalInfo;
    }

    /**
     * Get geoLocationAdditionalInfo
     * @return geoLocationAdditionalInfo
     */
    @ApiModelProperty(value = "")
    public String getGeoLocationAdditionalInfo() {
        return geoLocationAdditionalInfo;
    }

    public void setGeoLocationAdditionalInfo(String geoLocationAdditionalInfo) {
        this.geoLocationAdditionalInfo = geoLocationAdditionalInfo;
    }

    /**
     * Get streetNumber
     * @return streetNumber
     */
    @ApiModelProperty(value = "")
    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    /**
     * Get streetNumberRepetitionIndex
     * @return streetNumberRepetitionIndex
     */
    @ApiModelProperty(value = "")
    public String getStreetNumberRepetitionIndex() {
        return streetNumberRepetitionIndex;
    }

    public void setStreetNumberRepetitionIndex(String streetNumberRepetitionIndex) {
        this.streetNumberRepetitionIndex = streetNumberRepetitionIndex;
    }

    /**
     * Get streetCategoryCode
     * @return streetCategoryCode
     */
    @ApiModelProperty(value = "")
    public String getStreetCategoryCode() {
        return streetCategoryCode;
    }

    public void setStreetCategoryCode(String streetCategoryCode) {
        this.streetCategoryCode = streetCategoryCode;
    }

    /**
     * Get streetLabel
     * @return streetLabel
     */
    @ApiModelProperty(value = "")
    public String getStreetLabel() {
        return streetLabel;
    }

    public void setStreetLabel(String streetLabel) {
        this.streetLabel = streetLabel;
    }

    /**
     * Get distributionMention
     * @return distributionMention
     */
    @ApiModelProperty(value = "")
    public String getDistributionMention() {
        return distributionMention;
    }

    public void setDistributionMention(String distributionMention) {
        this.distributionMention = distributionMention;
    }

    /**
     * Get cedexOffice
     * @return cedexOffice
     */
    @ApiModelProperty(value = "")
    public String getCedexOffice() {
        return cedexOffice;
    }

    public void setCedexOffice(String cedexOffice) {
        this.cedexOffice = cedexOffice;
    }
    /**
     * Get postalCode
     * @return postalCode
     */
    @ApiModelProperty(value = "")
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Get communeCode
     * @return communeCode
     */
    @ApiModelProperty(value = "")
    public String getCommuneCode() {
        return communeCode;
    }

    public void setCommuneCode(String communeCode) {
        this.communeCode = communeCode;
    }

    /**
     * Get countryCode
     * @return countryCode
     */
    @ApiModelProperty(value = "")
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Get phone
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
     * Get phone2
     * @return phone2
     */
    @ApiModelProperty(value = "")
    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    /**
     * Get fax
     * @return fax
     */
    @ApiModelProperty(value = "")
    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     * Get email
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
     * Get departmentCode
     * @return departmentCode
     */
    @ApiModelProperty(value = "")
    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    /**
     * Get oldStructureId
     * @return oldStructureId
     */
    @ApiModelProperty(value = "")
    public String getOldStructureId() {
        return oldStructureId;
    }

    public void setOldStructureId(String oldStructureId) {
        this.oldStructureId = oldStructureId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkSituation workSituation = (WorkSituation) o;
        return Objects.equals(this.situId, workSituation.situId) &&
                Objects.equals(this.modeCode, workSituation.modeCode) &&
                Objects.equals(this.activitySectorCode, workSituation.activitySectorCode) &&
                Objects.equals(this.pharmacistTableSectionCode, workSituation.pharmacistTableSectionCode) &&
                Objects.equals(this.roleCode, workSituation.roleCode) &&
                Objects.equals(this.registrationAuthority, workSituation.registrationAuthority) &&
                Objects.equals(this.siteSIRET, workSituation.siteSIRET) &&
                Objects.equals(this.siteSIREN, workSituation.siteSIREN) &&
                Objects.equals(this.siteFINESS, workSituation.siteFINESS) &&
                Objects.equals(this.legalEstablishmentFINESS, workSituation.legalEstablishmentFINESS) &&
                Objects.equals(this.structureTechnicalId, workSituation.structureTechnicalId) &&
                Objects.equals(this.legalCommercialName, workSituation.legalCommercialName) &&
                Objects.equals(this.publicCommercialName, workSituation.publicCommercialName) &&
                Objects.equals(this.recipientAdditionalInfo, workSituation.recipientAdditionalInfo) &&
                Objects.equals(this.geoLocationAdditionalInfo, workSituation.geoLocationAdditionalInfo) &&
                Objects.equals(this.streetNumber, workSituation.streetNumber) &&
                Objects.equals(this.streetNumberRepetitionIndex, workSituation.streetNumberRepetitionIndex) &&
                Objects.equals(this.streetCategoryCode, workSituation.streetCategoryCode) &&
                Objects.equals(this.streetLabel, workSituation.streetLabel) &&
                Objects.equals(this.distributionMention, workSituation.distributionMention) &&
                Objects.equals(this.cedexOffice, workSituation.cedexOffice) &&
                Objects.equals(this.postalCode, workSituation.postalCode) &&
                Objects.equals(this.communeCode, workSituation.communeCode) &&
                Objects.equals(this.countryCode, workSituation.countryCode) &&
                Objects.equals(this.phone, workSituation.phone) &&
                Objects.equals(this.phone2, workSituation.phone2) &&
                Objects.equals(this.fax, workSituation.fax) &&
                Objects.equals(this.email, workSituation.email) &&
                Objects.equals(this.departmentCode, workSituation.departmentCode) &&
                Objects.equals(this.oldStructureId, workSituation.oldStructureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(situId, modeCode, activitySectorCode, pharmacistTableSectionCode, roleCode, registrationAuthority, siteSIRET, siteSIREN, siteFINESS, legalEstablishmentFINESS, structureTechnicalId, legalCommercialName, publicCommercialName, recipientAdditionalInfo, geoLocationAdditionalInfo, streetNumber, streetNumberRepetitionIndex, streetCategoryCode, streetLabel, distributionMention, cedexOffice, postalCode, communeCode, countryCode, phone, phone2, fax, email, departmentCode, oldStructureId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkSituation {\n");

        sb.append("    situId: ").append(toIndentedString(situId)).append("\n");
        sb.append("    modeCode: ").append(toIndentedString(modeCode)).append("\n");
        sb.append("    activitySectorCode: ").append(toIndentedString(activitySectorCode)).append("\n");
        sb.append("    pharmacistTableSectionCode: ").append(toIndentedString(pharmacistTableSectionCode)).append("\n");
        sb.append("    roleCode: ").append(toIndentedString(roleCode)).append("\n");
        sb.append("    registrationAuthority: ").append(toIndentedString(registrationAuthority)).append("\n");
        sb.append("    siteSIRET: ").append(toIndentedString(siteSIRET)).append("\n");
        sb.append("    siteSIREN: ").append(toIndentedString(siteSIREN)).append("\n");
        sb.append("    siteFINESS: ").append(toIndentedString(siteFINESS)).append("\n");
        sb.append("    legalEstablishmentFINESS: ").append(toIndentedString(legalEstablishmentFINESS)).append("\n");
        sb.append("    structureTechnicalId: ").append(toIndentedString(structureTechnicalId)).append("\n");
        sb.append("    legalCommercialName: ").append(toIndentedString(legalCommercialName)).append("\n");
        sb.append("    publicCommercialName: ").append(toIndentedString(publicCommercialName)).append("\n");
        sb.append("    recipientAdditionalInfo: ").append(toIndentedString(recipientAdditionalInfo)).append("\n");
        sb.append("    geoLocationAdditionalInfo: ").append(toIndentedString(geoLocationAdditionalInfo)).append("\n");
        sb.append("    streetNumber: ").append(toIndentedString(streetNumber)).append("\n");
        sb.append("    streetNumberRepetitionIndex: ").append(toIndentedString(streetNumberRepetitionIndex)).append("\n");
        sb.append("    streetCategoryCode: ").append(toIndentedString(streetCategoryCode)).append("\n");
        sb.append("    streetLabel: ").append(toIndentedString(streetLabel)).append("\n");
        sb.append("    distributionMention: ").append(toIndentedString(distributionMention)).append("\n");
        sb.append("    cedexOffice: ").append(toIndentedString(cedexOffice)).append("\n");
        sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
        sb.append("    communeCode: ").append(toIndentedString(communeCode)).append("\n");
        sb.append("    countryCode: ").append(toIndentedString(countryCode)).append("\n");
        sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
        sb.append("    phone2: ").append(toIndentedString(phone2)).append("\n");
        sb.append("    fax: ").append(toIndentedString(fax)).append("\n");
        sb.append("    email: ").append(toIndentedString(email)).append("\n");
        sb.append("    departmentCode: ").append(toIndentedString(departmentCode)).append("\n");
        sb.append("    oldStructureId: ").append(toIndentedString(oldStructureId)).append("\n");
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

