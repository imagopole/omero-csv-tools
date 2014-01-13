package org.imagopole.omero.tools.impl.dto;

import org.imagopole.omero.tools.TestsUtil;
import org.testng.annotations.Test;
import org.unitils.mock.Mock;
import org.unitils.mock.core.MockObject;

import pojos.DatasetData;
import pojos.ImageData;

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

}
