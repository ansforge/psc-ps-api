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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.ans.psc.api.PsApiDelegate;
import fr.ans.psc.model.AlternativeIdentifier;
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
        Ps ps;
        
        // Optimization: Try nationalId first (uses unique index, < 1ms)
        ps = psRepository.findByNationalId(psId);
        
        // Fallback: search in ids array if not found by nationalId
        if (ps == null) {
            try {
                ps = psRepository.findByIdsContaining(psId);
                
                // If found but deactivated, search for active merged account
                if (ps != null && !ApiUtils.isPsActivated(ps)) {
                    log.info("PS {} found but deactivated, searching for active merged account", psId);
                    Query query = new Query(Criteria.where("ids").in(psId).and("activated").ne(null));
                    List<Ps> activePsList = mongoTemplate.find(query, Ps.class);
                    ps = activePsList.stream()
                            .filter(ApiUtils::isPsActivated)
                            .findFirst()
                            .orElse(ps); // Keep deactivated one if no active found
                }
            } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
                log.warn("Multiple PS found with id {}, searching for active one using MongoTemplate", psId);
                // Use MongoTemplate to get all PS containing this ID and find the active one
                Query query = new Query(Criteria.where("ids").in(psId));
                List<Ps> psList = mongoTemplate.find(query, Ps.class);
                ps = psList.stream()
                        .filter(ApiUtils::isPsActivated)
                        .findFirst()
                        .orElse(psList.isEmpty() ? null : psList.get(0));
            }
        }
        
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
        Ps storedPs;
        
        try {
            storedPs = psRepository.findByIdsContaining(ps.getNationalId());
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            log.warn("Multiple PS found with nationalId {}, searching for active one using MongoTemplate", ps.getNationalId());
            Query query = new Query(Criteria.where("ids").in(ps.getNationalId()));
            List<Ps> psList = mongoTemplate.find(query, Ps.class);
            storedPs = psList.stream()
                    .filter(ApiUtils::isPsActivated)
                    .findFirst()
                    .orElse(psList.isEmpty() ? null : psList.get(0));
        }
        
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
        Ps storedPs;
        
        try {
            storedPs = psRepository.findByIdsContaining(searchId);
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            log.warn("Multiple PS found with searchId {}, searching for active one using MongoTemplate", searchId);
            // Use MongoTemplate to get all PS containing this ID and find the active one
            Query query = new Query(Criteria.where("ids").in(searchId));
            List<Ps> psList = mongoTemplate.find(query, Ps.class);
            storedPs = psList.stream()
                    .filter(ApiUtils::isPsActivated)
                    .findFirst()
                    .orElse(psList.isEmpty() ? null : psList.get(0));
        }
        
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
                // Always preserve professions from stored PS for UUID-based PS (PSI)
                if (storedPs.getProfessions() != null && !storedPs.getProfessions().isEmpty()) {
                    ps.setProfessions(storedPs.getProfessions());
                    log.info("Preserved existing professions from stored PS for {}", ps.getNationalId());
                } else {
                    // If stored PS has no professions, look for professions in alternativeIdentifiers
                    log.info("Stored PS has no professions, looking in alternative identifiers");
                    if (ps.getAlternativeIds() != null) {
                        for (fr.ans.psc.model.AlternativeIdentifier altId : ps.getAlternativeIds()) {
                            if (!altId.getIdentifier().equals(ps.getNationalId())) {
                                try {
                                    Ps professionPs = psRepository.findByNationalId(altId.getIdentifier());
                                    if (professionPs != null && professionPs.getProfessions() != null && !professionPs.getProfessions().isEmpty()) {
                                        ps.setProfessions(professionPs.getProfessions());
                                        log.info("Found and preserved professions from PS {}", altId.getIdentifier());
                                        break;
                                    }
                                } catch (Exception e) {
                                    log.warn("Could not find professions for alternative identifier {}", altId.getIdentifier());
                                }
                            }
                        }
                    }
                }
            }
            // if ids is empty or null, use that of storedPs
            ApiUtils.setAppropriateIds(ps, storedPs);
            
            // Ensure the new nationalId (UUID) is always in the ids list for searchability
            if (!ps.getIds().contains(ps.getNationalId())) {
                ps.getIds().add(ps.getNationalId());
                log.info("Added nationalId {} to ids list for PS {}", ps.getNationalId(), existingId);
                // Regenerate alternativeIds to include the newly added nationalId
                // Save existing quality values first
                java.util.Map<String, Integer> qualityMap = new java.util.HashMap<>();
                if (ps.getAlternativeIds() != null) {
                    for (fr.ans.psc.model.AlternativeIdentifier altId : ps.getAlternativeIds()) {
                        if (altId.getIdentifier() != null && altId.getQuality() != null) {
                            qualityMap.put(altId.getIdentifier(), altId.getQuality());
                        }
                    }
                }
                ps.setAlternativeIds(ApiUtils.idTripletCreationFromIds(ps.getIds()));
                // Restore quality values
                if (!qualityMap.isEmpty() && ps.getAlternativeIds() != null) {
                    for (fr.ans.psc.model.AlternativeIdentifier altId : ps.getAlternativeIds()) {
                        if (altId.getIdentifier() != null && qualityMap.containsKey(altId.getIdentifier())) {
                            altId.setQuality(qualityMap.get(altId.getIdentifier()));
                        }
                    }
                }
            }
            
            // For UUID-based PS (PSI): before saving, fetch and merge alternativeIds from
            // referenced RPPS fiches (e.g. CAB_RPPS identifiers). This ensures they are
            // preserved even when togglePsref takes the "already done" fast-path.
            if (ApiUtils.isValidUUID(ps.getNationalId()) && ps.getAlternativeIds() != null) {
                Set<String> existingAltIdIdentifiers = new HashSet<>();
                for (fr.ans.psc.model.AlternativeIdentifier a : ps.getAlternativeIds()) {
                    if (a.getIdentifier() != null) existingAltIdIdentifiers.add(a.getIdentifier());
                }
                for (fr.ans.psc.model.AlternativeIdentifier altId : new ArrayList<>(ps.getAlternativeIds())) {
                    String altIdentifier = altId.getIdentifier();
                    if (altIdentifier != null && !altIdentifier.equals(ps.getNationalId())) {
                        try {
                            // Search by nationalId, ids array, OR alternativeIds.identifier to catch CAB_RPPS
                            Query refQuery = new Query(new org.springframework.data.mongodb.core.query.Criteria().orOperator(
                                    Criteria.where("nationalId").is(altIdentifier),
                                    Criteria.where("ids").in(altIdentifier),
                                    Criteria.where("alternativeIds.identifier").is(altIdentifier)
                            ));
                            List<Ps> refPsList = mongoTemplate.find(refQuery, Ps.class);
                            // Exclude the PSI fiche itself
                            for (Ps rppsPs : refPsList) {
                                if (rppsPs.getNationalId().equals(ps.getNationalId())) continue;
                                if (rppsPs.getAlternativeIds() != null) {
                                    for (fr.ans.psc.model.AlternativeIdentifier rppsAltId : rppsPs.getAlternativeIds()) {
                                        if (rppsAltId.getIdentifier() != null
                                                && !existingAltIdIdentifiers.contains(rppsAltId.getIdentifier())
                                                && !ApiUtils.isValidUUID(rppsAltId.getIdentifier())) {
                                            ps.getAlternativeIds().add(rppsAltId);
                                            existingAltIdIdentifiers.add(rppsAltId.getIdentifier());
                                            if (!ps.getIds().contains(rppsAltId.getIdentifier())) {
                                                ps.getIds().add(rppsAltId.getIdentifier());
                                            }
                                            log.info("Pre-merged alternativeId {} ({}) from fiche {} into PSI {}",
                                                    rppsAltId.getIdentifier(), rppsAltId.getOrigine(),
                                                    rppsPs.getNationalId(), ps.getNationalId());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Could not pre-merge alternativeIds from {}: {}", altIdentifier, e.getMessage());
                        }
                    }
                }
            }

            ps = mongoTemplate.save(ps);
            log.info("Ps {} successfully updated", ps.getNationalId());
            
            // Trigger fusion based on nationalId change (existing logic)
            if (existingId != null && !"".equals(existingId) && !ps.getNationalId().equals(existingId)) {
                PsRef psRef = new PsRef(existingId, ps.getNationalId(), ps.getActivated(), ps.getDeactivated());
                toggleApiDelegateImpl.togglePsref(psRef);
            }
            
            // NEW LOGIC: Trigger fusion based on alternativeIdentifiers
            // If nationalId is PSI UUID and alternativeIdentifiers contain other PS to merge
            if (ApiUtils.isValidUUID(ps.getNationalId()) && ps.getAlternativeIds() != null) {
                for (fr.ans.psc.model.AlternativeIdentifier altId : ps.getAlternativeIds()) {
                    String altIdentifier = altId.getIdentifier();
                    // Skip the PS's own nationalId
                    if (!altIdentifier.equals(ps.getNationalId())) {
                        // Check if there's an existing PS with this altIdentifier - handle multiple results
                        try {
                            Ps existingPsToMerge = psRepository.findByIdsContaining(altIdentifier);
                            // Only merge if the existing PS is activated
                            if (existingPsToMerge != null && !existingPsToMerge.getNationalId().equals(ps.getNationalId()) 
                                    && ApiUtils.isPsActivated(existingPsToMerge)) {
                                log.info("Found existing activated PS {} to merge with {}", altIdentifier, ps.getNationalId());
                                PsRef psRef = new PsRef(altIdentifier, ps.getNationalId(), ps.getActivated(), ps.getDeactivated());
                                toggleApiDelegateImpl.togglePsref(psRef);
                            } else if (existingPsToMerge != null && !ApiUtils.isPsActivated(existingPsToMerge)) {
                                log.warn("PS {} is deactivated, skipping merge with {}", altIdentifier, ps.getNationalId());
                            }
                        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
                            log.warn("Multiple PS found with altIdentifier {}, using findByNationalId instead", altIdentifier);
                            Ps existingPsToMerge = psRepository.findByNationalId(altIdentifier);
                            // Only merge if the existing PS is activated
                            if (existingPsToMerge != null && !existingPsToMerge.getNationalId().equals(ps.getNationalId()) 
                                    && ApiUtils.isPsActivated(existingPsToMerge)) {
                                log.info("Found existing activated PS {} to merge with {}", altIdentifier, ps.getNationalId());
                                PsRef psRef = new PsRef(altIdentifier, ps.getNationalId(), ps.getActivated(), ps.getDeactivated());
                                toggleApiDelegateImpl.togglePsref(psRef);
                            } else if (existingPsToMerge != null && !ApiUtils.isPsActivated(existingPsToMerge)) {
                                log.warn("PS {} is deactivated, skipping merge with {}", altIdentifier, ps.getNationalId());
                            }
                        }
                    }
                }
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
        Ps storedPs;
        
        try {
            storedPs = psRepository.findByIdsContaining(psId);
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            log.warn("Multiple PS found with psId {}, searching for active one using MongoTemplate", psId);
            Query query = new Query(Criteria.where("ids").in(psId));
            List<Ps> psList = mongoTemplate.find(query, Ps.class);
            storedPs = psList.stream()
                    .filter(ApiUtils::isPsActivated)
                    .findFirst()
                    .orElse(psList.isEmpty() ? null : psList.get(0));
        }
        
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

        // Use MongoTemplate with pagination
        Query query = new Query().with(paging);
        
        List<Ps> psList = mongoTemplate.find(query, Ps.class);
        
        if (psList != null && !psList.isEmpty()) {
            log.debug("List of Ps successfully retrieved");
            return new ResponseEntity<>(psList, HttpStatus.OK);
        } else {
            log.debug("No more Ps on this page");
            return new ResponseEntity<>(null, HttpStatus.GONE);
        }
    }
}
