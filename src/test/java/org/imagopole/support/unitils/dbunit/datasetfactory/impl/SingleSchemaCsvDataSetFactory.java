/**
 *
 */
package org.imagopole.support.unitils.dbunit.datasetfactory.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;


import org.imagopole.support.unitils.dbunit.util.SingleSchemaCsvDataSetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.core.UnitilsException;
import org.unitils.dbunit.datasetfactory.DataSetFactory;
import org.unitils.dbunit.util.MultiSchemaDataSet;


/**
 * A data set factory that can handle data set definitions for a single database schema.
 *
 * The directory layout must be in line with DbUnit's convention:
 * - each file within the directory must be named according to the table
 * - the file must have a header with the columns names
 * - the rest of the rows are the columns values
 *
 *Layout:
 * <code>
 *     <base_directory>
 *         <table_name>.csv
 * </code>
 *
 * @author seb
 *
 */
public class SingleSchemaCsvDataSetFactory implements DataSetFactory {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(SingleSchemaCsvDataSetFactory.class);

    /** Config key for the CSV datasets base directory */
    private final static String CSV_DATASETS_DIRECTORY_KEY = "unitils-support.DbUnit.csvDataSet.baseDir.default";

    /** Default value for the CSV datasets base directory */
    private final static String CSV_DATASETS_DIRECTORY_DEFAULT_VALUE = "/dbunit_csv/";

    /**
     * The schema name to use when no name was explicitly specified.
     */
    protected String defaultSchemaName;

    /**
     * Root directory for all CSV DataSets (one CSV DataSet = one directory)
     */
    protected String csvDataSetsBaseDirectory;

    /**
     * Initializes this DataSetFactory
     *
     * @param configuration     The configuration, not null
     * @param defaultSchemaName The name of the default schema of the test database, not null
     */
    public void init(Properties configuration, String defaultSchemaName) {
        log.trace("Unitils configuration: {} {}", configuration, defaultSchemaName);

        String csvBaseDir = configuration.getProperty(
                        CSV_DATASETS_DIRECTORY_KEY,
                        CSV_DATASETS_DIRECTORY_DEFAULT_VALUE);

        this.defaultSchemaName = defaultSchemaName;
        this.csvDataSetsBaseDirectory = csvBaseDir;
        log.debug("Using csvDataSets base directory: {}", this.csvDataSetsBaseDirectory);
    }

    /**
     * Creates a {@link MultiSchemaDataSet} using the given file.
     *
     * @param dataSetFiles The dataset files, not null
     * @return A {@link MultiSchemaDataSet} containing the datasets per schema, not null
     */
    public MultiSchemaDataSet createDataSet(File... dataSetFiles) {
        try {
            SingleSchemaCsvDataSetReader singleSchemaCsvDataSetReader =
                new SingleSchemaCsvDataSetReader(defaultSchemaName, csvDataSetsBaseDirectory);

            return singleSchemaCsvDataSetReader.readDataSetCsv(dataSetFiles);
        } catch (Exception e) {
            throw new UnitilsException(
               "Unable to create DbUnit dataset for data set files: " + Arrays.toString(dataSetFiles), e);
        }
    }

    /**
     * @return The extension that files which can be interpreted by this factory must have
     */
    public String getDataSetFileExtension() {
        return "csv";
    }

}
