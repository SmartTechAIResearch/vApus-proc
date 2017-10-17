/*
 * Copyright 2017 (c) Sizing Servers Lab
 * University College of West-Flanders, Department GKG * 
 * 	
 * Author(s):
 * 	Dieter Vandroemme
 */
package be.sizingservers.vapus.proc.agent;

import be.sizingservers.vapus.agent.util.BashHelper;
import be.sizingservers.vapus.agent.util.CounterInfo;
import be.sizingservers.vapus.agent.util.Entity;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dieter
 */
public class CpuUsage {

    private enum lbls {
        user, nice, system, idle, iowait, irq, softirq, steal, guest, guest_nice
    }

    //key = cpu label, value = [user, nice, system, idle, iowait, irq, softirq, steal, guest, guest_nice  ]
    private static HashMap<String, Long[]> prevRawValues;

    private CpuUsage() {
    }

    public static void init() throws IOException {
        CpuUsage.prevRawValues = getRawValues();
    }

    /**
     * 
     * @param entity
     * @throws IOException 
     */
    public static void addTo(Entity entity) throws IOException {
        HashMap<String, Double[]> calculatedValues = calculate();

        for (int i = 0; i != lbls.values().length; i++) {
            String lbl = lbls.values()[i].name();
            CounterInfo ci = new CounterInfo("cpu." + lbl + " (%)");

            // Pivot
            for (Map.Entry<String, Double[]> entry : calculatedValues.entrySet()) {
                ci.getSubs().add(new CounterInfo(entry.getKey(), entry.getValue()[i]));
            }

            entity.getSubs().add(ci);
        }
    }

    /**
     *
     * @return Calculated usage percentages for all cpu instances.
     * @throws IOException
     */
    private static HashMap<String, Double[]> calculate() throws IOException {
        HashMap<String, Double[]> calculatedValues = new HashMap<String, Double[]>();

        HashMap<String, Long[]> rawValues = getRawValues();

        for (Map.Entry<String, Long[]> entry : rawValues.entrySet()) {
            String name = entry.getKey();

            Long[] prevRaw = CpuUsage.prevRawValues.get(name);
            Long[] raw = entry.getValue();

            calculatedValues.put(name, calculate(prevRaw, raw));
        }

        CpuUsage.prevRawValues = rawValues;

        return calculatedValues;
    }

    /**
     *
     * @param prevRaw
     * @param raw
     * @return Percentage calculated for user, nice, system, idle, iowait, irq,
     * softirq, steal, guest, guest_nice cpu usage.
     */
    private static Double[] calculate(Long[] prevRaw, Long[] raw) {
        Double[] values = new Double[prevRaw.length];

        long prevTotal = prevRaw[lbls.user.ordinal()] + prevRaw[lbls.nice.ordinal()] + prevRaw[lbls.system.ordinal()]
                + prevRaw[lbls.idle.ordinal()] + prevRaw[lbls.iowait.ordinal()] + prevRaw[lbls.irq.ordinal()]
                + prevRaw[lbls.softirq.ordinal()] + prevRaw[lbls.steal.ordinal()];

        long total = raw[lbls.user.ordinal()] + raw[lbls.nice.ordinal()] + raw[lbls.system.ordinal()]
                + raw[lbls.idle.ordinal()] + raw[lbls.iowait.ordinal()] + raw[lbls.irq.ordinal()]
                + raw[lbls.softirq.ordinal()] + raw[lbls.steal.ordinal()];

        long totald = total - prevTotal;
        //user, nice, system, idle, iowait, irq, softirq, steal, guest, guest_nice   
        for (int i = 0; i != raw.length; i++) {
            values[i] = (((double) (raw[i] - prevRaw[i])) / totald) * 100;
        }

        return values;
    }

    private static HashMap<String, Long[]> getRawValues() throws IOException {
        try {
            String[] arr = BashHelper.getOutput("grep cpu /proc/stat --color=never").split("\\r?\\n");

            HashMap<String, Long[]> rawValues = new HashMap<String, Long[]>(arr.length);
            for (int i = 0; i != arr.length; i++) {
                addOrUpdateValue(rawValues, arr[i]);
            }

            return rawValues;
        } catch (IOException ex) {
            throw new IOException("Could not get cpu values from /proc/stat: " + ex);
        }
    }

    private static void addOrUpdateValue(HashMap<String, Long[]> values, String line) {
        String[] row = line.split(" +"); //Split on any number of spaces.

        Long[] arr = new Long[row.length - 1];
        for (int i = 1; i != row.length; i++) {
            arr[i - 1] = Long.parseLong(row[i]);
        }

        String label = row[0];
        if (label.length() == 3) {
            label = "total";
        } else {
            label = label.substring(3); //strip cpu
        }

        values.put(label, arr);
    }
}
