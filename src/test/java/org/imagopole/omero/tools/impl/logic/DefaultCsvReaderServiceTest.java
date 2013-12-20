package org.imagopole.omero.tools.impl.logic;

import org.imagopole.omero.tools.TestsUtil;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.TestedObject;

public class DefaultCsvReaderServiceTest extends UnitilsTestNG {

    @TestedObject
    private DefaultCsvReaderService csvService;

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void readUniqueRecordsShouldRejectNullContent() {
        csvService.readUniqueRecords(null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void readUniqueRecordsShouldRejectEmptyContent() {
        csvService.readUniqueRecords("    ");
    }

}
