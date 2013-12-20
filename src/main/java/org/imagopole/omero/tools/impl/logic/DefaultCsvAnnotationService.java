/**
 *
 */
package org.imagopole.omero.tools.impl.logic;


import static org.imagopole.omero.tools.util.AnnotationsUtil.fromTagsMap;
import static org.imagopole.omero.tools.util.AnnotationsUtil.indexByAnnotationValue;
import static org.imagopole.omero.tools.util.DatasetsUtil.includeFromKeysWhitelist;
import static org.imagopole.omero.tools.util.DatasetsUtil.indexByName;
import static org.imagopole.omero.tools.util.MultimapsUtil.includeFromKeysWhitelist;
import static org.imagopole.omero.tools.util.MultimapsUtil.invertKeyValues;
import static org.imagopole.omero.tools.util.MultimapsUtil.pairAndFlatten;
import static pojos.DataObject.asPojos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import omero.ServerError;
import omero.model.IObject;

import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.api.dto.LinksData;
import org.imagopole.omero.tools.api.logic.CsvAnnotationService;
import org.imagopole.omero.tools.impl.dto.AnnotationLinksData;
import org.imagopole.omero.tools.util.BlitzUtil;
import org.imagopole.omero.tools.util.Check;
import org.imagopole.omero.tools.util.DatasetsUtil;
import org.imagopole.omero.tools.util.FunctionsUtil;
import org.imagopole.omero.tools.util.MultimapsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.TagAnnotationData;

/**
 * @author seb
 *
 */
public class DefaultCsvAnnotationService implements CsvAnnotationService {

    private static final int ANNOT_TUPLE_SIZE = 2;

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultCsvAnnotationService.class);

    private OmeroContainerService containerService;

    private OmeroAnnotationService annotationService;

    private OmeroUpdateService updateService;

    /**
     * Vanilla constructor
     */
    public DefaultCsvAnnotationService() {
        super();
    }

    /**
     * Builds a list of OMERO model entities based on the CSV file content:
     *  - all <code>Dataset<code> are already persistent entities
     *  - <code>TagAnnotation</code> may be persistent already, or persisted here,
     *    depending on whether they are already present in db or new, respectively (in
     *    which case they are saved before linking).
     *
     * The implementation attempts to use in-memory processing over server round-trips, at the
     * expense of having to maintain indices for entities lookup by name/id (and quite a lot of
     * collections shuffling). TODO: try an alternate custom hql queries implementation?.
     *
     * Currently tagging is hierarchical - ie. tags are applied to datasets *within* the
     * specified project. In the future users might wish to tag across all their datasets, group-wide?
     *
     * Main processing logic:
     * <pre>
     * {@code
     *
     * CSV lines (assumed deduplicated) in the form of key=dataset_name , values=[tags_names]
     *                         |
     *                         v
     *   (1) exclude lines with an invalid dataset_name (ie. "unknown"/missing in db) (*)
     *                         |
     *                         v
     *   (2) index the lines in the form of key=tag_name , values=[datasets_names]
     *                         |
     *                         v
     *   (3) split lines based on tags names
     *                         |
     *          --------------- -------------------
     *         |                                   |
     *         v                                   v
     *   lines with "known" tags name        lines with "new" tags name
     *   (ie. tags present in db)            (ie. tags "unknown"/missing in db)
     *         |                                   |
     *         v                                   v
     *   (4) exclude tag + dataset pairs      (5) create "new" tags in db (*)
     *   which are already linked in db (*)   (ie. assign IDs)
     *         |                                   |
     *          --------------- -------------------
     *                         |
     *           (persistent tag + dataset entities)
     *                         |
     *                         v
     *   (6) create link objects for OMERO data model persistence (*)
     *
     *
     * (*) Requires server round-trip / db query
     *
     * }
     * </pre>
     *
     * @param experimenterId the experimenter
     * @param projectId the project to which datasets must belong
     * @param uniqueLines the CSV line data in multivalue map format with key=dataset name and
     *        values = list of tags names. Both datasets names and tags names are expected to
     *        be unique by this implementation (no exception thrown if not - undefined behaviour).
     * @return the model entities to be persisted, split into two sublists: newly created (persistent)
     *         tags and known (persistent) tags. Both are linked to persistent datasets.
     * @throws ServerError OMERO client or server failure
     */
    @Override
    public LinksData saveTagsAndLinkNestedDatasets(
            Long experimenterId,
            Long projectId,
            Multimap<String, String> uniqueLines) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(projectId, "projectId");
        Check.notNull(uniqueLines, "uniqueLines");
        Check.notEmpty(uniqueLines.asMap(), "uniqueLines");

        // -------- step (1)
        // -- load current state from database for experimenter:
        //    - (i) all of his/her datasets -> index by name
        //    - (ii) all of his/her tags -> index by value

        // -- (i) lookup db state and index for processing: known datasets *within* the given project
        Collection<DatasetData> experimenterDatasets =
            getContainerService().listDatasetsByExperimenterAndProject(experimenterId, projectId);
        Multimap<String, DatasetData> experimenterDatasetsByName = indexByName(experimenterDatasets);

        // -- (ii) lookup db state and index for processing: known tags owned by experimenter
        Collection<TagAnnotationData> experimenterTags =
            getAnnotationService().listTagsByExperimenter(experimenterId);
        Map<String, AnnotationData> experimenterTagsByName = indexByAnnotationValue(experimenterTags);

        // -- filter csv content based on db state: only retain entries with a valid ("known")
        // dataset name
        Multimap<String, String> validLinesByDatasetName =
            sieveUnknownDatasets(uniqueLines, experimenterDatasets);

        // -- replicate results of datasets names validation onto the db datasets index
        // to keep only a minimal (and consistent) index
        // ie. only retain in the db index datasets which have actually been requested in the csv file
        Multimap<String, DatasetData> requestedDatasetsByName =
            includeFromKeysWhitelist(experimenterDatasetsByName, validLinesByDatasetName.keySet());


        // -------- step (2)
        // -- inverse the input lines key-value mapping:
        //    - key=tag_name
        //    - values=[dataset_names] (with only valid names)
        Multimap<String, String> linesIndexedByTagNames =
            invertKeyValues(validLinesByDatasetName);


        // -------- step (3)
        /// -- prepare tag names filters for input lines splitting:
        //    - (i) "known" (db) tag names
        //    - (ii) "new" tag names

        // -- (i) extract "known" tag names from db index
        Set<String> knownTagsNames = experimenterTagsByName.keySet();

        // -- (ii) extract "new" tag names from db index and input csv lines
        //    ie. new tags = (input tags - known tags)
        Set<String> newTagsNames = Sets.difference(linesIndexedByTagNames.keySet(), knownTagsNames);

        // -- separate csv lines into sub-requests:
        //    - (i) requests for "known" (db) tags -> extra filtering required (existing links check)
        //    - (ii) requests for "new" tags -> no special handling (may be created directly)

        // -- (i) lines with a "known" tag
        Multimap<String, String> linesForKnownTags =
            includeFromKeysWhitelist(linesIndexedByTagNames, knownTagsNames);

        // -- (ii) lines with a "new" tag
        Multimap<String, String> linesForNewTags =
            includeFromKeysWhitelist(linesIndexedByTagNames,newTagsNames);

        // -- sanity check for the splitting process: input and output lines must be of consistent sizes
        rejectIfSplittingInconsistent(linesIndexedByTagNames, linesForKnownTags, linesForNewTags);

        // -------- step (4)
        // -- query database to find which of the "known" tags requested in the csv file
        // are already associated with a dataset requested in the file too
        // then convert the resulting model objects to an indexed string representation which
        // may be compared with the csv lines (ie. in a key=tag_name, values=[dataset_names] format)
        Multimap<String, String> knownTagDatasetLinesByTagName =
            convertCurrentLinksToPseudoLines(
                experimenterId, requestedDatasetsByName, experimenterDatasets);

        // -- compare both "lines" sets:
        //    - "known" tags requested in csv file only
        //    - "known" tags requested in csv file and present in db
        Map<String, Collection<String>> linesOnlyInCsv = linesForKnownTags.asMap();
        Map<String, Collection<String>> linesInCsvWithLinks = knownTagDatasetLinesByTagName.asMap();

        MapDifference<String, Collection<String>> knownTagsLinesDiff =
            Maps.difference(linesOnlyInCsv, linesInCsvWithLinks);

        // -- filter csv content based on db state: only retain entries with a "known" tag name which
        // has not yet been linked to the requested dataset
        // ie. remove the already linked datasets-tag pairs from the CSV requests
        Multimap<String, String> knownTagsLinesForLinking =
            sieveAlreadyLinkedRequests(knownTagsLinesDiff);


        // -------- step (5)
        // -- update database with the "new" tag names (still unlinked) and retrieve their
        // newly created identifiers
        // then, build another name to identifier index, which will serve as the data source
        // when linking datasets to "newly created" tags
        Map<String, AnnotationData> newPersistedTagsByName = saveAndReturnTagObjects(newTagsNames);


        // -------- step (6)
        // -- flatten the filtered csv lines for all tag requests ("known" and "new")
        //  ie. associate each tag + dataset combination in a two-items list with:
        //  first element = tag_name, second element = dataset_name
        Collection<List<String>> knownTagsPairs = pairAndFlatten(knownTagsLinesForLinking);
        Collection<List<String>> newTagsPairs = pairAndFlatten(linesForNewTags);

        // -- convert the flattened requests pairs to linked OMERO model entities
        //    - (i) for known tags: both dataset and tag are persistent entities
        //    - (ii) for new tags: persistent entities (persistent dataset and newly saved tag)

        // -- (i) create one annotation link per "known" tag name with:
        //    - parent = persistent dataset object
        //    - child = persistent tag object
        List<IObject> knownTagsAnnotationLinks =
            linkTagsToDatasets(experimenterDatasetsByName, experimenterTagsByName, knownTagsPairs);

         // -- (ii) create one annotation link per "new" tag name with:
         //    - parent = persistent/valid dataset object
         //    - child = newly persisted tag object
        List<IObject> newTagsAnnotationLinks =
            linkTagsToDatasets(experimenterDatasetsByName, newPersistedTagsByName, newTagsPairs);

        // -- combine all annotation lists into a single server request
        return AnnotationLinksData.forLinks(knownTagsAnnotationLinks, newTagsAnnotationLinks);
    }

    /**
     * @param newTagsNames
     * @return
     * @throws ServerError
     */
    @SuppressWarnings("unchecked")
    private Map<String, AnnotationData> saveAndReturnTagObjects(Set<String> newTagsNames) throws ServerError {
        // convert tag names to transient model entities
        List<IObject> newTagsObjects = toTagEntities(newTagsNames);

        // batch save them
        Collection<IObject> newPersistedTagsObjects = getUpdateService().saveAll(newTagsObjects);

        // convert the newly created model objects to Pojos
        Collection<TagAnnotationData> newPersistedTags = asPojos(newPersistedTagsObjects);
        log.trace("Persisted new tags to db: {} - {} {}",
                  newPersistedTags.size(), newTagsObjects.size());

        // index the newly created Pojos by tag name
        Map<String, AnnotationData> newPersistedTagsByName = indexByAnnotationValue(newPersistedTags);

        // -- paranoid sanity check for the intermediate save process: input tags and saved tags
        // must be of consistent sizes (fails with the no-op update service)
        rejectIfPersistedSizesInconsistent(newPersistedTags.size(), newTagsObjects.size());

        return newPersistedTagsByName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IObject> saveAllAnnotationLinks(LinksData linksData) throws ServerError {
        Check.notNull(linksData, "linksData");

        List<IObject> iObjects = linksData.getAllAnnotationLinks();

        return getUpdateService().saveAll(iObjects);
    }

    private void rejectIfSplittingInconsistent(
            Multimap<String, String> linesIndexedByTagNames,
            Multimap<String, String> linesForKnownTags,
            Multimap<String, String> linesForNewTags) {

        int knowns = linesForKnownTags.size();
        int news = linesForNewTags.size();
        int expectedSize = linesIndexedByTagNames.size();

        if (knowns + news != expectedSize) {
            throw new IllegalStateException(
                String.format("Split failure lists - expected %d but got %d known + %d new",
                              expectedSize, knowns, news));
        }
    }

    private void rejectIfPersistedSizesInconsistent(int actualSize, int expectedSize) {
        if (actualSize != expectedSize) {
            throw new IllegalStateException(
                String.format("Persisted objects list size mismatch - expected %d but got %d",
                               expectedSize, actualSize));
        }
    }

    private List<IObject> toTagEntities(Set<String> newTagsNames) {
        List<IObject> result = new ArrayList<IObject>();

        if (null != newTagsNames) {

            for (String tagName : newTagsNames) {
                TagAnnotationData transientTag = new TagAnnotationData(tagName);
                result.add(transientTag.asIObject());
            }
        }

        return result;
    }

    private List<IObject> linkTagsToDatasets(
            Multimap<String, DatasetData> experimenterDatasetsByName,
            Map<String, AnnotationData> experimenterTagsByName,
            Collection<List<String>> knownTagsPairs) {

        List<IObject> result = new ArrayList<IObject>(knownTagsPairs.size());

        for (Iterator<List<String>> i = knownTagsPairs.iterator(); i.hasNext();) {
            // we know we always have a 2 items list from the flattened map
            FluentIterable<String> tuples = FluentIterable.from(i.next());

            if (tuples.size() != ANNOT_TUPLE_SIZE) {
                throw new IllegalStateException("Invalid tag x dataset pair detected");
            }

            String tagName = tuples.first().get();
            String datasetName = tuples.last().get();

            AnnotationData persistentTag =
                lookupPersistentTagByNameOrFail(tagName, experimenterTagsByName);

            DatasetData persistentDataset =
                lookupPersistentDatasetByNameOrFail(datasetName, experimenterDatasetsByName);

            IObject tagAnnotationLink = BlitzUtil.link(persistentTag, persistentDataset);

            result.add(tagAnnotationLink);
        }

        log.debug("tagAnnotationLinksForKnowntags: {} - {}", result.size(), result);

        return result;
    }

    private Multimap<String, String> sieveAlreadyLinkedRequests(
            MapDifference<String, Collection<String>> linesDiff) {

        log.debug("linesDiff.equal?: {}", linesDiff.areEqual());
        log.trace("linesDiff.differences: {}", linesDiff.entriesDiffering());
        log.trace("linesDiff.left: {}", linesDiff.entriesOnlyOnLeft());
        log.trace("linesDiff.right: {}", linesDiff.entriesOnlyOnRight());
        log.trace("linesDiff.common: {}", linesDiff.entriesInCommon());

        Multimap<String, String> knownTagsForLinking = null;

        if (linesDiff.areEqual()) {
            // all requested tags have already been linked to the requested datasets
            // nothing to do
            log.info("All known tags already linked to requested datasets - no-op");
            knownTagsForLinking = HashMultimap.create();
        } else {
            log.info("Lines differ: {} differences, {} onLeft, {} onRight, {} common",
                      linesDiff.entriesDiffering().size(),
                      linesDiff.entriesOnlyOnLeft().size(),
                      linesDiff.entriesOnlyOnRight().size(),
                      linesDiff.entriesInCommon().size());

            // always keep the tags not linked yet from the requested lines - ie. the "on left" ones
            // (the "only in CSV" lines)
            Map<String, Collection<String>> retainedTags = linesDiff.entriesOnlyOnLeft();

            knownTagsForLinking = MultimapsUtil.forMap(retainedTags);

            // for entries which appear both in CSV and in the linked ones, further the filtering
            // to exclude the entries already linked
            Map<String, ValueDifference<Collection<String>>> mapDifference =
                linesDiff.entriesDiffering();

            for (String tag : mapDifference.keySet()) {
                ValueDifference<Collection<String>> differencesForTag = mapDifference.get(tag);

                Collection<String> datasetsOnlyInCsvForTag = differencesForTag.leftValue();
                Collection<String> datasetsAlreadyLinkedForTag = differencesForTag.rightValue();

                Set<String> onlyInCsv = Sets.newHashSet(datasetsOnlyInCsvForTag);
                Set<String> alreadyLinked = Sets.newHashSet(datasetsAlreadyLinkedForTag);

                // keep the tags in csv but not already linked
                Set<String> keepDatasetsNames = Sets.difference(onlyInCsv, alreadyLinked);

                knownTagsForLinking.putAll(tag, keepDatasetsNames);
            }

            log.info("Retained {} known tags not linked to requested datasets",
                     knownTagsForLinking.size());
        }

        log.debug("knownTagsForLinking: {} - {}", knownTagsForLinking.size(), knownTagsForLinking);

        return knownTagsForLinking;
    }

    private AnnotationData lookupPersistentTagByNameOrFail(
            String tagName,
            Map<String, AnnotationData> experimenterTagsByName) {

        AnnotationData tagObject = null;

        if (experimenterTagsByName.containsKey(tagName)) {
               tagObject = experimenterTagsByName.get(tagName);
           } else {
               throw new IllegalStateException(
                   String.format("Could not find *known* tag name from index: ", tagName));
           }

        return tagObject;
    }

    private DatasetData lookupPersistentDatasetByNameOrFail(
            String datasetName,
            Multimap<String, DatasetData> experimenterDatasetsByName) {

        DatasetData datasetObject = null;

        // we need to account for cases where duplicate datasets names exist for an experimenter
        // in such a case, we have no way to find out what the tag requestor intended, so the
        // policy is to refuse to tag (possibly incorrectly) any homonyms
        // another, possibly less safe policy, would be to apply the tag to all homonyms
        if (experimenterDatasetsByName.containsKey(datasetName)) {
             Collection<DatasetData> datasets = experimenterDatasetsByName.get(datasetName);

             //reject homonyms
             if (null == datasets || datasets.size() > 1) {
                 throw new IllegalStateException(
                     String.format("Ambiguous dataset name with homonyms: ", datasetName));
             }

             datasetObject = Iterables.getOnlyElement(datasets);
           } else {
               throw new IllegalStateException(
                     String.format("Could not find *known* dataset name from index: ", datasetName));
           }

        return datasetObject;
    }

    private Multimap<String, String> convertCurrentLinksToPseudoLines(
            Long experimenterId,
            Multimap<String, DatasetData> requestedDatasetsByName,
            Collection<DatasetData> experimenterDatasets) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notEmpty(requestedDatasetsByName.asMap(), "requestedDatasetsByName");
        Check.notEmpty(experimenterDatasets, "experimenterDatasets");

        // transform the tags-datasets pairs coming from the database into "lines"
        // for comparison with the csv file
        // ie. switch from the map of already linked datasets/tags with:
        // key=dataset_id and values=[tags]
        // to a multimap of character strings with:
        // key=tag and values=[dataset_names]:
        // <Long>, [TagAnnotationData] -> <String>, [TagAnnotationData] -> <String>, [String] -> invert
        log.trace("requestedDatasetsByName: {}",
                   requestedDatasetsByName.size(), requestedDatasetsByName);

        // - extract the requested datasets IDs
        Collection<Long> requestedDatasetsIds =
            Collections2.transform(requestedDatasetsByName.values(), FunctionsUtil.toDatasetId);
        log.debug("requestedDatasetsIds: {} - {}", requestedDatasetsIds.size(), requestedDatasetsIds);

        // - then query for existing tags attached to those datasets
        Map<Long, Collection<TagAnnotationData>> databaseDatasetTagsMap =
            getAnnotationService().listTagsLinkedToDatasets(experimenterId, requestedDatasetsIds);
        log.debug("databaseDatasetTagsMap: {} - {}",
                  databaseDatasetTagsMap.size(), databaseDatasetTagsMap);

        // convert the query result to a multimap for convenient manipulation
        Multimap<Long, TagAnnotationData> dbDatasetTagsMultimap = fromTagsMap(databaseDatasetTagsMap);

        // keep their name only: key=dataset_id, values=[attached_tag_names]
        Multimap<Long, String> dbDatasetIdToTagNames =
            Multimaps.transformValues(dbDatasetTagsMultimap, FunctionsUtil.toTagValue);

        // then, convert to name-names multimap: key=dataset_name, values=[attached_tag_names]
        Multimap<String, String> dbDatasetNameToTagNames = HashMultimap.create();
        BiMap<Long, DatasetData> availableDatasetsById = DatasetsUtil.indexById(experimenterDatasets);

        for (Long datasetId : dbDatasetIdToTagNames.keySet()) {
            Collection<String> unchangedTagNames = dbDatasetIdToTagNames.get(datasetId);

            if (availableDatasetsById.containsKey(datasetId)) {
                DatasetData dataset = availableDatasetsById.get(datasetId);
                String datasetName = dataset.getName();

                dbDatasetNameToTagNames.putAll(datasetName, unchangedTagNames);
            } else {
                log.error("ERROR - Unable to map dataset id {} to name - inconsistent state",
                          datasetId);
            }
        }

        log.debug("dbDatasetNameToTagNames: {} - {}",
                   dbDatasetNameToTagNames.size(), dbDatasetNameToTagNames);

        // provide the inverse mapping again to make sure both maps are comparable
        Multimap<String, String> dbTagNameToDatasetsNames = invertKeyValues(dbDatasetNameToTagNames);

        log.debug("dbTagNameToDatasetsNames: {} - {}",
                   dbTagNameToDatasetsNames.size(), dbTagNameToDatasetsNames);

        return dbTagNameToDatasetsNames;
    }

    /**
     * @param uniqueLines
     * @param experimenterDatasets
     * @return
     */
    private Multimap<String, String> sieveUnknownDatasets(
            Multimap<String, String> uniqueLines,
            Collection<DatasetData> experimenterDatasets) {

        // filter out CSV tagging requests for datasets which are
        // unknown to this experimenter (ie. missing from db query results)
        Collection<String> allDatabaseDatasetsNames =
            Collections2.transform(experimenterDatasets, FunctionsUtil.toDatasetName);
        log.debug("allDatabaseDatasetsNames: {} - {}",
                   allDatabaseDatasetsNames.size(), allDatabaseDatasetsNames);

        // only retain CSV tagging requests for existing datasets
        Multimap<String, String> validCsvRecordsByContainerName =
            MultimapsUtil.includeFromKeysWhitelist(uniqueLines, allDatabaseDatasetsNames);

        log.debug("validCsvRecordsByContainerName: {} - {}",
                  validCsvRecordsByContainerName.keys().size(), validCsvRecordsByContainerName);

        return validCsvRecordsByContainerName;
    }

    /**
     * @return the containerService
     */
    public OmeroContainerService getContainerService() {
        return containerService;
    }

    /**
     * @param containerService the containerService to set
     */
    public void setContainerService(OmeroContainerService containerService) {
        this.containerService = containerService;
    }

    /**
     * @return the annotationService
     */
    public OmeroAnnotationService getAnnotationService() {
        return annotationService;
    }

    /**
     * @param annotationService the annotationService to set
     */
    public void setAnnotationService(OmeroAnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    /**
     * Returns updateService.
     * @return the updateService
     */
    public OmeroUpdateService getUpdateService() {
        return updateService;
    }

    /**
     * Sets updateService.
     * @param updateService the updateService to set
     */
    public void setUpdateService(OmeroUpdateService updateService) {
        this.updateService = updateService;
    }

}
