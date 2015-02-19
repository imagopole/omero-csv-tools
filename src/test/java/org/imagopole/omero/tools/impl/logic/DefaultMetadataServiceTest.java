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
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.ContainersBlitzService;
import org.imagopole.support.unitils.dbunit.annotation.UnloadDataSet;
import org.imagopole.support.unitils.dbunit.datasetfactory.impl.SingleSchemaCsvDataSetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.unitils.dbunit.annotation.DataSet;

import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ImageData;

import com.google.common.collect.Iterables;

public class DefaultMetadataServiceTest extends AbstractBlitzClientTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultMetadataServiceTest.class);

    /** @TestedObject */
    private DefaultMetadataService metadataService;

    @Override
    protected void setUpAfterIceConnection(ServiceFactoryPrx session) {
         log.debug("setUpAfterIceConnection with session {}", session);

         OmeroAnnotationService annotationService = new AnnotationBlitzService(session);
         OmeroContainerService containerService = new ContainersBlitzService(session);

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
        metadataService.listImagesPlusAnnotationsByExperimenterAndDataset(
                null, 1L, AnnotationType.tag, AnnotatedType.image);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectNullDataset() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndDataset(
                1L, null, AnnotationType.tag, AnnotatedType.image);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectNullAnnotationType() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndDataset(
                1L, 1L, null, AnnotatedType.image);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectNullAnnotatedType() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndDataset(
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
          expectedExceptionsMessageRegExp = "No image records found within container \\d* of type dataset .*")
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldRejectEmptyDatasets() throws ServerError {
        metadataService.listImagesPlusAnnotationsByExperimenterAndDataset(
            DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Orphans.DATASET_ID,
            AnnotationType.tag, AnnotatedType.image);
    }

    @DataSet(value= { DataSets.Csv.IMAGES }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void listImagesPlusAnnotationsByExperimenterAndDatasetShouldSupportNestedImages() throws ServerError {
        Collection<PojoData> result =
            metadataService.listImagesPlusAnnotationsByExperimenterAndDataset(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.Images.DATASET_ID,
                AnnotationType.tag, AnnotatedType.image);

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
            metadataService.listImagesPlusAnnotationsByExperimenterAndDataset(
                DbUnit.EXPERIMENTER_ID, DbUnit.DataSets.Csv.ImagesAnnotated.DATASET_ID,
                AnnotationType.tag, AnnotatedType.image);

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

}
