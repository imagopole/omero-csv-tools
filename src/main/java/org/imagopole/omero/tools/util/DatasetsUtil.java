/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.Collection;
import java.util.Collections;

import omero.gateway.model.DatasetData;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;

/**
 * Utility class for dataset entities handling.
 *
 * @author seb
 *
 */
public final class DatasetsUtil {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetsUtil.class);

    /**
     * Private constructor.
     */
    private DatasetsUtil() {
        super();
    }

    public static Collection<PojoData> toPojos(Collection<DatasetData> datasets) {
        Collection<PojoData> result = Collections.emptyList();

        if (null != datasets) {

            result = Collections2.transform(datasets, FunctionsUtil.datasetToPojo);
            LOG.trace("datasets: {}", result.size());

        }

        return result;
    }

}
