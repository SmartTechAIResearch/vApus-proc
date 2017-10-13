/*
 * Copyright 2014 (c) Sizing Servers Lab
 * University College of West-Flanders, Department GKG * 
 * Author(s):
 * 	Dieter Vandroemme
 */
package be.sizingservers.vapus.proc.agent;

import be.sizingservers.vapus.agent.util.BashHelper;
import be.sizingservers.vapus.agent.util.Entities;
import be.sizingservers.vapus.agent.Agent;
import be.sizingservers.vapus.agent.Monitor;
import be.sizingservers.vapus.agent.Server;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 *
 * @author didjeeh
 */
public class PollProcAndSendCounters extends TimerTask {

    private final ProcMonitor monitor;
    private final Server server;
    private final Socket socket;
    private final Entities wiwWithCounters;

    /**
     *
     * @param wiwEntities
     * @param server
     * @param socket
     */
    public PollProcAndSendCounters(Entities wiwEntities, ProcMonitor monitor, Server server, Socket socket) {
        this.monitor = monitor;
        //This will contain the counter values, we do not want to pollute the given object.
        this.wiwWithCounters = wiwEntities.safeClone();
        this.server = server;
        this.socket = socket;
    }

    /**
     * Run this task.
     */
    @Override
    public void run() {
        try {
            for (int i = 0;; i++) { //Try 3 times.
                try {
                    send();
                    break;
                } catch (Exception e) {
                    if (i == 2) {
                        throw e;
                    }
                }
            }
        } catch (IOException ex) {
            Agent.getLogger().log(Level.SEVERE, "Failed reading the file (stopping the timer task now): {0}", ex);
            super.cancel();
        } catch (Exception ex) {
            Agent.getLogger().log(Level.SEVERE, "Failed reading the file (stopping the timer task now): {0}", ex);
            super.cancel();
        }
    }

    /**
     * If the current last line does not equal the previous one, it is send to
     * the client.
     *
     * @throws IOException
     */
    private void send() throws IOException, Exception {

        this.wiwWithCounters.setTimestamp();
        this.wiwWithCounters.setCounters(monitor.getWIH(this.wiwWithCounters));

        String json = (new Gson()).toJson(this.wiwWithCounters);

        this.server.send(json, this.socket);
    }
}
