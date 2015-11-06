package org.imagopole.omero.tools.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.File;

import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.TagAnnotationData;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unitils.mock.Mock;
import org.unitils.mock.core.MockObject;



public class FunctionsUtilTest {

    @Test(dataProvider="numbers-provider")
    public void toLongOrNullTests(String input, Long expected) {
        Long result = FunctionsUtil.toLongOrNull.apply(input);

        assertEquals(result, expected);
    }

    @Test
    public void toPojoNameTests() {
        Mock<PojoData> nullMock = new MockObject<PojoData>(PojoData.class, null);
        nullMock.returns(null).getName();

        assertNull(FunctionsUtil.toPojoName.apply(nullMock.getMock()));
        nullMock.assertInvoked().getName();

        Mock<PojoData> mock = new MockObject<PojoData>(PojoData.class, null);
        mock.returns("dataset.name").getName();
        assertEquals("dataset.name", FunctionsUtil.toPojoName.apply(mock.getMock()));
        mock.assertInvoked().getName();

        Mock<PojoData> emptyMock = new MockObject<PojoData>(PojoData.class, null);
        emptyMock.returns("    ").getName();
        assertEquals("    ", FunctionsUtil.toPojoName.apply(emptyMock.getMock()));
        emptyMock.assertInvoked().getName();
    }

    @Test
    public void toPojoIdTests() {
        Mock<PojoData> nullMock = new MockObject<PojoData>(PojoData.class, null);
        nullMock.returns(null).getId();

        assertNull(FunctionsUtil.toPojoId.apply(nullMock.getMock()));
        nullMock.assertInvoked().getId();

        Mock<PojoData> mock = new MockObject<PojoData>(PojoData.class, null);
        mock.returns(1L).getId();
        assertEquals(Long.valueOf(1), FunctionsUtil.toPojoId.apply(mock.getMock()));
        mock.assertInvoked().getId();
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
