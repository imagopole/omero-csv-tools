/**
 *
 */
package org.imagopole.omero.tools.api.cli;

import static org.imagopole.omero.tools.util.ParseUtil.empty;

import com.google.common.base.Objects;

import org.imagopole.omero.tools.api.cli.Args.Defaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String annotatedTypeArg;
    private String annotationTypeArg;
    private String csvContainerTypeArg;
    private Long containerId;

    // -- optional with dynamic default based on naming convention
    private String csvFileName;

    // -- optional
    private Character csvDelimiter = Defaults.COMMA_DELIMITER;
    private Boolean csvSkipHeader  = Defaults.SKIP_HEADER_ON;
    private String csvCharsetName  = Defaults.UTF_8_CHARSET;
    private Integer port           = Defaults.ICE_SSL_PORT;
    private Boolean dryRun         = Defaults.DRY_RUN_OFF;
    private Boolean exportMode     = Defaults.EXPORT_MODE_OFF;

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

        if (null != configuredFilename && ! configuredFilename.isEmpty()) {
            log.debug("Reusing csv filename from configured argument: {}", configuredFilename);
            return configuredFilename;
        }

        // otherwise use the naming convention
        String inferredFileName =
            NamingConventions.build(getAnnotatedTypeArg(), getAnnotationTypeArg(), getExportMode());
        log.info("Inferring csv filename from container & annotation types: {}", inferredFileName);

        return inferredFileName;
     }

     public String dump() {
        return Objects.toStringHelper(this)
            .add("hostname", getHostname())
            .add("username", getUsername())
            .add("password",  empty(getPassword()) ? "-" : "***")
            .add("session-key", empty(getSessionKey()) ? "-" : "***")
            .add("port", getPort())
            .add("dry-run", getDryRun())
            .add("annotated-type", getAnnotatedTypeArg())
            .add("annotation-type", getAnnotationTypeArg())
            .add("csv-container-type", getCsvContainerTypeArg())
            .add("csv-container-id", getContainerId())
            .add("csv-file-name", getCsvFileName())
            .add("csv-delimiter", getCsvDelimiter())
            .add("csv-skip-header", getCsvSkipHeader())
            .add("csv-charset", getCsvCharsetName())
            .add("export-mode", getExportMode())
            .toString();
     }

    /**
     * FileAnnotation naming convention to hold tagging requests in CSV files.
     *
     * As follows: <annotatedType>_<annotationType>.csv (annotation mode)
     *             <annotatedType>_<annotationType>.export.csv (export mode)
     * Eg.
     *   - dataset_tag.csv     | dataset_tag.export.csv     : tags applied to datasets
     *   - dataset_comment.csv | dataset_comment.export.csv : comments applied to datasets
     *   - image_tag.csv       | image_tag.export.csv       : tags applied to images
     */
    private static final class NamingConventions {

        private static final String SEPARATOR = "_";
        private static final String PREFIX = "";
        private static final String EXTENSION = ".csv";
        private static final String EXPORT_QUALIFIER = ".export";

        private static final String build(String containerType, String annotationType) {
            return PREFIX + containerType + SEPARATOR + annotationType + EXTENSION;
        }

        private static final String build(String containerType, String annotationType, Boolean isExportMode) {
            if (null == isExportMode || !isExportMode) {
                return build(containerType, annotationType);
            }

            return PREFIX + containerType + SEPARATOR + annotationType + EXPORT_QUALIFIER + EXTENSION;
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
     * @return the dryRun
     */
    public Boolean getDryRun() {
        return dryRun;
    }

    /**
     * @param dryRun the dryRun to set
     */
    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
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
     */
    public Boolean getExportMode() {
        return exportMode;
    }

    /**
     * @param exportMode the exportMode to set
     */
    public void setExportMode(Boolean exportMode) {
        this.exportMode = exportMode;
    }

}
