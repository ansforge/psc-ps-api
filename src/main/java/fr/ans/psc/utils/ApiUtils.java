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

import fr.ans.psc.model.Ps;
import fr.ans.psc.model.PsRef;

import java.util.Date;

public class ApiUtils {

    public static Long getInstantTimestamp() {
        return new Date().getTime() / 1000;
    }

    public static boolean isPsActivated(Ps ps) {
        return ps != null && ps.getActivated() != null && (ps.getDeactivated() == null || ps.getActivated() > ps.getDeactivated());
    }

}
