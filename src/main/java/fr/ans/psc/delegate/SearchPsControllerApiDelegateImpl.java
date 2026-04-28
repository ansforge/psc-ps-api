package fr.ans.psc.delegate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.ans.psc.api.SearchPsControllerApiDelegate;
import fr.ans.psc.model.Profession;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsSearchResult;
import fr.ans.psc.model.WorkLocation;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchPsControllerApiDelegateImpl implements SearchPsControllerApiDelegate {

    private final MongoTemplate mongoTemplate;

    public SearchPsControllerApiDelegateImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
	
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Override
	public ResponseEntity<List<String>> rechercherNationalIdParTraitsIdentite(String lastName, String firstNames,
			String genderCode, LocalDate birthdate, String birthTownCode, String birthCountryCode, String birthplace) {
		
		log.debug("rechercherNationalIdParTraitsIdentite {} {} {} {} {} {} {}", lastName, firstNames, genderCode,
				birthdate, birthTownCode, birthCountryCode, birthplace);
		
		List<String> firstNamesList = Arrays.asList(firstNames.split(" "));
        
        Query query = new Query();

        // Recherche optimisée avec index sur champs lowercase (equality au lieu de regex)
        List<String> firstNamesLower = firstNamesList.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        
        query.addCriteria(Criteria.where("firstNamesLowerArray").all(firstNamesLower));

        // Recherche par égalité (au lieu de regex) - utilise l'index
        // OR sur lastNameLower + usualLastNameLower : un PS matche si le terme correspond
        // soit à son nom légal, soit à son nom d'usage.
        String lastNameLower = lastName.toLowerCase();
        query.addCriteria(new Criteria().orOperator(
            Criteria.where("lastNameLower").is(lastNameLower),
            Criteria.where("usualLastNameLower").is(lastNameLower)
        ));
        
        query.addCriteria(Criteria.where("genderCode").is(genderCode));
        query.addCriteria(Criteria.where("dateOfBirth").is(birthdate.format(formatter)));

        if (birthTownCode != null) {
            query.addCriteria(Criteria.where("birthAddressCode").is(birthTownCode));
        }
        if (birthCountryCode != null) {
            query.addCriteria(Criteria.where("birthCountryCode").is(birthCountryCode));
        }
        if (birthplace != null) {
            query.addCriteria(Criteria.where("birthAddressLower").is(birthplace.toLowerCase()));
        }

        List<Ps> pss = mongoTemplate.find(query, Ps.class);

		List<String> nationalIds = pss.stream().map(Ps::getNationalId).collect(Collectors.toList());
		return new ResponseEntity<>(nationalIds, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<List<PsSearchResult>> rechercherParNomPrenom(String lastName, String firstNames) {

		log.debug("rechercherParNomPrenom lastName={} firstNames={}", lastName, firstNames);

		Query query = new Query();

		if (lastName != null && !lastName.isBlank()) {
			String lastNameLower = lastName.toLowerCase();
			Criteria lastNameCriteria = new Criteria().orOperator(
					Criteria.where("lastNameLower").is(lastNameLower),
					Criteria.where("usualLastNameLower").is(lastNameLower),
					Criteria.where("professions.lastNameLower").is(lastNameLower)
			);
			query.addCriteria(lastNameCriteria);
		}

		if (firstNames != null && !firstNames.isBlank()) {
			List<String> firstNamesLower = Arrays.stream(firstNames.split(" "))
					.map(String::toLowerCase)
					.filter(s -> !s.isBlank())
					.collect(Collectors.toList());
			query.addCriteria(Criteria.where("firstNamesLowerArray").all(firstNamesLower));
		}

		List<Ps> pss = mongoTemplate.find(query, Ps.class);

		List<PsSearchResult> results = pss.stream().map(ps -> {
			Profession firstProfession = (ps.getProfessions() != null && !ps.getProfessions().isEmpty())
					? ps.getProfessions().get(0) : null;
			String professionCode = firstProfession != null ? firstProfession.getCode() : null;
			List<WorkLocation> workLocations = extractWorkLocations(firstProfession);
			return new PsSearchResult(ps.getNationalId(), professionCode, workLocations);
		}).collect(Collectors.toList());

		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	/**
	 * Extrait les lieux d'exercice (raison sociale + cedex) depuis la première profession du PS.
	 */
	private List<WorkLocation> extractWorkLocations(Profession profession) {
		if (profession == null || profession.getWorkSituations() == null) {
			return List.of();
		}
		return profession.getWorkSituations().stream()
				.map(ws -> ws.getStructure())
				.filter(s -> s != null)
				.map(s -> new WorkLocation(s.getLegalCommercialName(), s.getCedexOffice()))
				.collect(Collectors.toList());
	}

}
