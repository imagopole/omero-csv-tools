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

    /** Argument to the first column {@link java.util.Formatter} pattern. */
    private String leadingColumnArg;

    /** Argument to the remaining columns header {@link java.util.Formatter} pattern. */
    private String standardColumnArg;

    private DefaultCsvHeader(String leadingColumnArg, String standardColumnArg) {
        super();
        this.leadingColumnArg = leadingColumnArg;
        this.standardColumnArg = standardColumnArg;
    }

    /**
     * Static factory method with default columns arguments.
     *
     * @return the default CSV header formatter
     */
    public static CsvHeader create() {
        return new DefaultCsvHeader(
                Comments.DEFAULT_ENTITY_COLUMN_ARG,
                Comments.DEFAULT_ANNOTATION_COLUMN_ARG);
    }

    /**
     * Static factory method with custom columns arguments.
     *
     * @param leadingColumnArg argument to the first column format pattern
     * @param standardColumnArg argument to the remaining columns format pattern
     * @return the CSV header formatter
     */
    public static CsvHeader create(String leadingColumnArg, String standardColumnArg) {
        Check.notEmpty(leadingColumnArg, "leadingColumnArg");
        Check.notEmpty(standardColumnArg, "standardColumnArg");

        return new DefaultCsvHeader(leadingColumnArg, standardColumnArg);
    }

    /**
     * Formats and constant definitions for CSV headers generation.
     */
    private static final class Comments {

        /** Description for the CSV layout. */
        private static final String HEADER_DESCRIPTION =
           "Schema description: <object_name><separator><list_of_annotations>";

        /** {@link java.util.Formatter} template for remaining header columns: <code>$annotation_target name</code>. */
        private static final String ENTITY_COLUMN_FORMAT = "%s name";

       /** {@link java.util.Formatter} template for remaining header columns: <code>Annotation $annotation_number</code>.*/
        private static final String ANNOTATION_COLUMN_FORMAT = "%s %s";

        /** First column default unique argument. */
        private static final String DEFAULT_ENTITY_COLUMN_ARG = "Entity";

        /** Remaining columns default first argument. */
        private static final String DEFAULT_ANNOTATION_COLUMN_ARG = "Annotation";

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
        result.add(String.format(Comments.ENTITY_COLUMN_FORMAT, getLeadingColumnArg()));

        // one column per annotation value
        for (int i = 0; i < maxColumns; ++i) {
            // add value column number with humanized one-based index
            result.add(String.format(
                Comments.ANNOTATION_COLUMN_FORMAT, getStandardColumnArg(), i + 1));
        }

        return result;
    }

    /**
     * Returns leadingColumnPattern.
     * @return the leadingColumnPattern
     */
    public String getLeadingColumnArg() {
        return leadingColumnArg;
    }

    /**
     * Returns standardColumnPattern.
     * @return the standardColumnPattern
     */
    public String getStandardColumnArg() {
        return standardColumnArg;
    }

}
