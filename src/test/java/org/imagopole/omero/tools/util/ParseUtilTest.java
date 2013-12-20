package org.imagopole.omero.tools.util;

import static org.testng.Assert.assertEquals;

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
}
