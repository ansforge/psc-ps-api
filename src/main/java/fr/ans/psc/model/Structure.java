package fr.ans.psc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@ApiModel(description = "Structure")
public class Structure {

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
     * Get siteSIRET
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     * Get phone2
     *
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
     *
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
     * Get departmentCode
     *
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
     *
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
        Structure structure = (Structure) o;
        return Objects.equals(this.siteSIRET, structure.siteSIRET) &&
                Objects.equals(this.siteSIREN, structure.siteSIREN) &&
                Objects.equals(this.siteFINESS, structure.siteFINESS) &&
                Objects.equals(this.legalEstablishmentFINESS, structure.legalEstablishmentFINESS) &&
                Objects.equals(this.structureTechnicalId, structure.structureTechnicalId) &&
                Objects.equals(this.legalCommercialName, structure.legalCommercialName) &&
                Objects.equals(this.publicCommercialName, structure.publicCommercialName) &&
                Objects.equals(this.recipientAdditionalInfo, structure.recipientAdditionalInfo) &&
                Objects.equals(this.geoLocationAdditionalInfo, structure.geoLocationAdditionalInfo) &&
                Objects.equals(this.streetNumber, structure.streetNumber) &&
                Objects.equals(this.streetNumberRepetitionIndex, structure.streetNumberRepetitionIndex) &&
                Objects.equals(this.streetCategoryCode, structure.streetCategoryCode) &&
                Objects.equals(this.streetLabel, structure.streetLabel) &&
                Objects.equals(this.distributionMention, structure.distributionMention) &&
                Objects.equals(this.cedexOffice, structure.cedexOffice) &&
                Objects.equals(this.postalCode, structure.postalCode) &&
                Objects.equals(this.communeCode, structure.communeCode) &&
                Objects.equals(this.countryCode, structure.countryCode) &&
                Objects.equals(this.phone, structure.phone) &&
                Objects.equals(this.phone2, structure.phone2) &&
                Objects.equals(this.fax, structure.fax) &&
                Objects.equals(this.email, structure.email) &&
                Objects.equals(this.departmentCode, structure.departmentCode) &&
                Objects.equals(this.oldStructureId, structure.oldStructureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siteSIRET, siteSIREN, siteFINESS, legalEstablishmentFINESS, structureTechnicalId, legalCommercialName, publicCommercialName, recipientAdditionalInfo, geoLocationAdditionalInfo, streetNumber, streetNumberRepetitionIndex, streetCategoryCode, streetLabel, distributionMention, cedexOffice, postalCode, communeCode, countryCode, phone, phone2, fax, email, departmentCode, oldStructureId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Structure {\n");

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
