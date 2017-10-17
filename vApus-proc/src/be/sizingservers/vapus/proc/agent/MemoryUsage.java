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

/**
 *
 * @author dieter
 */
public class MemoryUsage {

    private enum lbls {
        memTotal, memFree, buffers, cached
    }

    private MemoryUsage() {
    }

    public static void addTo(Entity entity) throws IOException {
        entity.getSubs().add(get(getRawValues()));
    }

    private static CounterInfo get(Long[] raw) {
        CounterInfo ci = new CounterInfo("memory (kB)");

        long buff = raw[lbls.buffers.ordinal()];
        long cach = raw[lbls.cached.ordinal()];
        long free = raw[lbls.memFree.ordinal()];
        long used = raw[lbls.memTotal.ordinal()] - buff - cach - free;

        ci.getSubs().add(new CounterInfo("buffers", buff));
        ci.getSubs().add(new CounterInfo("cached", cach));
        ci.getSubs().add(new CounterInfo("free", free));
        ci.getSubs().add(new CounterInfo("used", used));

        return ci;
    }

    /**
     *
     * @return [MemTotal, MemFree, Buffers, Cached]
     * @throws IOException
     */
    private static Long[] getRawValues() throws IOException {
        String[] arr = BashHelper.getOutput("egrep 'Mem|Buffers|Cached' /proc/meminfo | egrep -v 'Swap|Available' --color=never").split("\\r?\\n");

        /*
        MemTotal:        4028204 kB
        MemFree:          251144 kB
        Buffers:          247484 kB
        Cached:          1655524 kB
         */
        Long[] rawValues = new Long[arr.length];
        for (int i = 0; i != arr.length; i++) {
            rawValues[i] = Long.parseLong(arr[i].split(" +")[1]);
        }

        return rawValues;
    }
}
