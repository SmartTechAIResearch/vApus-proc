/*
 * Copyright 2014 (c) Sizing Servers Lab
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

/**
 *
 * @author dieter
 */
public class NetworkUsage {

    /*
    Inter-|   Receive                                                |  Transmit
 face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
 ens33: 193092449  133097    0    0    0     0          0         0  3353378   52383    0    0    0     0       0          0
    lo:   11840     160    0    0    0     0          0         0    11840     160    0    0    0     0       0          0
     */

    private enum lbls {
        rx, tx
    }

    //key = nic label, value = [ receiveBytes, transmitBytes  ]
    private static HashMap<String, Long[]> prevRawValues;

    private NetworkUsage() {
    }

    public static void init() throws IOException {
        NetworkUsage.prevRawValues = getRawValues();
    }

    public static void addTo(Entity entity) throws IOException {
        HashMap<String, Long[]> rawValues = getRawValues();

        CounterInfo ci = new CounterInfo("network");

        for (HashMap.Entry<String, Long[]> entry : rawValues.entrySet()) {
            String name = entry.getKey();

            Long[] prevRaw = NetworkUsage.prevRawValues.get(name);
            Long[] raw = entry.getValue();

            ci.getSubs().add(get(name, prevRaw, raw));
        }

        entity.getSubs().add(ci);

        NetworkUsage.prevRawValues = rawValues;
    }

    private static CounterInfo get(String name, Long[] prevRaw, Long[] raw) {
        CounterInfo ci = new CounterInfo(name);

        //tx, rx
        for (int i = 0; i != raw.length; i++) {
            ci.getSubs().add(new CounterInfo(NetworkUsage.lbls.values()[i].name() + " (kB)", ((double) (raw[i] - prevRaw[i])) / 1024));
        }

        return ci;
    }

    private static HashMap<String, Long[]> getRawValues() throws IOException {
        try {
            String[] arr = BashHelper.getOutput("cat /proc/net/dev | awk '{print $1, $2, $10}'").split("\\r?\\n");

            HashMap<String, Long[]> rawValues = new HashMap<String, Long[]>(arr.length);
            for (int i = 2; i != arr.length; i++) { //First two rows are headers
                addOrUpdateValue(rawValues, arr[i]);
            }

            return rawValues;
        } catch (IOException ex) {
            throw new IOException("Could not get network values from /proc/net/dev: " + ex);
        }
    }

    private static void addOrUpdateValue(HashMap<String, Long[]> values, String line) {
        String[] row = line.split(" +"); //Split on any number of spaces.

        Long[] arr = new Long[row.length - 1];
        for (int i = 1; i != row.length; i++) {
            arr[i - 1] = Long.parseLong(row[i]);
        }

        String label = row[0];
        label = label.substring(0, label.length() - 1); //strip :

        values.put(label, arr);
    }
}
