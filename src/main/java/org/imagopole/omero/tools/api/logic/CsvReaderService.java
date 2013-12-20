package org.imagopole.omero.tools.api.logic;

import com.google.common.collect.Multimap;

public interface CsvReaderService {

    Multimap<String, String> readUniqueRecords(String content);

}
