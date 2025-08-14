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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ans.psc.api.ToggleApiDelegate;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;
import fr.ans.psc.repository.PsRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ToggleApiDelegateImpl implements ToggleApiDelegate {
	private final PsRepository psRepository;
	private final MongoTemplate mongoTemplate;

	private final static String PSI = "PSI";
	private final static String RPPS = "RPPS";
	private final static String ADELI = "ADELI";
	private final static String ORIGIN = "origin";
	private final static String ID_TYPE = "id_type";

	public ToggleApiDelegateImpl(PsRepository psRepository, MongoTemplate mongoTemplate) {
		this.psRepository = psRepository;
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	@Transactional
	public ResponseEntity<String> togglePsref(PsRef psRef) {
		String nationalIdRef = psRef.getNationalIdRef();
		String nationalId = psRef.getNationalId();

		String origin1 = determineOriginAndType(nationalId).get(ORIGIN);
		String origin2 = determineOriginAndType(nationalIdRef).get(ORIGIN);

		String targetId;
		String oldId;

		Ps targetPs = new Ps();
		Ps oldPs = new Ps();

		// PSI/RPPS
		if ((origin1.equals(PSI) && origin2.equals(RPPS)) || (origin1.equals(RPPS) && origin2.equals(PSI))) {

			targetId = origin1.equals(PSI) ? nationalId : nationalIdRef;
			oldId = origin1.equals(RPPS) ? nationalId : nationalIdRef;

			targetPs = psRepository.findByIdsContaining(targetId);
			oldPs = psRepository.findByIdsContaining(oldId);

			if (targetPs != null && oldPs != null) {
				targetPs.setProfessions(oldPs.getProfessions());
				targetPs.setIdType(determineOriginAndType(targetPs.getNationalId()).get(ID_TYPE));
			}
		}
		// PSI
		else if (origin1.equals(PSI) || origin2.equals(PSI)) {

			if (origin1.equals(origin2)) {
				String result = String.format("Both origins are PSI, impossible merge");
				log.info(result);
				return new ResponseEntity<>(result, HttpStatus.NOT_ACCEPTABLE);

			}
			targetId = origin1.equals(PSI) ? nationalId : nationalIdRef;
			oldId = origin1.equals(PSI) ? nationalIdRef : nationalId;
		}
		// RPPS
		else if (origin1.equals(RPPS) || origin2.equals(RPPS) && !origin1.equals(origin2)) {

			targetId = origin1.equals(RPPS) ? nationalId : nationalIdRef;
			oldId = origin1.equals(RPPS) ? nationalIdRef : nationalId;
		}
		// ADELI
		else if ((origin1.equals(ADELI) || origin2.equals(ADELI)) && !origin1.equals(origin2)) {

			targetId = origin1.equals(ADELI) ? nationalIdRef : nationalId;
			oldId = origin1.equals(ADELI) ? nationalId : nationalIdRef;
		}
		// Defaut
		else {
			targetId = nationalId;
			oldId = nationalIdRef;
		}

		if (!((origin1.equals(PSI) && origin2.equals(RPPS)) || (origin1.equals(RPPS) && origin2.equals(PSI)))) {

			targetPs = psRepository.findByIdsContaining(targetId);
			oldPs = psRepository.findByIdsContaining(oldId);

		}

		// STEP 1: check if target Ps exists
		if (targetPs != null) {
			targetPs.setIdType(determineOriginAndType(targetPs.getNationalId()).get(ID_TYPE));
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
			// TODO : refaire la liste d'alternativeIds en fonction de la liste d'ids
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

	public static Map<String, String> determineOriginAndType(String idNat) {
		Map<String, String> result = new HashMap<>();
		String id_type = "";
		String origin = "RASS";
		if (idNat != null) {
			if (isValidUUID(idNat)) {
				origin = PSI;
				id_type = "";
			} else if (idNat.startsWith("0")) {
				origin = ADELI;
				id_type = "0";
			} else if (idNat.startsWith("1")) {
				origin = "CAB_ADELI";
				id_type = "1";
			} else if (idNat.startsWith("3")) {
				origin = "FINESS";
				id_type = "3";
			} else if (idNat.startsWith("4")) {
				origin = "SIREN";
				id_type = "4";
			} else if (idNat.startsWith("5")) {
				origin = "SIRET";
				id_type = "5";
			} else if (idNat.startsWith("6")) {
				origin = "CAB_RPPS";
				id_type = "6";
			} else if (idNat.startsWith("8")) {
				origin = RPPS;
				id_type = "8";
			}
		}
		result.put(ID_TYPE, id_type);
		result.put(ORIGIN, origin);

		return result;
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

		Ps ps = psRepository.findByIdsContaining(nationalId);

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

	public static boolean isValidUUID(String id) {
		if (id == null) {
			return false;
		}
		try {
			UUID.fromString(id);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	//TODO a faire en utilisant l'objet Ps
//	public static List<String> idTripletCreationFromIds(List<String> ids) {
//	
//		List<String> alternativeIds = new ArrayList<>();
//
//		for (String idNat : ids) {
//			Map<String, String> originAndType = determineOriginAndType(idNat);
//			String origin = originAndType.get(ORIGIN);
//
//			String triplet = "{" + idNat + "," + origin + ",1}";
//			alternativeIds.add(triplet);
//		}
//
//		return alternativeIds;	
// }

}
