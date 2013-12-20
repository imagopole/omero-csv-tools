/**
 *
 */
package org.imagopole.omero.tools.impl.blitz;



import java.io.ByteArrayOutputStream;
import java.io.IOException;

import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;

import org.imagopole.omero.tools.api.blitz.OmeroFileService;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author seb
 *
 */
public class FileBlitzService implements OmeroFileService {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(FileBlitzService.class);

    /** Internal copy block size */
    private static final int BUF_SIZE = 4096;

    /** OMERO Ice session */
    private ServiceFactoryPrx session;

    /**
     * @param session
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

    private byte[] loadFromStore(RawFileStorePrx store, Long fileSize) throws ServerError, IOException {
        Check.notNull(store, "store");
        Check.notNull(fileSize, "fileSize");

        int fileLength = fileSize.intValue();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int pos = 0;
        for (pos = 0; (pos + BUF_SIZE) < fileLength; pos += BUF_SIZE) {
            byte[] bytes = store.read(pos, BUF_SIZE);
            bos.write(bytes);
        }

        int remainingOffset = fileLength - pos;
        byte[] remainingBytes = store.read(pos, remainingOffset);
        bos.write(remainingBytes);

        bos.flush();
        bos.close();

        return bos.toByteArray();
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
