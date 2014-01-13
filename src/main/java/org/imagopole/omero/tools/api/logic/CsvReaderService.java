package org.imagopole.omero.tools.api.logic;

import com.google.common.collect.Multimap;

/**
 * Service layer to the CSV processing application logic.
 *
 * @author seb
 *
 */
public interface CsvReaderService {

    /**
     * Index the CSV file content by line.
     *
     * key=annotated_name, values=[annotations_names]
     *
     * eg.
     * key=dataset name, values=tags names
     *
     * @param content the CSV file content to index
     * @return the indexed CSV lines, with key as the first column, values as the other columns
     */
    Multimap<String, String> readUniqueRecords(String content);

}
