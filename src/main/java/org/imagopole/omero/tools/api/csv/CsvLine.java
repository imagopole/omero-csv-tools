/**
 *
 */
package org.imagopole.omero.tools.api.csv;

/**
 * Internal representation for CSV line.
 *
 * @author seb
 *
 */
public interface CsvLine {

    /** Returns the line index in the file. */
    Long getNumber();

    /** Returns the column value for a given index. */
    String getValueAt(int col);

    /** Returns the number of columns. */
    int getSize();

}
