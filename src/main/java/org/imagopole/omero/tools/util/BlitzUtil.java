/**
 *
 */
package org.imagopole.omero.tools.util;

import omero.sys.ParametersI;

/**
 * Utility class for Blitz parameters handling.
 *
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

    /**
     * Build a Blitz filter for the given experimenter.
     *
     * @param experimenterId the experimenter
     * @return the OMERO Blitz parameters
     */
    public static ParametersI byExperimenter(Long experimenterId) {
        Check.notNull(experimenterId, "experimenterId");

        ParametersI params = new ParametersI();
        params.exp(omero.rtypes.rlong(experimenterId));

        return params;
    }

}
