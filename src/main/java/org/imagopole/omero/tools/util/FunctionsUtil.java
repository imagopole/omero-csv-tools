/**
 *
 */
package org.imagopole.omero.tools.util;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.imagopole.omero.tools.api.dto.PojoData;
import org.imagopole.omero.tools.impl.dto.DefaultPojoData;

import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.TagAnnotationData;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Utility class as holder of misc. Guava functions.
 *
 * @author seb
 *
 */
public final class FunctionsUtil {

    /**
     * Private constructor.
     */
    private FunctionsUtil() {
        super();
    }

    public static final Function<String, Long> toLongOrNull = new Function<String, Long>() {

        @Override
        @Nullable
        public Long apply(@Nullable String input) {
            return ParseUtil.parseLongOrNull(input);
        }

    };

    public static final Function<DatasetData, PojoData> datasetToPojo =
                    new Function<DatasetData, PojoData>() {

        @Override
        @Nullable
        public PojoData apply(@Nullable DatasetData input) {
            PojoData result = null;

            if (null != input) {
                result = DefaultPojoData.fromDatasetData(input);
            }

            return result;
        }

    };

    public static final Function<ImageData, PojoData> imageToPojo =
                    new Function<ImageData, PojoData>() {

        @Override
        @Nullable
        public PojoData apply(@Nullable ImageData input) {
            PojoData result = null;

            if (null != input) {
                result = DefaultPojoData.fromImageData(input);
            }

            return result;
        }

    };

    public static final Function<PlateData, PojoData> plateToPojo =
                    new Function<PlateData, PojoData>() {

        @Override
        @Nullable
        public PojoData apply(@Nullable PlateData input) {
            PojoData result = null;

            if (null != input) {
                result = DefaultPojoData.fromPlateData(input);
            }

            return result;
        }

    };

    public static final Function<PlateAcquisitionData, PojoData> plateAcquisitionToPojo =
                    new Function<PlateAcquisitionData, PojoData>() {

        @Override
        @Nullable
        public PojoData apply(@Nullable PlateAcquisitionData input) {
            PojoData result = null;

            if (null != input) {
                result = DefaultPojoData.fromPlateAcquisitionData(input);
            }

            return result;
        }

    };

    public static final Function<PojoData, String> toPojoName =
                    new Function<PojoData, String>() {

        @Override
        @Nullable
        public String apply(@Nullable PojoData input) {
            String result = null;

            if (null != input) {
                result = input.getName();
            }

            return result;
        }

    };

    public static final Function<PojoData, Long> toPojoId =
                    new Function<PojoData, Long>() {

        @Override
        @Nullable
        public Long apply(@Nullable PojoData input) {
            Long result = null;

            if (null != input) {
                result = input.getId();
            }

            return result;
        }

    };

    public static final Function<TagAnnotationData, String> toTagValue =
                    new Function<TagAnnotationData, String>() {

        @Override
        @Nullable
        public String apply(@Nullable TagAnnotationData input) {
            String result = null;

            if (null != input) {
                result = input.getTagValue();
            }

            return result;
        }

    };

    public static final Function<FileAnnotationData, String> toAnnotationFileName =
                    new Function<FileAnnotationData, String>() {

        @Override
        @Nullable
        public String apply(@Nullable FileAnnotationData input) {
            String result = null;

            if (null != input) {
                result = input.getFileName();
            }

            return result;
        }

    };

    public static final Function<AnnotationData, String> toAnnotationStringContent =
                    new Function<AnnotationData, String>() {

        @Override
        @Nullable
        public String apply(@Nullable AnnotationData input) {
            String result = null;

            if (null != input) {
                result = input.getContentAsString();
            }

            return result;
        }

    };

    public static final Maps.EntryTransformer<String, String, List<String>> asTuplesMultimap =
                    new Maps.EntryTransformer<String, String, List<String>>() {

        @Override
        public List<String> transformEntry(@Nullable String key, @Nullable String value) {
            List<String> result = Collections.emptyList();

            if (!isNullOrEmpty(key) && !isNullOrEmpty(value)) {

                result = Lists.newArrayList(key, value);

            }

            return result;
        }

    };

}
