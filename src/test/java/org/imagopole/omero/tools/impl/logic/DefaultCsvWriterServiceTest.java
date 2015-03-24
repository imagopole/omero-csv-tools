package org.imagopole.omero.tools.impl.logic;

import static org.imagopole.omero.tools.TestsUtil.newDatasetPojo;

import java.util.ArrayList;

import org.imagopole.omero.tools.TestsUtil;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.TestedObject;

import com.google.common.collect.Lists;

public class DefaultCsvWriterServiceTest extends UnitilsTestNG {

    @TestedObject
    private DefaultCsvWriterService csvWriterService;

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeLinesShouldRejectNullAnnotatedTypeParam() {
          csvWriterService.writeLines(AnnotationType.tag, null, Lists.newArrayList(newDatasetPojo("some.pojo")));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeLinesShouldRejectNullAnnotationTypeParam() {
        csvWriterService.writeLines(null, AnnotatedType.dataset, Lists.newArrayList(newDatasetPojo("some.pojo")));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeLinesShouldRejectNullPojosParam() {
        csvWriterService.writeLines(AnnotationType.tag, AnnotatedType.dataset, null);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          expectedExceptionsMessageRegExp = TestsUtil.PRECONDITION_FAILED_REGEX)
    public void writeLinesShouldRejectEmptyPojosParam() {
        csvWriterService.writeLines(AnnotationType.tag, AnnotatedType.dataset, new ArrayList<PojoData>());
    }

}
