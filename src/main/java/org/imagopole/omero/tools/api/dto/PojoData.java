/**
 *
 */
package org.imagopole.omero.tools.api.dto;

import java.util.Collection;

import pojos.AnnotationData;
import pojos.DataObject;


/**
 * Simple adapter interface intended to wrap OMERO model pojos under a uniform data type.
 *
 * Typically used to allow processing of <code>Dataset</code> and <code>Image</code> pojos in
 * a unified manner.
 *
 * @author seb
 *
 */
public interface PojoData {

    /** The OMERO entity id */
    Long getId();

    /** The OMERO entity name */
    String getName();

    /** The underlying OMERO entity */
    DataObject getModelObject();

    /** The annotations linked to the underlying OMERO entity. */
    Collection<AnnotationData> getAnnotations();

}
