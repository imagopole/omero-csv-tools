/**
 *
 */
package org.imagopole.omero.tools.impl.dto;

import org.imagopole.omero.tools.api.dto.AnnotationInfo;

/**
 * Internal data type for annotations metatata representation.
 *
 * @author seb
 *
 */
public class DefaultAnnotationInfo implements AnnotationInfo {

    /** The annotation namespace. */
    private String namespace;

    /** The annotation description.*/
    private String description;

    /**
     * Private constructor.
     */
    private DefaultAnnotationInfo(String namespace, String description) {
        super();
        this.namespace = namespace;
        this.description = description;
    }

    /**
     * Static factory method.
     *
     * @param namespace the optional annotation namespace
     * @param description the optional annotation description
     * @return the wrapped metadata
     */
    public static AnnotationInfo forInfo(String namespace, String description) {
        return new DefaultAnnotationInfo(namespace, description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
    }

}
