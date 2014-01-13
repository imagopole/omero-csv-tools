/**
 *
 */
package org.imagopole.omero.tools.api.csv;

import java.util.Collection;

/**
 * Represents an annotation "request" from a CSV definition.
 *
 * Implementors must ensure that null values are not allowed for
 * gettable attributes (ie. annotatedName and annotationValues).
 *
 * Format:
 * <pre>
 * {@code
 * <object_name><separator><list_of_annotations>
 * }
 * </pre>
 *
 * Eg.
 * <pre>
 * {@code
 * dataset_name , tag-0 , tag-1
 * dataset_name , "comment abc" , "comment xyz"
 * }
 * </pre>
 *
 * @author seb
 *
 */
public interface CsvAnnotationLine extends CsvLine {

    /**
     * "Annotated" name (ie. the annotation target).
     *
     * Typically a container name (eg. dataset name, image name).
     *
     * Equivalent to getValueAt(0) */
    String getAnnotatedName();

    /** Annotations to be applied to the "targetName". */
    Collection<String> getAnnotationsValues();

    /**
     * The number of annotations.
     *
     *  Equivalent to getSize() - 1 */
    int getAnnotationsSize();

}
