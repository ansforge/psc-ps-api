package fr.ans.psc.delegate;

import fr.ans.psc.api.PsApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.repository.PsRepository;
import fr.ans.psc.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class PsApiDelegateImpl implements PsApiDelegate {


    public static final Integer PAGE_SIZE = 1000;
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
        Ps ps = psRepository.findByIdsContaining(psId);
        // check if Ps containing that id exists
        if (ps == null) {
            operationLog = "No Ps found with nationalIdRef {}";
            log.warn(operationLog, psId);
            return new ResponseEntity<>(HttpStatus.GONE);
        }
        // check if Ps is activated
        if (!ApiUtils.isPsActivated(ps)) {
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
        Ps storedPs = psRepository.findByIdsContaining(ps.getNationalId());
        // PS EXISTS, UPDATE AND REACTIVATION
        if (storedPs != null) {
            // DON'T UPDATE IF ALREADY ACTIVATED
            if (ApiUtils.isPsActivated(storedPs)) {
                log.warn("Ps {} already exists and is activated, will not be updated", ps.getNationalId());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            // set mongo _id to avoid error if it's an update
            // Then update Ps data
            log.info("Ps {} already exists, will be updated", ps.getNationalId());
            ps.set_id(storedPs.get_id());
            //if ids is null or doesn't contain nat id, we take the one from the stored ps
            setAppropriateIds(ps, storedPs);
            ps.setActivated(timestamp);
            mongoTemplate.save(ps);
            log.info("Ps {} successfully stored or updated", ps.getNationalId());
            for (String id : ps.getIds()) {
                log.info("Ps {} has been reactivated", id);
            }
        }
        // PS DOES NOT EXIST, PHYSICAL CREATION
        else {
            log.info("PS {} doesn't exist already, will be created", ps.getNationalId());
            //if ids is null or doesn't contain nat id, we put nat id in it
            setAppropriateIds(ps, null);
            ps.setActivated(timestamp);
            mongoTemplate.save(ps);
            log.info("Ps {} successfully stored or updated", ps.getNationalId());
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> updatePs(Ps ps) {
        // check if ps is activated before trying to update it
        Ps storedPs = psRepository.findByIdsContaining(ps.getNationalId());
        if (storedPs != null) {
            if (!ApiUtils.isPsActivated(storedPs)) {
                log.warn("Ps {} is deactivated, can not update it", ps.getNationalId());
                return new ResponseEntity<>(HttpStatus.GONE);
            }
            // set technical id then update
            ps.set_id(storedPs.get_id());
            // if ids is empty or null, use that of storedPs
            setAppropriateIds(ps, storedPs);
            mongoTemplate.save(ps);
            log.info("Ps {} successfully updated", ps.getNationalId());
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            log.warn("No Ps found with nationalId {}, can not update it", ps.getNationalId());
            return new ResponseEntity<>(HttpStatus.GONE);
        }
    }

    @Override
    public ResponseEntity<Void> deletePsById(String encodedPsId) {
        String psId = URLDecoder.decode(encodedPsId, StandardCharsets.UTF_8);
        Ps storedPs = psRepository.findByIdsContaining(psId);
        if (storedPs == null) {
            log.warn("No Ps found with nationalId {}, will not be deleted", psId);
            return new ResponseEntity<>(HttpStatus.GONE);
        }
        // deactivate the ps
        storedPs.setDeactivated(ApiUtils.getInstantTimestamp());
        mongoTemplate.save(storedPs);

        for ( String id : storedPs.getIds() ) {
            log.info("Ps {} successfully deleted", id);
        }

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

    @Override
    public ResponseEntity<List<Ps>> getPsByPage(BigDecimal page){
        Pageable paging = PageRequest.of(page.intValue(), PAGE_SIZE);

        Page<Ps> psPage = psRepository.findAll(paging);
        if(psPage != null && !psPage.isEmpty()) {
            ArrayList<Ps> psList = new ArrayList<>(psPage.getContent());
            return new ResponseEntity<>(psList, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.GONE);
        }
    }

//    private ArrayList<Ps> unwind(ArrayList<Ps> psList){
//        ArrayList<Ps> unwoundPsList = new ArrayList<>();
//        Ps tempPs;
//        Profession tempProfession;
//        for(Ps ps : psList){
//            if (ps.getDeactivated()==null || ps.getActivated() > ps.getDeactivated()) {
//                for (Profession profession : ps.getProfessions()) {
//                    for (Expertise expertise : profession.getExpertises()) {
//                        for (WorkSituation workSituation : profession.getWorkSituations()) {
//                            tempPs = ps.clone();
//                            tempPs.setProfessions(Arrays.asList(profession.clone()));
//                            tempPs.getProfessions().get(0).setExpertises(Arrays.asList(expertise.clone()));
//                            tempPs.getProfessions().get(0).setWorkSituations(Arrays.asList(workSituation.clone()));
//                            unwoundPsList.add(tempPs);
//                        }
//                    }
//                }
//            }
//        }
//        return unwoundPsList;
//    }

    private void setAppropriateIds(Ps psToCheck, Ps storedPs){
        if (psToCheck.getIds() == null || psToCheck.getIds().isEmpty())
            psToCheck.setIds(storedPs == null || storedPs.getIds() == null ? new ArrayList<>(Collections.singletonList(psToCheck.getNationalId())) : storedPs.getIds());
        else if (!psToCheck.getIds().contains(psToCheck.getNationalId())) psToCheck.getIds().add(psToCheck.getNationalId());
    }
}
