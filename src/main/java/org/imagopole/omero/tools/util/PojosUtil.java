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

import org.imagopole.omero.tools.api.dto.PojoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author seb
 *
 */
public final class PojosUtil {

    /** Application logs */
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
     * the returned <code>Multimap<code> implementation allows duplicate values
     * for a given key (otherwise, a BiMap would be fine).
     *
     * @param pojos
     * @return
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

}
