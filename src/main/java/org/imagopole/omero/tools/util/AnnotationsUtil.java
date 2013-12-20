/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.AnnotationData;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;

/**
 * @author seb
 *
 */
public final class AnnotationsUtil {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationsUtil.class);

    /**
     * Private constructor.
     */
    private AnnotationsUtil() {
        super();
    }

    public static Multimap<String, FileAnnotationData> indexByName(
                    Collection<FileAnnotationData> fileAnnotations) {

        Multimap<String, FileAnnotationData> result = ArrayListMultimap.create();

        if (null != fileAnnotations) {

           result = Multimaps.index(fileAnnotations, FunctionsUtil.toAnnotationFileName);

        }

        LOG.debug("fileAnnotationsIndexedByName: {} - {}", result.size(), result);

        return result;
    }

    public static Multimap<Long, TagAnnotationData> fromTagsMap(
                    Map<Long, Collection<TagAnnotationData>> map) {
        Check.notNull(map, "map");

        Multimap<Long, TagAnnotationData> result = HashMultimap.create();

        for (Long key : map.keySet()) {
            result.putAll(key, map.get(key));
        }

        return result;
    }

    /**
     * Indexes a collection of annotations by string value.
     *
     * Note: no duplicates allowed!
     *
     * @param annotations
     * @return
     */
    public static Map<String, AnnotationData> indexByAnnotationValue(
            Collection<? extends AnnotationData> annotations) {

        BiMap<String, AnnotationData> result = HashBiMap.create();

        if (null != annotations) {

            for (AnnotationData annot : annotations) {
                String key = annot.getContentAsString();

                if (result.containsKey(key)) {
                    //throw new IllegalStateException("Duplicate annotation value: " + key);
                    LOG.warn("WARNING - Duplicate annotation value: {} - {}", key, annot);
                } else {
                    result.put(key, annot);
                }
            }

        }

        return result;
    }

    /**
     * Indexes annotation records converted from the CSV file by row.
     *
     * The returned multimap uses: key=annotated_name, values=[annotations_names]
     *
     * This allows to merge rows sharing the same key, and for all column values
     * to be unique for a given line.
     *
     * @param records the CsvAnnotationLines parsed from file
     * @return the annotation values keyed by annotated name (eg. dataset name)
     */
    public static Multimap<String, String> indexByRow(Collection<CsvAnnotationLine> records) {

        Multimap<String, String> result = HashMultimap.create();

        if (null != records) {

            for (CsvAnnotationLine line : records) {
                String rowKey = line.getAnnotatedName();
                Collection<String> columnValues = line.getAnnotationsValues();

                if (null != rowKey && null != columnValues) {
                    result.putAll(rowKey, columnValues);
                } else {
                    LOG.warn("WARNING - Null annotated name or values : {} - {}",
                             rowKey, columnValues);
                }
            }

        }

        return Multimaps.unmodifiableMultimap(result);
    }

}
