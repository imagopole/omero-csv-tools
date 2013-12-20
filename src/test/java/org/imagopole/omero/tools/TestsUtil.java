/**
 *
 */
package org.imagopole.omero.tools;

import java.io.File;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import pojos.DatasetData;
import pojos.FileAnnotationData;


/**
 * @author seb
 *
 */
public class TestsUtil {

    /** Error message returned by all <code>Check</code> methods */
    public static final String PRECONDITION_FAILED_REGEX = "^Condition not met.*";

    public static final DatasetData newDataset(String name) {
        DatasetData ds = new DatasetData();
        ds.setName(name);

        return ds;
    }

    public static final DatasetData newDataset(Long id) {
        DatasetData ds = new DatasetData();
        ds.setId(id);

        return ds;
    }

    public static final FileAnnotationData newAttachment(String name) {
        return new FileAnnotationData(new File(name));
    }

    public static Multimap<String, String> emptyStringMultimap() {
        Multimap<String, String> emptyMultimap = HashMultimap.create();
        return emptyMultimap;
    }

    /**
     * TestNG groups.
     */
    public final static class Groups {
        public static final String SETUP = "setup";
        public static final String INTEGRATION = "integration";
    }

    /**
     * Environment variables keys for integration tests.
     */
    public final static class Env {
        public final static String ICE_CONFIG = "ICE_CONFIG";
        public final static String ICE_CONFIG_LOCATION = "ice.config.location";
    }

    /**
     * Constants for the DbUnit XML datasets.
     *
     * Values must be kept in sync with those defined in the XML/CSV fixtures.
     */
    public final static class DbUnit {

        /**
         * DbUnit DataSets names and fixtures values.
         */
        public final static class DataSets {

            /**
             * DbUnit CsvDataSet names.
             *
             * "Special" DbUnit DataSet(s) to work around the FlatXmlDataSet root element bearing the
             * same name as OMERO's database table name... so an OMERO dataset table can be loaded
             * via a DbUnit DataSet using a custom <code>SingleSchemaCsvDataSetFactory</code>...
             *
             *  @see FlatXmlProducer
             *  @see XmlProducer
             */
            public final static class Csv {
                /** Naming convention for CSV DataSets directories */
                private final static String DBUNIT_CSV_PREFIX = "/dbunit_csv/";

                /** Project-Dataset hierarchy */
                public final static String LINKED =  DBUNIT_CSV_PREFIX + "linked/";

                /** Dataset only hierarchy */
                public final static String ORPHANS = DBUNIT_CSV_PREFIX + "orphans/";
            }

            /**
             * DbUnit DataSets FlatXmlDataset names.
             */
            public final static class Xml {
                /** Naming convention for XML DataSets files */
                private final static String DBUNIT_XML_PREFIX = "dbunit.";

                /** The base boilerplate for required for all tests (experimenter, session, event, etc) */
                public final static String COMMON = DBUNIT_XML_PREFIX + "common-fixtures.xml";

                /** Standalone Project */
                public final static String PROJECT = DBUNIT_XML_PREFIX + "project.xml";

                /** Zero-length OriginalFile attached to project */
                public final static String EMPTY_ATTACHMENT = DBUNIT_XML_PREFIX + "empty-attachment.xml";

                /** Zero-length OriginalFile attached to project, with the same name as <code>EMPTY_ATTACHMENT</code> */
                public final static String EMPTY_ATTACHMENT_DUPLICATE = DBUNIT_XML_PREFIX + "empty-attachment-dup.xml";
            }
        }

        //---- DbUnit DataSets values (XML + CSV) ----//
        public final static Long GROUP_ID = 801L;
        public final static Long EXPERIMENTER_ID = 802L;
        public final static Long PROJECT_ID = 803L;
        public final static Long DATASET_LINKED_ID = 805L;
        public final static Long DATASET_ORPHAN_ID = 806L;
        public final static String DATASET_LINKED_NAME = "DbUnit.linked-Dataset";
        public final static String DATASET_ORPHAN_NAME = "DbUnit.orphans-Orphan-Dataset";
        public final static String EMPTY_ORIGINAL_FILE_NAME = "dbunit_tag.csv";
    }

}

