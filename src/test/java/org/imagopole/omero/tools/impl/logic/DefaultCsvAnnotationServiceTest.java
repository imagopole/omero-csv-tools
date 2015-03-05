package org.imagopole.omero.tools.impl.logic;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ome.model.containers.Dataset;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.DatasetAnnotationLink;
import omero.model.IObject;
import omero.model.ImageAnnotationLinkI;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model.PlateAnnotationLinkI;
import omero.model.TagAnnotation;

import org.imagopole.omero.tools.AbstractBlitzClientTest;
import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.TestsUtil.DbUnit;
import org.imagopole.omero.tools.TestsUtil.DbUnit.DataSets;
import org.imagopole.omero.tools.TestsUtil.DbUnit.DataSets.Csv;
import org.imagopole.omero.tools.TestsUtil.Groups;
import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.dto.LinksData;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.ShimContainersBlitzService;
import org.imagopole.omero.tools.impl.blitz.UpdateBlitzService;
import org.imagopole.omero.tools.util.BlitzUtil;
import org.imagopole.support.unitils.dbunit.annotation.UnloadDataSet;
import org.imagopole.support.unitils.dbunit.datasetfactory.impl.SingleSchemaCsvDataSetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.unitils.dbunit.annotation.DataSet;

import pojos.DataObject;
import pojos.TagAnnotationData;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class DefaultCsvAnnotationServiceTest extends AbstractBlitzClientTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultCsvAnnotationServiceTest.class);

    /** @TestedObject */
    private DefaultCsvAnnotationService csvAnnotationService;

    @Override
    protected void setUpAfterIceConnection(ServiceFactoryPrx session) {
        log.debug("setUpAfterIceConnection with session {}", session);

        OmeroContainerService containerService = new ShimContainersBlitzService(session);
        OmeroAnnotationService annotationService = new AnnotationBlitzService(session);
        OmeroUpdateService updateService = new UpdateBlitzService(session);

        csvAnnotationService = new DefaultCsvAnnotationService();

        csvAnnotationService.setContainerService(containerService);
        csvAnnotationService.setAnnotationService(annotationService);
        csvAnnotationService.setUpdateService(updateService);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedDatasetsShouldRejectNullExperimenter() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedDatasets(null, 1L, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedDatasetsShouldRejectNullProject() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedDatasets(1L, null, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedDatasetsShouldRejectNullLines() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedDatasets(1L, 1L, null);
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedImagesShouldRejectNullExperimenter() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedImages(
            null, 1L, ContainerType.dataset, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedImagesShouldRejectNullContainerId() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedImages(1L, null,
            ContainerType.dataset, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedImagesShouldRejectNullContainerType() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedImages(1L, 1L,
            null, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedImagesShouldRejectNullLines() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedImages(1L, 1L, ContainerType.dataset, null);
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedPlatesShouldRejectNullExperimenter() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedPlates(null, 1L, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedPlatesShouldRejectNullScreen() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedPlates(1L, null, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedPlatesShouldRejectNullLines() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedPlates(1L, 1L, null);
    }


    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedPlateAcquisitionsShouldRejectNullExperimenter() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedPlateAcquisitions(null, 1L, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedPlateAcquisitionsShouldRejectNullPlate() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedPlateAcquisitions(1L, null, TestsUtil.emptyStringMultimap());
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedPlateAcquisitionsShouldRejectNullLines() throws ServerError {
        csvAnnotationService.saveTagsAndLinkNestedPlateAcquisitions(1L, 1L, null);
    }


    @DataSet(value= { DataSets.Csv.ORPHANS }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class },
          expectedExceptionsMessageRegExp = "None of the requested CSV target names exist for experimenter")
    public void saveTagsAndLinkNestedDatasetsShouldRejectOrphanedDatasets() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Orphans.DATASET_NAME, "DbUnit.Tag");

        // the specified dataset is not nested within a project, therefore will be ignored from the
        // tagging
        csvAnnotationService.saveTagsAndLinkNestedDatasets(DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, lines);
    }

    @DataSet(value= { DataSets.Csv.LINKED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedDatasetsShouldCreateNewTagsStillUnlinkedToDatasets() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Linked.DATASET_NAME, "DbUnit.Tag");

        // the specified dataset is nested within a project so will be fetched
        // the method should create the new tag but not link it to the dataset
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedDatasets(
                            DbUnit.EXPERIMENTER_ID,
                            DbUnit.PROJECT_ID,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertTrue((null == knowns || knowns.isEmpty()), "No knowns expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertNotNull(news, "Non-null news expected");
        assertEquals(news.size(), 1, "One new tag saved expected");

        //clean up test side effects (remove the created tag)
        DatasetAnnotationLink newLink = (DatasetAnnotationLink) Iterables.getOnlyElement(news);
        Annotation createdTag = newLink.getChild();
        super.getSession().getUpdateService().deleteObject(createdTag);
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedDatasetsShouldIgnoreTagsAlreadyLinkedToDatasets() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Annotated.DATASET_NAME, Csv.Annotated.TAG_NAME_LINKED);

        // the method should ignore the existing tag
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedDatasets(
                            DbUnit.EXPERIMENTER_ID,
                            DbUnit.PROJECT_ID,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertTrue((null == knowns || knowns.isEmpty()), "No knowns expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertTrue((null == news || news.isEmpty()), "No news expected");

        // check no dataset-tag association has been created
        Map<Long, List<Annotation>> tagDatasetLinks =
            super.getSession().getMetadataService().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(Csv.Annotated.DATASET_ID),
                        BlitzUtil.byExperimenter(DbUnit.EXPERIMENTER_ID));

        assertNotNull(tagDatasetLinks, "Non-null links expected");
        assertEquals(tagDatasetLinks.size(), 1, "One link expected");
        assertTrue(tagDatasetLinks.containsKey(Csv.Annotated.DATASET_ID), "Wrong dataset id");

        // double check the returned data: we should only get the existing linked tag
        List<Annotation> tags = tagDatasetLinks.get(Csv.Annotated.DATASET_ID);
        assertNotNull(tags, "Non-null tags expected");

        TagAnnotationData tag = (TagAnnotationData) DataObject.asPojo(Iterables.getOnlyElement(tags));
        assertEquals(tag.getContentAsString(), Csv.Annotated.TAG_NAME_LINKED, "Wrong tag name");
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedDatasetsShouldLinkExistingTagsToDatasets() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Annotated.DATASET_NAME, Csv.Annotated.TAG_NAME_UNLINKED);

        // the method should create a link to the existing tag (to be saved later on)
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedDatasets(
                            DbUnit.EXPERIMENTER_ID,
                            DbUnit.PROJECT_ID,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertNotNull(knowns, "Non-null knowns expected");
        assertEquals(knowns.size(), 1, "One knowns tag expected");

        DatasetAnnotationLink link = (DatasetAnnotationLink) Iterables.getOnlyElement(knowns);
        TagAnnotationData tagToLink = (TagAnnotationData) DataObject.asPojo(link.getChild());
        assertEquals(tagToLink.getContentAsString(), Csv.Annotated.TAG_NAME_UNLINKED, "Wrong tag name");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertTrue((null == news || news.isEmpty()), "No news expected");

        // check no dataset-tag association has been created
        Map<Long, List<Annotation>> tagDatasetLinks =
            super.getSession().getMetadataService().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(Csv.Annotated.DATASET_ID),
                        BlitzUtil.byExperimenter(DbUnit.EXPERIMENTER_ID));

        assertNotNull(tagDatasetLinks, "Non-null links expected");
        assertEquals(tagDatasetLinks.size(), 1, "One link expected");
        assertTrue(tagDatasetLinks.containsKey(Csv.Annotated.DATASET_ID), "Wrong dataset id");

        // double check the returned data: we should only get the existing linked tag
        List<Annotation> tags = tagDatasetLinks.get(Csv.Annotated.DATASET_ID);
        assertNotNull(tags, "Non-null tags expected");

        TagAnnotationData tag = (TagAnnotationData) DataObject.asPojo(Iterables.getOnlyElement(tags));
        assertEquals(tag.getContentAsString(), Csv.Annotated.TAG_NAME_LINKED, "Wrong tag name");
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedDatasetsShouldSupportLinkedAndUnlinkedTags() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Annotated.DATASET_NAME, Csv.Annotated.TAG_NAME_LINKED);
        lines.put(Csv.Annotated.DATASET_NAME, Csv.Annotated.TAG_NAME_UNLINKED);

        // the method should create a link to the existing tag (to be saved later on) and
        // ignore the existing tag
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedDatasets(
                            DbUnit.EXPERIMENTER_ID,
                            DbUnit.PROJECT_ID,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertNotNull(knowns, "Non-null knowns expected");
        assertEquals(knowns.size(), 1, "One knowns tag expected");

        DatasetAnnotationLink link = (DatasetAnnotationLink) Iterables.getOnlyElement(knowns);
        TagAnnotationData tagToLink = (TagAnnotationData) DataObject.asPojo(link.getChild());
        assertEquals(tagToLink.getContentAsString(), Csv.Annotated.TAG_NAME_UNLINKED, "Wrong tag name");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertTrue((null == news || news.isEmpty()), "No news expected");

        // check no dataset-tag association has been created
        Map<Long, List<Annotation>> tagDatasetLinks =
            super.getSession().getMetadataService().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(Csv.Annotated.DATASET_ID),
                        BlitzUtil.byExperimenter(DbUnit.EXPERIMENTER_ID));

        assertNotNull(tagDatasetLinks, "Non-null links expected");
        assertEquals(tagDatasetLinks.size(), 1, "One link expected");
        assertTrue(tagDatasetLinks.containsKey(Csv.Annotated.DATASET_ID), "Wrong dataset id");

        // double check the returned data: we should only get the existing linked tag
        List<Annotation> tags = tagDatasetLinks.get(Csv.Annotated.DATASET_ID);
        assertNotNull(tags, "Non-null tags expected");

        TagAnnotationData tag = (TagAnnotationData) DataObject.asPojo(Iterables.getOnlyElement(tags));
        assertEquals(tag.getContentAsString(), Csv.Annotated.TAG_NAME_LINKED, "Wrong tag name");
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedDatasetsShouldSupportLinkedAndUnlinkedAndNewTags() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Annotated.DATASET_NAME, Csv.Annotated.TAG_NAME_LINKED);
        lines.put(Csv.Annotated.DATASET_NAME, Csv.Annotated.TAG_NAME_UNLINKED);
        lines.put(Csv.Annotated.DATASET_NAME, "DbUnit.Tag");

        // the method should create a link to the existing tag (to be saved later on) and
        // ignore the existing tag
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedDatasets(
                            DbUnit.EXPERIMENTER_ID,
                            DbUnit.PROJECT_ID,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertNotNull(knowns, "Non-null knowns expected");
        assertEquals(knowns.size(), 1, "One knowns tag expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertNotNull(news, "Non-null news expected");
        assertEquals(news.size(), 1, "One new tag saved expected");

        DatasetAnnotationLink link = (DatasetAnnotationLink) Iterables.getOnlyElement(knowns);
        TagAnnotationData tagToLink = (TagAnnotationData) DataObject.asPojo(link.getChild());
        assertEquals(tagToLink.getContentAsString(), Csv.Annotated.TAG_NAME_UNLINKED, "Wrong tag name");

        // check no dataset-tag association has been created
        Map<Long, List<Annotation>> tagDatasetLinks =
            super.getSession().getMetadataService().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(Csv.Annotated.DATASET_ID),
                        BlitzUtil.byExperimenter(DbUnit.EXPERIMENTER_ID));

        assertNotNull(tagDatasetLinks, "Non-null links expected");
        assertEquals(tagDatasetLinks.size(), 1, "One link expected");
        assertTrue(tagDatasetLinks.containsKey(Csv.Annotated.DATASET_ID), "Wrong dataset id");

        // double check the returned data: we should only get the existing linked tag
        List<Annotation> tags = tagDatasetLinks.get(Csv.Annotated.DATASET_ID);
        assertNotNull(tags, "Non-null tags expected");

        TagAnnotationData tag = (TagAnnotationData) DataObject.asPojo(Iterables.getOnlyElement(tags));
        assertEquals(tag.getContentAsString(), Csv.Annotated.TAG_NAME_LINKED, "Wrong tag name");

        //clean up test side effects (remove the created tag)
        DatasetAnnotationLink newLink = (DatasetAnnotationLink) Iterables.getOnlyElement(news);
        Annotation createdTag = newLink.getChild();
        super.getSession().getUpdateService().deleteObject(createdTag);
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED_HIERARCHY }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedDatasetsShouldSupportMultipleDatasets() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.AnnotatedHierarchy.DATASET_NAME_TAGGED_FULLY, Csv.AnnotatedHierarchy.TAG_NAME_LINKED_1);
        lines.put(Csv.AnnotatedHierarchy.DATASET_NAME_TAGGED_FULLY, Csv.AnnotatedHierarchy.TAG_NAME_LINKED_2);
        lines.put(Csv.AnnotatedHierarchy.DATASET_NAME_TAGGED, Csv.AnnotatedHierarchy.TAG_NAME_LINKED_2);

        // the method should create a link to the existing tag on the second dataset and
        // ignore the existing tags on the fully tagged dataset
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedDatasets(
                            DbUnit.EXPERIMENTER_ID,
                            DbUnit.PROJECT_ID,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertNotNull(knowns, "Non-null knowns expected");
        assertEquals(knowns.size(), 1, "One knowns tag expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertTrue((null == news || news.isEmpty()), "No news expected");

        DatasetAnnotationLink link = (DatasetAnnotationLink) Iterables.getOnlyElement(knowns);
        TagAnnotationData tagToLink = (TagAnnotationData) DataObject.asPojo(link.getChild());
        assertEquals(tagToLink.getContentAsString(), Csv.AnnotatedHierarchy.TAG_NAME_LINKED_2, "Wrong tag name");

        // check no dataset-tag association has been created
        Map<Long, List<Annotation>> tagDatasetLinks =
            super.getSession().getMetadataService().loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(),
                        null,
                        null,
                        Dataset.class.getName(),
                        Lists.newArrayList(Csv.AnnotatedHierarchy.DATASET_ID_TAGGED),
                        BlitzUtil.byExperimenter(DbUnit.EXPERIMENTER_ID));

        assertNotNull(tagDatasetLinks, "Non-null links expected");
        assertEquals(tagDatasetLinks.size(), 1, "One link expected");
        assertTrue(tagDatasetLinks.containsKey(Csv.AnnotatedHierarchy.DATASET_ID_TAGGED), "Wrong dataset id");

        // double check the returned data: we should only get the existing linked tag
        List<Annotation> tags = tagDatasetLinks.get(Csv.AnnotatedHierarchy.DATASET_ID_TAGGED);
        assertNotNull(tags, "Non-null tags expected");

        TagAnnotationData tag = (TagAnnotationData) DataObject.asPojo(Iterables.getOnlyElement(tags));
        assertEquals(tag.getContentAsString(), Csv.AnnotatedHierarchy.TAG_NAME_LINKED_1, "Wrong tag name");
    }


    @DataSet(value= { DataSets.Csv.IMAGES }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedImagesWithinDatasetTest() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Images.IMAGE_NAME, "DbUnit.Tag");

        // the specified image is nested within a top-level dataset
        // the method should create the new tag but not link it to the image
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedImages(
                            DbUnit.EXPERIMENTER_ID,
                            Csv.Images.DATASET_ID,
                            ContainerType.dataset,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertTrue((null == knowns || knowns.isEmpty()), "No knowns expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertNotNull(news, "Non-null news expected");
        assertEquals(news.size(), 1, "One new tag saved expected");

        // persist the tag association
        Collection<IObject> imageTagLinks = csvAnnotationService.saveAllAnnotationLinks(data);
        assertNotNull(imageTagLinks, "Non-null links expected");
        assertEquals(imageTagLinks.size(), 1, "One new link saved expected");

        //clean up test side effects (remove the created tag and its association to the image)
        ImageAnnotationLinkI imageTagLink = (ImageAnnotationLinkI) Iterables.getOnlyElement(imageTagLinks);
        Annotation createdTag = imageTagLink.getChild();
        super.getSession().getUpdateService().deleteObject(imageTagLink);
        super.getSession().getUpdateService().deleteObject(createdTag);
    }


    @DataSet(value= { DataSets.Csv.ORPHANS }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalStateException.class },
          expectedExceptionsMessageRegExp = "None of the requested CSV target names exist for experimenter")
    public void saveTagsAndLinkNestedPlatesShouldRejectOrphanedPlates() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Orphans.PLATE_NAME, "DbUnit.Tag");

        // the specified plate is not nested within a screen, therefore will be ignored from the
        // tagging
        csvAnnotationService.saveTagsAndLinkNestedPlates(DbUnit.EXPERIMENTER_ID, DbUnit.SCREEN_ID, lines);
    }

    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedPlatesTest() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Annotated.PLATE_NAME, "DbUnit.Tag");

        // the specified plate is nested within a screen
        // the method should create the new tag but not link it to the plate
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedPlates(
                            DbUnit.EXPERIMENTER_ID,
                            DbUnit.SCREEN_ID,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertTrue((null == knowns || knowns.isEmpty()), "No knowns expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertNotNull(news, "Non-null news expected");
        assertEquals(news.size(), 1, "One new tag saved expected");

        // persist the tag association
        Collection<IObject> plateTagLinks = csvAnnotationService.saveAllAnnotationLinks(data);
        assertNotNull(plateTagLinks, "Non-null links expected");
        assertEquals(plateTagLinks.size(), 1, "One new link saved expected");

        //clean up test side effects (remove the created tag and its association to the plate)
        PlateAnnotationLinkI plateTagLink = (PlateAnnotationLinkI) Iterables.getOnlyElement(plateTagLinks);
        Annotation createdTag = plateTagLink.getChild();
        super.getSession().getUpdateService().deleteObject(plateTagLink);
        super.getSession().getUpdateService().deleteObject(createdTag);
    }


    @DataSet(value= { DataSets.Csv.ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedPlateAcquisitionsTests() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.Annotated.PLATE_ACQUISITION_NAME, Csv.Annotated.TAG_NAME_LINKED);
        lines.put(Csv.Annotated.PLATE_ACQUISITION_NAME, "DbUnit.Tag");

        // the specified plateacquisition is nested within a plate
        // the method should: create the new tag but not link it to the plateacquisition
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedPlateAcquisitions(
                            DbUnit.EXPERIMENTER_ID,
                            Csv.Annotated.PLATE_ID_WITH_RUNS,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertTrue((null == knowns || knowns.isEmpty()), "No knowns expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertNotNull(news, "Non-null news expected");
        assertEquals(news.size(), 1, "One new tag expected");

        // persist the tag association
        Collection<IObject> plateAcquisitionTagLinks = csvAnnotationService.saveAllAnnotationLinks(data);
        assertNotNull(plateAcquisitionTagLinks, "Non-null links expected");
        assertEquals(plateAcquisitionTagLinks.size(), 1, "One new link saved expected");

        //clean up test side effects (remove the created tag and its association to the plate)
        PlateAcquisitionAnnotationLinkI plateAcquisitionTagLink =
            (PlateAcquisitionAnnotationLinkI) Iterables.getOnlyElement(plateAcquisitionTagLinks);
        Annotation createdTag = plateAcquisitionTagLink.getChild();
        super.getSession().getUpdateService().deleteObject(plateAcquisitionTagLink);
        super.getSession().getUpdateService().deleteObject(createdTag);
    }


    @DataSet(value= { DataSets.Csv.IMAGES_ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedImagesWithinPlateAcquisitionTest() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.ImagesAnnotated.IMAGE_NAME_PWPAWSI_w1i0, "DbUnit.Tag.One");
        lines.put(Csv.ImagesAnnotated.IMAGE_NAME_PWPAWSI_w1i0, "DbUnit.Tag.Two");

        // the specified image is nested within a plateacquisition
        // the method should create the new tag but not link it to the image
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedImages(
                            DbUnit.EXPERIMENTER_ID,
                            Csv.ImagesAnnotated.PLATE_ACQUISITION_ID,
                            ContainerType.plateacquisition,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertTrue((null == knowns || knowns.isEmpty()), "No knowns expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertNotNull(news, "Non-null news expected");
        assertEquals(news.size(), 2, "Two new tags expected");

        // persist the tag associations
        Collection<IObject> imageTagLinks = csvAnnotationService.saveAllAnnotationLinks(data);
        assertNotNull(imageTagLinks, "Non-null links expected");
        assertEquals(imageTagLinks.size(), 2, "Two new links saved expected");

        //clean up test side effects (remove the created tags and their association to the image)
        for (IObject linkObject: imageTagLinks) {
            ImageAnnotationLinkI imageTagLink = (ImageAnnotationLinkI) linkObject;
            Annotation createdTag = imageTagLink.getChild();
            super.getSession().getUpdateService().deleteObject(imageTagLink);
            super.getSession().getUpdateService().deleteObject(createdTag);
        }
    }

    @DataSet(value= { DataSets.Csv.IMAGES_ANNOTATED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION })
    public void saveTagsAndLinkNestedImagesWithinPlateTest() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(Csv.ImagesAnnotated.IMAGE_NAME_PWWSI_w1i0, "DbUnit.Tag");

        // the specified image is nested within a plate without runs
        // the method should create the new tag but not link it to the image
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedImages(
                            DbUnit.EXPERIMENTER_ID,
                            Csv.ImagesAnnotated.PLATE_ID,
                            ContainerType.plate,
                            lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertTrue((null == knowns || knowns.isEmpty()), "No knowns expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertNotNull(news, "Non-null news expected");
        assertEquals(news.size(), 1, "One new tag expected");

        // persist the tag associations
        Collection<IObject> imageTagLinks = csvAnnotationService.saveAllAnnotationLinks(data);
        assertNotNull(imageTagLinks, "Non-null links expected");
        assertEquals(imageTagLinks.size(), 1, "One new link saved expected");

        //clean up test side effects (remove the created tags and their association to the image)
        ImageAnnotationLinkI imageTagLink = (ImageAnnotationLinkI) Iterables.getOnlyElement(imageTagLinks);
        Annotation createdTag = imageTagLink.getChild();
        super.getSession().getUpdateService().deleteObject(imageTagLink);
        super.getSession().getUpdateService().deleteObject(createdTag);
    }

}
