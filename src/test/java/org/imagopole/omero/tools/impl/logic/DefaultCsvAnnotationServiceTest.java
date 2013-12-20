package org.imagopole.omero.tools.impl.logic;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.DatasetAnnotationLink;
import omero.model.IObject;

import org.imagopole.omero.tools.AbstractBlitzClientTest;
import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.TestsUtil.DbUnit;
import org.imagopole.omero.tools.TestsUtil.DbUnit.DataSets;
import org.imagopole.omero.tools.TestsUtil.Groups;
import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.api.dto.LinksData;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.ContainersBlitzService;
import org.imagopole.omero.tools.impl.blitz.UpdateBlitzService;
import org.imagopole.support.unitils.dbunit.annotation.UnloadDataSet;
import org.imagopole.support.unitils.dbunit.datasetfactory.impl.SingleSchemaCsvDataSetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.unitils.dbunit.annotation.DataSet;

public class DefaultCsvAnnotationServiceTest extends AbstractBlitzClientTest {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultCsvAnnotationServiceTest.class);

    /** @TestedObject */
    private DefaultCsvAnnotationService csvAnnotationService;

    @Override
    protected void setUpAfterIceConnection(ServiceFactoryPrx session) {
        log.debug("setUpAfterIceConnection with session {}", session);

        OmeroContainerService containerService = new ContainersBlitzService(session);
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

    @DataSet(value= { DataSets.Csv.ORPHANS }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION },
          expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void saveTagsAndLinkNestedDatasetsShouldRejectOrphanedDatasets() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(DbUnit.DATASET_ORPHAN_NAME, "DbUnit.Tag");

        // the specified dataset is not nested within a project, therefore will be ignored from the
        // tagging
        // TODO: have a more useful error message to indicate this rather than an IAE further
        // down the line
        csvAnnotationService.saveTagsAndLinkNestedDatasets(DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, lines);
    }

    @DataSet(value= { DataSets.Csv.LINKED }, factory = SingleSchemaCsvDataSetFactory.class)
    @UnloadDataSet
    @Test(groups = { Groups.INTEGRATION }
        )
    public void saveTagsAndLinkNestedDatasetsShouldCreateNewTagsUnnlinkedToDatasets() throws ServerError, IOException {
        Multimap<String, String> lines = HashMultimap.create();
        lines.put(DbUnit.DATASET_LINKED_NAME, "DbUnit.Tag");

        // the specified dataset is nested within a project so will be fetched
        // the method should create the new tag but not link it to the dataset
        LinksData data =
            csvAnnotationService.saveTagsAndLinkNestedDatasets(DbUnit.EXPERIMENTER_ID, DbUnit.PROJECT_ID, lines);

        assertNotNull(data, "Non-null result expected");

        Collection<IObject> knowns = data.getKnownAnnotationLinks();
        assertTrue((null == knowns || knowns.isEmpty()), "No knowns expected");

        Collection<IObject> news = data.getNewAnnotationLinks();
        assertNotNull(news, "Non-null news expected");
        assertEquals(news.size(), 1, "One new tag saved expected");

        //clean up test side effects (remove the created tag)
        DatasetAnnotationLink newLink = (DatasetAnnotationLink) Iterables.getOnlyElement(news);
        Annotation tag = newLink.getChild();
        super.getSession().getUpdateService().deleteObject(tag);
    }

}
