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

    @JsonProperty("structure")
    private Structure structure;

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
     * Get situId
     *
     * @return situId
     */
    @ApiModelProperty(value = "")
    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
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
                Objects.equals(this.structure, workSituation.structure);

    }

    @Override
    public int hashCode() {
        return Objects.hash(situId, modeCode, activitySectorCode, pharmacistTableSectionCode, roleCode, registrationAuthority, structure);
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

