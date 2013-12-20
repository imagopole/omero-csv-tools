/**
 *
 */
package org.imagopole.omero.tools.util;

import java.util.Collection;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.DatasetData;

/**
 * @author seb
 *
 */
public final class DatasetsUtil {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetsUtil.class);

    /**
     * Private constructor.
     */
    private DatasetsUtil() {
        super();
    }

    public static Multimap<String, DatasetData> includeFromKeysWhitelist(
            Multimap<String, DatasetData> records,
            Collection<String> keysWhitelist) {

        Check.notNull(records, "records");
        Check.notNull(keysWhitelist, "keysWhitelist");

        Multimap<String, DatasetData> result =
            Multimaps.filterKeys(records, Predicates.in(keysWhitelist));

        LOG.trace("records: {} - {}", records.size(), records);

        return result;
    }

    /**
     * Indexes a collection of datasets by name.
     *
     * Note: name duplication is possible for datasets, hence the returned <code>Multimap<code>
     * implementation allows duplicate values for a given key (otherwise, a BiMap would be fine).
     *
     * @param datasets
     * @return
     */
    public static Multimap<String, DatasetData> indexByName(Collection<DatasetData> datasets) {
        Multimap<String, DatasetData> result = ArrayListMultimap.create();

        if (null != datasets) {

            result = Multimaps.index(datasets, FunctionsUtil.toDatasetName);

        }

        return result;
    }

    public static BiMap<Long, DatasetData> indexById(Collection<DatasetData> datasets) {
        BiMap<Long, DatasetData> result = HashBiMap.create();

        if (null != datasets) {

            for (DatasetData dataset : datasets) {
                Long key = dataset.getId();
                result.put(key, dataset);
            }

        }

        return result;
    }

}
