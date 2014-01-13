/**
 *
 */
package org.imagopole.omero.tools;

import java.io.File;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.dataset.xml.XmlProducer;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.util.FunctionsUtil;

import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;


/**
 * @author seb
 *
 */
public class TestsUtil {

    /** Error message returned by all <code>Check</code> methods */
    public static final String PRECONDITION_FAILED_REGEX = "^Condition not met.*";

    /** Default prefix for dataset names fixtures */
    public static final String DEFAULT_NAME_PREFIX = "testng.";

    public static final DatasetData newDataset(String name) {
        DatasetData ds = new DatasetData();
        ds.setName(name);

        return ds;
    }

    public static final DatasetData newDataset(Long id) {
        DatasetData ds = new DatasetData();
        ds.setId(id);
        ds.setName(DEFAULT_NAME_PREFIX + id);

        return ds;
    }

    public static final PojoData newDatasetPojo(String name) {
        return FunctionsUtil.datasetToPojo.apply(newDataset(name));
    }

    public static final ImageData newImage(String name) {
        ImageData img = new ImageData();
        img.setName(name);

        return img;
    }

    public static final ImageData newImage(Long id) {
        ImageData img = new ImageData();
        img.setId(id);
        img.setName(DEFAULT_NAME_PREFIX + id);

        return img;
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
     * Constants for the DbUnit DataSets.
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

                /** Project-Dataset hierarchy with tags already associated to dataset */
                public final static String ANNOTATED =  DBUNIT_CSV_PREFIX + "annotated/";

                /** Project-Dataset hierarchy with tags already associated to multiple datasets */
                public final static String ANNOTATED_HIERARCHY =  DBUNIT_CSV_PREFIX + "annotated_hierarchy/";

                /** Dataset-Images hierarchy */
                public final static String IMAGES =  DBUNIT_CSV_PREFIX + "images/";

                public final static class Linked {
                    public final static Long DATASET_ID = 805L;
                    public final static String DATASET_NAME = "DbUnit.linked-Dataset";
                }

                public final static class Orphans {
                    public final static Long DATASET_ID = 806L;
                    public final static String DATASET_NAME = "DbUnit.orphans-Orphan-Dataset";
                }

                public final static class Annotated {
                    public final static Long DATASET_ID = 807L;
                    public final static String DATASET_NAME = "DbUnit.annotated-Dataset";
                    public final static String TAG_NAME_LINKED = "DbUnit.annotated-Tag.Linked";
                    public final static String TAG_NAME_UNLINKED = "DbUnit.annotated-Tag.Unlinked";
                }

                public final static class AnnotatedHierarchy {
                    public final static Long DATASET_ID_TAGGED = 808L;
                    public final static Long DATASET_ID_TAGGED_FULLY = 809L;
                    public final static String DATASET_NAME_TAGGED = "DbUnit.hierarchy-Dataset.Tagged";
                    public final static String DATASET_NAME_TAGGED_FULLY = "DbUnit.hierarchy-Dataset.Tagged-Fully";
                    public final static String TAG_NAME_LINKED_1 = "DbUnit.hierarchy-Tag.Linked-1";
                    public final static String TAG_NAME_LINKED_2 = "DbUnit.hierarchy-Tag.Linked-2";
                    public final static String TAG_NAME_UNLINKED = "DbUnit.hierarchy-Tag.Unlinked";
                }

                public final static class Images {
                    public final static Long DATASET_ID = 810L;
                    public final static String IMAGE_NAME = "DbUnit.images-Image";
                }
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
                public final static String EMPTY_ATTACHMENT =
                    DBUNIT_XML_PREFIX + "empty-attachment.xml";

                /** Zero-length OriginalFile attached to project, with the same
                 * name as <code>EMPTY_ATTACHMENT</code> */
                public final static String EMPTY_ATTACHMENT_DUPLICATE =
                    DBUNIT_XML_PREFIX + "empty-attachment-dup.xml";
            }
        }

        //---- DbUnit DataSets common values (both XML + CSV DataSets) ----//
        public final static Long GROUP_ID = 801L;
        public final static Long EXPERIMENTER_ID = 802L;
        public final static Long PROJECT_ID = 803L;
        public final static String EMPTY_ORIGINAL_FILE_NAME = "dbunit_tag.csv";
    }

}

