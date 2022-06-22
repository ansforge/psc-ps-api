package fr.ans.psc.delegate;

import fr.ans.psc.api.PsApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;
import fr.ans.psc.repository.PsRepository;
import fr.ans.psc.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PsApiDelegateImpl implements PsApiDelegate {

    private final PsRepository psRepository;
    private final MongoTemplate mongoTemplate;

    public PsApiDelegateImpl(PsRepository psRepository, MongoTemplate mongoTemplate) {
        this.psRepository = psRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ResponseEntity<Ps> getPsById(String encodedPsId) {
        String psId = URLDecoder.decode(encodedPsId, StandardCharsets.UTF_8);
        String operationLog;

        Ps ps = psRepository.findByPsRefsNationalIdRef(psId);

        // check if Ps containing a PsRef with that nationalIdRef exists
        if (ps == null) {
            operationLog = "No Ps found with nationalIdRef {}";
            log.warn(operationLog, psId);
            return new ResponseEntity<>(HttpStatus.GONE);
        }

        PsRef psRef = ps.getPsRefs().stream().filter(ref -> ref.getNationalIdRef().equals(psId)).findFirst().orElse(null);

        // check if PsRef eis activated
        if (!ApiUtils.isPsRefActivated(psRef)) {
            operationLog = "Ps {} is deactivated";
            log.warn(operationLog, psId);
            return new ResponseEntity<>(HttpStatus.GONE);
        }

        log.info("Ps {} has been found", ps.getNationalId());
        return new ResponseEntity<>(ps, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> createNewPs(Ps ps) {
        long timestamp = ApiUtils.getInstantTimestamp();
        Ps storedPs = psRepository.findByPsRefsNationalIdRef(ps.getNationalId());

        // PSREF EXIST, UPDATE AND REACTIVATION
        if (storedPs != null) {
            PsRef storedPsRef = storedPs.getPsRefs().stream().filter(ref -> ref.getNationalIdRef().equals(ps.getNationalId())).findFirst().orElse(null);

            // DON'T UPDATE IF ALREADY ACTIVATED
            if (ApiUtils.isPsRefActivated(storedPsRef)) {
                log.warn("Ps {} already exists and is activated, will not be updated", ps.getNationalId());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            // set mongo _id to avoid error if it's an update
            // Then update Ps data
            log.info("Ps {} already exists, will be updated", ps.getNationalId());
            ps.set_id(storedPs.get_id());
            mongoTemplate.save(ps);
            log.info("Ps {} successfully stored or updated", ps.getNationalId());

            // REACTIVATE ALL PSREF THAT POINTED TOWARDS UPDATED PS
            // It's programmatically possible that the updated Ps has a modified nationalId (and still the same mongo _id)
            // So we reset every PsRef pointer with the nationalId of the updated Ps no matter that it has actually changed or not
            List<PsRef> psRefList = (storedPs.getPsRefs() == null ? new ArrayList<>() : storedPs.getPsRefs());
            log.info("psRefList size {}", psRefList.size());
            psRefList.stream().filter(psRef -> !ApiUtils.isPsRefActivated(psRef)).forEach(psRef -> {
                psRef.setActivated(timestamp);
                psRef.setNationalId(storedPs.getNationalId());
                log.info("PsRef {} has been reactivated", psRef.getNationalIdRef());
            });
            mongoTemplate.save(storedPs);

        }
        // PREF DOES NOT EXIST, PHYSICAL CREATION
        else {
            log.info("PS {} doesn't exist already, will be created", ps.getNationalId());
            PsRef storedPsRef = new PsRef(ps.getNationalId(), ps.getNationalId(), timestamp);
            ps.getPsRefs().add(storedPsRef);
            mongoTemplate.save(ps);
            log.info("Ps {} successfully stored or updated", ps.getNationalId());
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> updatePs(Ps ps) {
        // check if PsRef is activated before trying to update it
        Ps storedPs = psRepository.findByPsRefsNationalIdRef(ps.getNationalId());
        if (storedPs != null) {
            PsRef storedPsRef = storedPs.getPsRefs().stream().filter(ref -> ref.getNationalIdRef().equals(ps.getNationalId())).findFirst().orElse(null);
            if (!ApiUtils.isPsRefActivated(storedPsRef)) {
                log.warn("Ps {} is deactivated, can not update it", ps.getNationalId());
                return new ResponseEntity<>(HttpStatus.GONE);
            }

            // set technical id then update
            ps.set_id(storedPs.get_id());
        } else {
            log.warn("No Ps found with nationalId {}, can not update it", ps.getNationalId());
            return new ResponseEntity<>(HttpStatus.GONE);
        }
        mongoTemplate.save(ps);
        log.info("Ps {} successfully updated", ps.getNationalId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deletePsById(String encodedPsId) {
        String psId = URLDecoder.decode(encodedPsId, StandardCharsets.UTF_8);
        Ps storedPs = psRepository.findByPsRefsNationalIdRef(psId);
        if (storedPs == null) {
            log.warn("No Ps found with nationalId {}, will not be deleted", psId);
            return new ResponseEntity<>(HttpStatus.GONE);
        }

        // get all PsRefs that point to this ps
        List<PsRef> psRefList = storedPs.getPsRefs();

        // deactivate each PsRef pointing to this ps
        long timestamp = ApiUtils.getInstantTimestamp();

        psRefList.forEach(psRef -> {
            psRef.setDeactivated(timestamp);
            log.info("Ps {} successfully deleted", psRef.getNationalIdRef());
        });
        mongoTemplate.save(storedPs);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> forceDeletePsById(String encodedPsId) {
        String psId = URLDecoder.decode(encodedPsId, StandardCharsets.UTF_8);
        Ps ps = psRepository.findByNationalId(psId);

        if (ps == null) {
            log.warn("No Ps found with id {}, could not delete it", psId);
            return new ResponseEntity<>(HttpStatus.GONE);
        }

        mongoTemplate.remove(ps);
        log.info("Ps {} successfully deleted", psId);


        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
