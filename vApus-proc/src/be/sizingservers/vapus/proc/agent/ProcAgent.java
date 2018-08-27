/*
 * 2014 Sizing Servers Lab, affiliated with IT bachelor degree NMCT
 * University College of West-Flanders, Department GKG (www.sizingservers.be, www.nmct.be, www.howest.be/en) 
 * Author(s):
 * 	Dieter Vandroemme
 */
package be.sizingservers.vapus.proc.agent;

import be.sizingservers.vapus.agent.util.BashHelper;
import be.sizingservers.vapus.agent.Agent;

/**
 *
*
 */
public class ProcAgent extends Agent {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BashHelper.setAllowStopProcessGracefully(true);
        Agent.main(args, new ProcServer(), new ProcMonitor());
    }
}
