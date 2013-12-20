/**
 *
 */
package org.imagopole.omero.tools.impl.dto;

import java.util.List;

import com.google.common.collect.Lists;

import omero.model.IObject;

import org.imagopole.omero.tools.api.dto.LinksData;

/**
 * @author seb
 *
 */
public class AnnotationLinksData implements LinksData {

    private List<IObject> knownAnnotationLinks;
    private List<IObject> newAnnotationLinks;

    /**
     * @param knownAnnotationLinks
     * @param newAnnotationLinks
     */
    private AnnotationLinksData(
                    List<IObject> knownAnnotationLinks,
                    List<IObject> newAnnotationLinks) {
        super();
        this.knownAnnotationLinks = knownAnnotationLinks;
        this.newAnnotationLinks = newAnnotationLinks;
    }

    public static LinksData forLinks(
                    List<IObject> knownAnnotationLinks,
                    List<IObject> newAnnotationLinks) {

        return new AnnotationLinksData(knownAnnotationLinks, newAnnotationLinks);
    }

    @Override
    public List<IObject> getKnownAnnotationLinks() {
        return this.knownAnnotationLinks;
    }

    @Override
    public List<IObject> getNewAnnotationLinks() {
        return this.newAnnotationLinks;
    }

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
