/**
 *
 */
package org.imagopole.omero.tools.impl.ctrl;

import java.util.Collection;

import org.imagopole.omero.tools.api.RtException;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.ctrl.CsvExportController;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.api.logic.CsvWriterService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher layer to the conversion/export related services.
 *
 * @author seb
 *
 */
public class DefaultCsvExportController implements CsvExportController {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultCsvExportController.class);

    /** CSV conversion service for CSV generation */
    private CsvWriterService csvWriterService;

    /**
     * Vanilla constructor
     */
    public DefaultCsvExportController() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convertToCsv(
            AnnotationType annotationType,
            AnnotatedType annotatedType,
            Collection<PojoData> pojos) throws RtException{

        Check.notNull(annotatedType, "annotatedType");
        Check.notNull(annotationType, "annotationType");
        Check.notEmpty(pojos, "pojos");

        log.debug("Writing {} pojos to CSV", pojos.size());

        return getCsvWriterService().writeLines(annotationType, annotatedType, pojos);
    }

    /**
     * Returns csvWriterService.
     * @return the csvWriterService
     */
    public CsvWriterService getCsvWriterService() {
        return csvWriterService;
    }

    /**
     * Sets csvWriterService.
     * @param csvWriterService the csvWriterService to set
     */
    public void setCsvWriterService(CsvWriterService csvWriterService) {
        this.csvWriterService = csvWriterService;
    }

}
