/**
 *
 */
package org.imagopole.omero.tools.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;

import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.api.blitz.OmeroFileService;
import org.imagopole.omero.tools.api.blitz.OmeroQueryService;
import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.cli.CsvAnnotationConfig;
import org.imagopole.omero.tools.api.ctrl.CsvAnnotationController;
import org.imagopole.omero.tools.api.ctrl.CsvExportController;
import org.imagopole.omero.tools.api.ctrl.FileReaderController;
import org.imagopole.omero.tools.api.ctrl.FileWriterController;
import org.imagopole.omero.tools.api.ctrl.MetadataController;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.api.dto.LinksData;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.ContainersBlitzService;
import org.imagopole.omero.tools.impl.blitz.FileBlitzService;
import org.imagopole.omero.tools.impl.blitz.QueryBlitzService;
import org.imagopole.omero.tools.impl.blitz.UpdateBlitzService;
import org.imagopole.omero.tools.impl.blitz.UpdateNoOpBlitzService;
import org.imagopole.omero.tools.impl.ctrl.DefaultCsvAnnotationController;
import org.imagopole.omero.tools.impl.ctrl.DefaultCsvExportController;
import org.imagopole.omero.tools.impl.ctrl.DefaultFileReaderController;
import org.imagopole.omero.tools.impl.ctrl.DefaultFileWriterController;
import org.imagopole.omero.tools.impl.ctrl.DefaultMetadataController;
import org.imagopole.omero.tools.impl.logic.DefaultCsvAnnotationService;
import org.imagopole.omero.tools.impl.logic.DefaultCsvReaderService;
import org.imagopole.omero.tools.impl.logic.DefaultCsvWriterService;
import org.imagopole.omero.tools.impl.logic.DefaultFileReaderService;
import org.imagopole.omero.tools.impl.logic.DefaultFileWriterService;
import org.imagopole.omero.tools.impl.logic.DefaultMetadataService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for CSV and annotations processing.
 *
 * @author seb
 *
 */
public class CsvAnnotator {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CsvAnnotator.class);

    private CsvAnnotationConfig config;
    private ServiceFactoryPrx session;
    private CsvAnnotationController annotationController;
    private MetadataController metadataController;
    private CsvExportController exportController;
    private FileReaderController fileReaderController;
    private FileWriterController fileWriterController;

    /**
     * Parameterized constructor.
     *
     * @param config the CSV tool configuration settings
     * @param session the OMERO Blitz session
     */
    private CsvAnnotator(CsvAnnotationConfig config, ServiceFactoryPrx session) {
        super();

        Check.notNull(session, "session");
        Check.notNull(config, "config");
        this.config = config;
        this.session = session;
    }

    /**
     * Static factory method.
     *
     * @param config the CSV tool configuration settings
     * @param session the OMERO Blitz session
     * @return an initialized <code>CsvAnnotator</code>
     * >
     */
    public static final CsvAnnotator forSession(
            CsvAnnotationConfig config,
            ServiceFactoryPrx session) {

        Check.notNull(config, "config");
        Check.notNull(session, "session");

        CsvAnnotator annotator = new CsvAnnotator(config, session);
        annotator.wireDependencies();

        return annotator;
    }

    /**
     * Main entry point to the application logic.
     *
     * @param experimenterId the experimenter
     * @throws ServerError OMERO client or server failure
     * @throws IOException CSV file read failure
     */
    public void runFromConfig(final Long experimenterId) throws ServerError, IOException {
        Check.notNull(experimenterId, "experimenterId");

        // config is expected to be valid at this point
        log.debug("Config dump: {}", config.dump());

        final Boolean isExportMode = config.getExportMode();

        if (null != isExportMode && isExportMode) {
            log.debug("Export mode requested");
            runExportModeFromConfig(experimenterId);
        } else {
            log.debug("Annotate mode requested");
            runAnnotateModeFromConfig(experimenterId);
        }
    }

    /**
     * Main entry point to the "annotate mode" logic.
     *
     * @param experimenterId the experimenter
     * @throws ServerError OMERO client or server failure
     * @throws IOException CSV file read failure
     */
    private void runAnnotateModeFromConfig(final Long experimenterId) throws ServerError, IOException {
        Check.notNull(experimenterId, "experimenterId");

        final Long containerId = config.getContainerId();
        final String csvFileName = config.getOrInferCsvFilename();

        // convert cli arguments to valid enum values or fail
        final String containerTypeArg = config.getCsvContainerTypeArg();
        final String annotationTypeArg = config.getAnnotationTypeArg();
        final String annotatedTypeArg = config.getAnnotatedTypeArg();

        final ContainerType fileContainerType = ContainerType.valueOf(containerTypeArg);
        final AnnotationType annotationType = AnnotationType.valueOf(annotationTypeArg);
        final AnnotatedType annotatedType = AnnotatedType.valueOf(annotatedTypeArg);

        // read CSV content from input source (local file or remote file annotation)
        // and decode into string data
        final CsvData csvData =
            fileReaderController.readByFileContainerType(
                    experimenterId,
                    containerId,
                    fileContainerType,
                    csvFileName);

        // process CSV string to OMERO model object annotation links
        LinksData linksData = annotationController.buildAnnotationsByTypes(
                experimenterId,
                containerId,
                annotationType,
                annotatedType,
                csvData);

        // persist to database
        if (config.getDryRun()) {
            log.info("Dry run requested - skipping database persistence");
            // TODO: add stdout info?
        } else {
            log.info("Persisting changes to database");

            annotationController.saveAllAnnotationLinks(linksData);
        }
    }

    /**
     * Main entry point to the "export mode" logic.
     *
     * @param experimenterId the experimenter
     * @throws ServerError OMERO client or server failure
     * @throws IOException CSV file write failure
     */
    private void runExportModeFromConfig(final Long experimenterId) throws ServerError, IOException {
        Check.notNull(experimenterId, "experimenterId");

        final Long containerId = config.getContainerId();
        final String csvFileName = config.getOrInferCsvFilename();

        // convert cli arguments to valid enum values or fail
        final String containerTypeArg = config.getCsvContainerTypeArg();
        final String annotationTypeArg = config.getAnnotationTypeArg();
        final String annotatedTypeArg = config.getAnnotatedTypeArg();

        final ContainerType fileContainerType = ContainerType.valueOf(containerTypeArg);
        final AnnotationType annotationType = AnnotationType.valueOf(annotationTypeArg);
        final AnnotatedType annotatedType = AnnotatedType.valueOf(annotatedTypeArg);

        // lookup the (annotated) OMERO data hierarchy for export
        Collection<PojoData> entitiesPlusAnnotations =
            metadataController.listEntitiesPlusAnnotations(
                experimenterId,
                containerId,
                annotationType,
                annotatedType);

        // convert the OMERO data hierarchy into CSV content
        String fileContent = exportController.convertToCsv(entitiesPlusAnnotations);

        // upload and attach the generated CSV to the OMERO container
        fileWriterController.writeByFileContainerType(
                experimenterId,
                containerId,
                fileContainerType,
                csvFileName,
                fileContent);
    }

    /**
     * Initialize required services.
     *
     * Calls sequence down "layers":
     *
     * CsvAnnotator -> controllers -> application logic ("business") services -> OMERO Blitz services
     */
    private void wireDependencies() {
        Charset charset = lookupCharsetOrFail(config.getCsvCharsetName());

        //-- omero/blitz common "layer"
        OmeroAnnotationService annotationService = new AnnotationBlitzService(session);
        OmeroFileService fileService = new FileBlitzService(session);
        OmeroUpdateService updateService = buildUpdateService(session, config);
        OmeroContainerService containerService = new ContainersBlitzService(session);

        CsvAnnotationController annotationController =
            buildAnnotationController(session, config, annotationService, containerService, updateService);

        FileReaderController fileReaderController =
            buildFileReaderController(session, config, charset, annotationService, fileService);

        FileWriterController fileWriterController =
            buildFileWriterController(session, config, charset, fileService, updateService);

        MetadataController metadataController =
            buildMetadataController(session, config, annotationService, containerService);

        CsvExportController exportController =
            buildExportController(session, config);

        this.annotationController = annotationController;
        this.fileReaderController = fileReaderController;
        this.fileWriterController = fileWriterController;
        this.metadataController = metadataController;
        this.exportController = exportController;
    }

    private FileReaderController buildFileReaderController(
            final ServiceFactoryPrx session,
            final CsvAnnotationConfig config,
            final Charset charset,
            final OmeroAnnotationService annotationService,
            final OmeroFileService fileService) {

        //-- business logic "layer"
        DefaultFileReaderService fileReaderService = new DefaultFileReaderService();
        fileReaderService.setFileService(fileService);
        fileReaderService.setAnnotationService(annotationService);
        fileReaderService.setCharset(charset);

        //-- controller "layer"
        final DefaultFileReaderController fileReaderController = new DefaultFileReaderController();
        fileReaderController.setFileReaderService(fileReaderService);

        return fileReaderController;
    }

    private FileWriterController buildFileWriterController(
            final ServiceFactoryPrx session,
            final CsvAnnotationConfig config,
            final Charset charset,
            final OmeroFileService fileService,
            final OmeroUpdateService updateService) {

        //-- omero/blitz "layer"
        OmeroQueryService queryService = new QueryBlitzService(session);

        //-- business logic "layer"
        DefaultFileWriterService fileWriterService = new DefaultFileWriterService();
        fileWriterService.setFileService(fileService);
        fileWriterService.setQueryService(queryService);
        fileWriterService.setUpdateService(updateService);
        fileWriterService.setCharset(charset);

        //-- controller "layer"
        final DefaultFileWriterController fileWriterController = new DefaultFileWriterController();
        fileWriterController.setFileWriterService(fileWriterService);

        return fileWriterController;
    }

    private CsvAnnotationController buildAnnotationController(
            final ServiceFactoryPrx session,
            final CsvAnnotationConfig config,
            final OmeroAnnotationService annotationService,
            final OmeroContainerService containerService,
            final OmeroUpdateService updateService) {

        //-- business logic "layer"
        DefaultCsvReaderService csvReaderService = new DefaultCsvReaderService();
        csvReaderService.setDelimiter(config.getCsvDelimiter());
        csvReaderService.setSkipHeader(config.getCsvSkipHeader());

        DefaultCsvAnnotationService csvAnnotationService = new DefaultCsvAnnotationService();
        csvAnnotationService.setContainerService(containerService);
        csvAnnotationService.setAnnotationService(annotationService);
        csvAnnotationService.setUpdateService(updateService);

        //-- controller "layer"
        DefaultCsvAnnotationController annotationController = new DefaultCsvAnnotationController();
        annotationController.setCsvReaderService(csvReaderService);
        annotationController.setCsvAnnotationService(csvAnnotationService);

        return annotationController;
    }

    private MetadataController buildMetadataController(
            final ServiceFactoryPrx session,
            final CsvAnnotationConfig config,
            final OmeroAnnotationService annotationService,
            final OmeroContainerService containerService) {

        //-- business logic "layer"
        DefaultMetadataService metadataService = new DefaultMetadataService();
        metadataService.setAnnotationService(annotationService);
        metadataService.setContainerService(containerService);

        //-- controller "layer"
        DefaultMetadataController metadataController = new DefaultMetadataController();
        metadataController.setMetadataService(metadataService);

        return metadataController;
    }

    private CsvExportController buildExportController(
            final ServiceFactoryPrx session,
            final CsvAnnotationConfig config) {

        //-- business logic "layer"
        DefaultCsvWriterService csvWriterService = new DefaultCsvWriterService();
        csvWriterService.setDelimiter(config.getCsvDelimiter());
        csvWriterService.setSkipHeader(config.getCsvSkipHeader());

        //-- controller "layer"
        DefaultCsvExportController exportController = new DefaultCsvExportController();
        exportController.setCsvWriterService(csvWriterService);

        return exportController;
    }

    private OmeroUpdateService buildUpdateService(
            final ServiceFactoryPrx session,
            final CsvAnnotationConfig config) {

        OmeroUpdateService updateService = null;

        if (config.getDryRun()) {
            log.info("Dry run requested - using no-op database update service");
            updateService = new UpdateNoOpBlitzService(session);
        } else {
            log.info("Using default database update service");
            updateService = new UpdateBlitzService(session);
        }

        return updateService;
    }

    private static Charset lookupCharsetOrFail(String charsetName) {
        // maybe move this to a check to config.validate() first?
        return Charset.forName(charsetName);
    }

}
