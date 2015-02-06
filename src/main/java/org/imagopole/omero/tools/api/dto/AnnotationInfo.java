package org.imagopole.omero.tools.api.dto;

/**
 * Internal data type for annotations metatata representation.
 *
 * @author seb
 *
 */
public interface AnnotationInfo {

    /**
     * Retrieve the annotation namespace.
     *
     * @return the NS.
     */
    String getNamespace();

    /**
     * Retrieve the annotation description.
     *
     * @return the description
     */
    String getDescription();

}
