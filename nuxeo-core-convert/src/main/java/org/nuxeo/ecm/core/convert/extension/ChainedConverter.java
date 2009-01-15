package org.nuxeo.ecm.core.convert.extension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.service.ConversionServiceImpl;
import org.nuxeo.ecm.core.convert.service.MimeTypeTranslationHelper;

/**
 *
 * Specific {@link Converter} implementation that acts as a converters chain.
 * The chain can be :
 *  - a chain of mime-types
 *  - a chain of converter names
 *
 *  This depends on the properties of the descriptor
 *
 * @author tiry
 *
 */
public class ChainedConverter implements Converter {

    protected boolean subConvertersBased = false;

    protected List<String> steps = new ArrayList<String>();
    protected List<String> subConverters = new ArrayList<String>();

    public ChainedConverter() {
        subConvertersBased = false;
        this.subConverters=null;
    }

    public ChainedConverter(List<String> subConverters) {
        subConvertersBased = true;
        this.subConverters = subConverters;
        this.steps = null;
    }

    public BlobHolder convert(BlobHolder blobHolder,
            Map<String, Serializable> parameters) throws ConversionException {

        if (subConvertersBased) {
            return convertBasedSubConverters(blobHolder, parameters);
        } else {
            return convertBasedOnMimeTypes(blobHolder, parameters);
        }
    }

    protected BlobHolder convertBasedSubConverters(BlobHolder blobHolder,
            Map<String, Serializable> parameters) throws ConversionException {

        try {
            String srcMT = blobHolder.getBlob().getMimeType();

            BlobHolder result = blobHolder;

            for (String converterName : subConverters) {

                ConverterDescriptor desc = ConversionServiceImpl
                        .getConverterDesciptor(converterName);
                if (!desc.getSourceMimeTypes().contains(srcMT)) {
                    throw new ConversionException(
                            "Conversion Chain is not well defined");
                }
                Converter converter = ConversionServiceImpl
                        .getConverter(converterName);
                result = converter.convert(result, parameters);
                srcMT = desc.getDestinationMimeType();
            }

            return result;
        } catch (ClientException e) {
            throw new ConversionException(
                    "error while trying to executre converters chain", e);
        }
    }

    /*
     * Try to find a chain of converters that fits the mime-types chain
     *
     */
    protected BlobHolder convertBasedOnMimeTypes(BlobHolder blobHolder,
            Map<String, Serializable> parameters) throws ConversionException {

        try {
            String srcMT = blobHolder.getBlob().getMimeType();

            BlobHolder result = blobHolder;
            for (String dstMT : steps) {
                String converterName = MimeTypeTranslationHelper
                        .getConverterName(srcMT, dstMT);
                if (converterName == null) {
                    throw new ConversionException(
                            "Chained conversion error : unable to find converter between "
                                    + srcMT + " and " + dstMT);
                }

                Converter converter = ConversionServiceImpl
                        .getConverter(converterName);

                result = converter.convert(result, parameters);
                srcMT = dstMT;
            }

            return result;
        } catch (ClientException e) {
            throw new ConversionException(
                    "error while trying to determine converter name", e);
        }
    }

    public void init(ConverterDescriptor descriptor) {
        if (!subConvertersBased) {
            steps.addAll(descriptor.getSteps());
            steps.add(descriptor.getDestinationMimeType());
        }
        else {
            ConverterDescriptor fconv = ConversionServiceImpl
            .getConverterDesciptor(subConverters.get(0));
            ConverterDescriptor lconv = ConversionServiceImpl
            .getConverterDesciptor(subConverters.get(subConverters.size()-1));

            descriptor.sourceMimeTypes = fconv.sourceMimeTypes;
            descriptor.destinationMimeType = lconv.destinationMimeType;

        }
    }

    public List<String> getSteps() {
        return steps;
    }

}