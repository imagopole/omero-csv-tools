package org.imagopole.omero.tools.impl.logic;

import java.util.ArrayList;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.TestedObject;

public class DefaultCsvWriterServiceTest extends UnitilsTestNG {

    @TestedObject
    private DefaultCsvWriterService csvWriterService;

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeLinesShouldRejectNullParam() {
        csvWriterService.writeLines(null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeLinesShouldRejectEmptyParam() {
        csvWriterService.writeLines(new ArrayList<PojoData>());
    }

}
