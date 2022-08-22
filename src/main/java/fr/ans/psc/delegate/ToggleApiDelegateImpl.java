package fr.ans.psc.delegate;

import fr.ans.psc.api.ToggleApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;
import fr.ans.psc.repository.PsRepository;
import fr.ans.psc.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ToggleApiDelegateImpl implements ToggleApiDelegate {
  private final PsRepository psRepository;
  private final MongoTemplate mongoTemplate;

  public ToggleApiDelegateImpl(PsRepository psRepository, MongoTemplate mongoTemplate) {
    this.psRepository = psRepository;
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  @Transactional
  public ResponseEntity<String> togglePsref(PsRef psRef) {
    String nationalIdRef = psRef.getNationalIdRef();
    String nationalId = psRef.getNationalId();

    Ps oldPs = psRepository.findByIdsContaining(nationalIdRef);
    Ps targetPs = psRepository.findByIdsContaining(nationalId);

    // STEP 1: check if target Ps exists
    if (targetPs != null) {

      // STEP 2: check if target ps contains psRef's nationalIdRef in ids
      if (targetPs.getIds().contains(nationalIdRef)) {
        String result = String.format("PsRef %s already references Ps %s, no need to toggle", psRef.getNationalIdRef(), psRef.getNationalId());
        log.info(result);
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);

      } else {
        // STEP 3: remove deprecated ps
        if (oldPs != null) {
          mongoTemplate.remove(oldPs);
          log.info("Ps {} successfully removed", oldPs.getNationalId());
        }
      }

      // STEP 4: Add the psref's nationalIdRef to the target ps ids
      targetPs.getIds().add(psRef.getNationalIdRef());
      mongoTemplate.save(targetPs);

      String result = String.format("PsRef %s is now referencing Ps %s", psRef.getNationalIdRef(), targetPs.getNationalId());
      log.info(result);

      return new ResponseEntity<>(result, HttpStatus.OK);
    } else {
      String result = String.format("Could not toggle PsRef %s on Ps %s because this Ps does not exist", psRef.getNationalIdRef(), psRef.getNationalId());
      log.error(result);
      return new ResponseEntity<>(result, HttpStatus.GONE);
    }
  }
}
