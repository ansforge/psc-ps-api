package fr.ans.psc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de réponse pour la recherche par nom/prénom.
 * Contient l'id national et les raisons sociales associées au PS.
 */
public class PsNameSearchResult {

    @JsonProperty("nationalId")
    private String nationalId;

    @JsonProperty("companyNames")
    private List<String> companyNames;

    public PsNameSearchResult(String nationalId, List<String> companyNames) {
        this.nationalId = nationalId;
        this.companyNames = companyNames;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public List<String> getCompanyNames() {
        return companyNames;
    }

    public void setCompanyNames(List<String> companyNames) {
        this.companyNames = companyNames;
    }
}
