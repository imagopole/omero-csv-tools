/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.Collection;
import java.util.Collections;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.PlateData;

import com.google.common.collect.Collections2;

/**
 * Utility class for plate entities handling.
 *
 * @author seb
 *
 */
public final class PlatesUtil {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(PlatesUtil.class);

    /**
     * Private constructor.
     */
    private PlatesUtil() {
        super();
    }

    public static Collection<PojoData> toPojos(Collection<PlateData> plates) {
        Collection<PojoData> result = Collections.emptyList();

        if (null != plates) {

            result = Collections2.transform(plates, FunctionsUtil.plateToPojo);
            LOG.trace("plates: {}", result.size());

        }

        return result;
    }

}
