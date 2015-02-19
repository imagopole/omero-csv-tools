package org.imagopole.omero.tools.impl.blitz;

import static org.imagopole.omero.tools.TestsUtil.newAttachment;
import static org.imagopole.omero.tools.TestsUtil.newDataset;
import static org.imagopole.omero.tools.TestsUtil.newImage;
import static org.imagopole.omero.tools.TestsUtil.newProject;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import omero.model.DatasetAnnotationLinkI;
import omero.model.IObject;
import omero.model.ImageAnnotationLinkI;
import omero.model.ProjectAnnotationLinkI;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.blitz.AnnotationLinker;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.impl.blitz.AnnotationLinkers.DatasetAnnotationLinker;
import org.imagopole.omero.tools.impl.blitz.AnnotationLinkers.ImageAnnotationLinker;
import org.imagopole.omero.tools.impl.blitz.AnnotationLinkers.ProjectAnnotationLinker;
import org.testng.annotations.Test;


public class AnnotationLinkersTest {

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void forAnnotatedTypeShouldRejectNulls() {
       AnnotationLinkers.forAnnotatedType(null);
    }

    @Test
    public void forAnnotatedTypeShouldSupportDatasets() {
        AnnotationLinker result = AnnotationLinkers.forAnnotatedType(AnnotatedType.dataset);

        assertNotNull(result, "Non-null result expected");
        assertTrue(result instanceof DatasetAnnotationLinker, "Incorrect linker type");
    }

    @Test
    public void forAnnotatedTypeShouldSupportImages() {
        AnnotationLinker result = AnnotationLinkers.forAnnotatedType(AnnotatedType.image);

        assertNotNull(result, "Non-null result expected");
        assertTrue(result instanceof ImageAnnotationLinker, "Incorrect linker type");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void forContainerTypeShouldRejectNulls() {
        AnnotationLinkers.forContainerType(null);
    }

    @Test
    public void forAnnotatedTypeShouldSupportProjects() {
        AnnotationLinker result = AnnotationLinkers.forContainerType(ContainerType.project);

        assertNotNull(result, "Non-null result expected");
        assertTrue(result instanceof ProjectAnnotationLinker, "Incorrect linker type");
    }

    @Test
    public void forContainerTypeShouldSupportDatasets() {
        AnnotationLinker result = AnnotationLinkers.forContainerType(ContainerType.dataset);

        assertNotNull(result, "Non-null result expected");
        assertTrue(result instanceof DatasetAnnotationLinker, "Incorrect linker type");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void datasetAnnotationLinkerShouldRejectNullAnnotationParam() {
        AnnotationLinker linker = AnnotationLinkers.forAnnotatedType(AnnotatedType.dataset);
        linker.link(null, newDataset("dataset.name"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void datasetAnnotationLinkerShouldRejectNullModelObjectParam() {
        AnnotationLinker linker = AnnotationLinkers.forAnnotatedType(AnnotatedType.dataset);
        linker.link(newAttachment("some.annotation"), null);
    }

    @Test(expectedExceptions = { ClassCastException.class })
    public void datasetAnnotationLinkerShouldRaiseClassCastOnInvalidModelObject() {
        AnnotationLinker linker = AnnotationLinkers.forAnnotatedType(AnnotatedType.dataset);
        linker.link(newAttachment("some.annotation"), newImage("image.name"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void projectAnnotationLinkerShouldRejectNullAnnotationParam() {
        AnnotationLinker linker = AnnotationLinkers.forContainerType(ContainerType.project);
        linker.link(null, newProject("project.name"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void projectAnnotationLinkerShouldRejectNullModelObjectParam() {
        AnnotationLinker linker = AnnotationLinkers.forContainerType(ContainerType.project);
        linker.link(newAttachment("some.annotation"), null);
    }

    @Test(expectedExceptions = { ClassCastException.class })
    public void projectAnnotationLinkerShouldRaiseClassCastOnInvalidModelObject() {
        AnnotationLinker linker = AnnotationLinkers.forContainerType(ContainerType.project);
        linker.link(newAttachment("some.annotation"), newImage("image.name"));
    }

    @Test
    public void projectAnnotationLinkerTests() {
        AnnotationLinker linker = AnnotationLinkers.forContainerType(ContainerType.project);

        IObject result = linker.link(newAttachment("projcet.annotation"), newProject("project.name"));

        assertNotNull(result, "Non-null results expected");
        assertTrue(result instanceof ProjectAnnotationLinkI, "Incorrect link type");

        ProjectAnnotationLinkI link = (ProjectAnnotationLinkI) result;
        assertNotNull(link.getParent(), "Non-null link parent expected");
        assertNotNull(link.getChild(), "Non-null link child expected");
    }

    @Test
    public void datasetAnnotationLinkerTests() {
        AnnotationLinker linker = AnnotationLinkers.forAnnotatedType(AnnotatedType.dataset);

        IObject result = linker.link(newAttachment("dataset.annotation"), newDataset("dataset.name"));

        assertNotNull(result, "Non-null results expected");
        assertTrue(result instanceof DatasetAnnotationLinkI, "Incorrect link type");

        DatasetAnnotationLinkI link = (DatasetAnnotationLinkI) result;
        assertNotNull(link.getParent(), "Non-null link parent expected");
        assertNotNull(link.getChild(), "Non-null link child expected");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void imageAnnotationLinkerShouldRejectNullAnnotationParam() {
        AnnotationLinker linker = AnnotationLinkers.forAnnotatedType(AnnotatedType.image);
        linker.link(null, newImage("image.name"));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void imageAnnotationLinkerShouldRejectNullModelObjectParam() {
        AnnotationLinker linker = AnnotationLinkers.forAnnotatedType(AnnotatedType.image);
        linker.link(newAttachment("some.annotation"), null);
    }

    @Test(expectedExceptions = { ClassCastException.class })
    public void imageAnnotationLinkerShouldRaiseClassCastOnInvalidModelObject() {
        AnnotationLinker linker = AnnotationLinkers.forAnnotatedType(AnnotatedType.image);
        linker.link(newAttachment("some.annotation"), newDataset("dataset.name"));
    }

    @Test
    public void ImageAnnotationLinkerTests() {
        AnnotationLinker linker = AnnotationLinkers.forAnnotatedType(AnnotatedType.image);

        IObject result = linker.link(newAttachment("image.annotation"), newImage("image.name"));

        assertNotNull(result, "Non-null results expected");
        assertTrue(result instanceof ImageAnnotationLinkI, "Incorrect link type");

        ImageAnnotationLinkI link = (ImageAnnotationLinkI) result;
        assertNotNull(link.getParent(), "Non-null link parent expected");
        assertNotNull(link.getChild(), "Non-null link child expected");
    }

}
