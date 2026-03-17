package fr.ans.psc.delegate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.ans.psc.api.SearchPsControllerApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsNameSearchResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchPsControllerApiDelegateImpl implements SearchPsControllerApiDelegate {

    private final MongoTemplate mongoTemplate;

    public SearchPsControllerApiDelegateImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
	
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	/**
	 * Crée un Pattern regex pour recherche insensible à la casse
	 * 
	 * @param text Texte à rechercher
	 * @return Pattern regex case-insensitive
	 */
	private Pattern createCaseInsensitivePattern(String text) {
		if (text == null) {
			return null;
		}
		// Échappe les caractères spéciaux regex et crée un pattern case-insensitive
		String escapedText = Pattern.quote(text);
		return Pattern.compile("^" + escapedText + "$", Pattern.CASE_INSENSITIVE);
	}

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
        query.addCriteria(Criteria.where("lastNameLower").is(lastName.toLowerCase()));
        
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
	public ResponseEntity<List<PsNameSearchResult>> rechercherParNomPrenom(String lastName, String firstNames) {

		log.debug("rechercherParNomPrenom lastName={} firstNames={}", lastName, firstNames);

		Query query = new Query();

		if (lastName != null && !lastName.isBlank()) {
			query.addCriteria(Criteria.where("lastNameLower").is(lastName.toLowerCase()));
		}

		if (firstNames != null && !firstNames.isBlank()) {
			List<String> firstNamesLower = Arrays.stream(firstNames.split(" "))
					.map(String::toLowerCase)
					.filter(s -> !s.isBlank())
					.collect(Collectors.toList());
			query.addCriteria(Criteria.where("firstNamesLowerArray").all(firstNamesLower));
		}

		List<Ps> pss = mongoTemplate.find(query, Ps.class);

		List<PsNameSearchResult> results = pss.stream().map(ps -> {
			List<String> companyNames = extractCompanyNames(ps);
			return new PsNameSearchResult(ps.getNationalId(), companyNames);
		}).collect(Collectors.toList());

		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	/**
	 * Extrait les raisons sociales (legalCommercialName) depuis les structures
	 * rattachées aux situations de travail du PS.
	 */
	private List<String> extractCompanyNames(Ps ps) {
		if (ps.getProfessions() == null) {
			return List.of();
		}
		return ps.getProfessions().stream()
				.filter(p -> p.getWorkSituations() != null)
				.flatMap(p -> p.getWorkSituations().stream())
				.map(ws -> ws.getStructure())
				.filter(s -> s != null && s.getLegalCommercialName() != null && !s.getLegalCommercialName().isBlank())
				.map(s -> s.getLegalCommercialName())
				.distinct()
				.collect(Collectors.toList());
	}

}
