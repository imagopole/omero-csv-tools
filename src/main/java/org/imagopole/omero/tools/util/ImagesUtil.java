/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Collections2;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.ImageData;

/**
 * Utility class for image entities handling.
 *
 * @author seb
 *
 */
public final class ImagesUtil {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(ImagesUtil.class);

    /**
     * Private constructor.
     */
    private ImagesUtil() {
        super();
    }

    public static Collection<PojoData> toPojos(Collection<ImageData> images) {
        Collection<PojoData> result = Collections.emptyList();

        if (null != images) {

            result = Collections2.transform(images, FunctionsUtil.imageToPojo);
            LOG.trace("images: {}", result.size());

        }

        return result;
    }

}
