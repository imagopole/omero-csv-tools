/**
 *
 */
package org.imagopole.omero.tools.impl.dto;

import java.util.Collection;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/**
 * Simple adapter intended to wrap OMERO model pojos under a uniform data type.
 *
 * @author seb
 *
 */
public class DefaultPojoData implements PojoData {

    /** Application logs. */
    private final static Logger LOG = LoggerFactory.getLogger(DefaultPojoData.class);

    /** The pojo name (eg. dataset or image name). */
    private String name;

    /** The underlying adapted object. */
    private DataObject modelObject;

    /** The annotations linked to the underlying adapted object (may be null). */
    private Collection<AnnotationData> annotations;

    /**
     * Parameterized constructor.
     *
     * @param name the pojo name
     * @param dataObject the underlying adapted object
     * @param annotations the annotations linked to the pojo
     */
    private DefaultPojoData(String name, DataObject dataObject, Collection<AnnotationData> annotations) {
        super();

        this.name = name;
        this.modelObject = dataObject;
        this.annotations = annotations;
    }

    /**
     * Static factory method for <code>omero.model.DatasetData</code> pojos.
     *
     * Any <code>pojos.AnnotationData</code> already set on the <code>dataObject</code>
     * argument are copied/exposed via the <code>getAnnotations</code> method.
     *
     * @param dataObject the underlying adapted object
     * @return the wrapped object
     */
    @SuppressWarnings("unchecked")
    public static PojoData fromDatasetData(DatasetData dataObject) {
        Check.notNull(dataObject, "dataObject");
        Check.notEmpty(dataObject.getName(), "name");

        return new DefaultPojoData(dataObject.getName(), dataObject, dataObject.getAnnotations());
    }

    /**
     * Static factory method for <code>omero.model.ImageData</code> pojos.
     *
     * Any <code>pojos.AnnotationData</code> already set on the <code>dataObject</code>
     * argument are copied/exposed via the <code>getAnnotations</code> method.
     *
     * @param dataObject the underlying adapted object
     * @return the wrapped object
     */
    @SuppressWarnings("unchecked")
    public static PojoData fromImageData(ImageData dataObject) {
        Check.notNull(dataObject, "dataObject");
        Check.notEmpty(dataObject.getName(), "name");

        return new DefaultPojoData(dataObject.getName(), dataObject, dataObject.getAnnotations());
    }

    /**
     * Static factory method for annotated <code>org.imagopole.omero.tools.api.dto.PojoData</code> pojos.
     *
     * The <code>annotations</code> argument overrides any <code>pojos.AnnotationData</code>
     * already set on the <code>pojo</code> argument.
     *
     * @param pojo the wrapped <code>omero.model.ObjectData</code> pojo to copy
     * @param annotations the annotations linked to the pojo
     * @return the wrapped object plus its annotations
     */
    public static PojoData fromAnnotatedPojo(PojoData pojo, Collection<AnnotationData> annotations) {
        Check.notNull(pojo, "pojo");
        Check.notEmpty(pojo.getName(), "name");
        Check.notNull(pojo.getModelObject(), "dataObject");

        if (null != pojo.getAnnotations() && !pojo.getAnnotations().isEmpty()
            && null != annotations && !annotations.isEmpty()) {
            LOG.warn("Note: overriding {} current annotation(s) with {} on pojo: {}",
                     annotations.size(), annotations.size(), pojo.getModelObject());
        }

        return new DefaultPojoData(pojo.getName(), pojo.getModelObject(), annotations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return modelObject.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataObject getModelObject() {
        return modelObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<AnnotationData> getAnnotations() {
        return annotations;
    }

}
