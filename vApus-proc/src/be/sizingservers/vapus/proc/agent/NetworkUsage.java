/*
 * 2014 Sizing Servers Lab, affiliated with IT bachelor degree NMCT
 * University College of West-Flanders, Department GKG (www.sizingservers.be, www.nmct.be, www.howest.be/en) 
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

    /**
     * 
     * @param entity
     * @throws IOException 
     */
    public static void addTo(Entity entity) throws IOException {
        HashMap<String, Double[]> calculatedValues = calculate();

        for (int i = 0; i != NetworkUsage.lbls.values().length; i++) {
            String lbl = NetworkUsage.lbls.values()[i].name();
            CounterInfo ci = new CounterInfo("network." + lbl + " (kB)");

            // Pivot
            for (Map.Entry<String, Double[]> entry : calculatedValues.entrySet()) {
                ci.getSubs().add(new CounterInfo(entry.getKey(), entry.getValue()[i]));
            }

            entity.getSubs().add(ci);
        }
    }
    
    /**
     *
     * @return Calculated rx and tx in kB for all nic instances.
     * @throws IOException
     */
    private static HashMap<String, Double[]> calculate() throws IOException {
        HashMap<String, Double[]> calculatedValues = new HashMap<String, Double[]>();

        HashMap<String, Long[]> rawValues = getRawValues();

        int i = 0;
        for (Map.Entry<String, Long[]> entry : rawValues.entrySet()) {
            String name = entry.getKey();

            Long[] prevRaw = NetworkUsage.prevRawValues.get(name);
            Long[] raw = entry.getValue();

            calculatedValues.put(name, calculate(prevRaw, raw));
        }

        NetworkUsage.prevRawValues = rawValues;

        return calculatedValues;
    }

    /**
     *
     * @param prevRaw
     * @param raw
     * @return Kb calculated for sectorsRead, sectorsWritten disk usage.
     */
    private static Double[] calculate(Long[] prevRaw, Long[] raw) {
        Double[] values = new Double[prevRaw.length];
              
        //rx, tx
        for (int i = 0; i != raw.length; i++) {
            values[i] = ((double) (raw[i] - prevRaw[i])) / 1024;
        }

        return values;
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
