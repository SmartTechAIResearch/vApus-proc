An agent that is written in Java that collects counters from dstat.  
Extends from the vApus-agent Netbeans packages and vApus-vApus-agent communication protocol compliant. See the vApus-agent readme.

Available counters:

* **cpu**. *guest (%) guest_nice (%) idle (%) iowait (%) irq (%) nice (%) softirq (%) steal (%) system (%) user (%)* / instances and total
* **disk**. *read (kB) write (kB)* / instances
* **memory (kB)** / *buffers cached free used*
* **network**. *rx (kB) .tx (kB)* / instances
* **swap (kB)** / *in out*

avgqu-sz (iostat) pending, maybe --> difficult to calculate.