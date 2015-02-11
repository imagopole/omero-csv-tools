/**
 *
 */
package org.imagopole.omero.tools.api.csv;

import java.util.Collection;

import org.imagopole.omero.tools.api.RtException;

/**
 * Converts the internal representation to CSV.
 *
 * @author seb
 *
 */
public interface CsvLineWriter<T> {

    /**
     * Converts model objects to CSV data.
     *
     * @param<T> lines the input lines
     * @return the CSV content
     * @throws RtException processing or IO failure
     */
    String toCsv(Collection<T> lines) throws RtException;

}
