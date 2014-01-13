/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import omero.model.Dataset;
import omero.model.DatasetAnnotationLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLinkI;

import org.imagopole.omero.tools.api.blitz.AnnotationLinker;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
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
