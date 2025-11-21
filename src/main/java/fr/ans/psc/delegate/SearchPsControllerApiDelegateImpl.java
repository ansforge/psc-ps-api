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

        // Recherche insensible à la casse pour les prénoms
        // On crée une regex case-insensitive pour chaque prénom
        List<Pattern> firstNamePatterns = firstNamesList.stream()
            .map(this::createCaseInsensitivePattern)
            .collect(Collectors.toList());
        
        query.addCriteria(Criteria.where("firstNames.firstName").all(firstNamePatterns));
        
        // Recherche insensible à la casse pour le nom
        query.addCriteria(Criteria.where("lastName").regex(createCaseInsensitivePattern(lastName)));
        
        query.addCriteria(Criteria.where("genderCode").is(genderCode));
        query.addCriteria(Criteria.where("dateOfBirth").is(birthdate.format(formatter)));

        if (birthTownCode != null) {
            query.addCriteria(Criteria.where("birthAddressCode").is(birthTownCode));
        }
        if (birthCountryCode != null) {
            query.addCriteria(Criteria.where("birthCountryCode").is(birthCountryCode));
        }
        if (birthplace != null) {
            query.addCriteria(Criteria.where("birthAddress").is(birthplace));
        }

        List<Ps> pss = mongoTemplate.find(query, Ps.class);
        

		List<String> nationalIds = pss.stream().map(Ps::getNationalId).collect(Collectors.toList());
		   return new ResponseEntity<>(nationalIds, HttpStatus.OK);

	}
	
}
