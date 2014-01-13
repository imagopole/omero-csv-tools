/**
 *
 */
package org.imagopole.omero.tools.impl.csv;

import java.util.Collection;

import com.google.common.collect.Iterables;

import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.util.Check;

/**
 * Represents an annotation request from the CSV definition.
 *
 * @author seb
 *
 */
public class SimpleAnnotationLine implements CsvAnnotationLine {

    /** The line index in the file. */
    private Long lineNumber;

    /** Annotated" name (ie. the annotation target). */
    private String targetName;

    /** Annotations to be applied to the "targetName". */
    private Collection<String> annotationsValues;

    /**
     * Parameterized constructor.
     *
     * @param lineNumber the line number
     * @param targetName the annotation target
     * @param annotationsValues the annotation values
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

    /**
     * Static factory method.
     *
     * @param lineNumber the line number
     * @param targetName the annotation target
     * @param annotationsValues the annotation values
     * @return the CSV line
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getNumber() {
        return this.lineNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueAt(int col) {
        return Iterables.get(this.annotationsValues, col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        // add offset for the leading target name (first column)
        return getAnnotationsSize() + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAnnotationsSize() {
        return getAnnotationsValues().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnnotatedName() {
        return this.targetName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getAnnotationsValues() {
        return this.annotationsValues;
    }

}
