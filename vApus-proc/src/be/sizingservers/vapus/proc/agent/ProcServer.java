/*
 * 2014 Sizing Servers Lab, affiliated with IT bachelor degree NMCT
 * University College of West-Flanders, Department GKG (www.sizingservers.be, www.nmct.be, www.howest.be/en) 
 * Author(s):
 * 	Dieter Vandroemme
 */
package be.sizingservers.vapus.proc.agent;

import be.sizingservers.vapus.agent.Monitor;
import be.sizingservers.vapus.agent.Server;
import java.net.Socket;

/**
 *
*
 */
public class ProcServer extends be.sizingservers.vapus.agent.Server {

    @Override
    protected Monitor getNewMonitor(Server server, Socket socket, long id) {
        return new ProcMonitor(server, socket, id);
    }
}
