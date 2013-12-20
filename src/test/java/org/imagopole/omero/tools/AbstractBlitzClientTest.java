package org.imagopole.omero.tools;

import static org.imagopole.omero.tools.util.ParseUtil.empty;
import static org.testng.Assert.fail;

import java.io.File;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;

import org.imagopole.omero.tools.TestsUtil.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.unitils.UnitilsTestNG;


public abstract class AbstractBlitzClientTest extends UnitilsTestNG {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(AbstractBlitzClientTest.class);

    /** OMERO Ice client */
    private omero.client client;

    /** OMERO Ice session */
    private ServiceFactoryPrx session;

    @BeforeClass
    @Parameters(Env.ICE_CONFIG_LOCATION)
    public void setUpBlitzClient(@Optional String iceConfigLocation)
                    throws ServerError, CannotCreateSessionException, PermissionDeniedException {

        String iceConfig = System.getenv(Env.ICE_CONFIG);

        if (!empty(iceConfig) && !empty(iceConfig.trim())) {
            log.debug("Loading Ice configuration from 'ICE_CONFIG' at {}", iceConfig);
            client = new omero.client();
        } else if (!empty(iceConfigLocation) && !empty(iceConfigLocation.trim())){
            log.debug("Loading Ice configuration from 'ice.config.location' at {}", iceConfigLocation);
            client = new omero.client(new File(iceConfigLocation));
        } else {
            fail("Run integration tests with ICE_CONFIG or ice.config.location environment variables");
        }

        session = client.createSession();
        session.detachOnDestroy();

        log.debug("Got session {} from client {} - secure: {}", session, client, client.isSecure());

        setUpAfterIceConnection(session);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownBlitzClient() {
        log.debug("Releasing OMERO client resources: client ref: {}, session ref: {}", client, session);

        if (null != client) {
            client.closeSession();
            client.__del__();
        }
    }

    protected abstract void setUpAfterIceConnection(ServiceFactoryPrx session);

    protected ServiceFactoryPrx getSession() {
        return session;
    }

}
