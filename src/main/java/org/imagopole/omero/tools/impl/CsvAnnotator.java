/**
 *
 */
package org.imagopole.omero.tools.impl;

import static org.imagopole.omero.tools.util.AnnotationsUtil.getExportModeAnnotationInfo;
import static org.imagopole.omero.tools.util.AnnotationsUtil.getImportModeAnnotationInfo;
import static org.imagopole.omero.tools.util.ParseUtil.getFileBasename;

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
import org.imagopole.omero.tools.api.cli.Args.Defaults;
import org.imagopole.omero.tools.api.cli.Args.FileType;
import org.imagopole.omero.tools.api.cli.Args.RunMode;
import org.imagopole.omero.tools.api.cli.CsvAnnotationConfig;
import org.imagopole.omero.tools.api.ctrl.CsvAnnotationController;
import org.imagopole.omero.tools.api.ctrl.CsvExportController;
import org.imagopole.omero.tools.api.ctrl.FileReaderController;
import org.imagopole.omero.tools.api.ctrl.FileWriterController;
import org.imagopole.omero.tools.api.ctrl.MetadataController;
import org.imagopole.omero.tools.api.dto.AnnotationInfo;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.api.dto.LinksData;
import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.FileBlitzService;
import org.imagopole.omero.tools.impl.blitz.QueryBlitzService;
import org.imagopole.omero.tools.impl.blitz.ShimContainersBlitzService;
import org.imagopole.omero.tools.impl.blitz.UpdateBlitzService;
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

        // convert cli arguments to valid enum values or fail
        final String runModeArg = config.getRunModeArg();
        final RunMode runMode = RunMode.valueOf(runModeArg);

        log.debug("Requested run mode: {}", runMode);

        switch (runMode) {

            case annotate:
                runAnnotateModeFromConfig(experimenterId);
                break;

            case export:
                runExportModeFromConfig(experimenterId);
                break;

            case transfer:
                runTransferModeFromConfig(experimenterId);
                break;

            case auto:
                runAutoModeFromConfig(experimenterId);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported run mode parameter");

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

        log.debug("annotate-mode:start");

        final Long containerId = config.getContainerId();
        final String csvFileName = config.getOrInferCsvFilename();

        // convert cli arguments to valid enum values or fail
        final String fileTypeArg = config.getCsvFileTypeArg();
        final String containerTypeArg = config.getCsvContainerTypeArg();
        final String annotationTypeArg = config.getAnnotationTypeArg();

        final FileType fileType = FileType.valueOf(fileTypeArg);
        final ContainerType containerType = ContainerType.valueOf(containerTypeArg);
        final AnnotationType annotationType = AnnotationType.valueOf(annotationTypeArg);
        final AnnotatedType annotatedType = config.getOrInferEffectiveAnnotatedType();

        // read CSV content from input source (local file or remote file annotation)
        // and decode into string data
        final CsvData csvData =
            fileReaderController.readByFileContainerType(
                    experimenterId,
                    containerId,
                    containerType,
                    fileType,
                    csvFileName);

        // process CSV string to OMERO model object annotation links
        LinksData linksData = annotationController.buildAnnotationsByTypes(
                experimenterId,
                containerId,
                containerType,
                annotationType,
                annotatedType,
                csvData);

        // persist to database
        log.info("Persisting changes to database");
        annotationController.saveAllAnnotationLinks(linksData);

        log.debug("annotate-mode:end");
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

        log.debug("export-mode:start");

        final Long containerId = config.getContainerId();
        final String csvFileName = config.getOrInferCsvFilename();

        // convert cli arguments to valid enum values or fail
        final String fileTypeArg = config.getCsvFileTypeArg();
        final String containerTypeArg = config.getCsvContainerTypeArg();
        final String annotationTypeArg = config.getAnnotationTypeArg();

        final FileType fileType = FileType.valueOf(fileTypeArg);
        final ContainerType containerType = ContainerType.valueOf(containerTypeArg);
        final AnnotationType annotationType = AnnotationType.valueOf(annotationTypeArg);
        final AnnotatedType annotatedType = config.getOrInferEffectiveAnnotatedType();

        // lookup the (annotated) OMERO data hierarchy for export
        Collection<PojoData> entitiesPlusAnnotations =
            metadataController.listEntitiesPlusAnnotations(
                experimenterId,
                containerId,
                containerType,
                annotationType,
                annotatedType);

        // convert the OMERO data hierarchy into CSV content
        String fileContent =
            exportController.convertToCsv(annotationType, annotatedType, entitiesPlusAnnotations);

        // get the file annotation metadata (ie. namespace & description)
        AnnotationInfo annotationInfo = getExportModeAnnotationInfo(containerId, containerType);

        // upload and attach the generated CSV to the OMERO container
        fileWriterController.writeByFileContainerType(
                experimenterId,
                containerId,
                containerType,
                fileType,
                csvFileName,
                fileContent,
                annotationInfo);

        log.debug("export-mode:end");
    }

    /**
     * Main entry point to the "import/transfer mode" (ie. upload + attach) logic.
     *
     * @param experimenterId the experimenter
     * @throws ServerError OMERO client or server failure
     * @throws IOException CSV file upload/attach failure
     */
    private void runTransferModeFromConfig(final Long experimenterId) throws ServerError, IOException {
        Check.notNull(experimenterId, "experimenterId");

        log.debug("transfer-mode:start");

        final Long containerId = config.getContainerId();
        final String configCsvFileName = config.getOrInferCsvFilename();

        // ignore the fileType in attach mode:
        // always read from a local file and write to a remote file annotation
        final String fileTypeArg = config.getCsvFileTypeArg();
        if (null != fileTypeArg && !fileTypeArg.trim().isEmpty()) {
            log.info("Transfer mode - ignoring/resetting configured fileType: {}", fileTypeArg);
            config.setCsvFileTypeArg(null);
        }

        // convert cli arguments to valid enum values or fail
        final String containerTypeArg = config.getCsvContainerTypeArg();
        final ContainerType containerType = ContainerType.valueOf(containerTypeArg);

        // read CSV content from local file
        final CsvData csvData =
            fileReaderController.readByFileContainerType(
                    experimenterId,
                    containerId,
                    containerType,
                    FileType.local,
                    configCsvFileName);

        if (null != csvData) {

            // get the file annotation data + metadata (ie. namespace & description)
            String fileContent = csvData.getFileContent();
            AnnotationInfo annotationInfo = getImportModeAnnotationInfo(containerId, containerType);

            // strip the path information from the remote file annotation name
            String remoteCsvFileName = getFileBasename(configCsvFileName);

            // upload and attach the generated CSV to the OMERO container
            fileWriterController.writeByFileContainerType(
                    experimenterId,
                    containerId,
                    containerType,
                    FileType.remote,
                    remoteCsvFileName,
                    fileContent,
                    annotationInfo);

        } else {
            log.warn("Failed to read local file content for transfer from: {}", configCsvFileName);
        }

        log.debug("transfer-mode:end");
    }

    /**
     * Main entry point to the "auto-pilot mode" (transfer + annotate) logic.
     *
     * @param experimenterId the experimenter
     * @throws ServerError OMERO client or server failure
     * @throws IOException CSV file upload/attach failure
     */
    private void runAutoModeFromConfig(final Long experimenterId) throws ServerError, IOException {
        Check.notNull(experimenterId, "experimenterId");

        //-- auto mode:
        //-- (i) upload and attach local CSV file
        runTransferModeFromConfig(experimenterId);

        //-- (ii) re-configure cli parameters for next step (annotate)
        String configCsvFileName = config.getOrInferCsvFilename();
        String remoteCsvFileName = getFileBasename(configCsvFileName);

        log.info("Auto mode - overriding configured fileType: {}->remote and fileName: {}->{}",
                 config.getCsvFileTypeArg(), configCsvFileName, remoteCsvFileName);
        config.setCsvFileTypeArg(Defaults.FILE_TYPE_REMOTE);
        config.setCsvFileName(remoteCsvFileName);

        //-- (iii) process remote CSV file annotation
        runAnnotateModeFromConfig(experimenterId);
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
        // note: ContainersBlitzService is replaced with an intercepting implementation until support
        //       for SPW data makes it into server-side OMERO implementations (QueryDefinitions and DataObject)
        OmeroAnnotationService annotationService = new AnnotationBlitzService(session);
        OmeroFileService fileService = new FileBlitzService(session);
        OmeroUpdateService updateService = new UpdateBlitzService(session);
        OmeroContainerService containerService = new ShimContainersBlitzService(session);

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

    private static Charset lookupCharsetOrFail(String charsetName) {
        // maybe move this to a check to config.validate() first?
        return Charset.forName(charsetName);
    }

}
