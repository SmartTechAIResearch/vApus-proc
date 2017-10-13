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

/**
 *
 * @author dieter
 */
public class DiskUsage {

    /*majorNumber, minorNumber, deviceName, readsCompletedSuccesfully, readsMerged,
        sectorsRead, timeSpentReadingInMs, writesCompletedSuccesfully, writesMerged,
        sectorsWritten, timeSpentWritingInMs, iosCurrentlyInProgress, timeSpentDoingIOsInMs,
        weightedTimeSpentDoingIOsInMs  
     */
    private enum lbls {
        read, write
    }       
    
    //key = disk / partition label, value = [ sectorsRead, sectorsWritten  ]
    private static HashMap<String, Long[]> prevRawValues;
    private static int[] bytesPerSector;   

    private DiskUsage(){
    }
    
    public static void init() throws IOException {
        DiskUsage.prevRawValues = getRawValues();
        DiskUsage.bytesPerSector = new int[DiskUsage.prevRawValues.size()];  
        
        int i = 0;
        int bytesPSector = 512; //default
        for (HashMap.Entry<String, Long[]> entry : prevRawValues.entrySet()) {
            //Partitions will fail (e.g. sda1), because a folder does not exists. We assume it has the same sector size as the previous dev (the disk, e.g. sda, if it is a partition).
            String output = BashHelper.getOutput("cat /sys/block/" + entry.getKey() + "/queue/hw_sector_size");
        
            if(!output.startsWith("cat")){ //error starts with cat.
                bytesPSector = Integer.parseInt(output);
            }
            
            DiskUsage.bytesPerSector[i++] = bytesPSector;
        }
    }
    
    public static void addTo(Entity entity) throws IOException {
        HashMap<String, Long[]> rawValues = getRawValues();
        
        CounterInfo ci = new CounterInfo("disk");

        int i = 0;
        for (HashMap.Entry<String, Long[]> entry : rawValues.entrySet()) {
            String name = entry.getKey();

            Long[] prevRaw = DiskUsage.prevRawValues.get(name);
            Long[] raw = entry.getValue();

            ci.getSubs().add(get(name, prevRaw, raw, DiskUsage.bytesPerSector[i++]));
        }

        entity.getSubs().add(ci);
        
        DiskUsage.prevRawValues = rawValues;
    }

    private static CounterInfo get(String name, Long[] prevRaw, Long[] raw, int bytesPSector) {
        CounterInfo ci = new CounterInfo(name);
                 
        for (int i = 0; i != raw.length; i++) {
            ci.getSubs().add(new CounterInfo(DiskUsage.lbls.values()[i].name() + " (kB)", ((double) (raw[i] - prevRaw[i])) / (bytesPSector * 1024 )));
        }

        return ci;
    }

    private static HashMap<String, Long[]> getRawValues() throws IOException {
        try {
            //device name, sectors read, sectors written
            String[] arr = BashHelper.getOutput("grep -v loop /proc/diskstats --color=never | awk '{print $3, $6, $10}'").split("\\r?\\n");

            HashMap<String, Long[]> rawValues = new HashMap<String, Long[]>(arr.length);
            for (int i = 0; i != arr.length; i++) {
                addOrUpdateValue(rawValues, arr[i]);
            }

            return rawValues;
        } catch (IOException ex) {
            throw new IOException("Could not get disk values from /proc/diskstats: " + ex);
        }
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
