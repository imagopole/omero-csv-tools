package org.imagopole.omero.tools.util;

import static org.testng.Assert.assertEquals;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.RtException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;



public class ParseUtilTest {

    @Test(dataProvider="numbers-provider")
    public void parseLongOrNullTests(String input, Long expectedLong, Integer expectedInt) {
        Long result = ParseUtil.parseLongOrNull(input);

        assertEquals(result, expectedLong);
    }

    @Test(dataProvider="numbers-provider")
    public void parseIntegerOrNullTests(String input, Long expectedLong, Integer expectedInt) {
        Integer result = ParseUtil.parseIntegerOrNull(input);

        assertEquals(result, expectedInt);
    }

    @Test(dataProvider="characters-provider")
    public void parseCharacterOrNullTests(String input, Character expected) {
        Character result = ParseUtil.parseCharacterOrNull(input);

        assertEquals(result, expected);
    }

    @Test(dataProvider="booleans-provider")
    public void parseBooleanOrNullTests(String input, Boolean expected) {
        Boolean result = ParseUtil.parseBooleanOrNull(input);

        assertEquals(result, expected);
    }

    @Test(dataProvider="empty-mime-types-provider",
          expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void parseContentTypeOrFailEmptyTests(String input) {
        ParseUtil.parseContentTypeOrFail(input);
    }

    @Test(dataProvider="malformed-mime-types-provider",
          expectedExceptions = { RtException.class, MimeTypeParseException.class })
    public void parseContentTypeOrFailMalformedTests(String input) {
        ParseUtil.parseContentTypeOrFail(input);
    }

    @Test(dataProvider="mime-types-provider")
    public void parseContentTypeOrFailTests(String input, String expected) {
        MimeType result = ParseUtil.parseContentTypeOrFail(input);

        assertEquals(result.getBaseType(), expected);
    }

    @Test(dataProvider="file-names-provider")
    public void getFileBasenameTests(String input, String expected) {
        String result = ParseUtil.getFileBasename(input);

        assertEquals(result, expected);
    }

    @DataProvider(name="numbers-provider")
    private Object[][] provideNumbers() {
        return new Object[][] {
            { null, null, null },
            { "", null, null },
            { "    ", null, null },
            { "NaN", null, null },
            { "1", 1L, 1 },
        };
    }

    @DataProvider(name="characters-provider")
    private Object[][] provideCharacters() {
        return new Object[][] {
            { null, null },
            { "", null },
            { "    ", null },
            { "Char", 'C' },
            { "c", 'c' },
        };
    }

    @DataProvider(name="booleans-provider")
    private Object[][] provideBooleans() {
        return new Object[][] {
            { null, null },
            { "", null },
            { "    ", null },
            { "Str", false },
            { "true", true },
            { "False", false },
            { "TRUE", true },
        };
    }

    @DataProvider(name="empty-mime-types-provider")
    private Object[][] provideEmptyMimeTypes() {
        return new Object[][] {
            { null   },
            { ""     },
            { "    " }
        };
    }

    @DataProvider(name="malformed-mime-types-provider")
    private Object[][] provideMalformedMimeTypes() {
        return new Object[][] {
            { "/"             },
            { "invalid.type"  },
            { "invalid.type/" },
            { "/invalid.type" },
            { "invalid type"  }
        };
    }

    @DataProvider(name="mime-types-provider")
    private Object[][] provideMimeTypes() {
        return new Object[][] {
            { "text/plain",         "text/plain"       },
            { "  text/csv",         "text/csv"         },
            { "application/json  ", "application/json" }
        };
    }

    @DataProvider(name="file-names-provider")
    private Object[][] provideFileNames() {
        return new Object[][] {
            { "/",                  ""         },
            { "file",               "file"     },
            { " file",              "file"     },
            { "file ",              "file"     },
            { "/file",              "file"     },
            { "/path/to/file/",     "file"     },
            { "/path/to/file.txt",  "file.txt" }
        };
    }

}
