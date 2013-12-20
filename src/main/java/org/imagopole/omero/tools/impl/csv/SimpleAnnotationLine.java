/**
 *
 */
package org.imagopole.omero.tools.impl.csv;

import java.util.Collection;

import com.google.common.collect.Iterables;

import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.util.Check;

/**
 * @author seb
 *
 */
public class SimpleAnnotationLine implements CsvAnnotationLine {

    private Long lineNumber;
    private String targetName;
    private Collection<String> annotationsValues;

    /**
     * @param targetName
     * @param annotationsValues
     */
    private SimpleAnnotationLine(
                    Long lineNumber,
                    String targetName,
                    Collection<String> annotationsValues) {
        super();

        this.lineNumber = lineNumber;
        this.targetName = targetName;
        this.annotationsValues = annotationsValues;
    }

    public static CsvAnnotationLine create(
                Long lineNumber,
                String targetName,
                Collection<String> annotationsValues) {

        Check.strictlyPositive(lineNumber, "lineNumber");
        Check.notEmpty(targetName, String.format("targetName at line %d", lineNumber));
        Check.notEmpty(annotationsValues,
                       String.format("annotationsValues at line %d for %s", lineNumber, targetName));

        return new SimpleAnnotationLine(lineNumber, targetName, annotationsValues);
    }

    @Override
    public Long getNumber() {
        return this.lineNumber;
    }

    @Override
    public String getValueAt(int col) {
        return Iterables.get(this.annotationsValues, col);
    }


    @Override
    public int getSize() {
        // add offset for the leading target name (first column)
        return getAnnotationsSize() + 1;
    }

    @Override
    public int getAnnotationsSize() {
        return getAnnotationsValues().size();
    }

    @Override
    public String getAnnotatedName() {
        return this.targetName;
    }

    @Override
    public Collection<String> getAnnotationsValues() {
        return this.annotationsValues;
    }

}
