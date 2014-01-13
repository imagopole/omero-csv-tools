/**
 *
 */
package org.imagopole.omero.tools.api.blitz;

import omero.model.IObject;
import pojos.AnnotationData;
import pojos.DataObject;

/**
 * Wires links between annotated objects and their annotations.
 *
 * @author seb
 *
 */
public interface AnnotationLinker {

    /**
     * Creates an association between an OMERO model entity and its annotation.
     *
     * @param annotationObject the annotation object (child of association)
     * @param modelObject the annotated object (parent of association)
     * @return an annotation link model entity
     */
    IObject link(AnnotationData annotationObject, DataObject modelObject);

}
