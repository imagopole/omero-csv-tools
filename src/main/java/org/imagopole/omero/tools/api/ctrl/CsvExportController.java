package org.imagopole.omero.tools.api.ctrl;

import java.util.Collection;

import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.dto.PojoData;

/**
 * Dispatcher layer to conversion/export services (read-only).
 *
 * @author seb
 *
 */
public interface CsvExportController {

    /**
     * Converts the model entities to CSV.
     *
     * @param pojos the input lines
     * @return the CSV content
     * @throws RtException processing or IO failure
     */
    String convertToCsv(Collection<PojoData> pojos) throws RtException;

}
