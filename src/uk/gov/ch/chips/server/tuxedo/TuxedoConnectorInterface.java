package uk.gov.ch.chips.server.tuxedo;

import weblogic.wtc.jatmi.TPException;
import weblogic.wtc.jatmi.Reply;
import weblogic.wtc.jatmi.TypedBuffer;

public interface TuxedoConnectorInterface {
    public Reply callService(TypedBuffer buffer) throws TPException;
    public Reply callService(TypedBuffer buffer, int timeoutMillis) throws TPException;
    public void callAsynchronousService(TypedBuffer buffer);
    public void checkConnection(int maxWaitTimeInMillis) throws Exception;
}
