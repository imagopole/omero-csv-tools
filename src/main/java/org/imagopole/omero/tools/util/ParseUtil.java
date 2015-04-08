/**
 *
 */
package org.imagopole.omero.tools.util;

import java.io.File;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.imagopole.omero.tools.api.RtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for input conversion handling.
 *
 * @author seb
 *
 */
public final class ParseUtil {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(ParseUtil.class);

    /**
     * Private util
     */
    private ParseUtil() {
        super();
    }

    public static final boolean empty(String input) {
        return (null == input || input.isEmpty());
    }

    public static final Boolean parseBooleanOrNull(String input) {
        Boolean result = null;

        if (!empty(input)  && !empty(input.trim())) {
            result = Boolean.valueOf(input.trim());
        }

        return result;
    }

    public static final Character parseCharacterOrNull(String input) {
        Character result = null;

        if (!empty(input) && !empty(input.trim())) {
            result = input.trim().charAt(0);
        }

        return result;
    }

    public static final Integer parseIntegerOrNull(String input) {
        Integer result = null;

        if (!empty(input) && !empty(input.trim())) {

            try {
                result = Integer.parseInt(input.trim());
            } catch (NumberFormatException ignore) {
                LOG.debug("Invalid integer: {} - ignoring", input);
            }

        }

        return result;
    }

    public static final Long parseLongOrNull(String input) {
        Long result = null;

        if (!empty(input)) {

            try {
                result = Long.parseLong(input);
            } catch (NumberFormatException ignore) {
                LOG.debug("Invalid long: {} - ignoring", input);
            }

        }

        return result;
    }

    public static MimeType parseContentTypeOrFail(String contentType) {
        Check.notEmpty(contentType, "contentType");

        try {
            return new MimeType(contentType);
        } catch (MimeTypeParseException mtpe) {
            throw new RtException(mtpe.getMessage(), mtpe);
        }
    }

    public static String getFileBasename(String fileName) {
        Check.notEmpty(fileName, "fileName");

        return new File(fileName.trim()).getName();
    }

}
