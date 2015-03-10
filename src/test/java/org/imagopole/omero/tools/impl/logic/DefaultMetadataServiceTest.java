package org.imagopole.omero.tools.impl.logic;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;

import org.imagopole.omero.tools.AbstractBlitzClientTest;
import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.TestsUtil.DbUnit;
import org.imagopole.omero.tools.TestsUtil.DbUnit.DataSets;
import org.imagopole.omero.tools.TestsUtil.Groups;
import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.ShimContainersBlitzService;
import org.imagopole.omero.tools.util.FunctionsUtil;
import org.imagopole.support.unitils.dbunit.annotation.UnloadDataSet;
import org.imagopole.support.unitils.dbunit.datasetfactory.impl.SingleSchemaCsvDataSetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.unitils.dbunit.annotation.DataSet;

import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

public class DefaultMetadataServiceTest extends AbstractBlitzClientTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultMetadataServiceTest.class);

    /** @TestedObject */
    private DefaultMetadataService metadataService;

    @Override
    protected void setUpAfterIceConnection(ServiceFactoryPrx session) {
         log.debug("setUpAfterIceConnection with session {}", session);

         OmeroAnnotationService annotationService = new AnnotationBlitzService(session);
         OmeroContainerService containerService = new ShimContainersBlitzService(session);

         metadataService = new DefaultMetadataService();
         metadataService.setAnnotationService(annotationService);
         metadataService.setContainerService(containerService);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listDatasetsPlusAnnotationsByExperimenterAndProjectShouldRejectNullExperimenter() throws ServerError {
        metadataService.listDatasetsPlusAnnotationsByExperimenterAndProject(
                null, 1L, AnnotationType.tag, AnnotatedType.dataset);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listDatasetsPlusAnnotationsByExperimenterAndProjectShouldRejectNullProject() throws ServerError {
        metadataService.listDatasetsPlusAnnotationsByExperimenterAndProject(
                1L, null, AnnotationType.tag, AnnotatedType.dataset);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listDatasetsPlusAnnotationsByExperimenterAndProjectShouldRejectNullAnnotationType() throws ServerError {
        metadataService.listDatasetsPlusAnnotationsByExperimenterAndProject(
                1L, 1L, null, AnnotatedType.dataset);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listDatasetsPlusAnnotationsByExperimenterAndProjectShouldRejectNullAnnotatedType() throws ServerError {
        metadataService.listDatasetsPlusAnnotationsByExperimenterAndProject(
                1L, 1L, AnnotationType.tag, null);
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectNullExperimenter() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                null, 1L, ContainerType.dataset, AnnotationType.tag, AnnotatedType.image);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectNullDataset() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                1L, null, ContainerType.dataset, AnnotationType.tag, AnnotatedType.image);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectNullContainerType() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                1L, 1L, null, AnnotationType.tag, AnnotatedType.image);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectNullAnnotationType() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                1L, 1L, ContainerType.dataset, null, AnnotatedType.image);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectNullAnnotatedType() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                1L, 1L, ContainerType.dataset, AnnotationType.tag, null);
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listPlatesPlusAnnotationsByExperimenterAndScreenShouldRejectNullExperimenter() throws ServerError {
        metadataService.listPlatesPlusAnnotationsByExperimenterAndScreen(
                null, 1L, AnnotationType.tag, AnnotatedType.plate);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listPlatesPlusAnnotationsByExperimenterAndScreenShouldRejectNullScreen() throws ServerError {
        metadataService.listPlatesPlusAnnotationsByExperimenterAndScreen(
                1L, null, AnnotationType.tag, AnnotatedType.plate);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listPlatesPlusAnnotationsByExperimenterAndScreenShouldRejectNullAnnotationType() throws ServerError {
        metadataService.listPlatesPlusAnnotationsByExperimenterAndScreen(
                1L, 1L, null, AnnotatedType.plate);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listPlatesPlusAnnotationsByExperimenterAndScreenShouldRejectNullAnnotatedType() throws ServerError {
        metadataService.listPlatesPlusAnnotationsByExperimenterAndScreen(
                1L, 1L, AnnotationType.tag, null);
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlateShouldRejectNullExperimenter() throws ServerError {
        metadataService.listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
                null, 1L, AnnotationType.tag, AnnotatedType.plateacquisition);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlateShouldRejectNullPlate() throws ServerError {
        metadataService.listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
                1L, null, AnnotationType.tag, AnnotatedType.plateacquisition);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlateShouldRejectNullAnnotationType() throws ServerError {
        metadataService.listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
                1L, 1L, null, AnnotatedType.plateacquisition);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlateShouldRejectNullAnnotatedType() throws ServerError {
        metadataService.listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
                1L, 1L, AnnotationType.tag, null);
    }


    @DataSet(value= { DataSets.Csv.ORPHANS }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class },
          expectedExceptionsMessageRegExp = "No dataset records found within container \\d* of type project .*")
    public void listDatasetsPlusAnnotationsByExperimenterAndProjectShouldRejectEmptyProjects() throws ServerError {
        metadataService.listDatasetsPlusAnnotationsByExperimenterAndProject(
                DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, AnnotationType.tag, AnnotatedType.dataset);
    }

    @DataSet(value= { DataSets.Csv.LINKED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listDatasetsPlusAnnotationsByExperimenterAndProjectShouldSupportNestedDatasets() throws ServerError {
        Collection<PojoData> result =
            metadataService.listDatasetsPlusAnnotationsByExperimenterAndProject(
                DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, AnnotationType.tag, AnnotatedType.dataset);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 1, "One nested dataset expected");

        PojoData pojo = Iterables.getOnlyElement(result);

        assertTrue(DatasetData.class.isAssignableFrom(pojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Linked.DATASET_NAME, pojo.getName(), "Wrong dataset name");
        assertNull(pojo.getAnnotations(), "Null annotations expected");
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listDatasetsPlusAnnotationsByExperimenterAndProjectTest() throws ServerError {
        Collection<PojoData> result =
            metadataService.listDatasetsPlusAnnotationsByExperimenterAndProject(
                DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, AnnotationType.tag, AnnotatedType.dataset);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 1, "One nested dataset expected");

        PojoData pojo = Iterables.getOnlyElement(result);
        Collection<AnnotationData> pojoAnnotations = pojo.getAnnotations();

        assertTrue(DatasetData.class.isAssignableFrom(pojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Annotated.DATASET_NAME, pojo.getName(), "Wrong dataset name");
        assertNotNull(pojoAnnotations, "Non-null annotations expected");
        assertEquals(pojoAnnotations.size(), 1, "One nested dataset expected");

        AnnotationData tagAnnotation = Iterables.getOnlyElement(pojoAnnotations);
        assertEquals(DbUnit.DataSets.Csv.Annotated.TAG_NAME_LINKED,
                     tagAnnotation.getContentAsString(), "Wrong tag value");
    }


    @DataSet(value= { DataSets.Csv.ORPHANS }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class },
          expectedExceptionsMessageRegExp = "No plate records found within container \\d* of type screen .*")
    public void listPlatesPlusAnnotationsByExperimenterAndScreenShouldRejectEmptyScreen() throws ServerError {
        metadataService.listPlatesPlusAnnotationsByExperimenterAndScreen(
                DbUnit.EXPERIMENTER_ID, DbUnit.SCREEN_ID, AnnotationType.tag, AnnotatedType.plate);
    }

    @DataSet(value= { DataSets.Csv.LINKED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listPlatesPlusAnnotationsByExperimenterAndScreenShouldSupportNestedPlates() throws ServerError {
        Collection<PojoData> result =
            metadataService.listPlatesPlusAnnotationsByExperimenterAndScreen(
                DbUnit.EXPERIMENTER_ID, DbUnit.SCREEN_ID, AnnotationType.tag, AnnotatedType.plate);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 1, "One nested plate expected");

        PojoData pojo = Iterables.getOnlyElement(result);

        assertTrue(PlateData.class.isAssignableFrom(pojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Linked.PLATE_NAME, pojo.getName(), "Wrong plate name");
        assertNull(pojo.getAnnotations(), "Null annotations expected");
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listPlatesPlusAnnotationsByExperimenterAndScreenTest() throws ServerError {
        Collection<PojoData> result =
            metadataService.listPlatesPlusAnnotationsByExperimenterAndScreen(
                DbUnit.EXPERIMENTER_ID, DbUnit.SCREEN_ID, AnnotationType.tag, AnnotatedType.plate);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 1, "One nested plate expected");

        PojoData pojo = Iterables.getOnlyElement(result);
        Collection<AnnotationData> pojoAnnotations = pojo.getAnnotations();

        assertTrue(PlateData.class.isAssignableFrom(pojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Annotated.PLATE_NAME, pojo.getName(), "Wrong plate name");
        assertNotNull(pojoAnnotations, "Non-null annotations expected");
        assertEquals(pojoAnnotations.size(), 1, "One nested plate expected");

        AnnotationData tagAnnotation = Iterables.getOnlyElement(pojoAnnotations);
        assertEquals(DbUnit.DataSets.Csv.Annotated.TAG_NAME_LINKED,
                     tagAnnotation.getContentAsString(), "Wrong tag value");
    }


    @DataSet(value= { DataSets.Csv.ORPHANS }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class },
          expectedExceptionsMessageRegExp = "No plateacquisition records found within container \\d* of type plate .*")
    public void listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlateShouldRejectEmptyPlate() throws ServerError {
        metadataService.listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Orphans.PLATE_ID,
                AnnotationType.tag, AnnotatedType.plateacquisition);
    }

    @DataSet(value= { DataSets.Csv.LINKED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlateShouldSupportNestedPlateRuns() throws ServerError {
        Collection<PojoData> result =
            metadataService.listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Linked.PLATE_ID_WITH_RUNS,
                AnnotationType.tag, AnnotatedType.plateacquisition);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 2, "Two nested plateacquisitions expected");

        Collection<PojoData> orderedResult =
            Ordering.natural().nullsFirst().onResultOf(FunctionsUtil.toPojoName).sortedCopy(result);
        PojoData firstPojo = Iterables.getFirst(orderedResult, null);
        PojoData lastPojo = Iterables.getLast(orderedResult, null);

        assertTrue(PlateAcquisitionData.class.isAssignableFrom(firstPojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Linked.PLATE_ACQUISITION_NAME, firstPojo.getName(), "Wrong plate acquisition name");
        assertNull(firstPojo.getAnnotations(), "Null annotations expected");

        assertTrue(PlateAcquisitionData.class.isAssignableFrom(lastPojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Linked.PLATE_ACQUISITION_NAME_DEFAULT, lastPojo.getName(), "Wrong plate acquisition name");
        assertNull(lastPojo.getAnnotations(), "Null annotations expected");
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlateTest() throws ServerError {
        Collection<PojoData> result =
            metadataService.listPlateAcquisitionsPlusAnnotationsByExperimenterAndPlate(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Annotated.PLATE_ID_WITH_RUNS,
                AnnotationType.tag, AnnotatedType.plateacquisition);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 1, "One nested plateacquisition expected");

        PojoData pojo = Iterables.getOnlyElement(result);
        Collection<AnnotationData> pojoAnnotations = pojo.getAnnotations();

        assertTrue(PlateAcquisitionData.class.isAssignableFrom(pojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Annotated.PLATE_ACQUISITION_NAME, pojo.getName(), "Wrong plateacquisition name");
        assertNotNull(pojoAnnotations, "Non-null annotations expected");
        assertEquals(pojoAnnotations.size(), 1, "One nested plateacquisition expected");

        AnnotationData tagAnnotation = Iterables.getOnlyElement(pojoAnnotations);
        assertEquals(DbUnit.DataSets.Csv.Annotated.TAG_NAME_LINKED, tagAnnotation.getContentAsString(), "Wrong tag value");
    }


    @DataSet(value= { DataSets.Csv.ORPHANS }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class },
          expectedExceptionsMessageRegExp = "No image records found within container \\d* of type dataset .*")
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectEmptyDatasets() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
            DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Orphans.DATASET_ID,
            ContainerType.dataset, AnnotationType.tag, AnnotatedType.image);
    }

    @DataSet(value= { DataSets.Csv.IMAGES }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldSupportNestedImages() throws ServerError {
        Collection<PojoData> result =
            metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Images.DATASET_ID,
                ContainerType.dataset, AnnotationType.tag, AnnotatedType.image);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 1, "One nested dataset expected");

        PojoData pojo = Iterables.getOnlyElement(result);

        assertTrue(ImageData.class.isAssignableFrom(pojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Images.IMAGE_NAME, pojo.getName(), "Wrong image name");
        assertNull(pojo.getAnnotations(), "Null annotations expected");
    }

    @DataSet(value= { DataSets.Csv.IMAGES_ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listImagesPlusAnnotationsByExperimenterAndDatasetTest() throws ServerError {
        Collection<PojoData> result =
            metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.ImagesAnnotated.DATASET_ID,
                ContainerType.dataset, AnnotationType.tag, AnnotatedType.image);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 1, "One nested image expected");

        PojoData pojo = Iterables.getOnlyElement(result);
        Collection<AnnotationData> pojoAnnotations = pojo.getAnnotations();

        assertTrue(ImageData.class.isAssignableFrom(pojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.ImagesAnnotated.IMAGE_NAME, pojo.getName(), "Wrong image name");
        assertNotNull(pojoAnnotations, "Non-null annotations expected");
        assertEquals(pojoAnnotations.size(), 1, "One nested image expected");

        AnnotationData tagAnnotation = Iterables.getOnlyElement(pojoAnnotations);
        assertEquals(DbUnit.DataSets.Csv.ImagesAnnotated.TAG_NAME_LINKED,
                     tagAnnotation.getContentAsString(), "Wrong tag value");
    }


    @DataSet(value= { DataSets.Csv.ORPHANS }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class },
          expectedExceptionsMessageRegExp = "No image records found within container \\d* of type plate .*")
    public void listImagesPlusAnnotationsByExperimenterAndPlateShouldRejectEmptyPlates() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
            DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Orphans.PLATE_ID,
            ContainerType.plate, AnnotationType.tag, AnnotatedType.image);
    }

    @DataSet(value= { DataSets.Csv.IMAGES }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listImagesPlusAnnotationsByExperimenterAndPlateShouldSupportNestedImages() throws ServerError {
        Collection<PojoData> result =
            metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Images.PLATE_ID,
                ContainerType.plate, AnnotationType.tag, AnnotatedType.image);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 2, "Two nested images expected");

        Collection<PojoData> orderedResult =
            Ordering.natural().nullsFirst().onResultOf(FunctionsUtil.toPojoName).sortedCopy(result);

        PojoData firstPojo = Iterables.getFirst(orderedResult, null);
        PojoData lastPojo = Iterables.getLast(orderedResult, null);

        assertTrue(ImageData.class.isAssignableFrom(firstPojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Images.IMAGE_NAME_PWWSI_w1i0, firstPojo.getName(), "Wrong image name");
        assertNull(firstPojo.getAnnotations(), "Null annotations expected");

        assertTrue(ImageData.class.isAssignableFrom(lastPojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Images.IMAGE_NAME_PWWSI_w2i0, lastPojo.getName(), "Wrong image name");
        assertNull(firstPojo.getAnnotations(), "Null annotations expected");
    }

    @DataSet(value= { DataSets.Csv.IMAGES_ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listImagesPlusAnnotationsByExperimenterAndPlateTest() throws ServerError {
        Collection<PojoData> result =
            metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                    DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.ImagesAnnotated.PLATE_ID,
                    ContainerType.plate, AnnotationType.tag, AnnotatedType.image);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 2, "Two nested images expected");

        Collection<PojoData> orderedResult =
            Ordering.natural().nullsFirst().onResultOf(FunctionsUtil.toPojoName).sortedCopy(result);

        PojoData firstpojo = Iterables.getFirst(orderedResult, null);
        PojoData lastPojo = Iterables.getLast(orderedResult, null);

        Collection<AnnotationData> firstAnnotations = firstpojo.getAnnotations();
        Collection<AnnotationData> lastAnnotations = lastPojo.getAnnotations();

        assertTrue(ImageData.class.isAssignableFrom(firstpojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.ImagesAnnotated.IMAGE_NAME_PWWSI_w1i0, firstpojo.getName(), "Wrong image name");
        assertNull(firstAnnotations, "Null annotations expected");

        assertTrue(ImageData.class.isAssignableFrom(lastPojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.ImagesAnnotated.IMAGE_NAME_PWWSI_w2i0, lastPojo.getName(), "Wrong image name");
        assertNotNull(lastAnnotations, "Non-null annotations expected");
        assertEquals(lastAnnotations.size(), 1, "One nested annotation expected");

        AnnotationData lastTagAnnotation = Iterables.getOnlyElement(lastAnnotations);
        assertEquals(DbUnit.DataSets.Csv.ImagesAnnotated.TAG_NAME_LINKED,
                     lastTagAnnotation.getContentAsString(), "Wrong tag value");
    }


    @DataSet(value= { DataSets.Csv.IMAGES }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listImagesPlusAnnotationsByExperimenterAndPlateAcquisitionShouldSupportNestedImages() throws ServerError {
        Collection<PojoData> result =
            metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Images.PLATE_ACQUISITION_ID,
                ContainerType.plateacquisition, AnnotationType.tag, AnnotatedType.image);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 2, "Two nested images expected");

        Collection<PojoData> orderedResult =
            Ordering.natural().nullsFirst().onResultOf(FunctionsUtil.toPojoName).sortedCopy(result);

        PojoData firstPojo = Iterables.getFirst(orderedResult, null);
        PojoData lastPojo = Iterables.getLast(orderedResult, null);

        assertTrue(ImageData.class.isAssignableFrom(firstPojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Images.IMAGE_NAME_PWPAWSI_w1i0, firstPojo.getName(), "Wrong image name");
        assertNull(firstPojo.getAnnotations(), "Null annotations expected");

        assertTrue(ImageData.class.isAssignableFrom(lastPojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.Images.IMAGE_NAME_PWPAWSI_w1i1, lastPojo.getName(), "Wrong image name");
        assertNull(firstPojo.getAnnotations(), "Null annotations expected");
    }

    @DataSet(value= { DataSets.Csv.IMAGES_ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listImagesPlusAnnotationsByExperimenterAndPlateAcquisitionTest() throws ServerError {
        Collection<PojoData> result =
            metadataService.listImagesPlusAnnotationsByExperimenterAndContainer(
                    DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.ImagesAnnotated.PLATE_ACQUISITION_ID,
                    ContainerType.plateacquisition, AnnotationType.tag, AnnotatedType.image);

        assertNotNull(result, "Non-null result expected");
        assertEquals(result.size(), 2, "Two nested images expected");

        Collection<PojoData> orderedResult =
            Ordering.natural().nullsFirst().onResultOf(FunctionsUtil.toPojoName).sortedCopy(result);

        PojoData firstpojo = Iterables.getFirst(orderedResult, null);
        PojoData lastPojo = Iterables.getLast(orderedResult, null);

        Collection<AnnotationData> firstAnnotations = firstpojo.getAnnotations();
        Collection<AnnotationData> lastAnnotations = lastPojo.getAnnotations();

        assertTrue(ImageData.class.isAssignableFrom(firstpojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.ImagesAnnotated.IMAGE_NAME_PWPAWSI_w1i0, firstpojo.getName(), "Wrong image name");
        assertNull(firstAnnotations, "Null annotations expected");

        assertTrue(ImageData.class.isAssignableFrom(lastPojo.getModelObject().getClass()), "Wrong model type");
        assertEquals(DbUnit.DataSets.Csv.ImagesAnnotated.IMAGE_NAME_PWPAWSI_w1i1, lastPojo.getName(), "Wrong image name");
        assertNotNull(lastAnnotations, "Non-null annotations expected");
        assertEquals(lastAnnotations.size(), 1, "One nested annotation expected");

        AnnotationData lastTagAnnotation = Iterables.getOnlyElement(lastAnnotations);
        assertEquals(DbUnit.DataSets.Csv.ImagesAnnotated.TAG_NAME_LINKED,
                     lastTagAnnotation.getContentAsString(), "Wrong tag value");
    }

}
