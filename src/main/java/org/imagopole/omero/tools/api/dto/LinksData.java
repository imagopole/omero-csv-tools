/**
 *
 */
package org.imagopole.omero.tools.api.dto;

import java.util.List;

import omero.model.IObject;

/**
 * Internal data type for annotations associations representation.
 *
 * Result of the annotation & linking process.
 *
 * @author seb
 *
 */
public interface LinksData {

    /**
     * Retrieve the associations linked to previously existing tags.
     *
     * @return a list of link model entities
     */
    List<IObject> getKnownAnnotationLinks();

    /**
     * Retrieve the associations linked to newly created tags.
     *
     * @return a list of link model entities
     */
    List<IObject> getNewAnnotationLinks();

    /**
     * Retrieve all association links, regardless of their creation/availability status.
     *
     * @return a list of link model entities
     */
    List<IObject> getAllAnnotationLinks();

}
