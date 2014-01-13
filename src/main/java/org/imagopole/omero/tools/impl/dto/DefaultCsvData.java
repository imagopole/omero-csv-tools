/**
 *
 */
package org.imagopole.omero.tools.impl.dto;

import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.util.Check;

/**
 * Internal data type for CSV content representation.
 *
 * @author seb
 *
 */
public class DefaultCsvData implements CsvData {

    /**
     * The decoded CSV file content.
     */
    private String fileContent;
    //private AuditContext auditContext;

    /**
     * Private constructor.
     */
    private DefaultCsvData(String fileContent) {
        super();
        this.fileContent = fileContent;
    }

    /**
     * Static factory method.
     *
     * @param fileContent the CSV file content
     * @return the decoded CSV content
     */
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
