package uk.gov.ch.chips.server.tuxedo;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import weblogic.wtc.gwt.TuxedoConnection;
import weblogic.wtc.gwt.TuxedoConnectionFactory;
import weblogic.wtc.jatmi.Reply;
import weblogic.wtc.jatmi.TPException;
import weblogic.wtc.jatmi.TPReplyException;
import weblogic.wtc.jatmi.TypedBuffer;
import weblogic.wtc.jatmi.TypedFML32;

public class TuxedoConnector implements TuxedoConnectorInterface {

    private static final Logger log = Logger.getLogger(TuxedoConnector.class.getName());

    Context ctx;
    TuxedoConnectionFactory tcf;
    TuxedoConnection conn;
    String serviceName;

    /**
     * Constructor that takes the name of a tuxedo connection.
     * 
     * @param connectionName - the tux connection name
     */
    public TuxedoConnector(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Get a TuxedoConnection.
     * 
     * @param connName - The TuxedoConnectionFactory name to get the connection from
     * @return - A TuxedoConnection
     * @throws TPException
     */
    private void openConnection() throws TPException {
        try {
            ctx = new InitialContext();
            tcf = (TuxedoConnectionFactory) ctx.lookup("tuxedo.services.TuxedoConnection");
        } catch (NamingException ne) {
            log.error("Could not get TuxedoConnectionFactory", ne);
        }
        // get a connection
        if (null != tcf) {
            conn = tcf.getTuxedoConnection();
        }
    }

    /**
     * Synchronously calls a tuxedo service and gets a reply.
     * 
     * @param serviceName - The service name to call
     * @param buffer      - The message to be sent to the service
     * @return - The reply from the service
     * @throws TPException
     */
    public Reply callService(TypedBuffer buffer) throws TPException {
        Reply reply = null;
        try {
            openConnection();
            reply = conn.tpcall(this.serviceName, buffer, TuxedoConnection.TPNOTRAN);
        } catch (TPReplyException tpre) {
            log.error("Failed to get reply from Service", tpre);
            throw new RuntimeException(tpre);
        } catch (TPException tpe) {
            log.error(tpe);
            throw tpe;
        } finally {
            if (null != conn)
                closeConnection();
        }
        return reply;

    }

    /**
     * Synchronously calls a tuxedo service and gets a reply, but is time limited.
     * 
     * @param buffer        - The message to be sent to the service
     * @param timeoutMillis - maximum time in milliseconds to wait for the call to
     *                      complete
     * @return - The reply from the service
     * @throws TPException
     */
    public Reply callService(TypedBuffer buffer, int timeoutMillis) throws TPException {
        Reply reply = null;
        try {
            openConnection();

            TimedTuxedoCall tpCall = new TimedTuxedoCall(conn, this.serviceName, buffer, TuxedoConnection.TPNOTRAN);
            Thread t = new Thread(tpCall);
            t.start();
            try {
                t.join(timeoutMillis);
            } catch (InterruptedException ie) {
            }

            if (!tpCall.isFinished()) {
                t.interrupt();
                throw new RuntimeException("Timeout during tuxedo call." + "Service:" + this.serviceName);
            }

            reply = tpCall.getResult();

        } catch (TPReplyException tpre) {
            log.error("Failed to get reply from Service", tpre);
            throw new RuntimeException(tpre);
        } catch (TPException tpe) {
            log.error(tpe);
            throw tpe;
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            if (null != conn)
                closeConnection();
        }
        return reply;

    }

    /**
     * Asynchronously calls a tuxedo service and gets a reply.
     * 
     * @param buffer - The message to be sent to the service
     * @throws TPException
     */
    public void callAsynchronousService(TypedBuffer buffer) {
        try {
            openConnection();
            conn.tpacall(this.serviceName, buffer, TuxedoConnection.TPNOTRAN);
        } catch (TPReplyException tpre) {
            log.error("Failed to get reply from Service", tpre);
        } catch (TPException tpe) {
            log.error(tpe);
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (null != conn)
                closeConnection();
        }
    }

    /**
     * Checks if we can connect.
     */
    public void checkConnection(int maxWaitTimeInMillis) throws Exception {
        callService(new TypedFML32(), maxWaitTimeInMillis);
    }

    /**
     * Nuff Said
     *
     */
    private void closeConnection() {
        conn.tpterm();
    }

}