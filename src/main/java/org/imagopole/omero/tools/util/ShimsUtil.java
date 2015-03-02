/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import omero.model.IObject;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Screen;
import omero.model.Well;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.DataObject;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ScreenData;
import pojos.WellData;

import com.google.common.collect.Lists;

/**
 * Utility class to assist with currently unsupported model entities type conversion and application
 * logic interception/override behaviour.
 *
 * Should only be a transient workaround until the OMERO services implementations add support for
 * those model entities out of the box.
 *
 * @author seb
 *
 */
public final class ShimsUtil {

    /** Application logs. */
    private static final Logger LOG = LoggerFactory.getLogger(ShimsUtil.class);

    /**
     * Whitelist of model types currently unsupported by the underlying OMERO services and utilities
     * implementations, such as:
     *
     * <ul>
     *   <li><code>pojos.DataObject</code></li>
     *   <li><code>ome.services.query.PojosGetImagesQueryDefinition</code></li>
     * </ul>
     *
     * @see https://github.com/openmicroscopy/openmicroscopy/blob/develop/components/blitz/src/pojos/DataObject.java#L131-L167
     * @see https://github.com/openmicroscopy/openmicroscopy/blob/develop/components/server/src/ome/services/query/PojosGetImagesQueryDefinition.java#L61-L80
     */
    @SuppressWarnings("unchecked")
    private final static List<Class <? extends IObject>> UNSUPPORTED_MODEL_CLASSES =
            Lists.newArrayList(
                    Screen.class,
                    Plate.class,
                    PlateAcquisition.class,
                    Well.class);

    /**
     * Private constructor.
     */
    private ShimsUtil() {
        super();
    }

    /**
     * Wrapper around {@link DataObject#asPojos(Collection)} to enable extended type conversion support.
     *
     * Implementation copied from <code>pojos.DataObject</code>.
     *
     * @param iObjects
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Set asPojos(Collection<IObject> iObjects) {
        Set<DataObject> result = new HashSet<DataObject>();
        IObject obj;
        for (Iterator<IObject> it = iObjects.iterator(); it.hasNext();) {
            obj = (IObject) it.next();
            result.add(asPojo(obj));
        }
        return result;
    }

    /**
     * Wrapper around {@link DataObject#asPojo(IObject)} to enable extended type conversion support.
     *
     * @param obj the OMERO model object to convert to an OMERO pojo.
     * @return the OMERO pojo
     */
    public static DataObject asPojo(IObject obj) {
        Check.notNull(obj, "obj");

        DataObject result = null;

        if (isDataObjectShimRequired(obj)) {
            LOG.info("Converting as pojo via DataObject shim for {} {}",
                     obj.getClass().getName(), obj.getId().getValue());

            result = asPojoExtended(obj);
        } else {
            LOG.info("Converting as pojo via regular DataObject for {} {}",
                     obj.getClass().getName(), obj.getId().getValue());

            result = DataObject.asPojo(obj);
        }

        return result;
    }

    /**
     * Implementation similar to {@link DataObject#asPojo(IObject)}.
     *
     * @param obj the OMERO model object to convert to an OMERO pojo.
     * @return the OMERO pojo
     */
    private static DataObject asPojoExtended(IObject obj) {
        DataObject converted = null;

        if (obj instanceof Screen) {
            converted = new ScreenData((Screen) obj);
        } else if (obj instanceof Plate) {
            converted = new PlateData((Plate) obj);
        } else if (obj instanceof PlateAcquisition) {
            converted = new PlateAcquisitionData((PlateAcquisition) obj);
        } else if (obj instanceof Well) {
            converted = new WellData((Well) obj);
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "No shim available for iObjects of type: %s",
                    obj.getClass().getName()));
        }

        return converted;
    }

    /**
     * Determines whether the specified entity should be converted to an OMERO pojo via
     * an extended conversion utility instead of the regular {@link DataObject#asPojo(IObject)}.
     *
     * @param obj the model entity
     * @return true if the DataObject shim should apply, false otherwise
     */
    private static boolean isDataObjectShimRequired(IObject obj) {
        Check.notNull(obj, "obj");

        // check whether the iObject instance requires shimming
        for (Class<? extends IObject> klass : UNSUPPORTED_MODEL_CLASSES) {
            if (klass.isAssignableFrom(obj.getClass())) {
                return true;
            }
        }

        LOG.warn("No supported shim found for iObject: {}", obj);

        return false;
    }

    /**
     * Determines whether the specified container type should be queried via a custom HQL query
     * instead of the regular container service.
     *
     * @param containerClass the container model class
     * @return true if the HQL shim should apply, false otherwise
     */
    public static boolean isHqlQueryShimRequired(Class<? extends IObject> containerClass) {
        Check.notNull(containerClass, "containerClass");

        return UNSUPPORTED_MODEL_CLASSES.contains(containerClass);
    }

}
