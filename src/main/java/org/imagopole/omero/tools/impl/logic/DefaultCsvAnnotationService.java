/**
 *
 */
package org.imagopole.omero.tools.impl.logic;


import static org.imagopole.omero.tools.util.AnnotationsUtil.fromTagsMap;
import static org.imagopole.omero.tools.util.AnnotationsUtil.indexByAnnotationValue;
import static org.imagopole.omero.tools.util.MultimapsUtil.includeFromKeysWhitelist;
import static org.imagopole.omero.tools.util.MultimapsUtil.invertKeyValues;
import static org.imagopole.omero.tools.util.MultimapsUtil.pairAndFlatten;
import static org.imagopole.omero.tools.util.PojosUtil.indexById;
import static org.imagopole.omero.tools.util.PojosUtil.indexByName;
import static pojos.DataObject.asPojos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.imagopole.omero.tools.api.blitz.AnnotationLinker;
import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.dto.LinksData;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.api.logic.CsvAnnotationService;
import org.imagopole.omero.tools.impl.blitz.AnnotationLinkers;
import org.imagopole.omero.tools.impl.dto.AnnotationLinksData;
import org.imagopole.omero.tools.util.AnnotationsUtil;
import org.imagopole.omero.tools.util.Check;
import org.imagopole.omero.tools.util.DatasetsUtil;
import org.imagopole.omero.tools.util.FunctionsUtil;
import org.imagopole.omero.tools.util.ImagesUtil;
import org.imagopole.omero.tools.util.MultimapsUtil;
import org.imagopole.omero.tools.util.PojosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.TagAnnotationData;

/**
 * Service layer to the annotations processing application logic.
 *
 * @author seb
 *
 */
public class DefaultCsvAnnotationService implements CsvAnnotationService {

    /**
     * Size constraint for the tag name x pojo name association "tuple" (represented as a list).
     * First element is the tag name, second element is the pojo (dataset/image) name. */
    private static final int ANNOTATION_POJO_TUPLE_SIZE = 2;

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DefaultCsvAnnotationService.class);

    /** Local Blitz client for container handling */
    private OmeroContainerService containerService;

    /** Local Blitz client for AnnotationData handling */
    private OmeroAnnotationService annotationService;

    /** Local Blitz client for persistence handling */
    private OmeroUpdateService updateService;

    /**
     * Vanilla constructor
     */
    public DefaultCsvAnnotationService() {
        super();
    }

    /**
     * Create tags based on the CSV file content and build a list of tagged OMERO <code>Dataset</code>
     * link entities for update.
     *
     * Currently tagging is hierarchical - ie. tags are applied to datasets *within* the
     * specified project. In the future users might wish to tag across all their datasets, group-wide?
     *
     * Main processing logic:
     * <pre>
     * {@code
     *
     *   CSV lines                                           (0) load database state
     *   in the form of:                                         for experimenter's current
     *   key=dataset_name , values=[tags_names]                  datasets and tags (*)
     *                         |                                         |
     *                          ------------------ ----------------------
     *                                            |
     *                                            v
     *                        convert datasets to uniform internal datatype
     *                                            |
     *                                            v
     *                   run main processing logic (common to datasets and images)
     *                                            |
     *                                            v
     *                              (persistent tag + dataset entities)
     *                     return link objects for OMERO data model persistence
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

        // -------- step (0)
        // -- load current state from database for experimenter:
        //    - (i) all of his/her datasets -> index by name
        //    - (ii) all of his/her tags -> index by value

        // -- (i) lookup db state and index for processing: known datasets *within* the given project
        Collection<DatasetData> experimenterDatasets =
            getContainerService().listDatasetsByExperimenterAndProject(experimenterId, projectId);

        // -- (ii) lookup db state and index for processing: known tags owned by experimenter
        Collection<TagAnnotationData> experimenterTags =
            getAnnotationService().listTagsByExperimenter(experimenterId);

        // -- wrap omero model entities into an internal representation for processing
        // currently only containers (datasets/images) are converted from omero pojos to internal pojos
        // (omero tag pojos are handled as is)
        Collection<PojoData> experimenterPojos = DatasetsUtil.toPojos(experimenterDatasets);

        // -- common processing logic for datasets/images/pojos tagging
        return saveTagsAndLinkPojos(experimenterId,
                                    experimenterPojos,
                                    experimenterTags,
                                    uniqueLines,
                                    AnnotatedType.dataset);
    }

    /**
     * Create tags based on the CSV file content and build a list of tagged OMERO <code>Image</code>
     * link entities for update.
     *
     * Currently tagging is hierarchical - ie. tags are applied to images *within* the
     * specified dataset. In the future users might wish to tag across all their datasets,
     * project-wide or group-wide?
     *
     * Main processing logic:
     * <pre>
     * {@code
     *
     *   CSV lines                                           (0) load database state
     *   in the form of:                                         for experimenter's current
     *   key=image_name , values=[tags_names]                    images and tags (*)
     *                         |                                         |
     *                          ------------------ ----------------------
     *                                            |
     *                                            v
     *                        convert images to uniform internal datatype
     *                                            |
     *                                            v
     *                   run main processing logic (common to images and datasets)
     *                                            |
     *                                            v
     *                              (persistent tag + image entities)
     *                     return link objects for OMERO data model persistence
     *
     * (*) Requires server round-trip / db query
     *
     * }
     * </pre>
     *
     * @param experimenterId the experimenter
     * @param datasetId the dataset to which images must belong
     * @param uniqueLines the CSV line data in multivalue map format with key=image name and
     *        values = list of tags names. Both images names and tags names are expected to
     *        be unique by this implementation (no exception thrown if not - undefined behaviour).
     * @return the model entities to be persisted, split into two sublists: newly created (persistent)
     *         tags and known (persistent) tags. Both are linked to persistent images.
     * @throws ServerError OMERO client or server failure
     */
    @Override
    public LinksData saveTagsAndLinkNestedImages(
            Long experimenterId,
            Long datasetId,
            Multimap<String, String> uniqueLines) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(datasetId, "datasetId");
        Check.notNull(uniqueLines, "uniqueLines");
        Check.notEmpty(uniqueLines.asMap(), "uniqueLines");

        // -------- step (0)
        // -- load current state from database for experimenter:
        //    - (i) all of his/her images -> index by name
        //    - (ii) all of his/her tags -> index by value

        // -- (i) lookup db state and index for processing: known images *within* the given dataset
        Collection<ImageData> experimenterImages =
            getContainerService().listImagesByExperimenterAndDataset(experimenterId, datasetId);

        // -- (ii) lookup db state and index for processing: known tags owned by experimenter
        Collection<TagAnnotationData> experimenterTags =
            getAnnotationService().listTagsByExperimenter(experimenterId);

        // -- wrap omero model entities into an internal representation for processing
        // currently only containers (datasets/images) are converted from omero pojos to internal pojos
        // (omero tag pojos are handled as is)
        Collection<PojoData> experimenterPojos = ImagesUtil.toPojos(experimenterImages);

        // -- common processing logic for datasets/images/pojos tagging
        return saveTagsAndLinkPojos(experimenterId,
                                    experimenterPojos,
                                    experimenterTags,
                                    uniqueLines,
                                    AnnotatedType.image);
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

    /**
     * Builds a list of OMERO model entities based on the CSV file content:
     *  - all pojos (<code>Dataset</code> or <code>Image</code>) are already persistent entities
     *  - <code>TagAnnotation</code> may be persistent already, or persisted here,
     *    depending on whether they are already present in db or new, respectively (in
     *    which case they are saved before linking).
     *
     * The implementation attempts to use in-memory processing over server round-trips, at the
     * expense of having to maintain indices for entities lookup by name/id (and quite a lot of
     * collections shuffling). TODO: try an alternate custom hql queries implementation?.
     *
     *
     * Main processing logic:
     * <pre>
     * {@code
     *
     * CSV lines (assumed deduplicated) in the form of key=pojo_name , values=[tags_names]
     *                         |
     *                         v
     *   (1) exclude lines with an invalid pojo_name (ie. "unknown"/missing in db) (*)
     *                         |
     *                         v
     *   (2) index the lines in the form of key=tag_name , values=[pojos_names]
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
     *           (persistent tag + pojo entities)
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
     * @param experimenterPojos database state for experimenter's current datasets/images pojos
     * @param experimenterTags database state for experimenter's current tags
     * @param uniqueLines the CSV line data in multivalue map format with key=pojo name and
     *        values = list of tags names. Both pojos names and tags names are expected to
     *        be unique by this implementation (no exception thrown if not - undefined behaviour).
     * @param annotatedType
     * @return the model entities to be persisted, split into two sublists: newly created (persistent)
     *         tags and known (persistent) tags. Both are linked to persistent pojos.
     * @throws ServerError OMERO client or server failure
     */
    private LinksData saveTagsAndLinkPojos(
                    Long experimenterId,
                    Collection<PojoData> experimenterPojos,
                    Collection<TagAnnotationData> experimenterTags,
                    Multimap<String, String> uniqueLines,
                    AnnotatedType annotatedType) throws ServerError {

        Check.notNull(experimenterId, "experimenterId");
        Check.notNull(annotatedType, "annotatedType");
        Check.notNull(uniqueLines, "uniqueLines");
        Check.notEmpty(uniqueLines.asMap(), "uniqueLines");
        Check.notNull(experimenterPojos, "experimenterPojos");
        Check.notNull(experimenterTags, "experimenterTags");

        // -------- step (1)
        // -- from the current state loaded from database for experimenter:
        //    - (i) all of his/her datasets/images/pojos -> index by name
        //    - (ii) all of his/her tags -> index by value
        Multimap<String, PojoData> experimenterPojosByName = indexByName(experimenterPojos);
        Map<String, AnnotationData> experimenterTagsByName = indexByAnnotationValue(experimenterTags);

        // -- filter csv content based on db state: only retain entries with a valid ("known")
        // image/dataset/pojo name
        Multimap<String, String> validLinesByPojoName =
            sieveUnknownPojos(uniqueLines, experimenterPojos);

        // -- none of the requested image/dataset/pojos name are present in the database - no further processing
        rejectIfValidPojoNamesEmpty(validLinesByPojoName);

        // -- replicate results of pojo names validation onto the db images index
        // to keep only a minimal (and consistent) index
        // ie. only retain in the db index the pojos which have actually been requested in the csv file
        Multimap<String, PojoData> requestedPojosByName =
            PojosUtil.includeFromKeysWhitelist(experimenterPojosByName, validLinesByPojoName.keySet());

        // -- pre-fetch additional database state for later processing at step (4):
        //    - (i) get the datasets/images/pojos identifiers requested in csv
        //    - (ii) retrieve the tags already linked to those entities

        // -- (i) extract the requested pojos IDs
        Collection<Long> requestedPojosIds =
            Collections2.transform(requestedPojosByName.values(), FunctionsUtil.toPojoId);
        log.debug("requestedPojosIds: {} - {}", requestedPojosIds.size(), requestedPojosIds);

        // -- (ii)  query database for existing tags attached to those images/datasets/pojos
        Map<Long, Collection<TagAnnotationData>> databasePojosTagsLinks =
            getAnnotationService().listTagsLinkedToContainers(experimenterId,
                                                              requestedPojosIds,
                                                              annotatedType.getModelClass());
        log.debug("databasePojosTagsLinks: {} - {}",
                  databasePojosTagsLinks.size(), databasePojosTagsLinks);


        // -------- step (2)
        // -- inverse the input lines key-value mapping:
        //    - key=tag_name
        //    - values=[pojo_names] (with only valid names)
        Multimap<String, String> linesIndexedByTagNames = invertKeyValues(validLinesByPojoName);


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
        // -- remove from the CSV entries those which would duplicate an exising tag-pojo association:
        // - from the database query of existing associations, find which of the "known" tags requested in the csv file
        // are already associated with an image/dataset/pojo requested in the file too
        // - then convert the resulting model objects to an indexed string representation which
        // may be compared with the csv lines (ie. in a key=tag_name, values=[pojo_names] format)
        Multimap<String, String> knownTagPojoLinesByTagName =
            convertCurrentPojoLinksToPseudoLines(
                requestedPojosByName, databasePojosTagsLinks, experimenterPojos);

        // -- compare both "lines" sets:
        //    - "known" tags requested in csv file only
        //    - "known" tags requested in csv file and present in db
        Map<String, Collection<String>> linesOnlyInCsv = linesForKnownTags.asMap();
        Map<String, Collection<String>> linesInCsvWithLinks = knownTagPojoLinesByTagName.asMap();

        MapDifference<String, Collection<String>> knownTagsLinesDiff =
            Maps.difference(linesOnlyInCsv, linesInCsvWithLinks);

        // -- filter csv content based on db state: only retain entries with a "known" tag name which
        // has not yet been linked to the requested image
        // ie. remove the already linked image-tag pairs from the CSV requests
        Multimap<String, String> knownTagsLinesForLinking =
            sieveAlreadyLinkedTagRequests(knownTagsLinesDiff);


        // -------- step (5)
        // -- update database with the "new" tag names (still unlinked) and retrieve their
        // newly created identifiers
        // then, build another name to identifier index, which will serve as the data source
        // when linking images to "newly created" tags
        Map<String, AnnotationData> newPersistedTagsByName = saveAndReturnTagObjects(newTagsNames);


        // -------- step (6)
        // -- flatten the filtered csv lines for all tag requests ("known" and "new")
        //  ie. associate each tag + image combination in a two-items list with:
        //  first element = tag_name, second element = image_name
        Collection<List<String>> knownTagsPairs = pairAndFlatten(knownTagsLinesForLinking);
        Collection<List<String>> newTagsPairs = pairAndFlatten(linesForNewTags);

        // -- prepare to associate tags for specific pojos implementations (dataset/images)
        AnnotationLinker annotationLinker = AnnotationLinkers.forAnnotatedType(annotatedType);

        // -- convert the flattened requests pairs to linked OMERO model entities
        //    - (i) for known tags: both dataset and tag are persistent entities
        //    - (ii) for new tags: persistent entities (persistent dataset and newly saved tag)

        // -- (i) create one annotation link per "known" tag name with:
        //    - parent = persistent image object
        //    - child = persistent tag object
        List<IObject> knownTagsAnnotationLinks =
            linkTagsToPojos(annotationLinker,
                            experimenterPojosByName, experimenterTagsByName, knownTagsPairs);

        // -- (ii) create one annotation link per "new" tag name with:
        //    - parent = persistent/valid image object
        //    - child = newly persisted tag object
       List<IObject> newTagsAnnotationLinks =
           linkTagsToPojos(annotationLinker,
                           experimenterPojosByName, newPersistedTagsByName, newTagsPairs);


       // -- combine all annotation lists into a single server request
       return AnnotationLinksData.forLinks(knownTagsAnnotationLinks, newTagsAnnotationLinks);
    }

    /**
     * Persists new tags and indexes the result by name.
     *
     * @param newTagsNames the tags names to create
     * @return the persisted tags, indexed by name
     * @throws ServerError OMERO client or server failure
     */
    @SuppressWarnings("unchecked")
    private Map<String, AnnotationData> saveAndReturnTagObjects(Set<String> newTagsNames) throws ServerError {
        // convert tag names to transient model entities
        List<IObject> newTagsObjects = AnnotationsUtil.toTagEntities(newTagsNames);

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

    /**
     * Ensures that CSV pojo names with no counterpart in the database get rejected.
     *
     * @param validLinesByPojoName CSV file lines matching the database content
     */
    private void rejectIfValidPojoNamesEmpty(Multimap<String, String> validLinesByPojoName) {
        if (null == validLinesByPojoName || validLinesByPojoName.isEmpty()) {
            throw new IllegalStateException(
                String.format("None of the requested CSV target names exist for experimenter"));
        }
    }

    private List<IObject> linkTagsToPojos(
            AnnotationLinker linker,
            Multimap<String, PojoData> experimenterPojosByName,
            Map<String, AnnotationData> experimenterTagsByName,
            Collection<List<String>> knownTagsPairs) {

        List<IObject> result = new ArrayList<IObject>(knownTagsPairs.size());

        for (Iterator<List<String>> i = knownTagsPairs.iterator(); i.hasNext();) {
            // we know we always have a 2 items list from the flattened map
            FluentIterable<String> tuples = FluentIterable.from(i.next());

            if (tuples.size() != ANNOTATION_POJO_TUPLE_SIZE) {
                throw new IllegalStateException(
                    String.format("Invalid tag x pojo pair detected: %d", tuples.size()));
            }

            String tagName = tuples.first().get();
            String pojoName = tuples.last().get();

            AnnotationData persistentTag =
                lookupPersistentTagByNameOrFail(tagName, experimenterTagsByName);

            PojoData persistentPojo =
                lookupPersistentPojoByNameOrFail(pojoName, experimenterPojosByName);

            IObject tagAnnotationLink = linker.link(persistentTag, persistentPojo.getModelObject());

            result.add(tagAnnotationLink);
        }

        log.debug("tagAnnotationLinksForKnownTags: {} - {}", result.size(), result);

        return result;
    }

    private Multimap<String, String> sieveAlreadyLinkedTagRequests(
            MapDifference<String, Collection<String>> linesDiff) {

        log.debug("linesDiff.equal?: {}", linesDiff.areEqual());
        log.trace("linesDiff.differences: {}", linesDiff.entriesDiffering());
        log.trace("linesDiff.left: {}", linesDiff.entriesOnlyOnLeft());
        log.trace("linesDiff.right: {}", linesDiff.entriesOnlyOnRight());
        log.trace("linesDiff.common: {}", linesDiff.entriesInCommon());

        Multimap<String, String> knownTagsForLinking = null;

        if (linesDiff.areEqual()) {
            // all requested tags have already been linked to the requested pojos
            // nothing to do
            log.info("All known tags already linked to requested pojos - no-op");
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
            log.debug("retaining leftSide: {}", retainedTags);

            // for entries which are present both in CSV and in the already linked associations:
            // further the filtering so as to exclude the entries already linked
            Map<String, ValueDifference<Collection<String>>> mapsDiff = linesDiff.entriesDiffering();
            log.debug("filtering mapsDiff: {}", mapsDiff);

            for (String tag : mapsDiff.keySet()) {
                ValueDifference<Collection<String>> differencesForTag = mapsDiff.get(tag);

                Collection<String> pojosOnlyInCsvForTag = differencesForTag.leftValue();
                Collection<String> pojosAlreadyLinkedForTag = differencesForTag.rightValue();

                Set<String> onlyInCsv = Sets.newHashSet(pojosOnlyInCsvForTag);
                Set<String> alreadyLinked = Sets.newHashSet(pojosAlreadyLinkedForTag);

                // keep the tags present in csv but not already linked in database
                Set<String> retainedPojosNames = Sets.difference(onlyInCsv, alreadyLinked);

                knownTagsForLinking.putAll(tag, retainedPojosNames);
            }

            log.info("Retained {} known tags not linked to requested pojos",
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
                String.format("Could not find *known* tag name from index: %s", tagName));
        }

        return tagObject;
    }

    private PojoData lookupPersistentPojoByNameOrFail(
            String pojoName,
            Multimap<String, PojoData> experimenterPojosByName) {

        PojoData pojo = null;

        // we need to account for cases where duplicate dataset/image/pojo names exist for an experimenter
        // in such a case, we have no way to find out what the tag requestor intended, so the
        // policy is to refuse to tag (possibly incorrectly) any homonyms
        // another, possibly less safe policy, would be to apply the tag to all homonyms
        // TODO: this behaviour is a candidate for user controlled configuration setting
        if (experimenterPojosByName.containsKey(pojoName)) {
             Collection<PojoData> pojos = experimenterPojosByName.get(pojoName);

             //reject homonyms
             if (null == pojos || pojos.size() > 1) {
                 throw new IllegalStateException(
                     String.format("Ambiguous pojo name with homonyms: %s", pojoName));
             }

             pojo = Iterables.getOnlyElement(pojos);

           } else {
               throw new IllegalStateException(
                     String.format("Could not find *known* pojo name from index: %s", pojoName));
           }

        return pojo;
    }

    private Multimap<String, String> convertCurrentPojoLinksToPseudoLines(
            Multimap<String, PojoData> requestedPojosByName,
            Map<Long, Collection<TagAnnotationData>> databasePojosTagsLinks,
            Collection<PojoData> experimenterPojos) throws ServerError {

        Check.notEmpty(requestedPojosByName.asMap(), "requestedPojosByName");
        Check.notEmpty(experimenterPojos, "experimenterPojos");

        // transform the tags-pojos pairs coming from the database into "lines"
        // for comparison with the csv file
        // ie. switch from the map of already linked datasets/images-tags with:
        // key=pojo_id and values=[tags]
        // to a multimap of character strings with:
        // key=tag and values=[pojo_names]:
        // <Long>, [TagAnnotationData] -> <String>, [TagAnnotationData] -> <String>, [String] -> invert
        log.trace("requestedPojosByName: {}",
                   requestedPojosByName.size(), requestedPojosByName);

        // convert the query result to a multimap for convenient manipulation
        Multimap<Long, TagAnnotationData> dbPojoTagsMultimap = fromTagsMap(databasePojosTagsLinks);

        // keep their name only: key=pojo_id, values=[attached_tag_names]
        Multimap<Long, String> dbPojoIdToTagNames =
            Multimaps.transformValues(dbPojoTagsMultimap, FunctionsUtil.toTagValue);

        // then, convert to name-names multimap: key=pojo_name, values=[attached_tag_names]
        Multimap<String, String> dbPojoNameToTagNames = HashMultimap.create();
        Map<Long, PojoData> availablePojosById = indexById(experimenterPojos);

        for (Long pojoId : dbPojoIdToTagNames.keySet()) {
            Collection<String> unchangedTagNames = dbPojoIdToTagNames.get(pojoId);

            if (availablePojosById.containsKey(pojoId)) {
                PojoData pojo = availablePojosById.get(pojoId);
                String pojoName = pojo.getName();

                dbPojoNameToTagNames.putAll(pojoName, unchangedTagNames);
            } else {
                log.error("ERROR - Unable to map pojo id {} to name - inconsistent state", pojoId);
            }
        }

        log.debug("dbPojoNameToTagNames: {} - {}",
                   dbPojoNameToTagNames.size(), dbPojoNameToTagNames);

        // provide the inverse mapping again to make sure both maps are comparable
        Multimap<String, String> dbTagNameToPojosNames = invertKeyValues(dbPojoNameToTagNames);

        log.debug("dbTagNameToPojosNames: {} - {}",
                  dbTagNameToPojosNames.size(), dbTagNameToPojosNames);

        return dbTagNameToPojosNames;
    }

    private Multimap<String, String> sieveUnknownPojos(
            Multimap<String, String> uniqueLines,
            Collection<PojoData> experimenterAnnotatedPojos) {

        // filter out CSV tagging requests for images/datasets/pojos which are
        // unknown to this experimenter (ie. missing from db query results)
        Collection<String> allDatabasePojosNames =
            Collections2.transform(experimenterAnnotatedPojos, FunctionsUtil.toPojoName);
        log.debug("experimenterAnnotatedPojos: {} - {}",
                  allDatabasePojosNames.size(), allDatabasePojosNames);

        // only retain CSV tagging requests for existing images/datasets/pojos
        Multimap<String, String> validCsvRecordsByPojoName =
            MultimapsUtil.includeFromKeysWhitelist(uniqueLines, allDatabasePojosNames);

        log.debug("validCsvRecordsByPojoName: {} - {}",
                  validCsvRecordsByPojoName.keys().size(), validCsvRecordsByPojoName);

        return validCsvRecordsByPojoName;
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
