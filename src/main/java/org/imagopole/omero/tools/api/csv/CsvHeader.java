/**
 *
 */
package org.imagopole.omero.tools.api.csv;

import java.util.Collection;

/**
 * Internal representation for CSV header row.
 *
 * @author seb
 *
 */
public interface CsvHeader {

    /**
     * Produces a list of text comments to enable multi-line comments.
     *
     * @return a list of CSV comments if applicable, or an empty list
     */
    Collection<String> getComments();

    /**
     * Produces a list of String tokens, one per column in the CSV header row.
     *
     * @param maxColumns the number of CSV columns
     * @return a list of String tokens, or an empty list
     */
    Collection<String> getRecord(int maxColumns);

}
