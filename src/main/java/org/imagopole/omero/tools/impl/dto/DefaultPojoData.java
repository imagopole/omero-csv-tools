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
 * @author seb
 *
 */
public class DefaultPojoData implements PojoData {

    private String name;
    private DataObject modelObject;

    private DefaultPojoData(String name, DataObject dataObject) {
        super();

        this.name = name;
        this.modelObject = dataObject;
    }

    public static PojoData fromDatasetData(DatasetData dataObject) {
        Check.notNull(dataObject, "dataObject");
        Check.notEmpty(dataObject.getName(), "name");

        return new DefaultPojoData(dataObject.getName(), dataObject);
    }

    public static PojoData fromImageData(ImageData dataObject) {
        Check.notNull(dataObject, "dataObject");
        Check.notEmpty(dataObject.getName(), "name");

        return new DefaultPojoData(dataObject.getName(), dataObject);
    }

    @Override
    public Long getId() {
        return modelObject.getId();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DataObject getModelObject() {
        return modelObject;
    }

}
