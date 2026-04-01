package fr.ans.psc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO représentant un lieu d'exercice (structure rattachée à une situation de travail).
 */
public class WorkLocation {

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("companyCedexOffice")
    private String companyCedexOffice;

    public WorkLocation(String companyName, String companyCedexOffice) {
        this.companyName = companyName;
        this.companyCedexOffice = companyCedexOffice;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyCedexOffice() {
        return companyCedexOffice;
    }

    public void setCompanyCedexOffice(String companyCedexOffice) {
        this.companyCedexOffice = companyCedexOffice;
    }
}
