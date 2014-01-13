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
 * @author seb
 *
 */
public class AnnotationLinkers {

    private final static AnnotationLinkers INSTANCE = new AnnotationLinkers();

    private AnnotationLinkers() {
        super();
    }

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
