/*
 * Copyright 2017 (c) Sizing Servers Lab
 * University College of West-Flanders, Department GKG * 
 * 	
 * Author(s):
 * 	Dieter Vandroemme
 */
package be.sizingservers.vapus.proc.agent;

import be.sizingservers.vapus.agent.Agent;
import be.sizingservers.vapus.agent.util.BashHelper;
import be.sizingservers.vapus.agent.util.CounterInfo;
import be.sizingservers.vapus.agent.util.Entity;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 *
 * @author dieter
 */
public class CpuUsage {
          
    private enum lbls{
        user, nice, system, idle, iowait, irq, softirq, steal, guest, guest_nice
    }

    //key = cpu label, value = [user, nice, system, idle, iowait, irq, softirq, steal, guest, guest_nice  ]
    private static HashMap<String, Long[]> prevRawValues = new HashMap<String, Long[]>();

    static void init() {
        try {            
            prevRawValues = getRawValues();
        } catch (IOException ex) {
            Agent.getLogger().log(Level.SEVERE, "Could not get cpu values from /proc/stat: {0}", ex);
        }
    }

    private CpuUsage() {
    }

    public static void addTo(Entity entity) throws IOException {
        HashMap<String, Long[]> rawValues = getRawValues();

        
        for (HashMap.Entry<String, Long[]> entry : rawValues.entrySet()) {
            String name = entry.getKey();

            Long[] prevRaw = CpuUsage.prevRawValues.get(name);
            Long[] raw = entry.getValue();
            

            entity.getSubs().add(get(name, prevRaw, raw));
        }
        
        CpuUsage.prevRawValues = rawValues;
    }

    private static CounterInfo get(String name, Long[] prevRaw, Long[] raw) {
        CounterInfo ci = new CounterInfo(name);
                       
        long prevTotal = prevRaw[lbls.user.ordinal()] + prevRaw[lbls.nice.ordinal()] + prevRaw[lbls.system.ordinal()]
                + prevRaw[lbls.idle.ordinal()] + prevRaw[lbls.iowait.ordinal()] + prevRaw[lbls.irq.ordinal()]
                + prevRaw[lbls.softirq.ordinal()] + prevRaw[lbls.steal.ordinal()];
        
        long total = raw[lbls.user.ordinal()] + raw[lbls.nice.ordinal()] + raw[lbls.system.ordinal()]
                + raw[lbls.idle.ordinal()] + raw[lbls.iowait.ordinal()] + raw[lbls.irq.ordinal()]
                + raw[lbls.softirq.ordinal()] + raw[lbls.steal.ordinal()];
        
        long totald = total - prevTotal;
        //user, nice, system, idle, iowait, irq, softirq, steal, guest, guest_nice   
        for(int i = 0; i != raw.length; i++){
            ci.getSubs().add(new CounterInfo(lbls.values()[i].name(), (((double)(raw[i] - prevRaw[i])) / totald) * 100));
        }
                
        return ci;
    }

    private static HashMap<String, Long[]> getRawValues() throws IOException {
        String[] arr = BashHelper.getOutput("grep cpu /proc/stat --color=never").split("\\r?\\n");

        HashMap<String, Long[]> rawValues = new HashMap<String, Long[]>(arr.length);
        for (int i = 0; i != arr.length; i++) {
            addOrUpdateValue(rawValues, arr[i]);
        }

        return rawValues;
    }

    private static void addOrUpdateValue(HashMap<String, Long[]> values, String line) {
        String[] row = line.split(" +"); //Split on any number of spaces.

        Long[] arr = new Long[row.length - 1];
        for (int i = 1; i != row.length; i++) {
            arr[i - 1] = Long.parseLong(row[i]);
        }

        values.put(row[0], arr);
    }
}
