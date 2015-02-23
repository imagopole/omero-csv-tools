/**
 *
 */
package org.imagopole.omero.tools;


import java.io.IOException;

import omero.ServerError;
import omero.client;
import omero.api.ServiceFactoryPrx;
import omero.sys.EventContext;

import org.imagopole.omero.tools.api.cli.ArgsParser;
import org.imagopole.omero.tools.api.cli.CsvAnnotationConfig;
import org.imagopole.omero.tools.impl.CsvAnnotator;
import org.imagopole.omero.tools.impl.cli.CliArgsParser;
import org.imagopole.omero.tools.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;


/**
 * Configure and run the CSV Annotation Tool as a standalone program invoked
 * from the command line.
 *
 * In this context, the user credentials must be supplied as CLI arguments for authentication.
 *
 * @author seb
 *
 */
public class CsvAnnotatorCliMain {

    /** Application logs */
    private static final Logger LOG = LoggerFactory.getLogger(CsvAnnotatorCliMain.class);

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

    private static void configureCloseSessionOnExit(final client client) {

        if (null != client) {

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    LOG.debug("Closing current session on client {}", client);
                    client.closeSession();
                }
            });

        }
    }

    private static ServiceFactoryPrx createSession(
            final client client,
            String username,
            String password) throws CannotCreateSessionException, PermissionDeniedException, ServerError {

        Check.notEmpty(username, "username");
        Check.notEmpty(password, "password");

        ServiceFactoryPrx session = null;

        if (null != client) {

            session = client.createSession(username, password);
            session.detachOnDestroy();

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
     * Log and die
     * @param message
     * @param throwable
     */
    private static void die(String message, Throwable throwable) {
        LOG.error(message, throwable);
        die(message);
    }

    /**
     * Log and die for the calling OMERO script to handle the failure.
     *
     * Include the server stack trace in the error log.
     *
     * @param message error message
     * @param serverError the OMERO server error
     */
    private static void die(String message, ServerError serverError) {
        LOG.error("Remote stackTrace - {}", serverError.serverStackTrace);
        die(message, (Throwable) serverError);
    }

    /**
     * Main entry point for the CLI.
     *
     * @param args the CLI arguments
     */
    public static void main(String[] args) {

        final ArgsParser argsParser =
            new CliArgsParser(CsvAnnotatorCliMain.class.getName());

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
            configureCloseSessionOnExit(client);

            final ServiceFactoryPrx session =
                createSession(client, config.getUsername(), config.getPassword());
            if (null == session) {
                die("Failed to connect to OMERO [session]");
            }

            final EventContext context = getRemoteContext(session);
            final Long experimenterId = context.userId;

            final CsvAnnotator csvAnnotator = CsvAnnotator.forSession(config, session);
            csvAnnotator.runFromConfig(experimenterId);

        } catch (ServerError se) {
            die("Failed to query/update OMERO [server]: " + se.message, se);
        } catch (IOException ioe) {
            die("Failed to query/update OMERO [io]: " + ioe.getMessage(), ioe);
        } catch (CannotCreateSessionException ccse) {
            die("Failed to join OMERO [session]: " + ccse.getMessage(), ccse);
        } catch (PermissionDeniedException pde) {
            die("Failed to join OMERO [permissions]: " + pde.getMessage(), pde);
        } catch(Throwable t) {
            die("Failed to perform operation [unknown]: " + t.getMessage(), t);
        } finally {
            if (null != client) {
                client.closeSession();
                client.__del__();
            }
        }

        System.exit(SUCCESS);

    }

}
