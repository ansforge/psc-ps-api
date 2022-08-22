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
