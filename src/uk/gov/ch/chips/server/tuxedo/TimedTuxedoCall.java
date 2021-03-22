package uk.gov.ch.chips.server.tuxedo;

import weblogic.wtc.gwt.TuxedoConnection;
import weblogic.wtc.jatmi.Reply;
import weblogic.wtc.jatmi.TypedBuffer;

public class TimedTuxedoCall implements Runnable {

    private TuxedoConnection connection;
    private String serviceName;
    private Reply reply = null;
    private TypedBuffer buffer;
    private Exception reportException = null;
    private boolean finished = false;
    private int flags;

    TimedTuxedoCall(TuxedoConnection conn, String service, TypedBuffer buf, int flg) {
        this.connection = conn;
        this.serviceName = service;
        this.buffer = buf;
        this.flags = flg;
    }

    public void run() {

        try {
            reply = connection.tpcall(this.serviceName, this.buffer, this.flags);
        } catch (Exception e) {
            reportException = e;
        }
        finished = true;
    }

    public Reply getResult() throws Exception {
        if (reportException != null)
            throw reportException;
        return this.reply;
    }

    public boolean isFinished() {
        return finished;
    }
}