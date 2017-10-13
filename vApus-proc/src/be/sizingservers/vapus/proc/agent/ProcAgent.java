/*
 * Copyright 2014 (c) Sizing Servers Lab
 * University College of West-Flanders, Department GKG * 
 * Author(s):
 * 	Dieter Vandroemme
 */
package be.sizingservers.vapus.proc.agent;

import be.sizingservers.vapus.agent.util.BashHelper;
import be.sizingservers.vapus.agent.Agent;

/**
 *
 * @author didjeeh
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
