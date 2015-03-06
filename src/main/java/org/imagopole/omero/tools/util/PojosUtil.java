/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.imagopole.omero.tools.api.csv.CsvAnnotationLine;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.impl.csv.SimpleAnnotationLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.AnnotationData;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

/**
 * Utility class for pojo entities handling.
 *
 * @author seb
 *
 */
public final class PojosUtil {

    /** Application logs. */
    private static final Logger LOG = LoggerFactory.getLogger(PojosUtil.class);

    /**
     * Private constructor.
     */
    private PojosUtil() {
        super();
    }

    public static Multimap<String, PojoData> includeFromKeysWhitelist(
            Multimap<String, PojoData> records,
            Collection<String> keysWhitelist) {

        Check.notNull(records, "records");
        Check.notNull(keysWhitelist, "keysWhitelist");

        Multimap<String, PojoData> result =
            Multimaps.filterKeys(records, Predicates.in(keysWhitelist));

        LOG.trace("records: {} - {}", records.size(), records);

        return result;
    }

    /**
     * Indexes a collection of OMERO Pojo entities by name.
     *
     * Note: name duplication is possible for some pojo types (eg. datasets), hence
     * the returned <code>Multimap</code> implementation allows duplicate values
     * for a given key (otherwise, a BiMap would be fine).
     *
     * @param pojos the pojos to index
     * @return the indexed pojos
     */
    public static Multimap<String, PojoData> indexByName(Collection<PojoData> pojos) {
        Multimap<String, PojoData> result = ArrayListMultimap.create();

        if (null != pojos) {

            result = Multimaps.index(pojos, FunctionsUtil.toPojoName);

        }

        return result;
    }

    public static BiMap<Long, PojoData> indexById(Collection<PojoData> pojos) {
        BiMap<Long, PojoData> result = HashBiMap.create();

        if (null != pojos) {

            for (PojoData pojo : pojos) {
                Long key = pojo.getId();
                result.put(key, pojo);
            }

        }

        return result;
    }

    /**
     * Builds a list of pojos by ordered by name value using the specified comparator.
     *
     * @param pojos the pojos to be mapped to CSV lines
     * @return list of pojos as CSV lines, or an empty list
     */
    public static List<PojoData> toOrderedPojoNames(
            Collection<PojoData> pojos,
            Comparator<String> nameComparator) {

        Check.notNull(nameComparator, "nameComparator");

        List<PojoData> result = new ArrayList<PojoData>();

        if (null != pojos) {

            result = Lists.newArrayList(pojos);
            Collections.sort(result, Ordering.from(nameComparator).onResultOf(FunctionsUtil.toPojoName));

        }

        return result;
    }

    /**
     * Builds a list of CSV lines from a list pojos ordered using using the specified comparator.
     *
     * @param pojos the pojos to be mapped to CSV lines
     * @return list of pojos as CSV lines, or an empty list
     */
    public static List<CsvAnnotationLine> toSortedCsvAnnotationLines(
            Collection<PojoData> pojos,
            Comparator<String> nameComparator) {

        Check.notNull(nameComparator, "nameComparator");

        List<CsvAnnotationLine> result = new ArrayList<CsvAnnotationLine>();

        if (null != pojos) {

            // sort the "outer" (main) list on entity name
            List<PojoData> orderedPojos = toOrderedPojoNames(pojos, nameComparator);

            // pseudo line number based on list index from the re-ordered input pojos
            long lineNumber = 1L;
            for (PojoData pojo : orderedPojos) {

                String targetName = pojo.getName();
                Collection<AnnotationData> annotations = pojo.getAnnotations();

                // sort the "inner" (annotations) list on annotation name/value
                List<String> annotationsValues =
                    AnnotationsUtil.toOrderedAnnotationValues(annotations, nameComparator);

                CsvAnnotationLine line =
                    SimpleAnnotationLine.createLenient(lineNumber, targetName, annotationsValues);

                result.add(line);
                lineNumber++;

            }

        }

        return result;

    }

}
