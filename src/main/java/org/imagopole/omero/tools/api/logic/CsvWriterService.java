package org.imagopole.omero.tools.api.logic;

import java.util.Collection;

import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.dto.PojoData;

/**
 * Service layer to the CSV writing/printing application logic.
 *
 * @author seb
 *
 */
public interface CsvWriterService {

    /**
     * Converts model objects to CSV data.
     *
     * @param annotationType the type of annotation to use (eg. tag, comment)
     * @param annotatedType the target of the annotation link (eg. dataset, image)
     * @param lines the input lines
     * @return the CSV content
     * @throws RtException processing or IO failure
     */
    String writeLines(
            AnnotationType annotationType,
            AnnotatedType annotatedType,
            Collection<PojoData> lines) throws RtException;

}
