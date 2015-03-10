/**
 *
 */
package org.imagopole.omero.tools.api.cli;

import omero.model.Annotation;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Project;
import omero.model.Screen;
import omero.model.TagAnnotation;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

/**
 * Command line arguments supported by the CSV Annotation Tool.
 *
 * @author seb
 *
 */
public class Args {

    /** Separator for dumped enumeration values. */
    private static final String ENUM_SEPARATOR = ",";

    /**
     * Private constructor
     */
    private Args() {
        super();
    }

    /**
     * Enumeration for the --annotation-type argument.
     *
     * @author seb
     *
     */
    public static enum AnnotationType {

        /** Represents a <code>omero.model.TagAnnotation</code>. */
        tag;

        // to be reintroduced later if needed
        //comment;

        /**
         * Formats the enumeration values array to string.
         * @return the enum values as a comma separated string
         */
        public static String dump() {
            return Joiner.on(ENUM_SEPARATOR).join(values());
        }

        /**
         * The OMERO model class for this argument.
         * @return the implementation class
         */
        public Class<? extends Annotation> getModelClass() {
            Class<? extends Annotation> clazz = null;

            switch (this) {

                case tag:
                    clazz = TagAnnotation.class;
                    break;

                  // to be reintroduced later if needed
//                case comment:
//                    clazz = CommentAnnotation.class;
//                    break;

                default:
                    throw new IllegalArgumentException("Unknown annotation type");

            }

            return clazz;
        }
    }

    /**
     * Enumeration for the --annotated-type argument.
     *
     * @author seb
     *
     */
    public static enum AnnotatedType {

        /** Represents a <code>omero.model.Dataset</code>. */
        dataset,

        /** Represents an <code>omero.model.Image</code>. */
        image,

        /** Represents a <code>omero.model.Plate</code>. */
        plate,

        /** Represents a <code>omero.model.PlateAquisition</code>. */
        plateacquisition;

        /**
         * Formats the enumeration values array to string.
         * @return the enum values as a comma separated string
         */
        public static String dump() {
            return Joiner.on(ENUM_SEPARATOR).join(values());
        }

        /**
         * The OMERO model class for this argument.
         * @return the implementation class
         */
        public Class<? extends IObject> getModelClass() {
            Class<? extends IObject> clazz = null;

            switch (this) {

                case dataset:
                    clazz = Dataset.class;
                    break;

                case image:
                    clazz = Image.class;
                    break;

                case plate:
                    clazz = Plate.class;
                    break;

                case plateacquisition:
                    clazz = PlateAcquisition.class;
                    break;

                default:
                    throw new IllegalArgumentException("Unknown annotated type");

            }

            return clazz;
        }

    }

    /**
     * Enumeration for the --container-type argument.
     *
     * @author seb
     *
     */
    public static enum ContainerType {

        /** Represents a <code>omero.model.Project</code>. */
        project,

        /** Represents a <code>omero.model.Dataset</code>. */
        dataset,

        /** Represents a <code>omero.model.Screen</code>. */
        screen,

        /** Represents a <code>omero.model.Plate</code>. */
        plate,

        /** Represents a <code>omero.model.PlateAquisition</code>. */
        plateacquisition;

        /**
         * Formats the enumeration values array to string.
         * @return the enum values as a comma separated string
         */
        public static String dump() {
            return Joiner.on(ENUM_SEPARATOR).join(values());
        }

        /**
         * The OMERO model class for this argument.
         * @return the implementation class
         */
        public Class<? extends IObject> getModelClass() {
            Class<? extends IObject> clazz = null;

            switch (this) {

                case project:
                    clazz = Project.class;
                    break;

                case dataset:
                    clazz = Dataset.class;
                    break;

                case screen:
                    clazz = Screen.class;
                    break;

                case plate:
                    clazz = Plate.class;
                    break;

                case plateacquisition:
                    clazz = PlateAcquisition.class;
                    break;

                default:
                    throw new IllegalArgumentException("Unknown container type");

            }

            return clazz;
        }

        /**
         * The {@link AnnotatedType} directly below this container type in the OMERO hierarchy.
         *
         * @return the nested child type below this container
         */
        public AnnotatedType getChildAnnotatedType() {
            AnnotatedType childType = null;

            switch (this) {

                case project:
                    childType = AnnotatedType.dataset;
                    break;

                case dataset:
                    childType = AnnotatedType.image;
                    break;

                case screen:
                    childType = AnnotatedType.plate;
                    break;

                case plate:
                    childType = AnnotatedType.plateacquisition;
                    break;

                case plateacquisition:
                    childType = AnnotatedType.image;
                    break;

                default:
                    throw new IllegalArgumentException("Unknown container type");

            }

            return childType;
        }

    }

    /**
     * Enumeration for the --csv-file-type argument.
     *
     * @author seb
     *
     */
    public static enum FileType {

        /** Represents a local file on the filesystem (no OMERO model counterpart). */
        local,

        /** Represents a <code>omero.model.FileAnnotation</code> attachment linked to an OMERO container. */
        remote;

        /**
         * Formats the enumeration values array to string.
         * @return the enum values as a comma separated string
         */
        public static String dump() {
            return Joiner.on(ENUM_SEPARATOR).join(values());
        }

    }

    /**
     * Default constant values for optional arguments.
     *
     * @author seb
     *
     */
    public static final class Defaults {

        public static final Integer   ICE_SSL_PORT      =  4064;
        public static final Character COMMA_DELIMITER   =  ',';
        public static final String    UTF_8_CHARSET     =  Charsets.UTF_8.name();
        public static final Boolean   SKIP_HEADER_ON    =  Boolean.TRUE;
        public static final Boolean   EXPORT_MODE_OFF   =  Boolean.FALSE;
        public static final String    FILE_TYPE_REMOTE  =  FileType.remote.name();

        private Defaults() {
            super();
        }
    }

}
