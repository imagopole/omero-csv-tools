/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import omero.model.Dataset;
import omero.model.DatasetAnnotationLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLinkI;
import omero.model.Project;
import omero.model.ProjectAnnotationLinkI;

import org.imagopole.omero.tools.api.blitz.AnnotationLinker;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.util.Check;

import pojos.AnnotationData;
import pojos.DataObject;

/**
 * Factory and implementations for <code>AnnotationLinker</code> subclasses.
 *
 * @author seb
 *
 */
public class AnnotationLinkers {

    /** Singleton. */
    private final static AnnotationLinkers INSTANCE = new AnnotationLinkers();

    /** Private constructor. */
    private AnnotationLinkers() {
        super();
    }

    /**
     * Static factory method.
     *
     * @param annotatedType the type of data to be annotated
     * @return the implementation specific association builder for the given annotation target
     */
    public static AnnotationLinker forAnnotatedType(AnnotatedType annotatedType) {
        Check.notNull(annotatedType, "annotatedType");

        AnnotationLinker result = null;

        switch(annotatedType) {

            case dataset:
                result = INSTANCE.new DatasetAnnotationLinker();
                break;

            case image:
                result = INSTANCE.new ImageAnnotationLinker();
                break;

            default:
                throw new UnsupportedOperationException(
                    "Linking annotations to other containers than datasets/images not implemented");

        }

        return result;
    }

    /**
     * Static factory method.
     *
     * @param containerType the type of container to be annotated
     * @return the implementation specific association builder for the given annotation target
     */
    public static AnnotationLinker forContainerType(ContainerType containerType) {
        Check.notNull(containerType, "ContainerType");

        AnnotationLinker result = null;

        switch(containerType) {

            case project:
                result = INSTANCE.new ProjectAnnotationLinker();
                break;

            case dataset:
                result = INSTANCE.new DatasetAnnotationLinker();
                break;

            default:
                throw new UnsupportedOperationException(
                    "Linking to other containers than projects/datasets not implemented");

        }

        return result;
    }

    /**
     * Association builder for <code>omero.model.Project</code> annotations.
     *
     * @author seb
     *
     */
    public class ProjectAnnotationLinker implements AnnotationLinker {

        /**
         * {@inheritDoc}
         */
        @Override
        public IObject link(AnnotationData annotationObject, DataObject modelObject) throws ClassCastException {
            Check.notNull(annotationObject, "annotationObject");
            Check.notNull(modelObject, "modelObject");

            Project project = modelObject.asProject();

            ProjectAnnotationLinkI link = new ProjectAnnotationLinkI();
            link.setParent(project);
            link.setChild(annotationObject.asAnnotation());

            return link;
        }

    }

    /**
     * Association builder for <code>omero.model.Dataset</code> annotations.
     *
     * @author seb
     *
     */
    public class DatasetAnnotationLinker implements AnnotationLinker {

        /**
         * {@inheritDoc}
         */
        @Override
        public IObject link(AnnotationData annotationObject, DataObject modelObject) throws ClassCastException{
            Check.notNull(annotationObject, "annotationObject");
            Check.notNull(modelObject, "modelObject");

            Dataset dataset = modelObject.asDataset();

            DatasetAnnotationLinkI link = new DatasetAnnotationLinkI();
            link.setParent(dataset);
            link.setChild(annotationObject.asAnnotation());

            return link;
        }

    }

    /**
     * Association builder for <code>omero.model.Image</code> annotations.
     *
     * @author seb
     *
     */
    public class ImageAnnotationLinker implements AnnotationLinker {

        /**
         * {@inheritDoc}
         */
        @Override
        public IObject link(AnnotationData annotationObject, DataObject modelObject) throws ClassCastException{
            Check.notNull(annotationObject, "annotationObject");
            Check.notNull(modelObject, "modelObject");

            Image image = modelObject.asImage();

            ImageAnnotationLinkI link = new ImageAnnotationLinkI();
            link.setParent(image);
            link.setChild(annotationObject.asAnnotation());

            return link;
        }

    }

}
