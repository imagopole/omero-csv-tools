/**
 *
 */
package org.imagopole.omero.tools.impl.dto;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.util.Check;

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

    /** The pojo name (eg. dataset or image name). */
    private String name;

    /** The underlying adapted object. */
    private DataObject modelObject;

    /**
     * Parameterized constructor.
     *
     * @param name the pojo name
     * @param dataObject the underlying adapted object
     */
    private DefaultPojoData(String name, DataObject dataObject) {
        super();

        this.name = name;
        this.modelObject = dataObject;
    }

    /**
     * Static factory method for <code>omero.model.DatasetData</code> pojos.
     *
     * @param dataObject the underlying adapted object
     * @return the wrapped object
     */
    public static PojoData fromDatasetData(DatasetData dataObject) {
        Check.notNull(dataObject, "dataObject");
        Check.notEmpty(dataObject.getName(), "name");

        return new DefaultPojoData(dataObject.getName(), dataObject);
    }

    /**
     * Static factory method for <code>omero.model.ImageData</code> pojos.
     *
     * @param dataObject the underlying adapted object
     * @return the wrapped object
     */
    public static PojoData fromImageData(ImageData dataObject) {
        Check.notNull(dataObject, "dataObject");
        Check.notEmpty(dataObject.getName(), "name");

        return new DefaultPojoData(dataObject.getName(), dataObject);
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

}
