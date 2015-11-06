package org.imagopole.omero.tools.impl.dto;

import static org.imagopole.omero.tools.TestsUtil.newAttachment;
import static org.imagopole.omero.tools.TestsUtil.newImage;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.testng.annotations.Test;
import org.unitils.mock.Mock;
import org.unitils.mock.core.MockObject;

public class DefaultPojoDataTest {

    @Test(expectedExceptions = { IllegalArgumentException.class },
         expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectNullDataset() {
        DefaultPojoData.fromDatasetData(null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
         expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectNullDatasetName() {
        Mock<DatasetData> datasetMock = new MockObject<DatasetData>(DatasetData.class, null);
        datasetMock.returns(null).getName();

        DefaultPojoData.fromDatasetData(datasetMock.getMock());

        datasetMock.assertInvoked().getName();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
         expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectEmptyDatasetName() {
        DatasetData dataset = new DatasetData();
        dataset.setName("    ");

        DefaultPojoData.fromDatasetData(dataset);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectNullImage() {
        DefaultPojoData.fromImageData(null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
         expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectNullImageName() {
        Mock<ImageData> imageMock = new MockObject<ImageData>(ImageData.class, null);
        imageMock.returns(null).getName();

        DefaultPojoData.fromImageData(imageMock.getMock());

        imageMock.assertInvoked().getName();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
         expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectEmptyImageName() {
        ImageData image = new ImageData();
        image.setName("    ");

        DefaultPojoData.fromImageData(image);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectNullPlate() {
        DefaultPojoData.fromPlateData(null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectNullPlateName() {
        Mock<PlateData> plateMock = new MockObject<PlateData>(PlateData.class, null);
        plateMock.returns(null).getName();

        DefaultPojoData.fromPlateData(plateMock.getMock());

        plateMock.assertInvoked().getName();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectEmptyPlateName() {
        PlateData plate = new PlateData();
        plate.setName("    ");

        DefaultPojoData.fromPlateData(plate);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void pojoDataShouldRejectNullPlateAcquisition() {
        DefaultPojoData.fromPlateAcquisitionData(null);
    }

    @Test
    public void pojoDataShouldConvertNullPlateAcquisitionNameToDefault() {
        Mock<PlateAcquisitionData> paMock = new MockObject<PlateAcquisitionData>(PlateAcquisitionData.class, null);
        paMock.returns(null).getName();

        PojoData pojo = DefaultPojoData.fromPlateAcquisitionData(paMock.getMock());

        assertNotNull(pojo, "Non-null result expected");
        assertEquals(pojo.getName(), "Run " + pojo.getId(), "Incorrect pojo name");

        paMock.assertInvoked().getName();
    }

    @Test
    public void pojoDataShouldConvertEmptyPlateAcquisitionNameToDefault() {
        PlateAcquisitionData pa = new PlateAcquisitionData();
        pa.setName("    ");

        PojoData pojo = DefaultPojoData.fromPlateAcquisitionData(pa);

        assertNotNull(pojo, "Non-null result expected");
        assertEquals(pojo.getName(), "Run " + pojo.getId(), "Incorrect pojo name");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    @SuppressWarnings("unchecked")
    public void pojoDataCopyShouldRejectNullPojo() {
        DefaultPojoData.fromAnnotatedPojo(null, Collections.EMPTY_LIST);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    @SuppressWarnings("unchecked")
    public void pojoDataCopyShouldRejectEmptyName() {
        ImageData image = new ImageData();
        image.setName("    ");

        DefaultPojoData.fromAnnotatedPojo(null, Collections.EMPTY_LIST);
    }

    @Test
    public void pojoDataCopyShouldIgnoreNullAnnotations() {
        String expectedPojoName = "copy-and-ignore";
        ImageData image = newImage(expectedPojoName);

        PojoData inputPojo = DefaultPojoData.fromImageData(image);

        PojoData result = DefaultPojoData.fromAnnotatedPojo(inputPojo, null);
        assertNotNull(result, "Non-null result expected");
        assertNull(result.getAnnotations(), "Null annotations expected");
        assertEquals(result.getName(), expectedPojoName, "Incorrect pojo name");
        assertEquals(result.getId().longValue(), image.getId(), "Incorrect pojo id");
    }

    @Test
    public void pojoDataCopyTest() {
        String expectedPojoName = "copy-all";
        ImageData image = newImage(expectedPojoName);

        PojoData inputPojo = DefaultPojoData.fromImageData(image);
        List<AnnotationData> fixtureAnnotations =
            Arrays.asList(new AnnotationData[] { newAttachment("fixture.file.annotation") });

        PojoData result = DefaultPojoData.fromAnnotatedPojo(inputPojo, fixtureAnnotations);
        assertNotNull(result, "Non-null result expected");
        assertNotNull(result.getAnnotations(), "Non-null annotations expected");
        assertEquals(result.getAnnotations(), fixtureAnnotations, "Incorrect annotations");
        assertEquals(result.getName(), expectedPojoName, "Incorrect pojo name");
        assertEquals(result.getId().longValue(), image.getId(), "Incorrect pojo id");
    }

}
