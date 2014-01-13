/**
 *
 */
package org.imagopole.omero.tools.util;

import omero.sys.ParametersI;

/**
 * @author seb
 *
 */
public final class BlitzUtil {

    /**
     * Private constructor.
     */
    private BlitzUtil() {
        super();
    }

    public static ParametersI byExperimenter(Long experimenterId) {
        Check.notNull(experimenterId, "experimenterId");

        ParametersI params = new ParametersI();
        params.exp(omero.rtypes.rlong(experimenterId));

        return params;
    }

}
