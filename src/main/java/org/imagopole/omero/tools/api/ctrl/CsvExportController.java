package org.imagopole.omero.tools.api.ctrl;

import java.util.Collection;

import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.dto.PojoData;

/**
 * Dispatcher layer to conversion/export services (read-only).
 *
 * @author seb
 *
 */
public interface CsvExportController {

    /**
     * Converts the model entities to CSV with the specified header format.
     *
     * @param annotationType the type of annotation to use (eg. tag, comment)
     * @param annotatedType the target of the annotation link (eg. dataset, image)
     * @param pojos the input lines
     * @return the CSV content
     * @throws RtException processing or IO failure
     */
    String convertToCsv(
            AnnotationType annotationType,
            AnnotatedType annotatedType,
            Collection<PojoData> pojos) throws RtException;

}
