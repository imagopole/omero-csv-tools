/**
 *
 */
package org.imagopole.omero.tools.api.cli;

import com.google.common.base.Charsets;

import omero.model.Annotation;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Project;
import omero.model.TagAnnotation;

/**
 * Command line arguments supported by the CSV Annotation Tool.
 *
 * @author seb
 *
 */
public class Args {

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
        image;

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

        /** Represents a local file on the filesystem (no OMERO model counterpart). */
        local,

        /** Represents a <code>omero.model.Project</code>. */
        project,

        /** Represents a <code>omero.model.Dataset</code>. */
        dataset;

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

                case local:
                    throw new IllegalArgumentException("No model class for container of type local");

                default:
                    throw new IllegalArgumentException("Unknown annotated type");

            }

            return clazz;
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
        public static final Boolean   DRY_RUN_OFF       =  Boolean.FALSE;

        private Defaults() {
            super();
        }
    }

}
