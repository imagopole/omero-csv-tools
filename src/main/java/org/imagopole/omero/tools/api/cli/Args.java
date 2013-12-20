/**
 *
 */
package org.imagopole.omero.tools.api.cli;

import com.google.common.base.Charsets;

import omero.model.Annotation;
import omero.model.CommentAnnotation;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Project;
import omero.model.TagAnnotation;

/**
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

    public static enum AnnotationType {
        tag,
        comment;

        public Class<? extends Annotation> getModelClass() {
            Class<? extends Annotation> clazz = null;

            switch (this) {

                case tag:
                    clazz = TagAnnotation.class;
                    break;

                case comment:
                    clazz = CommentAnnotation.class;
                    break;

                default:
                    throw new IllegalArgumentException("Unknown annotation type");

            }

            return clazz;
        }
    }

    public static enum AnnotatedType {
        dataset,
        image;

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

    public static enum ContainerType {
        local,
        project,
        dataset;

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
