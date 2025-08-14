package fr.ans.psc.delegate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.threeten.bp.LocalDate;

import fr.ans.psc.api.SearchPsControllerApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.repository.PsRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchPsControllerApiDelegateImpl implements SearchPsControllerApiDelegate {

    private final PsRepository psRepository;

    public SearchPsControllerApiDelegateImpl(PsRepository psRepository) {
        this.psRepository = psRepository;
    }
	

	@Override
	public ResponseEntity<List<String>> rechercherNationalIdParTraitsIdentite(String lastName, String firstNames,
			String genderCode, String birthdate, String birthTownCode, String birthCountryCode, String birthPlace) {
		List<String> firstNamesList = Arrays.asList(firstNames.split(" "));
		List<Ps> pss =  psRepository.findPsByIdentity(firstNamesList, lastName,
				genderCode, birthdate, birthTownCode);
		List<String> nationalIds = pss.stream().map(Ps::getNationalId).collect(Collectors.toList());
		   return new ResponseEntity<>(nationalIds, HttpStatus.OK);

	}
	
}
