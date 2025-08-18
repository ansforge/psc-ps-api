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
package fr.ans.psc.utils;

import fr.ans.psc.model.AlternativeIdentifier;
import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApiUtils {

    public final static String ORIGIN = "origin";
    public final static String ID_TYPE = "id_type";
    public final static String QUALITY = "quality";
    public final static String PSI = "PSI";
    public final static String RPPS = "RPPS";
    public final static String ADELI = "ADELI";

    public static Long getInstantTimestamp() {
        return new Date().getTime() / 1000;
    }

    public static boolean isPsActivated(Ps ps) {
        return ps != null && ps.getActivated() != null
                && (ps.getDeactivated() == null || ps.getActivated() > ps.getDeactivated());
    }

    public static Map<String, String> determineOriginAndType(String idNat) {
        Map<String, String> result = new HashMap<>();
        String id_type = "";
        String origin = "RASS";
        String quality = "1";
        if (idNat != null) {
            if (isValidUUID(idNat)) {
                origin = PSI;
                id_type = "";
                quality = "2";
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
        result.put(QUALITY, quality);

        return result;
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

    public static List<AlternativeIdentifier> idTripletCreationFromIds(List<String> ids) {

        List<AlternativeIdentifier> alternativeIds = new ArrayList<>();

        for (String idNat : ids) {
            Map<String, String> originAndType = determineOriginAndType(idNat);

            AlternativeIdentifier triplet = new AlternativeIdentifier().identifier(idNat, originAndType.get(ORIGIN),
                    Integer.parseInt(originAndType.get(QUALITY)));
            alternativeIds.add(triplet);
        }

        return alternativeIds;
    }

    public static void setAppropriateIds(Ps psToCheck, Ps storedPs) {
        if (psToCheck.getIds() == null || psToCheck.getIds().isEmpty()) {
            psToCheck.setIds(storedPs == null || storedPs.getIds() == null
                    ? new ArrayList<>(Collections.singletonList(psToCheck.getNationalId()))
                    : storedPs.getIds());
        } else if (!psToCheck.getIds().contains(psToCheck.getNationalId())) {
            psToCheck.getIds().add(psToCheck.getNationalId());
        }
        psToCheck.setAlternativeIds(ApiUtils.idTripletCreationFromIds(psToCheck.getIds()));
    }
}
