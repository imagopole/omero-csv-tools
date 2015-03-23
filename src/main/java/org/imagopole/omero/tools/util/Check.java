/**
 *
 */
package org.imagopole.omero.tools.util;




import java.util.Collection;
import java.util.Map;


/**
 * Utility class for input handling and validation.
 *
 * @author seb
 *
 */
public final class Check {

    /** Private constructor (utility class) */
    private Check() {
        super();
    }

    public static final void notNull(Object obj, String argName) {
        if (null == obj) {
             rejectEmptyParam(argName);
        }
    }

    public static final void notEmpty(String obj, String argName) {
        Check.notNull(obj, argName);

        if (obj.trim().length() == 0) {
             rejectEmptyParam(argName);
        }
    }

    public static final void notEmpty(Collection<?> coll, String argName) {
        Check.notNull(coll, argName);

        if (coll.size() < 1) {
             rejectEmptyParam(argName);
        }
    }

    public static final void notEmpty(String[] obj, String argName) {
        Check.notNull(obj, argName);

        if (obj.length <= 0) {
            rejectEmptyParam(argName);
        }
    }

    public static final void notEmpty(Long[] obj, String argName) {
        Check.notNull(obj, argName);

        if (obj.length <= 0) {
            rejectEmptyParam(argName);
        }
    }

    public static final void notEmpty(Map<?, ?> obj, String argName) {
        Check.notNull(obj, argName);

        if (obj.keySet().isEmpty()) {
             rejectEmptyParam(argName);
        }
    }

    public static final void positive(Number number, String argName) {
        Check.notNull(number, argName);

        if (number.intValue() < 0) {
             rejectEmptyParam(argName);
        }
    }

    public static final void strictlyPositive(Number number, String argName) {
        Check.notNull(number, argName);

        if (number.intValue() < 1) {
             rejectEmptyParam(argName);
        }
    }

    private static void rejectEmptyParam(String argName) throws IllegalArgumentException {
        throw new IllegalArgumentException(
                "Condition not met - expected : non-empty parameter for " + argName);
    }
}
