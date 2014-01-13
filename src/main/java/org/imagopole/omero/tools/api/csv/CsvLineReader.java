/**
 *
 */
package org.imagopole.omero.tools.api.csv;

import java.util.Collection;

import org.imagopole.omero.tools.api.RtException;

/**
 * Converts the underlying CSV content to the internal representation.
 *
 * @author seb
 *
 */
public interface CsvLineReader<T extends CsvLine> {

    /**
     * Read the underlying CSV data.
     *
     * @return the CSV lines
     * @throws RtException processing or IO failure
     */
    Collection<T> getLines() throws RtException;
//
//    void setMalformedLineHandler(CsvLineHandler<T> lineHandler);
//
//    void setEmptyLineHandler(CsvLineHandler<T> lineHandler);
}
