/**
 *
 */
package org.imagopole.omero.tools;


import java.io.IOException;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import omero.ServerError;
import omero.client;
import omero.api.ServiceFactoryPrx;
import omero.sys.EventContext;

import org.imagopole.omero.tools.api.cli.ArgsParser;
import org.imagopole.omero.tools.api.cli.CsvAnnotationConfig;
import org.imagopole.omero.tools.impl.CsvAnnotator;
import org.imagopole.omero.tools.impl.cli.ScriptArgsParser;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Configure and run the CSV Annotation Tool as a external program invoked
 * from the runtime environment of an OMERO server-side script.
 *
 * In this context, the current user's session token must be supplied
 * as CLI parameter for authentication instead of username/password credentials.
 *
 * @author seb
 *
 */
public class CsvAnnotatorScriptMain {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(CsvAnnotatorScriptMain.class);

    /** Success exit code*/
    private static final int SUCCESS = 0;

    /** Failure exit code */
    private static final int FAILURE = -1;

    private static void die(String msg) {
        System.err.println(msg);
        System.exit(FAILURE);
    }

    private static client createOmeroClient(String hostname, Integer port) {

        final client client = new client(hostname, port);
        LOG.debug("Got client handle {} - secure: {}", client, client.isSecure());

        return client;
    }

    private static ServiceFactoryPrx joinCurrentSession(
            final client client,
            String sessionkKey) throws CannotCreateSessionException, PermissionDeniedException, ServerError {

        Check.notEmpty(sessionkKey, "sessionKey");

        ServiceFactoryPrx session = null;

        if (null != client) {

            session = client.joinSession(sessionkKey);
            // do not detach session on destroy as the OMERO script
            // may do some further work after invoking us

        }

        LOG.debug("Got session handle: {}", session);
        return session;
    }

    private static EventContext getRemoteContext(final ServiceFactoryPrx session) throws ServerError {
        Check.notNull(session, "session");

        EventContext context = session.getAdminService().getEventContext();

        LOG.debug("Remote context: [group {} - {} | {}] [user {} - {} | {}]",
                  context.groupId, context.groupName, context.groupPermissions,
                  context.userId, context.userName, context.isAdmin);

        return context;
    }

    /**
     * Log and die for the calling OMERO script to handle the failure
     * @param message
     * @param throwable
     */
    private static void die(String message, Throwable throwable) {
        LOG.error(message, throwable);
        die(message);
    }

    /**
     * Main entry point for the OMERO.script.
     *
     * @param args the script arguments
     */
    public static void main(String[] args) {

        final ArgsParser argsParser =
            new ScriptArgsParser(CsvAnnotatorScriptMain.class.getName());

        final CsvAnnotationConfig config = argsParser.parseArgs(args);
        if (argsParser.isHelp()) {
            die(argsParser.getHelp());
        }
        if (null == config) {
            die(argsParser.getUsage());
        }

        client client = null;

        try {
            client = createOmeroClient(config.getHostname(), config.getPort());
            // do not configure session close on exit as the OMERO script
            // may do some further work after invoking us

            final ServiceFactoryPrx session = joinCurrentSession(client, config.getSessionKey());
            if (null == session) {
                die("Failed to join OMERO [session]");
            }

            final EventContext context = getRemoteContext(session);
            final Long experimenterId = context.userId;

            final CsvAnnotator csvAnnotator = CsvAnnotator.forSession(config, session);
            csvAnnotator.runFromConfig(experimenterId);

        } catch (ServerError se) {
            die("Failed to query/update OMERO [server]: " + se.getMessage(), se);
        } catch (IOException ioe) {
            die("Failed to query/update OMERO [io]: " + ioe.getMessage(), ioe);
        } catch (CannotCreateSessionException ccse) {
            die("Failed to join OMERO [session]: " + ccse.getMessage(), ccse);
        } catch (PermissionDeniedException pde) {
            die("Failed to join OMERO [permissions]: " + pde.getMessage(), pde);
        } catch(Throwable t) {
            die("Failed to perform operation [unknown]: " + t.getMessage(), t);
        }

        System.exit(SUCCESS);

    }

}
