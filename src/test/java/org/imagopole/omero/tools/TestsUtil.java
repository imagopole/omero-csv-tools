/**
 *
 */
package org.imagopole.omero.tools;

import java.io.File;
import java.util.List;

import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.dataset.xml.XmlProducer;
import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.util.FunctionsUtil;
import org.testng.collections.Lists;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.TagAnnotationData;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


/**
 * @author seb
 *
 */
public class TestsUtil {

    /** Error message returned by all <code>Check</code> methods */
    public static final String PRECONDITION_FAILED_REGEX = "^Condition not met.*";

    /** Default prefix for dataset names fixtures */
    public static final String DEFAULT_NAME_PREFIX = "testng.";

    /** Default CSV format record separator */
    public static final String CRLF = "\r\n";

    /** Default CSV format quote character */
    public static final String DOUBLE_QUOTE = "\"";

    /** Default mode for reflection-based comparisons */
    public static final ReflectionComparatorMode[] DEFAULT_COMPARATOR_MODE = new ReflectionComparatorMode[] {};

    public static final ProjectData newProject(String name) {
        ProjectData pj = new ProjectData();
        pj.setName(name);

        return pj;
    }

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

    public static final PlateData newPlate(String name) {
        PlateData plate = new PlateData();
        plate.setName(name);

        return plate;
    }

    public static final PlateData newPlate(Long id) {
        PlateData plate = new PlateData();
        plate.setId(id);
        plate.setName(DEFAULT_NAME_PREFIX + id);

        return plate;
    }

    public static final PlateAcquisitionData newPlateAcquisition(String name) {
        PlateAcquisitionData pa = new PlateAcquisitionData();
        pa.setName(name);

        return pa;
    }

    public static final PlateAcquisitionData newPlateAcquisition(Long id) {
        PlateAcquisitionData pa = new PlateAcquisitionData();
        pa.setId(id);
        pa.setName(DEFAULT_NAME_PREFIX + id);

        return pa;
    }

    public static final FileAnnotationData newAttachment(String name) {
        return new FileAnnotationData(new File(name));
    }

    public static final TagAnnotationData newTag(String name) {
        return new TagAnnotationData(name);
    }

    public static Multimap<String, String> emptyStringMultimap() {
        Multimap<String, String> emptyMultimap = HashMultimap.create();
        return emptyMultimap;
    }

    public static List<CsvAnnotationLine> asListOrNull(CsvAnnotationLine... lines) {
        List<CsvAnnotationLine> result = null;

        if (null != lines) {
            result = Lists.newArrayList(lines);
        }

        return result;
    }

    public static String crlf(String input) {
        String result = null;

        if (null != input) {
            result = input.concat(CRLF);
        }

        return result;
    }

    public static String quote(String input) {
        String result = null;

        if (null != input) {
            result = DOUBLE_QUOTE.concat(input).concat(DOUBLE_QUOTE);
        }

        return result;
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

                /** Dataset-Images hierarchy with tags already associated to image */
                public final static String IMAGES_ANNOTATED =  DBUNIT_CSV_PREFIX + "images_annotated/";

                public final static class Linked {
                    public final static Long DATASET_ID = 806L;
                    public final static Long PLATE_ID = 906L;
                    public final static Long PLATE_ID_WITH_RUNS = 907L;
                    public final static String DATASET_NAME = "DbUnit.linked-Dataset";
                    public final static String PLATE_NAME = "DbUnit.linked-Plate";
                    public final static String PLATE_NAME_WITH_RUNS = "DbUnit.linked-Plate-With-Runs";
                    public final static String PLATE_ACQUISITION_NAME = "DbUnit.linked-PlateAcquisition";
                    public final static String PLATE_ACQUISITION_NAME_DEFAULT = "Run 914";
                }

                public final static class Orphans {
                    public final static Long DATASET_ID = 805L;
                    public final static Long PLATE_ID = 905L;
                    public final static String DATASET_NAME = "DbUnit.orphans-Orphan-Dataset";
                    public final static String PLATE_NAME = "DbUnit.orphans-Plate";
                }

                public final static class Annotated {
                    public final static Long DATASET_ID = 807L;
                    public final static Long PLATE_ID = 907L;
                    public final static Long PLATE_ID_WITH_RUNS = 908L;
                    public final static String DATASET_NAME = "DbUnit.annotated-Dataset";
                    public final static String PLATE_NAME = "DbUnit.annotated-Plate";
                    public final static String PLATE_ACQUISITION_NAME = "DbUnit.annotated-PlateAcquisition";
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
                    /** Plate with no plateacquisition children. 2 wells. 1 image per well at index 0. */
                    public final static Long PLATE_ID = 910L;
                    /** Plate with 1 plateacquisition child. 1 well. 2 images in well at indices 0 and 1. */
                    public final static Long PLATE_ID_WITH_RUNS= 911L;
                    public final static Long PLATE_ACQUISITION_ID = 920911L;
                    public final static String IMAGE_NAME = "DbUnit.images-Image";
                    /** Image at index 0 for well 1 of plate without runs. */
                    public final static String IMAGE_NAME_PWWSI_w1i0 = "DbUnit.images-HCS.Image.P-W-WS-I[w1i0]";
                    /** Image at index 0 for well 2 of plate without runs. */
                    public final static String IMAGE_NAME_PWWSI_w2i0 = "DbUnit.images-HCS.Image.P-W-WS-I[w2i0]";
                    /** Image at index 0 for well 1 of plate with single run. */
                    public final static String IMAGE_NAME_PWPAWSI_w1i0 = "DbUnit.images-HCS.Image.P-W-PA-WS-I[w1i0]";
                    /** Image at index 1 for well 1 of plate with single run. */
                    public final static String IMAGE_NAME_PWPAWSI_w1i1 = "DbUnit.images-HCS.Image.P-W-PA-WS-I[w1i1]";
                }

                public final static class ImagesAnnotated {
                    public final static Long DATASET_ID = 811L;
                    /** Plate with no plateacquisition children. 2 wells. 1 image per well at index 0. */
                    public final static Long PLATE_ID = 1010L;
                    /** Plate with 1 plateacquisition child. 1 well. 2 images in well at indices 0 and 1. */
                    public final static Long PLATE_ID_WITH_RUNS= 1011L;
                    public final static Long PLATE_ACQUISITION_ID = 9201011L;
                    public final static String DATASET_NAME = "DbUnit.images_annotated-Dataset";
                    public final static String IMAGE_NAME = "DbUnit.images_annotated-Image";
                    public final static String IMAGE_NAME_PWWSI_w1i0 = "DbUnit.images_annotated-HCS.Image.P-W-WS-I[w1i0]";
                    public final static String IMAGE_NAME_PWWSI_w2i0 = "DbUnit.images_annotated-HCS.Image.P-W-WS-I[w2i0]";
                    public final static String IMAGE_NAME_PWPAWSI_w1i0 = "DbUnit.images_annotated-HCS.Image.P-W-PA-WS-I[w1i0]";
                    public final static String IMAGE_NAME_PWPAWSI_w1i1 = "DbUnit.images_annotated-HCS.Image.P-W-PA-WS-I[w1i1]";
                    public final static String TAG_NAME_LINKED = "DbUnit.images_annotated-Tag.Linked";
                    public final static String TAG_NAME_UNLINKED = "DbUnit.images_annotated-Tag.Unlinked";
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

                /** Standalone Project, target for file annotations  */
                public final static String PROJECT_ATTACHMENT_TARGET =
                    DBUNIT_XML_PREFIX + "project-attachment-target.xml";

                /** Zero-length OriginalFile attached to project */
                public final static String EMPTY_ATTACHMENT =
                    DBUNIT_XML_PREFIX + "empty-attachment.xml";

                /** Zero-length OriginalFile attached to project, with the same
                 * name as <code>EMPTY_ATTACHMENT</code> */
                public final static String EMPTY_ATTACHMENT_DUPLICATE =
                    DBUNIT_XML_PREFIX + "empty-attachment-dup.xml";

                public final static class AttachmentTarget {
                    public final static Long PROJECT_ID = 804L;
                    public final static String PROJECT_NAME = "DbUnit-Project-AttachmentTarget";
                }
            }
        }

        //---- DbUnit DataSets common values (both XML + CSV DataSets) ----//
        public final static Long GROUP_ID = 801L;
        public final static Long EXPERIMENTER_ID = 802L;
        public final static Long PROJECT_ID = 803L;
        public final static Long SCREEN_ID = 903L;
        public final static String EMPTY_ORIGINAL_FILE_NAME = "dbunit_tag.csv";
    }

}

