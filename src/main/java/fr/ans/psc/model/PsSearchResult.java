package fr.ans.psc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de réponse pour la recherche par traits d'identité.
 * Contient l'id national, le code profession et les lieux d'exercice du PS.
 */
public class PsSearchResult {

    @JsonProperty("nationalId")
    private String nationalId;

    @JsonProperty("professionCode")
    private String professionCode;

    @JsonProperty("workLocations")
    private List<WorkLocation> workLocations;

    public PsSearchResult(String nationalId, String professionCode, List<WorkLocation> workLocations) {
        this.nationalId = nationalId;
        this.professionCode = professionCode;
        this.workLocations = workLocations;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getProfessionCode() {
        return professionCode;
    }

    public void setProfessionCode(String professionCode) {
        this.professionCode = professionCode;
    }

    public List<WorkLocation> getWorkLocations() {
        return workLocations;
    }

    public void setWorkLocations(List<WorkLocation> workLocations) {
        this.workLocations = workLocations;
    }
}
