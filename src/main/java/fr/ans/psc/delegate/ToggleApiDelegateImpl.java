package fr.ans.psc.delegate;

import fr.ans.psc.api.ToggleApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;
import fr.ans.psc.repository.PsRefRepository;
import fr.ans.psc.repository.PsRepository;
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
    public ResponseEntity<Void> togglePsref(PsRef psRef) {
        // STEP 1: check if Ps is already toggled
        PsRef storedPsRef = psRefRepository.findPsRefByNationalIdRef(psRef.getNationalIdRef());
        if (storedPsRef.rawEquals(psRef)) {
            log.info("PsRef {} already references Ps {}, no need to toggle", psRef.getNationalIdRef(), psRef.getNationalId());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // STEP 2: check if targeted Ps exists
        Ps targetPs = psRepository.findByNationalId(psRef.getNationalId());
        if (targetPs == null) {
            log.error("Could not toggle PsRef {} on Ps {} because this Ps does not exist", psRef.getNationalIdRef(), psRef.getNationalId());
            return new ResponseEntity<>(HttpStatus.GONE);
        }

        // STEP 3: PHYSICALLY DELETE OLD PS
        Ps oldPs = psRepository.findByNationalId(psRef.getNationalIdRef());
        mongoTemplate.remove(oldPs);
        log.info("Ps {} successfully removed", oldPs.getNationalId());

        // STEP 4: UPDATE DEPRECATED PSREF
        storedPsRef.setNationalId(psRef.getNationalId());
        mongoTemplate.save(storedPsRef);
        log.info("PsRef {} is now referencing Ps {}", storedPsRef.getNationalIdRef(), storedPsRef.getNationalId());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
