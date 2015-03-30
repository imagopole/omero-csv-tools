/**
 *
 */
package org.imagopole.omero.tools.api.cli;

import static org.imagopole.omero.tools.util.ParseUtil.empty;

import org.imagopole.omero.tools.api.cli.Args.AnnotatedType;
import org.imagopole.omero.tools.api.cli.Args.AnnotationType;
import org.imagopole.omero.tools.api.cli.Args.ContainerType;
import org.imagopole.omero.tools.api.cli.Args.Defaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * Configuration settings for the CSV Annotation Tool.
 *
 * @author seb
 *
 */
public class CsvAnnotationConfig {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(CsvAnnotationConfig.class);

    // -- required
    private String hostname;
    private String username;
    private String password;
    private String sessionKey;
    private String annotatedTypeArg; //-- now optional: if empty, defaults to implicit "child" mode
    private String annotationTypeArg;
    private String csvContainerTypeArg;
    private Long containerId;

    // -- optional with dynamic default based on naming convention
    private String csvFileName;

    // -- optional
    private String csvFileTypeArg  = Defaults.FILE_TYPE_REMOTE;
    private Character csvDelimiter = Defaults.COMMA_DELIMITER;
    private Boolean csvSkipHeader  = Defaults.SKIP_HEADER_ON;
    private String csvCharsetName  = Defaults.UTF_8_CHARSET;
    private Integer port           = Defaults.ICE_SSL_PORT;
    @Deprecated
    private Boolean exportMode     = Defaults.EXPORT_MODE_OFF;
    private String runModeArg      = Defaults.RUN_MODE_ANNOTATE;

    /**
     * Vanilla constructor
     */
    protected CsvAnnotationConfig() {
        super();
    }

    public static final CsvAnnotationConfig defaultConfig(){
       return new CsvAnnotationConfig();
    }

    public String getOrInferCsvFilename() {
        // get CLI argument as configured if available
        String configuredFilename = getCsvFileName();

        ContainerType containerType = ContainerType.valueOf(getCsvContainerTypeArg());
        AnnotationType annotationType = AnnotationType.valueOf(getAnnotationTypeArg());
        AnnotatedType annotatedType = getOrInferEffectiveAnnotatedType();

        if (null != configuredFilename && ! configuredFilename.isEmpty()) {
            log.debug("Reusing csv filename from configured argument: {}", configuredFilename);

            // perform tokens substitution if needed
            boolean shouldExpandTokens =
                configuredFilename.matches(NamingConventions.Patterns.HAS_TOKENS);

            if (shouldExpandTokens) {

                String expandedFileName =
                    NamingConventions.expandTokens(
                        configuredFilename,
                        containerType,
                        getContainerId(),
                        annotationType,
                        annotatedType,
                        getExportMode());

                log.debug("Tokens expansion in csv filename argument: {}", expandedFileName);
                return expandedFileName;
            }

            return configuredFilename;
        }

        // otherwise use the naming convention (based on the _effective_ annotation target)
        String inferredFileName =
            NamingConventions.buildFullName(annotatedType, annotationType, getExportMode());

        log.info("Inferring csv filename from container & annotation types: {}", inferredFileName);

        return inferredFileName;
     }

    /**
     * Determines the effective annotated type: when the default ('child') annotated is enabled,
     * lookup the actual type one level down the OMERO hierarchy starting from the selected container.
     *
     * @return the effective annotated type to be used for processing
     *
     * @see ContainerType#getChildAnnotatedType()
     */
    public AnnotatedType getOrInferEffectiveAnnotatedType() {
        // get CLI argument as configured if available
        String configuredType = getAnnotatedTypeArg();

        // if no annotation target defined, infer from the implicit OMERO data model container hierarchy
        if (empty(configuredType)) {
            ContainerType containerTypeParameter = ContainerType.valueOf(getCsvContainerTypeArg());
            AnnotatedType effectiveAnnotatedType = containerTypeParameter.getChildAnnotatedType();
            log.info("Inferring effective annotated type from arg and container type: {}", effectiveAnnotatedType);

            return effectiveAnnotatedType;
        } else {
            AnnotatedType configuredAnnotatedType = AnnotatedType.valueOf(configuredType);
            log.info("Reusing annotated type from configured argument: {}", configuredAnnotatedType);

            return configuredAnnotatedType;
        }
    }

     public String dump() {
        return Objects.toStringHelper(this)
            .add("hostname", getHostname())
            .add("username", getUsername())
            .add("password",  empty(getPassword()) ? "-" : "***")
            .add("session-key", empty(getSessionKey()) ? "-" : "***")
            .add("port", getPort())
            .add("annotated-type", getAnnotatedTypeArg())
            .add("annotation-type", getAnnotationTypeArg())
            .add("csv-container-type", getCsvContainerTypeArg())
            .add("csv-container-id", getContainerId())
            .add("csv-file-name", getCsvFileName())
            .add("csv-file-type", getCsvFileTypeArg())
            .add("csv-delimiter", getCsvDelimiter())
            .add("csv-skip-header", getCsvSkipHeader())
            .add("csv-charset", getCsvCharsetName())
            .add("run-mode", getRunModeArg())
            .add("export-mode", getExportMode())
            .toString();
     }

    /**
     * FileAnnotation naming convention to hold tagging requests in CSV files.
     *
     * <pre>
     * As follows: {@code <annotatedType>_<annotationType>.csv}        (annotation mode)
     *             {@code <annotatedType>_<annotationType>.export.csv} (export mode)
     * Eg.
     *   - dataset_tag.csv     | dataset_tag.export.csv     : tags applied to datasets
     *   - dataset_comment.csv | dataset_comment.export.csv : comments applied to datasets
     *   - image_tag.csv       | image_tag.export.csv       : tags applied to images
     * </pre>
     */
    private static final class NamingConventions {

        private static final String SEPARATOR = "_";
        private static final String PREFIX = "";
        private static final String EXTENSION = ".csv";
        private static final String EXPORT_QUALIFIER = ".export";

        /**
         * Regexp patterns to used for well-known tokens substitution within the csv filenames.
         *
         * Tokens are in the form of: {@code <token>}, with both a shorthand and expanded token
         * available (eg.  {@code <cid>} and {@code <container-id>}).
         */
        private static final class Patterns {

            private static final String HAS_TOKENS        = ".*<.*>.*";
            private static final String CONTAINER_ID      = "<cid>|<container-id>";
            private static final String CONTAINER_TYPE    = "<ctype>|<container-type>";
            private static final String ANNOTATION_TYPE   = "<antype>|<annotation-type>";
            private static final String ANNOTATED_TYPE    = "<antarget>|<annotated-type>";

            /** "Meta" pattern equivalent to {@code <annotated-type>_<annotation-type>}. */
            private static final String DEFAULT_BASENAME  = "<basename>";

            /** Expands to {@code .csv} or {@code .export.csv}. */
            private static final String DEFAULT_SUFFIX    = "<suffix>";

            /**
             * Constants class.
             */
            private Patterns() {
                super();
            }
        }

        private static final String buildBasename(
                AnnotatedType annotatedType, AnnotationType annotationType) {

            return PREFIX + annotatedType + SEPARATOR + annotationType;
        }

        private static String buildSuffix(Boolean isExportMode) {
            if (null == isExportMode || !isExportMode) {
                return EXTENSION;
            }

            return EXPORT_QUALIFIER.concat(EXTENSION);
        }

        private static final String buildFullName(
                AnnotatedType annotatedType, AnnotationType annotationType, Boolean isExportMode) {

           return buildBasename(annotatedType, annotationType).concat(buildSuffix(isExportMode));
        }

        /**
         * Perform substitution for well-known filename tokens.
         *
         * @param configuredFilename the input filename with token patterns
         * @return the expanded filename
         */
        private static String expandTokens(
            String configuredFilename,
            ContainerType containerType,
            Long containerId,
            AnnotationType annotationType,
            AnnotatedType annotatedType,
            Boolean isExportMode) {

            String result =
                configuredFilename
                    .trim()
                    .replaceAll(Patterns.DEFAULT_BASENAME, buildBasename(annotatedType, annotationType))
                    .replaceAll(Patterns.DEFAULT_SUFFIX, buildSuffix(isExportMode))
                    .replaceAll(Patterns.CONTAINER_ID, containerId.toString())
                    .replaceAll(Patterns.CONTAINER_TYPE, containerType.name())
                    .replaceAll(Patterns.ANNOTATION_TYPE, annotationType.name())
                    .replaceAll(Patterns.ANNOTATED_TYPE, annotatedType.name());

            return result;
        }

        private NamingConventions() {
            super();
        }
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the annotatedTypeArg
     */
    public String getAnnotatedTypeArg() {
        return annotatedTypeArg;
    }

    /**
     * @param annotatedTypeArg the annotatedTypeArg to set
     */
    public void setAnnotatedTypeArg(String annotatedTypeArg) {
        this.annotatedTypeArg = annotatedTypeArg;
    }

    /**
     * @return the annotationTypeArg
     */
    public String getAnnotationTypeArg() {
        return annotationTypeArg;
    }

    /**
     * @param annotationTypeArg the annotationTypeArg to set
     */
    public void setAnnotationTypeArg(String annotationTypeArg) {
        this.annotationTypeArg = annotationTypeArg;
    }

    /**
     * @return the csvContainerTypeArg
     */
    public String getCsvContainerTypeArg() {
        return csvContainerTypeArg;
    }

    /**
     * @param csvContainerTypeArg the csvContainerTypeArg to set
     */
    public void setCsvContainerTypeArg(String csvContainerTypeArg) {
        this.csvContainerTypeArg = csvContainerTypeArg;
    }

    /**
     * @return the containerId
     */
    public Long getContainerId() {
        return containerId;
    }

    /**
     * @param containerId the containerId to set
     */
    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    /**
     * @return the csvFileName
     */
    public String getCsvFileName() {
        return csvFileName;
    }

    /**
     * @param csvFileName the csvFileName to set
     */
    public void setCsvFileName(String csvFileName) {
        // prevent defaults overriding
        if (!empty(csvFileName)) {
            this.csvFileName = csvFileName;
        }
    }

    /**
     * @return the csvSkipHeader
     */
    public Boolean getCsvSkipHeader() {
        return csvSkipHeader;
    }

    /**
     * @param csvSkipHeader the csvSkipHeader to set
     */
    public void setCsvSkipHeader(Boolean csvSkipHeader) {
        // prevent defaults overriding
        if (null != csvSkipHeader) {
            this.csvSkipHeader = csvSkipHeader;
        }
    }

    /**
     * @return the csvCharsetName
     */
    public String getCsvCharsetName() {
        return csvCharsetName;
    }

    /**
     * @param csvCharsetName the csvCharsetName to set
     */
    public void setCsvCharsetName(String csvCharsetName) {
        // prevent defaults overriding
        if (!empty(csvCharsetName)) {
            this.csvCharsetName = csvCharsetName;
        }
    }

    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(Integer port) {
        // prevent defaults overriding
        if (null != port) {
            this.port = port;
        }
    }

    /**
     * Returns csvDelimiter.
     * @return the csvDelimiter
     */
    public Character getCsvDelimiter() {
        return csvDelimiter;
    }

    /**
     * Sets csvDelimiter.
     * @param csvDelimiter the csvDelimiter to set
     */
    public void setCsvDelimiter(Character csvDelimiter) {
        // prevent defaults overriding
        if (null != csvDelimiter) {
            this.csvDelimiter = csvDelimiter;
        }
    }

    /**
     * @return the sessionId
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionKey(String sessionId) {
        this.sessionKey = sessionId;
    }

    /**
     * @return the exportMode
     * @deprecated Superceded by --mode=export
     */
    @Deprecated
    public Boolean getExportMode() {
        return exportMode;
    }

    /**
     * @param exportMode the exportMode to set
     * @deprecated Superceded by --mode=export
     */
    @Deprecated
    public void setExportMode(Boolean exportMode) {
        this.exportMode = exportMode;
    }

    /**
     * Returns modeArg.
     * @return the modeArg
     */
    public String getRunModeArg() {
        return runModeArg;
    }

    /**
     * Sets modeArg.
     * @param runModeArg the modeArg to set
     */
    public void setRunModeArg(String runModeArg) {
        this.runModeArg = runModeArg;
    }

    /**
     * Returns csvfileTypeArg.
     * @return the csvfileTypeArg
     */
    public String getCsvFileTypeArg() {
        return csvFileTypeArg;
    }

    /**
     * Sets csvfileTypeArg.
     * @param csvfileTypeArg the csvfileTypeArg to set
     */
    public void setCsvFileTypeArg(String csvfileTypeArg) {
        // prevent defaults overriding
        if (null != csvSkipHeader) {
            this.csvFileTypeArg = csvfileTypeArg;
        }
    }

}
