/**
 *
 */
package org.imagopole.omero.tools.impl;

import java.io.IOException;
import java.nio.charset.Charset;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;

import org.imagopole.omero.tools.api.blitz.OmeroAnnotationService;
import org.imagopole.omero.tools.api.blitz.OmeroContainerService;
import org.imagopole.omero.tools.api.blitz.OmeroFileService;
import org.imagopole.omero.tools.api.blitz.OmeroUpdateService;
import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.cli.CsvAnnotationConfig;
import org.imagopole.omero.tools.api.ctrl.CsvAnnotationController;
import org.imagopole.omero.tools.api.ctrl.FileReaderController;
import org.imagopole.omero.tools.api.dto.CsvData;
import org.imagopole.omero.tools.api.dto.LinksData;
import org.imagopole.omero.tools.impl.blitz.AnnotationBlitzService;
import org.imagopole.omero.tools.impl.blitz.ContainersBlitzService;
import org.imagopole.omero.tools.impl.blitz.FileBlitzService;
import org.imagopole.omero.tools.impl.blitz.UpdateBlitzService;
import org.imagopole.omero.tools.impl.blitz.UpdateNoOpBlitzService;
import org.imagopole.omero.tools.impl.ctrl.DefaultCsvAnnotationController;
import org.imagopole.omero.tools.impl.ctrl.DefaultFileReaderController;
import org.imagopole.omero.tools.impl.logic.DefaultCsvAnnotationService;
import org.imagopole.omero.tools.impl.logic.DefaultCsvReaderService;
import org.imagopole.omero.tools.impl.logic.DefaultFileReaderService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author seb
 *
 */
public class CsvAnnotator {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CsvAnnotator.class);

    private CsvAnnotationConfig config;
    private ServiceFactoryPrx session;
    private CsvAnnotationController annotationController;
    private FileReaderController fileController;

    /**
     * @param config
     * @param session
     */
    private CsvAnnotator(CsvAnnotationConfig config, ServiceFactoryPrx session) {
        super();

        Check.notNull(session, "session");
        Check.notNull(config, "config");
        this.config = config;
        this.session = session;
    }

    public static final CsvAnnotator forSession(
            CsvAnnotationConfig config,
            ServiceFactoryPrx session) {

        Check.notNull(config, "config");
        Check.notNull(session, "session");

        CsvAnnotator annotator = new CsvAnnotator(config, session);
        annotator.wireDependencies();

        return annotator;
    }

    public void runFromConfig(final Long experimenterId) throws ServerError, IOException {
        Check.notNull(experimenterId, "experimenterId");

        // config is expected to be valid at this point
        log.debug("Config dump: {}", config.dump());

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
            fileController.readByFileContainerType(
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
     * Initialize required services.
     *
     * Calls sequence down "layers":
     *
     * CsvAnnotator -> controllers -> application logic ("business") services -> OMERO Blitz services
     */
    private void wireDependencies() {
        //-- omero/blitz common "layer"
        OmeroAnnotationService annotationService = new AnnotationBlitzService(session);

        CsvAnnotationController annotationController =
            buildAnnotationController(session, config,annotationService);

        FileReaderController fileController = buildFileController(session, config, annotationService);

        this.annotationController = annotationController;
        this.fileController = fileController;
    }

    private FileReaderController buildFileController(
            final ServiceFactoryPrx session,
            final CsvAnnotationConfig config,
            final OmeroAnnotationService annotationService) {

        //-- omero/blitz "layer"
        OmeroFileService fileService = new FileBlitzService(session);

        //-- business logic "layer"
        DefaultFileReaderService fileReaderService = new DefaultFileReaderService();
        fileReaderService.setFileService(fileService);
        fileReaderService.setAnnotationService(annotationService);
        fileReaderService.setCharset(lookupCharsetOrFail(config.getCsvCharsetName()));

        //-- controller "layer"
        final DefaultFileReaderController fileController = new DefaultFileReaderController();
        fileController.setFileReaderService(fileReaderService);

        return fileController;
    }

    private CsvAnnotationController buildAnnotationController(
            final ServiceFactoryPrx session,
            final CsvAnnotationConfig config,
            final OmeroAnnotationService annotationService) {

        //-- omero/blitz "layer"
        OmeroContainerService containerService = new ContainersBlitzService(session);

        OmeroUpdateService updateService = null;
        if (config.getDryRun()) {
            log.info("Dry run requested - using no-op database update service");
            updateService = new UpdateNoOpBlitzService(session);
        } else {
            log.info("Using default database update service");
            updateService = new UpdateBlitzService(session);
        }

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

    private static Charset lookupCharsetOrFail(String charsetName) {
        // maybe move this to a check to config.validate() first?
        return Charset.forName(charsetName);
    }

}
