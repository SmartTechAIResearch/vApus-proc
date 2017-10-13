/*
 * Copyright 2014 (c) Sizing Servers Lab
 * University College of West-Flanders, Department GKG * 
 * Author(s):
 * 	Dieter Vandroemme
 */
package be.sizingservers.vapus.proc.agent;

import be.sizingservers.vapus.agent.Monitor;
import be.sizingservers.vapus.agent.Server;
import java.net.Socket;

/**
 *
 * @author didjeeh
 */
public class ProcServer extends be.sizingservers.vapus.agent.Server {

    @Override
    protected Monitor getNewMonitor(Server server, Socket socket, long id) {
        return new ProcMonitor(server, socket, id);
    }
}
