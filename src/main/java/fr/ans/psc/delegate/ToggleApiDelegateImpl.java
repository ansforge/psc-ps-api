package fr.ans.psc.delegate;

import fr.ans.psc.api.ToggleApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;
import fr.ans.psc.repository.PsRefRepository;
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

    private final PsRefRepository psRefRepository;
    private final PsRepository psRepository;
    private final MongoTemplate mongoTemplate;

    public ToggleApiDelegateImpl(PsRefRepository psRefRepository, PsRepository psRepository, MongoTemplate mongoTemplate) {
        this.psRefRepository = psRefRepository;
        this.psRepository = psRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @Transactional
    public ResponseEntity<String> togglePsref(PsRef psRef) {
        // STEP 1: check if Ps is already toggled
        PsRef storedPsRef = psRefRepository.findPsRefByNationalIdRef(psRef.getNationalIdRef());
        if (psRef.rawEquals(storedPsRef)) {
            String result = String.format("PsRef %s already references Ps %s, no need to toggle",
                    psRef.getNationalIdRef(), psRef.getNationalId());
            log.info(result);
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);

        } else {
            // STEP 2: check if targeted Ps exists
            Ps targetPs = psRepository.findByNationalId(psRef.getNationalId());
            if (targetPs == null) {
                String result = String.format("Could not toggle PsRef %s on Ps %s because this Ps does not exist",
                        psRef.getNationalIdRef(), psRef.getNationalId());
                log.error(result);
                return new ResponseEntity<>(result, HttpStatus.GONE);
            }

            // STEP 3: PHYSICALLY DELETE OLD PS
            Ps oldPs = psRepository.findByNationalId(psRef.getNationalIdRef());
            if (oldPs != null) {
                mongoTemplate.remove(oldPs);
                log.info("Ps {} successfully removed", oldPs.getNationalId());
            }

            // STEP 4: UPDATE DEPRECATED PSREF OR CREATE ONE IF NEEDED
            if (storedPsRef == null) {
                storedPsRef = new PsRef(psRef.getNationalIdRef(), psRef.getNationalId(), ApiUtils.getInstantTimestamp());
            } else {
                storedPsRef.setNationalId(psRef.getNationalId());
            }
            mongoTemplate.save(storedPsRef);
            String result = String.format("PsRef %s is now referencing Ps %s", storedPsRef.getNationalIdRef(), storedPsRef.getNationalId());
            log.info(result);

            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }
}
