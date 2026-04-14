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

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ans.psc.api.ToggleApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;
import fr.ans.psc.repository.PsRepository;
import fr.ans.psc.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;

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
		
		log.info("TOGGLE FUSION START: nationalIdRef={}, nationalId={}", nationalIdRef, nationalId);

		String origin1 = ApiUtils.determineOriginAndType(nationalId).get(ApiUtils.ORIGIN);
		String origin2 = ApiUtils.determineOriginAndType(nationalIdRef).get(ApiUtils.ORIGIN);
		
		log.debug("Origins: origin1={}, origin2={}", origin1, origin2);

		String targetId;
		String oldId;

		Ps targetPs = new Ps();
		Ps oldPs = new Ps();

		// PSI/RPPS
		if ((origin1.equals(ApiUtils.PSI) && origin2.equals(ApiUtils.RPPS))
				|| (origin1.equals(ApiUtils.RPPS) && origin2.equals(ApiUtils.PSI))) {

			targetId = origin1.equals(ApiUtils.PSI) ? nationalId : nationalIdRef;
			oldId = origin1.equals(ApiUtils.RPPS) ? nationalId : nationalIdRef;

			targetPs = findPsByNationalIdOrIds(targetId);
			oldPs = findPsByNationalIdOrIds(oldId);

			if (targetPs != null && oldPs != null) {
				if (oldPs.getProfessions() != null) {
					final String sourceId = oldId;
					oldPs.getProfessions().forEach(prof -> {
						if (prof.getSourceId() == null) {
							prof.setSourceId(sourceId);
						}
					});
				}
				// Tag absorbing account's own professions with its nationalId
				if (targetPs.getProfessions() != null) {
					final String targetSourceId = targetId;
					targetPs.getProfessions().forEach(prof -> {
						if (prof.getSourceId() == null) {
							prof.setSourceId(targetSourceId);
						}
					});
				}
				// Merge professions: keep existing ones from other sources, replace those from this source
				List<fr.ans.psc.model.Profession> mergedProfessions = new ArrayList<>();
				if (targetPs.getProfessions() != null) {
					targetPs.getProfessions().stream()
						.filter(p -> !oldId.equals(p.getSourceId()))
						.forEach(mergedProfessions::add);
				}
				if (oldPs.getProfessions() != null) {
					mergedProfessions.addAll(oldPs.getProfessions());
				}
				targetPs.setProfessions(mergedProfessions);
				targetPs.setIdType(ApiUtils.determineOriginAndType(targetPs.getNationalId()).get(ApiUtils.ID_TYPE));
			}
		}
		// PSI
		else if (origin1.equals(ApiUtils.PSI) || origin2.equals(ApiUtils.PSI)) {

			if (origin1.equals(origin2)) {
				String result = String.format("Both origins are PSI, impossible merge");
				log.info(result);
				return new ResponseEntity<>(result, HttpStatus.NOT_ACCEPTABLE);

			}
			targetId = origin1.equals(ApiUtils.PSI) ? nationalId : nationalIdRef;
			oldId = origin1.equals(ApiUtils.PSI) ? nationalIdRef : nationalId;
		}
		// RPPS
		else if (origin1.equals(ApiUtils.RPPS) || origin2.equals(ApiUtils.RPPS) && !origin1.equals(origin2)) {

			targetId = origin1.equals(ApiUtils.RPPS) ? nationalId : nationalIdRef;
			oldId = origin1.equals(ApiUtils.RPPS) ? nationalIdRef : nationalId;
		}
		// ADELI
		else if ((origin1.equals(ApiUtils.ADELI) || origin2.equals(ApiUtils.ADELI)) && !origin1.equals(origin2)) {

			targetId = origin1.equals(ApiUtils.ADELI) ? nationalIdRef : nationalId;
			oldId = origin1.equals(ApiUtils.ADELI) ? nationalId : nationalIdRef;
		}
		// Defaut
		else {
			targetId = nationalId;
			oldId = nationalIdRef;
		}
		
		log.info("Fusion decision: targetId={} (will keep), oldId={} (will delete)", targetId, oldId);

		if (!((origin1.equals(ApiUtils.PSI) && origin2.equals(ApiUtils.RPPS))
				|| (origin1.equals(ApiUtils.RPPS) && origin2.equals(ApiUtils.PSI)))) {

			targetPs = findPsByNationalIdOrIds(targetId);
			oldPs = findPsByNationalIdOrIds(oldId);
			
			log.info("Found targetPs: {} (nationalId={})", targetPs != null, targetPs != null ? targetPs.getNationalId() : "null");
			log.info("Found oldPs: {} (nationalId={})", oldPs != null, oldPs != null ? oldPs.getNationalId() : "null");

		}

		// STEP 1: check if target Ps exists
		if (targetPs != null) {
			targetPs.setIdType(ApiUtils.determineOriginAndType(targetPs.getNationalId()).get(ApiUtils.ID_TYPE));
			
			// STEP 2: check if target ps contains psRef's nationalIdRef in ids
			if (targetPs.getIds().contains(oldId)) {
				log.info("Fusion already done: targetPs {} already contains oldId {}", targetPs.getNationalId(), oldId);
				
				// CRITICAL FIX: Clean up orphaned oldPs if it still exists
				if (oldPs != null && !oldPs.getNationalId().equals(targetPs.getNationalId())) {
					log.warn("CLEANUP: Found orphaned PS {} that should have been deleted during fusion. Deleting now.", oldPs.getNationalId());
					// Merge alternativeIds from oldPs before deleting it (e.g. CAB_RPPS identifiers)
					ApiUtils.setAppropriateIds(targetPs, oldPs);
					mongoTemplate.save(targetPs);
					log.info("Merged alternativeIds from orphaned PS {} into {}", oldPs.getNationalId(), targetPs.getNationalId());
					mongoTemplate.remove(oldPs);
					log.info("Orphaned PS {} successfully removed", oldPs.getNationalId());
					
					String result = String.format("Fusion was already done, but cleaned up orphaned PS %s", oldId);
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
				
				String result = String.format("PsRef %s already references Ps %s, no need to toggle", oldId, targetId);
				log.info(result);
				return new ResponseEntity<>(result, HttpStatus.CONFLICT);

			} else {
				// STEP 2.5: Ensure oldPs is not the same as targetPs (prevent deleting wrong PS)
				if (oldPs != null && oldPs.getNationalId().equals(targetPs.getNationalId())) {
					String result = String.format("Cannot merge: oldPs and targetPs are the same (%s). Fusion already done?", targetPs.getNationalId());
					log.warn(result);
					return new ResponseEntity<>(result, HttpStatus.CONFLICT);
				}
				
				// STEP 2.6: check if oldPs is activated before merging
				if (oldPs != null && !ApiUtils.isPsActivated(oldPs)) {
					String result = String.format("Cannot merge deactivated Ps %s with Ps %s", oldId, targetId);
					log.warn(result);
					return new ResponseEntity<>(result, HttpStatus.NOT_ACCEPTABLE);
				}
				
				// STEP 3: remove deprecated ps (CRITICAL - ensures old PS is deleted)
				if (oldPs != null) {
					log.info("Removing old PS {} (will be merged into {})", oldPs.getNationalId(), targetPs.getNationalId());
					mongoTemplate.remove(oldPs);
					log.info("Ps {} successfully removed from database", oldPs.getNationalId());
				} else {
					log.warn("oldPs is null for id {}, cannot remove (may already be deleted)", oldId);
				}
			}

			// STEP 4: Add the psref's nationalIdRef to the target ps ids
			targetPs.getIds().add(oldId);
			ApiUtils.setAppropriateIds(targetPs, oldPs);
			mongoTemplate.save(targetPs);

			String result = String.format("PsRef %s is now referencing Ps %s", targetId, oldId);
			log.info(result);

			return new ResponseEntity<>(result, HttpStatus.OK);
		} else {
			String result = String.format("Could not toggle PsRef %s on Ps %s because this Ps does not exist", oldId,
					targetId);
			log.error(result);
			return new ResponseEntity<>(result, HttpStatus.GONE);
		}
	}

	/**
	 * remove a reference to a secondary id toggled on a Ps
	 * 
	 * @param psRef (required) secondary id reference
	 * @return ResponseEntity with query result message and status
	 */
	@Override
	@Transactional
	public ResponseEntity<String> removeTogglePsref(PsRef psRef) {
		String nationalIdRef = psRef.getNationalIdRef();
		String nationalId = psRef.getNationalId();

		Ps ps = findPsByIdSafely(nationalId);

		if (ps == null) {
			String result = String.format("Could not remove PsRef %s from Ps %s because this Ps does not exist",
					psRef.getNationalIdRef(), psRef.getNationalId());
			log.error(result);
			return new ResponseEntity<>(result, HttpStatus.GONE);
		}

		if (!ps.getIds().contains(nationalIdRef)) {
			String result = String.format("Ps %s does not reference PsRef %s", psRef.getNationalId(),
					psRef.getNationalIdRef());
			log.error(result);
			return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
		}

		ps.getIds().remove(nationalIdRef);
		mongoTemplate.save(ps);

		String result = String.format("Ps %s is no longer referencing PsRef %s", ps.getNationalId(),
				psRef.getNationalIdRef());
		log.info(result);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	
	/**
	 * Helper method to find PS by exact nationalId first, then in ids array
	 * This ensures we get the correct PS when merging (not the already-merged one)
	 */
	private Ps findPsByNationalIdOrIds(String id) {
		// CRITICAL: First try exact nationalId match to get the original PS
		Ps ps = psRepository.findByNationalId(id);
		if (ps != null) {
			log.debug("Found PS by exact nationalId: {}", id);
			return ps;
		}
		
		// Fallback: search in ids array (for already-merged accounts)
		log.debug("Not found by nationalId, searching in ids array: {}", id);
		return findPsByIdSafely(id);
	}
	
	/**
	 * Helper method to safely find a PS by ID, handling multiple results
	 * Prioritizes nationalId lookup, then searches in ids array
	 * Always returns the activated PS if multiple matches exist
	 */
	private Ps findPsByIdSafely(String id) {
		// First try by nationalId (fast, uses unique index)
		Ps ps = psRepository.findByNationalId(id);
		if (ps != null) {
			return ps;
		}
		
		// Fallback: search in ids array
		try {
			ps = psRepository.findByIdsContaining(id);
			
			// If found but deactivated, search for active merged account
			if (ps != null && !ApiUtils.isPsActivated(ps)) {
				log.info("PS {} found but deactivated, searching for active merged account", id);
				Query query = new Query(Criteria.where("ids").in(id).and("activated").ne(null));
				List<Ps> activePsList = mongoTemplate.find(query, Ps.class);
				return activePsList.stream()
						.filter(ApiUtils::isPsActivated)
						.findFirst()
						.orElse(ps); // Keep deactivated one if no active found
			}
			
			return ps;
		} catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			log.warn("Multiple PS found with id {}, searching for active one using MongoTemplate", id);
			Query query = new Query(Criteria.where("ids").in(id));
			List<Ps> psList = mongoTemplate.find(query, Ps.class);
			return psList.stream()
					.filter(ApiUtils::isPsActivated)
					.findFirst()
					.orElse(psList.isEmpty() ? null : psList.get(0));
		}
	}
}
