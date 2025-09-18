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

		String origin1 = ApiUtils.determineOriginAndType(nationalId).get(ApiUtils.ORIGIN);
		String origin2 = ApiUtils.determineOriginAndType(nationalIdRef).get(ApiUtils.ORIGIN);

		String targetId;
		String oldId;

		Ps targetPs = new Ps();
		Ps oldPs = new Ps();

		// PSI/RPPS
		if ((origin1.equals(ApiUtils.PSI) && origin2.equals(ApiUtils.RPPS))
				|| (origin1.equals(ApiUtils.RPPS) && origin2.equals(ApiUtils.PSI))) {

			targetId = origin1.equals(ApiUtils.PSI) ? nationalId : nationalIdRef;
			oldId = origin1.equals(ApiUtils.RPPS) ? nationalId : nationalIdRef;

			targetPs = findPsByIdSafely(targetId);
			oldPs = findPsByIdSafely(oldId);

			if (targetPs != null && oldPs != null) {
				targetPs.setProfessions(oldPs.getProfessions());
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

		if (!((origin1.equals(ApiUtils.PSI) && origin2.equals(ApiUtils.RPPS))
				|| (origin1.equals(ApiUtils.RPPS) && origin2.equals(ApiUtils.PSI)))) {

			targetPs = findPsByIdSafely(targetId);
			oldPs = findPsByIdSafely(oldId);

		}

		// STEP 1: check if target Ps exists
		if (targetPs != null) {
			targetPs.setIdType(ApiUtils.determineOriginAndType(targetPs.getNationalId()).get(ApiUtils.ID_TYPE));
			// STEP 2: check if target ps contains psRef's nationalIdRef in ids
			if (targetPs.getIds().contains(oldId)) {
				String result = String.format("PsRef %s already references Ps %s, no need to toggle", oldId, targetId);
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
	 * Helper method to safely find a PS by ID, handling multiple results
	 */
	private Ps findPsByIdSafely(String id) {
		try {
			return psRepository.findByIdsContaining(id);
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
