/**
 *
 */
package org.imagopole.support.unitils.dbunit.util;

import java.io.File;
import java.net.URL;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.csv.CsvURLDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.core.UnitilsException;
import org.unitils.dbunit.util.MultiSchemaDataSet;

/**
 * A reader for DbUnit csv datasets.
 *
 * Warning: all tables are expected to be under a single schema - Unitils-style multichema is
 * not supported.
 *
 * All datasets are expected to be in a single directory, relative to <code>csvDataSetsBaseDirectory</code>.
 * Each dataset directory is expected to contain:
 * - one or more csv files (one per table)
 * - one <code>table-ordering.txt</code> with the list of tables
 *
 * @author seb
 * @see CsvDataSet
 */
public class SingleSchemaCsvDataSetReader {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(SingleSchemaCsvDataSetReader.class);

    /**
     * The schema name to use when no name was explicitly specified.
     */
    protected String defaultSchemaName;

    /**
     * The classpath resource to load the CSV dataSets from.
     *
     * Must be a directory.
     *
     * @see CsvURLDataSet
     */
    protected String csvDataSetsBaseDirectory;

    public SingleSchemaCsvDataSetReader(String defaultSchemaName, String csvDataSetsBaseDirectory) {
       super();

       if (null == csvDataSetsBaseDirectory || csvDataSetsBaseDirectory.isEmpty()) {
           throw new UnitilsException("A csvDataSetsBaseDirectory is required");
       }

       this.defaultSchemaName = defaultSchemaName;
       this.csvDataSetsBaseDirectory = csvDataSetsBaseDirectory;
    }

    /**
     * Parses the datasets from the given directories.
     *
     * @param dataSetFiles The dataset files, not null
     * @return The read data set, not null
     */
    public MultiSchemaDataSet readDataSetCsv(File... dataSetFiles) {
        try {

            MultiSchemaDataSet multiSchemaDataSet = new MultiSchemaDataSet();
            for (File dataSetFile : dataSetFiles) {

                try {
                    String singleDatasetDirectory = getDataSetDirectoryPath(dataSetFile);

                    URL csvDatasetDirectoryUrl = getClass().getResource(singleDatasetDirectory);
                    if (null == csvDatasetDirectoryUrl) {
                        throw new IllegalStateException(
                            String.format("CSV DataSet directory not found: %s", singleDatasetDirectory));
                    }

                    log.debug("Loading dbunit csv dataset: {} from dir: {}",
                              dataSetFile.getName(), csvDatasetDirectoryUrl);

                    IDataSet csvDataSet = new CsvURLDataSet(csvDatasetDirectoryUrl);

                    multiSchemaDataSet.setDataSetForSchema(defaultSchemaName, csvDataSet);
                } finally {
                    //closeQuietly(dataSetInputStream);
                }

            }

            return multiSchemaDataSet;

        } catch (Exception e) {
            throw new UnitilsException("Unable to parse data set csv.", e);
        }

    }

    /**
     * Returns the path to a single CSV DataSet directory
     *
     * @param dataSetFile the sub-directory for the CSV DataSet
     * @return the full path
     */
    private String getDataSetDirectoryPath(File dataSetFile) {
        String rootDatasetsDir = addTrailingSlashIfMissing(this.csvDataSetsBaseDirectory);

        String singleDatasetDirectory =
            addTrailingSlashIfMissing(rootDatasetsDir + dataSetFile.getName());

        log.debug("Requested csv dataset dir: {}", singleDatasetDirectory);

        return singleDatasetDirectory;
    }

    private String addTrailingSlashIfMissing(String singleDatasetDirectory) {
        if (!singleDatasetDirectory.endsWith("/")) {
            singleDatasetDirectory += "/";
        }
        return singleDatasetDirectory;
    }

}
