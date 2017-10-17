/*
 * Copyright 2014 (c) Sizing Servers Lab
 * University College of West-Flanders, Department GKG * 
 * Author(s):
 * 	Dieter Vandroemme
 */
package be.sizingservers.vapus.proc.agent;

import be.sizingservers.vapus.agent.Agent;
import be.sizingservers.vapus.agent.Monitor;
import be.sizingservers.vapus.agent.Properties;
import be.sizingservers.vapus.agent.Server;
import be.sizingservers.vapus.agent.util.BashHelper;
import be.sizingservers.vapus.agent.util.CounterInfo;
import be.sizingservers.vapus.agent.util.Directory;
import be.sizingservers.vapus.agent.util.Entities;
import be.sizingservers.vapus.agent.util.Entity;
import be.sizingservers.vapus.agent.util.HostName;
import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;

/**
 *
 * @author didjeeh
 */
public class ProcMonitor extends Monitor {

    private PollProcAndSendCounters pollProcAndSendCounters;

    /**
     * A empty instance only to be used to call getConfig() and getWDYH(). You
     * must do the needed init stuff here.
     */
    public ProcMonitor() {
    }

    /**
     *
     * @param server
     * @param socket
     * @param id
     */
    public ProcMonitor(Server server, Socket socket, long id) {
        super(server, socket, id);
    }

    @Override
    public void setConfig() {
        if (Monitor.config == null) {
            try {
                String dmidecode = BashHelper.getOutput("which dmidecode");
                String gawk = BashHelper.getOutput("wich gawk");
                if (dmidecode.length() == 0) {
                    String error = "dmidecode, needed to get the hw config, was not found.";
                    Monitor.config = error;
                } else if (gawk.length() == 0) {
                    String error = "gawk, needed to get the hw config, was not found.";
                    Monitor.config = error;
                } else {
                    String inxi = Directory.getExecutingDirectory(ProcMonitor.class) + "inxi";
                    BashHelper.runCommand("chmod +x " + inxi);
                    Monitor.config = BashHelper.getOutput("'" + inxi + "' -SCDMNm -xi -c 0").trim() + "\n\n" + BashHelper.getOutput(dmidecode + " -t memory");

                    String[] splitConfig = Monitor.config.split("\\r?\\n");
                    StringBuilder sb = new StringBuilder();
                    sb.append("<lines>");
                    for (int i = 0; i != splitConfig.length; i++) {
                        sb.append("<line>");
                        sb.append(splitConfig[i]);
                        sb.append("</line>");
                    }
                    sb.append("</lines>");

                    Monitor.config = sb.toString();
                }
            } catch (IOException ex) {
                Agent.getLogger().log(Level.SEVERE, "Failed setting config: {0}", ex);
            } catch (URISyntaxException ex) {
                Agent.getLogger().log(Level.SEVERE, "Failed setting config: {0}", ex);
            }
        }
    }

    @Override
    public void setWDYH() {
        if (Monitor.wdyh == null) {
            try {
                CpuUsage.init();
                DiskUsage.init();
                NetworkUsage.init();
                SwapUsage.init();
                
                Thread.sleep(Properties.getSendCountersInterval());

                Monitor.wdyhEntities = getWIH();
                Monitor.wdyh = new Gson().toJson(Monitor.wdyhEntities);
            } catch (IOException ex) {
                Agent.getLogger().log(Level.SEVERE, "Could not set WDYH: {0}", ex);
            } catch (InterruptedException ex) {
                Agent.getLogger().log(Level.SEVERE, "Could not set WDYH: {0}", ex);
            } catch (NumberFormatException ex) {
                Agent.getLogger().log(Level.SEVERE, "Could not set WDYH: {0}", ex);
            }
        }
    }

    public Entities getWIH() throws IOException {
        return getWIH(null);
    }

    public Entities getWIH(Entities wiw) throws IOException {
        Entities wih = new Entities();
        String hostName = HostName.get();
        if (hostName == null) {
            hostName = "Host";
        }

        Entity entity = new Entity(hostName, true);
        wih.getSubs().add(entity);
        if (wiw == null) {

            CpuUsage.addTo(entity);
            DiskUsage.addTo(entity);
            MemoryUsage.addTo(entity);
            NetworkUsage.addTo(entity);
            SwapUsage.addTo(entity);
            return wih;
        }
        
        boolean cpuAdded = false, memoryAdded = false, 
                diskAdded = false, networkAdded = false, swapAdded = false;
        ArrayList<CounterInfo> wiwCounters = wiw.getSubs().get(0).getSubs();
        for(int i = 0 ; i != wiwCounters.size(); i++){
            if(cpuAdded && memoryAdded && diskAdded && networkAdded && swapAdded) {
                break;
            }
            
            if(wiwCounters.get(i).getName().startsWith("cpu") && !cpuAdded){
                cpuAdded = true;
                CpuUsage.addTo(entity);
            }
            else if(wiwCounters.get(i).getName().startsWith("disk") && !diskAdded){
                diskAdded = true;
                DiskUsage.addTo(entity);
            }
            else if(wiwCounters.get(i).getName().startsWith("memory") && !memoryAdded){
                memoryAdded = true;
                MemoryUsage.addTo(entity);
            }
            else if(wiwCounters.get(i).getName().startsWith("network") && !networkAdded){
                networkAdded = true;
                NetworkUsage.addTo(entity);
            }
            else if(wiwCounters.get(i).getName().startsWith("swap") && !swapAdded){
                swapAdded = true;
                SwapUsage.addTo(entity);
            }
        }
        return wih;
    }

    @Override
    public void start() {
        if (super.running) {
            return;
        }
        super.running = true;

        super.poller = new Timer();
        this.pollProcAndSendCounters = new PollProcAndSendCounters(super.getWIWEntities(), this, super.server, super.socket);

        int interval = Properties.getSendCountersInterval();
        super.poller.scheduleAtFixedRate(pollProcAndSendCounters, interval, interval);
    }

    @Override
    public void stop() {
        if (!super.running) {
            return;
        }
        super.running = false;

        super.poller.cancel();
        super.poller.purge();
        super.poller = null;

        this.pollProcAndSendCounters.cancel();
        this.pollProcAndSendCounters = null;
    }
}
