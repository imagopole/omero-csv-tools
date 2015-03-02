/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;

import omero.model.Dataset;
import omero.model.DatasetAnnotationLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLinkI;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model.PlateAnnotationLinkI;
import omero.model.Project;
import omero.model.ProjectAnnotationLinkI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLinkI;

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

            case plate:
                result = INSTANCE.new PlateAnnotationLinker();
                break;

            case plateacquisition:
                result = INSTANCE.new PlateAcquisitionAnnotationLinker();
                break;

            case image:
                result = INSTANCE.new ImageAnnotationLinker();
                break;

            default:
                throw new UnsupportedOperationException(
                    "Linking annotations to other nodes than datasets/images "
                  + "or plates/plateacquisitions not implemented");

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

            case screen:
                result = INSTANCE.new ScreenAnnotationLinker();
                break;

            case plate:
                result = INSTANCE.new PlateAnnotationLinker();
                break;

            case plateacquisition:
                result = INSTANCE.new PlateAcquisitionAnnotationLinker();
                break;

            default:
                throw new UnsupportedOperationException(
                    "Linking to other containers than projects/datasets "
                  + "or screens/plates/plateacquisitions not implemented");

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

    /**
     * Association builder for <code>omero.model.Screen</code> annotations.
     *
     * @author seb
     *
     */
    public class ScreenAnnotationLinker implements AnnotationLinker {

        /**
         * {@inheritDoc}
         */
        @Override
        public IObject link(AnnotationData annotationObject, DataObject modelObject) throws ClassCastException {
            Check.notNull(annotationObject, "annotationObject");
            Check.notNull(modelObject, "modelObject");

            Screen screen = modelObject.asScreen();

            ScreenAnnotationLinkI link = new ScreenAnnotationLinkI();
            link.setParent(screen);
            link.setChild(annotationObject.asAnnotation());

            return link;
        }

    }

    /**
     * Association builder for <code>omero.model.Plate</code> annotations.
     *
     * @author seb
     *
     */
    public class PlateAnnotationLinker implements AnnotationLinker {

        /**
         * {@inheritDoc}
         */
        @Override
        public IObject link(AnnotationData annotationObject, DataObject modelObject) throws ClassCastException {
            Check.notNull(annotationObject, "annotationObject");
            Check.notNull(modelObject, "modelObject");

            Plate plate = modelObject.asPlate();

            PlateAnnotationLinkI link = new PlateAnnotationLinkI();
            link.setParent(plate);
            link.setChild(annotationObject.asAnnotation());

            return link;
        }

    }

    /**
     * Association builder for <code>omero.model.PlateAcquisition</code> annotations.
     *
     * @author seb
     *
     */
    public class PlateAcquisitionAnnotationLinker implements AnnotationLinker {

        /**
         * {@inheritDoc}
         */
        @Override
        public IObject link(AnnotationData annotationObject, DataObject modelObject) throws ClassCastException {
            Check.notNull(annotationObject, "annotationObject");
            Check.notNull(modelObject, "modelObject");

            PlateAcquisition plateAcquisition = (PlateAcquisition) modelObject.asIObject();

            PlateAcquisitionAnnotationLinkI link = new PlateAcquisitionAnnotationLinkI();
            link.setParent(plateAcquisition);
            link.setChild(annotationObject.asAnnotation());

            return link;
        }

    }

}
