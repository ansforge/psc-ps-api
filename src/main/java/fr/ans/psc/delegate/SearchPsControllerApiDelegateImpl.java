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
import fr.ans.psc.model.Ps;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchPsControllerApiDelegateImpl implements SearchPsControllerApiDelegate {

    private final MongoTemplate mongoTemplate;

    public SearchPsControllerApiDelegateImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
	
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	public ResponseEntity<List<String>> rechercherNationalIdParTraitsIdentite(String lastName, String firstNames,
			String genderCode, LocalDate birthdate, String birthTownCode, String birthCountryCode, String birthPlace) {
		
		log.debug("rechercherNationalIdParTraitsIdentite {} {} {} {} {} {} {}", lastName, firstNames, genderCode,
				birthdate, birthTownCode, birthCountryCode, birthPlace);
		
		List<String> firstNamesList = Arrays.asList(firstNames.split(" "));
        
        Query query = new Query();

        query.addCriteria(Criteria.where("firstNames.firstName").in(firstNamesList));
        query.addCriteria(Criteria.where("lastName").is(lastName));
        query.addCriteria(Criteria.where("genderCode").is(genderCode));
        query.addCriteria(Criteria.where("dateOfBirth").is(birthdate.format(formatter)));

        if (birthTownCode != null) {
            query.addCriteria(Criteria.where("birthAddressCode").is(birthTownCode));
        }
        if (birthCountryCode != null) {
            query.addCriteria(Criteria.where("birthCountryCode").is(birthCountryCode));
        }
        if (birthPlace != null) {
            query.addCriteria(Criteria.where("birthAddress").is(birthPlace));
        }

        List<Ps> pss = mongoTemplate.find(query, Ps.class);
        

		List<String> nationalIds = pss.stream().map(Ps::getNationalId).collect(Collectors.toList());
		   return new ResponseEntity<>(nationalIds, HttpStatus.OK);

	}
	
}
