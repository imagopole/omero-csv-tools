/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;


import static org.imagopole.omero.tools.util.ShimsUtil.isHqlQueryShimRequired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Well;
import omero.sys.ParametersI;

import org.imagopole.omero.tools.util.BlitzUtil;
import org.imagopole.omero.tools.util.Check;
import org.imagopole.omero.tools.util.ShimsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Shim service layer to the underlying container related OMERO gateway.
 *
 * Used to assist with currently unsupported model entities type conversion and application
 * logic interception/override behaviour.
 * Should only be a transient workaround until the OMERO services implementations add support for
 * those model entities out of the box.
 *
 * @author seb
 * @see ShimsUtil
 */
public class ShimContainersBlitzService extends ContainersBlitzService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(ShimContainersBlitzService.class);

    /**
     * Parameterized constructor.
     *
     * @param session the OMERO Blitz session
     */
    public ShimContainersBlitzService(ServiceFactoryPrx session) {
        super(session);
    }

    /**
     * {@inheritDoc}
     */
     @Override
     @SuppressWarnings("unchecked")
     public Collection<ImageData> listImagesByExperimenterAndContainer(
             Long experimenterId,
             Long containerId,
             Class<? extends IObject> containerClass) throws ServerError {

         Check.notNull(experimenterId, "experimenterId");
         Check.notNull(containerId, "containerId");
         Check.notNull(containerClass, "containerClass");

         Set<ImageData> result = new HashSet<ImageData>();

         // shim interception: lookup via the default container service for supported container types,
         // or redirect to custom HQL query for data types not yet included in the default
         // query definitions (eg. S/P/W data)
         List<Image> imageObjects = getImages(experimenterId, containerId, containerClass);

         if (null != imageObjects) {

            result = DataObject.asPojos(imageObjects);

         }

         log.debug("Found {} nested images for experimenter {} and container {} of type {}",
                   result.size(), experimenterId, containerId, containerClass.getSimpleName());

        return result;
     }

    /**
     * Queries images from the relevant data service (ie. HQL or container) based on the container class.
     *
     * @param experimenterId the experimenter
     * @param containerId the parent container
     * @param containerClass the parent container type
     * @return a list of images within this container, or an empty list
     * @throws ServerError  OMERO client or server failure
     */
    private List<Image> getImages(
            Long experimenterId,
            Long containerId,
            Class<? extends IObject> containerClass)  throws ServerError {

        List<Image> imageObjects = new ArrayList<Image>();

        if (isHqlQueryShimRequired(containerClass)) {
            log.info("Retrieving images via IQuery hql shim for {} {}",
                     containerClass.getSimpleName(), containerId);

            imageObjects = getImagesFromHqlQueryService(
                    experimenterId,
                    containerId,
                    containerClass);
        } else {
            log.info("Retrieving images via regular IContainer query for {} {}",
                     containerClass.getSimpleName(), containerId);

            imageObjects =
                getSession().getContainerService().getImages(
                    containerClass.getName(),
                    Lists.newArrayList(containerId),
                    BlitzUtil.byExperimenter(experimenterId));
        }

        return imageObjects;
    }

    /**
     * Implementation similar to {@link ome.services.query.PojosGetImagesQueryDefinition#buildQuery(org.hibernate.Session session)}.
     *
     * @param experimenterId the experimenter
     * @param containerId the parent container
     * @param containerClass the parent container type
     * @return a list of images within this container, or an empty list
     * @throws ServerError  OMERO client or server failure
     */
    private List<Image> getImagesFromHqlQueryService(
            Long experimenterId,
            Long containerId,
            Class<? extends IObject> containerClass) throws ServerError {

        ParametersI params = BlitzUtil.byExperimenter(experimenterId);

        StringBuffer query = new StringBuffer();
        query.append("from Image as img ");
        query.append("left outer join fetch img.details.creationEvent as ce ");
        query.append("left outer join fetch img.details.updateEvent as ue ");
        query.append("left outer join fetch img.pixels as pix ");
        // TODO: 5.1 model
        //sb.append("left outer join fetch pix.timeIncrement as increment ");
        query.append("left outer join fetch pix.pixelsType as pt ");
        query.append("left outer join fetch img.annotationLinksCountPerOwner as i_c_ann ");
        query.append("left outer join fetch img.datasetLinksCountPerOwner as i_c_ds ");

        if (Plate.class.isAssignableFrom(containerClass)) {
            params.addLong("plateID", containerId);

            query.append("join fetch img.wellSamples as ws ");
            query.append("join fetch ws.well as well ");
            query.append("where well.plate.id = :plateID");

        } else if (PlateAcquisition.class.isAssignableFrom(containerClass)) {
            params.addLong("plateAcquisitionID", containerId);

            query.append("join fetch img.wellSamples as ws ");
            query.append("left outer join fetch ws.plateAcquisition as pa ");
            query.append("where pa.id = :plateAcquisitionID");

        } else if(Well.class.isAssignableFrom(containerClass)) {
            params.addLong("wellID", containerId);

            query.append("join fetch img.wellSamples as ws ");
            query.append("where ws.well.id = :wellID");

        } else {
            throw new IllegalArgumentException(
                String.format(
                    "No shim available for containers of type: %s",
                    containerClass.getSimpleName()));
        }

        List<IObject> iObjects =
            getSession().getQueryService().findAllByQuery(query.toString(), params);

        return toImages(iObjects);
    }

    private List<Image> toImages(List<IObject> iObjects) {
        Check.notNull(iObjects, "iObjects");

        List<Image> result = new ArrayList<Image>(iObjects.size());

        if (null != iObjects && !iObjects.isEmpty()) {

            for (IObject obj : iObjects) {
                result.add((Image) obj);
            }

        }

        return result;
    }

}
