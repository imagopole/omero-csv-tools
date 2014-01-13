/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Guava multimaps handling.
 *
 * @author seb
 *
 */
public final class MultimapsUtil {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(MultimapsUtil.class);

    /**
     * Private constructor.
     */
    private MultimapsUtil() {
        super();
    }

    public static Multimap<String, String> invertKeyValues(Multimap<String, String> multimap) {
        Check.notNull(multimap, "multimap");

        Multimap<String, String> result = HashMultimap.create();

        Multimaps.invertFrom(multimap, result);

        return result;
    }

    public static Multimap<String, String> includeFromKeysWhitelist(
            Multimap<String, String> records,
            Collection<String> keysWhitelist) {

        Check.notNull(records, "records");
        Check.notNull(keysWhitelist, "keysWhitelist");

        Multimap<String, String> result =
            Multimaps.filterKeys(records, Predicates.in(keysWhitelist));

        return result;
    }

    public static Multimap<String,String> forMap(Map<String, Collection<String>> map) {
        Check.notNull(map, "map");

        Multimap<String, String> result = HashMultimap.create();

        for (String key : map.keySet()) {
            result.putAll(key, map.get(key));
        }

        return result;
    }

    public static Collection<List<String>> pairAndFlatten(Multimap<String, String> multimap) {
        Check.notNull(multimap, "multimap");

        // for each key, create a new pairs based on the key + each value for that key
        // (kind of a narrower cartesian product of the multimap's keys and its values)
        Multimap<String, List<String>> listsOfPairsIndexedByTag =
            Multimaps.transformEntries(multimap, FunctionsUtil.asTuplesMultimap);

        // TODO: these tuples really should be proper DTOs
        LOG.trace("listsOfPairsIndexedByTag: {} - {}",
                   listsOfPairsIndexedByTag.size(), listsOfPairsIndexedByTag);

        // the final result is a list of Tuples (or DTOs) - ie. the flat multimap's values
        Collection<List<String>> result = listsOfPairsIndexedByTag.values();
        LOG.trace("result: {} - {}", result.size(), result);

        return result;
    }

}
