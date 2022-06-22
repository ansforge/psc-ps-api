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

        Ps oldPs = psRepository.findByPsRefsNationalIdRef(psRef.getNationalIdRef());
        Ps targetPs = psRepository.findByNationalId(psRef.getNationalId());

        // STEP 1: check if target Ps exists
        if (targetPs != null) {

            // STEP 2: check if psref is already toggled
            if (targetPs.getPsRefs().stream().anyMatch(psRefToCheck -> psRefToCheck.rawEquals(psRef))) {
                String result = String.format("PsRef %s already references Ps %s, no need to toggle",
                        psRef.getNationalIdRef(), psRef.getNationalId());
                log.info(result);
                return new ResponseEntity<>(result, HttpStatus.CONFLICT);

            } else {

                // STEP 3: remove deprecated ps
                mongoTemplate.remove(oldPs);
                log.info("Ps {} successfully removed", oldPs.getNationalId());

                // STEP 4: Add the psref to the target ps
                targetPs.getPsRefs().add(new PsRef(psRef.getNationalIdRef(), psRef.getNationalId(), ApiUtils.getInstantTimestamp()));

                mongoTemplate.save(targetPs);
                String result = String.format("PsRef %s is now referencing Ps %s", psRef.getNationalIdRef(), targetPs.getNationalId());
                log.info(result);

                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        } else {
            String result = String.format("Could not toggle PsRef %s on Ps %s because this Ps does not exist",
                    psRef.getNationalIdRef(), psRef.getNationalId());
            log.error(result);
            return new ResponseEntity<>(result, HttpStatus.GONE);
        }
    }
}
