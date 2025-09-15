/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.delegate;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.ans.psc.api.PsApiDelegate;
import fr.ans.psc.model.Profession;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;
import fr.ans.psc.repository.PsRepository;
import fr.ans.psc.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PsApiDelegateImpl implements PsApiDelegate {

    public static final Integer PAGE_SIZE = 1000;
    private final PsRepository psRepository;
    private final MongoTemplate mongoTemplate;
    private final ToggleApiDelegateImpl toggleApiDelegateImpl;

    public PsApiDelegateImpl(PsRepository psRepository, MongoTemplate mongoTemplate,
            ToggleApiDelegateImpl toggleApiDelegateImpl) {
        this.psRepository = psRepository;
        this.mongoTemplate = mongoTemplate;
        this.toggleApiDelegateImpl = toggleApiDelegateImpl;
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
        // Remove prof - COMMENTED OUT to preserve professions data
        // ps.setProfessions(new ArrayList<Profession>());
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
            // if ids is null or doesn't contain nat id, we take the one from the stored ps
            ApiUtils.setAppropriateIds(ps, storedPs);
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
            // if ids is null or doesn't contain nat id, we put nat id in it
            ApiUtils.setAppropriateIds(ps, null);
            ps.setActivated(timestamp);
            mongoTemplate.save(ps);
            log.info("Ps {} successfully stored or updated", ps.getNationalId());
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> updatePs(Ps ps, String existingId) {
        // check if ps is activated before trying to update it
        // Use existingId parameter to find the stored PS, not ps.getNationalId()
        String searchId = existingId != null && !existingId.isEmpty() ? existingId : ps.getNationalId();
        Ps storedPs = psRepository.findByIdsContaining(searchId);
        if (storedPs != null) {
            if (!ApiUtils.isPsActivated(storedPs)) {
                log.warn("Ps {} is deactivated, can not update it", ps.getNationalId());
                return new ResponseEntity<>(HttpStatus.GONE);
            }
            // set technical id then update
            ps.set_id(storedPs.get_id());
            ps.setActivated(storedPs.getActivated());
            ps.setDeactivated(storedPs.getDeactivated());
            if (ApiUtils.isValidUUID(ps.getNationalId())) {
                ps.setProfessions(storedPs.getProfessions());
            }
            // if ids is empty or null, use that of storedPs
            ApiUtils.setAppropriateIds(ps, storedPs);
            
            // Ensure the new nationalId (UUID) is always in the ids list for searchability
            if (!ps.getIds().contains(ps.getNationalId())) {
                ps.getIds().add(ps.getNationalId());
                log.info("Added nationalId {} to ids list for PS {}", ps.getNationalId(), existingId);
            }
            
            // Regenerate alternativeIds with the updated ids list
            ps.setAlternativeIds(ApiUtils.idTripletCreationFromIds(ps.getIds()));
            
            ps = mongoTemplate.save(ps);
            log.info("Ps {} successfully updated", ps.getNationalId());
            if (existingId != null && !"".equals(existingId) && !ps.getNationalId().equals(existingId)) {
                PsRef psRef = new PsRef(existingId, ps.getNationalId(), ps.getActivated(), ps.getDeactivated());
                toggleApiDelegateImpl.togglePsref(psRef);
            }
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

        for (String id : storedPs.getIds()) {
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
    public ResponseEntity<List<Ps>> getPsByPage(BigDecimal page, BigDecimal size) {
        log.debug("get Ps By Page, page {} of size {}", page, size == null ? PAGE_SIZE : size.intValue());
        Pageable paging = PageRequest.of(page.intValue(), size == null ? PAGE_SIZE : size.intValue());

        Page<Ps> psPage = psRepository.findAll(paging);
        if (psPage != null && !psPage.isEmpty()) {
            ArrayList<Ps> psList = new ArrayList<>(psPage.getContent());
            log.debug("List of Ps successfully retrieved");
            return new ResponseEntity<>(psList, HttpStatus.OK);
        } else {
            log.debug("No more Ps on this page");
            return new ResponseEntity<>(null, HttpStatus.GONE);
        }
    }
}
