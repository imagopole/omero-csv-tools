/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.Collection;
import java.util.Collections;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.PlateAcquisitionData;

import com.google.common.collect.Collections2;

/**
 * Utility class for plate runs entities handling.
 *
 * @author seb
 *
 */
public final class PlateAcquisitionsUtil {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(PlateAcquisitionsUtil.class);

    /** Format template for <code>pojos.PlateAcquisitionData</code> name generation based on id. */
    private static final String PLATE_RUN_NAME_FORMAT = "Run %s";

    /**
     * Private constructor.
     */
    private PlateAcquisitionsUtil() {
        super();
    }

    public static Collection<PojoData> toPojos(Collection<PlateAcquisitionData> plateAcquisitions) {
        Collection<PojoData> result = Collections.emptyList();

        if (null != plateAcquisitions) {

            result = Collections2.transform(plateAcquisitions, FunctionsUtil.plateAcquisitionToPojo);
            LOG.trace("plate acquisitions: {}", result.size());

        }

        return result;
    }

    /**
     * Retrieve the plate acquisition name, or generates one based on a conventional format:
     * <code>Run <plateacquisition_id></code>.
     *
     * @param dataObject the plate acquisition pojo
     * @return the pojo name if already defined, or a default label based on the entity's identifier
     * @see PlateAcquisitionData#getLabel()
     * @see https://github.com/openmicroscopy/openmicroscopy/blob/develop/components/blitz/src/pojos/PlateAcquisitionData.java#L54-L55
     * @see https://github.com/openmicroscopy/openmicroscopy/blob/develop/components/tools/OmeroWeb/omeroweb/webclient/templates/webclient/data/container_subtree.html#L50-L54
     */
    public static String getOrInferName(PlateAcquisitionData dataObject) {
        Check.notNull(dataObject, "dataObject");

        String pojoName = dataObject.getName();

        //Notes:
        //    - the name attribute is nullable for PlateAcquisitions, hence clients may generate
        //      a default label for display via the getLabel() method.
        //    - the generated label is not stable across clients, since it relies on the plateacquisition
        //      timestamps conversion, therefore exposing potential locale-dependent discrepancies if
        //      client configurations are not kept in sync (eg. insight: JVM default locale vs web: Django settings.py)
        //    - to (try and) ensure stability of the generated pojo name, the current name generation strategy
        //      ignores timestamps, and relies on identifiers only (similarly to the OMEROWeb 'container subtree' Django template)
        if (null == pojoName || pojoName.trim().isEmpty()) {
            pojoName = String.format(PLATE_RUN_NAME_FORMAT, dataObject.getId());

            LOG.trace("Note: generating name from id on plate acquisition pojo: {} - {}",
                      dataObject.getId(), pojoName);
        }

        return pojoName;
    }

}
