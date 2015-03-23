/**
 *
 */
package org.imagopole.omero.tools.impl.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.imagopole.omero.tools.api.csv.CsvHeader;
import org.imagopole.omero.tools.util.Check;

/**
 * Default <code>CsvHeaderFormat</code> implementation.
 *
 * Produces comments as:
 * {@code}
 * <pre>
 * Generated on <date>
 * Schema description: <object_name><separator><list_of_annotations>
 * </pre>
 * {@code}
 *
 * Produces header row as:
 * {@code}
 * <pre>
 * Entity name<separator>Annotation 1<separator>Annotation n
 * </pre>
 * {@code}
 *
 * @author seb
 *
 */
public class DefaultCsvHeader implements CsvHeader {

    /** Private constructor. */
    private DefaultCsvHeader() {
        super();
    }

    /**
     * Static factory method.
     *
     * @return the default CSV header formatter
     */
    public static CsvHeader create() {
        return new DefaultCsvHeader();
    }

    /**
     * Formats and constant definitions for CSV headers generation.
     */
    private static final class Comments {

        /** Description for the CSV layout. */
        private static final String HEADER_DESCRIPTION =
           "Schema description: <object_name><separator><list_of_annotations>";

       /** First column header (annotation target) */
        private static final String TARGET_ENTITY_NAME = "Entity name";

       /** {@link java.util.Formatter} template for remaining header columns: <code>Annotation $annotation_number</code>.*/
        private static final String ANNOTATION_COLUMN_FORMAT = "Annotation %s";

       /** {@link java.util.Formatter} template for header comments: <code>Generated on $date</code>.*/
        private static final String GENERATED_ON_FORMAT = "Generated on %s";

       /** Constants class. */
       private Comments() {
            super();
       }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getComments() {
        String timestampComment = String.format(Comments.GENERATED_ON_FORMAT, new Date());
        String schemaDescription = Comments.HEADER_DESCRIPTION;

        return Arrays.asList(timestampComment, schemaDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getRecord(int maxColumns) {
        Check.positive(maxColumns, "maxColumns");

        List<String> result = new ArrayList<String>(maxColumns + 1);

        // leading column (annotation target)
        result.add(Comments.TARGET_ENTITY_NAME);

        // one column per annotation value
        for (int i = 0; i < maxColumns; ++i) {
            // add value column number with humanized one-based index
            result.add(String.format(Comments.ANNOTATION_COLUMN_FORMAT, i + 1));
        }

        return result;
    }

}
