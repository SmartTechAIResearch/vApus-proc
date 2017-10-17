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

/**
 *
 * @author dieter
 */
public class SwapUsage {

    private static Long[] prevRawValues;
    private static double page_size_in_kB;

    private enum lbls {
        in, out
    }

    private SwapUsage() {
    }

    public static void init() throws IOException {
        SwapUsage.prevRawValues = getRawValues();

        page_size_in_kB = Double.parseDouble(BashHelper.getOutput("getconf PAGE_SIZE")) / 1024;
    }

    public static void addTo(Entity entity) throws IOException {
        Double[] calculatedValues = calculate();

        CounterInfo ci = new CounterInfo("swap (kB)");

        for (int i = 0; i != SwapUsage.lbls.values().length; i++) {
            String lbl = SwapUsage.lbls.values()[i].name();
            ci.getSubs().add(new CounterInfo(lbl, calculatedValues[i]));
        }

        entity.getSubs().add(ci);
    }

    /**
     *
     * @return kB calculated for swap in and swap out.
     */
    private static Double[] calculate() throws IOException {
        Double[] values = new Double[SwapUsage.prevRawValues.length];
        Long[] rawValues = getRawValues();

        //in, out
        for (int i = 0; i != rawValues.length; i++) {
            values[i] = ((double) (rawValues[i] - SwapUsage.prevRawValues[i])) * SwapUsage.page_size_in_kB;
        }

        SwapUsage.prevRawValues = rawValues;
        return values;
    }

    /**
     *
     * @return [pswpin, pswpout]
     * @throws IOException
     */
    private static Long[] getRawValues() throws IOException {
        String[] arr = BashHelper.getOutput("egrep 'pswpin|pswpout' /proc/vmstat --color=never").split("\\r?\\n");

        /*
        pswpin 0
        pswpout 0
         */
        Long[] rawValues = new Long[arr.length];
        for (int i = 0; i != arr.length; i++) {
            rawValues[i] = Long.parseLong(arr[i].split(" +")[1]);
        }

        return rawValues;
    }
}
