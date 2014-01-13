/**
 *
 */
package org.imagopole.omero.tools.impl.dto;

import java.util.List;

import com.google.common.collect.Lists;

import omero.model.IObject;

import org.imagopole.omero.tools.api.dto.LinksData;

/**
 * Internal data type for annotations associations representation.
 *
 * @author seb
 *
 */
public class AnnotationLinksData implements LinksData {

    /** Associations linked to previously existing tags */
    private List<IObject> knownAnnotationLinks;

    /** Associations linked to newly created tags. */
    private List<IObject> newAnnotationLinks;

    /**
     * Parameterized constructor.
     *
     * @param knownAnnotationLinks the associations linked to previously existing tags
     * @param newAnnotationLinks  the associations linked to newly created tags
     */
    private AnnotationLinksData(
                    List<IObject> knownAnnotationLinks,
                    List<IObject> newAnnotationLinks) {
        super();
        this.knownAnnotationLinks = knownAnnotationLinks;
        this.newAnnotationLinks = newAnnotationLinks;
    }

    /**
     * Static factory method.
     *
     * @param knownAnnotationLinks the associations linked to previously existing tags
     * @param newAnnotationLinks  the associations linked to newly created tags
     * @return the annotations associations
     */
    public static LinksData forLinks(
                    List<IObject> knownAnnotationLinks,
                    List<IObject> newAnnotationLinks) {

        return new AnnotationLinksData(knownAnnotationLinks, newAnnotationLinks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IObject> getKnownAnnotationLinks() {
        return this.knownAnnotationLinks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IObject> getNewAnnotationLinks() {
        return this.newAnnotationLinks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IObject> getAllAnnotationLinks() {
        int knownsSize = 0;
        int newsSize = 0;

        if (null != getKnownAnnotationLinks()) {
            knownsSize = getKnownAnnotationLinks().size();
        }
        if (null != getNewAnnotationLinks()) {
            newsSize = getNewAnnotationLinks().size();
        }

        List<IObject> annotationLinks = Lists.newArrayListWithExpectedSize(knownsSize + newsSize);

        if (null != getKnownAnnotationLinks()) {
            annotationLinks.addAll(getKnownAnnotationLinks());
        }
        if (null != getNewAnnotationLinks()) {
            annotationLinks.addAll(getNewAnnotationLinks());
        }

        return annotationLinks;
    }

}
