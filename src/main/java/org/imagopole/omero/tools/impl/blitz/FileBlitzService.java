/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.OriginalFile;

import org.imagopole.omero.tools.api.blitz.OmeroFileService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service layer to the underlying file related OMERO gateway.
 *
 * @author seb
 *
 */
public class FileBlitzService implements OmeroFileService {

    /** Application logs. */
    private final Logger log = LoggerFactory.getLogger(FileBlitzService.class);

    /** Internal copy block size. */
    private static final int BUF_SIZE = 4096;

    /** OMERO Ice session. */
    private ServiceFactoryPrx session;

    /**
     * Parameterized constructor.
     *
     * @param session the OMERO Blitz session
     */
    public FileBlitzService(ServiceFactoryPrx session) {
        super();

        Check.notNull(session, "session");
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadOriginalFile(Long fileId, Long fileSize) throws ServerError, IOException {
        Check.notNull(fileId, "fileId");
        Check.strictlyPositive(fileSize, "fileSize");

        byte[] result = null;

        RawFileStorePrx store = null;
        try {
            store = setupStore(fileId);
            result = loadFromStore(store, fileSize);
        } finally {
            tearDownStore(store);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OriginalFile uploadOriginalFile(Long fileId, byte[] fileContent) throws ServerError, IOException {
        Check.notNull(fileId, "fileId");
        Check.notNull(fileContent, "fileContent");
        Check.strictlyPositive(fileContent.length, "fileSize");

        OriginalFile result = null;

        RawFileStorePrx store = null;
        try {
            store = setupStore(fileId);
            result = uploadToStore(store, fileContent);
        } finally {
            tearDownStore(store);
        }

        return result;
    }

    private byte[] loadFromStore(RawFileStorePrx store, Long fileSize) throws ServerError, IOException {
        Check.notNull(store, "store");
        Check.notNull(fileSize, "fileSize");

        int fileLength = fileSize.intValue();
        int bufSize = (BUF_SIZE > fileLength) ? fileLength : BUF_SIZE;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int pos = 0;
        for (pos = 0; (pos + bufSize) < fileLength; pos += bufSize) {
            byte[] bytes = store.read(pos, bufSize);
            bos.write(bytes);
        }

        int remainingOffset = fileLength - pos;
        byte[] remainingBytes = store.read(pos, remainingOffset);
        bos.write(remainingBytes);

        bos.flush();
        bos.close();

        return bos.toByteArray();
    }

    private OriginalFile uploadToStore(RawFileStorePrx store, byte[] fileContent) throws ServerError, IOException {
        Check.notNull(store, "store");
        Check.notNull(fileContent, "fileContent");
        Check.strictlyPositive(fileContent.length, "fileSize");

        OriginalFile result = null;

        int fileLength = fileContent.length;
        int bufSize = (BUF_SIZE > fileLength) ? fileLength : BUF_SIZE;

        ByteArrayInputStream bis = new ByteArrayInputStream(fileContent);

        int offset = 0, readlen = 0;
        byte[] bytes = new byte[bufSize];
        ByteBuffer bytesBuffer = null;

        // just write the in-memory array to the remote store instead? store.write(fileContent, 0, fileLength);
        while ( ( readlen = bis.read(bytes, 0, bufSize) ) > 0 ) {
            store.write(bytes, offset, readlen);
            offset += readlen;

            bytesBuffer = ByteBuffer.wrap(bytes);
            bytesBuffer.limit(readlen);
        }

        result = store.save();
        bis.close();

        return result;
    }

    private RawFileStorePrx setupStore(Long fileId) throws ServerError {
        Check.notNull(fileId, "fileId");

        RawFileStorePrx store = getSession().createRawFileStore();
        store.setFileId(fileId);

        return store;
    }

    private void tearDownStore(RawFileStorePrx store) {
        if (null != store) {
            try {
                store.close();
            } catch (ServerError ignore) {
                log.warn("Failed to tearDown rawFileStore", ignore);
            }
        }
    }

    /**
     * Returns session.
     * @return the session
     */
    public ServiceFactoryPrx getSession() {
        return session;
    }

    /**
     * Sets session.
     * @param session the session to set
     */
    public void setSession(ServiceFactoryPrx session) {
        this.session = session;
    }

}
