package org.imagopole.omero.tools.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.File;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unitils.mock.Mock;
import org.unitils.mock.core.MockObject;

import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;



public class FunctionsUtilTest {

    @Test(dataProvider="numbers-provider")
    public void toLongOrNullTests(String input, Long expected) {
        Long result = FunctionsUtil.toLongOrNull.apply(input);

        assertEquals(result, expected);
    }

    @Test
    public void toDatasetNameTests() {
        Mock<DatasetData> mock = new MockObject<DatasetData>(DatasetData.class, null);
        mock.returns(null).getName();

        assertNull(FunctionsUtil.toDatasetName.apply(mock.getMock()));
        mock.assertInvoked().getName();

        DatasetData input = new DatasetData();
        input.setName("dataset.name");
        assertEquals("dataset.name", FunctionsUtil.toDatasetName.apply(input));

        input.setName("    ");
        assertEquals("    ", FunctionsUtil.toDatasetName.apply(input));
    }

    @Test
    public void toDatasetIdTests() {
        Mock<DatasetData> mock = new MockObject<DatasetData>(DatasetData.class, null);
        mock.returns(null).getId();

        assertEquals(Long.valueOf(0), FunctionsUtil.toDatasetId.apply(mock.getMock()));
        mock.assertInvoked().getId();

        DatasetData input = new DatasetData();
        input.setId(1L);
        assertEquals(Long.valueOf(1), FunctionsUtil.toDatasetId.apply(input));
    }

    @Test
    public void toTagValueTests() {
        Mock<TagAnnotationData> mock =
            new MockObject<TagAnnotationData>(TagAnnotationData.class, null);
        mock.returns(null).getTagValue();

        assertNull(FunctionsUtil.toTagValue.apply(mock.getMock()));
        mock.assertInvoked().getTagValue();

        TagAnnotationData input = new TagAnnotationData("tag.name");
        assertEquals("tag.name", FunctionsUtil.toTagValue.apply(input));
    }

    @Test
    public void toAnnotationFileNameTests() {
        Mock<FileAnnotationData> mock =
            new MockObject<FileAnnotationData>(FileAnnotationData.class, null);
        mock.returns(null).getFileName();

        assertNull(FunctionsUtil.toAnnotationFileName.apply(mock.getMock()));
        mock.assertInvoked().getFileName();

        FileAnnotationData input = new FileAnnotationData(new File("file.name"));
        assertEquals("file.name", FunctionsUtil.toAnnotationFileName.apply(input));

        input = new FileAnnotationData(new File("    "));
        assertEquals("    ", FunctionsUtil.toAnnotationFileName.apply(input));
    }

    @DataProvider(name="numbers-provider")
    private Object[][] provideNumbers() {
        return new Object[][] {
            { null, null },
            { "", null},
            { "    ", null },
            { "NaN", null },
            { "1", 1L },
        };
    }

}
