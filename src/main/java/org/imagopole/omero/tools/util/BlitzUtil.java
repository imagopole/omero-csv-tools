/**
 *
 */
package org.imagopole.omero.tools.util;

import omero.model.DatasetAnnotationLinkI;
import omero.sys.ParametersI;
import pojos.AnnotationData;
import pojos.DatasetData;

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

    public static DatasetAnnotationLinkI link(AnnotationData tagObject, DatasetData datasetObject) {
        Check.notNull(tagObject, "tagObject");
        Check.notNull(datasetObject, "datasetObject");

        DatasetAnnotationLinkI link = new DatasetAnnotationLinkI();
        link.setParent(datasetObject.asDataset());
        link.setChild(tagObject.asAnnotation());

        return link;
    }

}
