/**
 *
 */
package org.imagopole.omero.tools.impl.dto;

import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.util.Check;

/**
 * @author seb
 *
 */
public class DefaultCsvData implements CsvData {

    private String fileContent;
    //private AuditContext auditContext;

    /**
     * Private constructor.
     */
    private DefaultCsvData(String fileContent) {
        super();
        this.fileContent = fileContent;
    }

    public static CsvData forContent(String fileContent) {
        Check.notEmpty(fileContent, "fileContent");

        return new DefaultCsvData(fileContent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileContent() {
        return fileContent;
    }

}
