/**
 *
 */
package org.imagopole.omero.tools.api.dto;

import java.util.List;

import omero.model.IObject;

/**
 * @author seb
 *
 */
public interface LinksData {

    List<IObject> getKnownAnnotationLinks();

    List<IObject> getNewAnnotationLinks();

    List<IObject> getAllAnnotationLinks();

}
