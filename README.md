An agent that is written in Java that collects counters from the proc folders.  
Extends from the vApus-agent Netbeans packages and vApus-vApus-agent communication protocol compliant. See the vApus-agent readme.

The first counters received is never correct and should be discarded.

Available counters (in comments quotes from the sources further below and mentions of implementation in the monitor agent):

* **cpu**. *idle (%) iowait (%) irq (%) nice (%) softirq (%) steal (%) system (%) user (%)* / instances and total

      /proc/stat
      
      Various pieces   of  information about  kernel activity  are  available in the
      /proc/stat file.  All  of  the numbers reported  in  this file are  aggregates
      since the system first booted.
      
      - user: normal processes executing in user mode
      - nice: niced (low priority) processes executing in user mode
      - system: processes executing in kernel mode
      - idle: twiddling thumbs
      - iowait: In a word, iowait stands for waiting for I/O to complete.
        But there are several problems:
      
        1. Cpu will not wait for I/O to complete, iowait is the time that a task is waiting for I/O to complete.
        When cpu goes into idle state for outstanding task io, another task will be scheduled on this CPU.
        2. In a multi-core CPU, the task waiting for I/O to complete is not running on any CPU, so the iowait of each CPU is difficult to calculate.
        3. The value of iowait field in /proc/stat will decrease in certain conditions.
        So, the iowait is not reliable by reading from /proc/stat.
      
      - irq: servicing interrupts
      - softirq: servicing softirqs
      - steal: involuntary wait
      (- guest: running a normal guest (time spent running a virtual CPU)
      - guest_nice: running a niced guest)
      
      Last two are included in user and nice and are thus dicarded.

* **disk**. *read (kB) write (kB) average_queue_size* / instances

      /proc/diskstats
      
      All fields except field 9 are cumulative since boot.
      
      - major number
      - minor number
      - device name
      - reads completed successfully
      - reads merged
      - **sectors read**
      - time spent reading (ms)
      - writes completed
      - writes merged
      - **sectors written**
      - time spent writing (ms)
      - I/Os currently in progress
      - time spent doing I/Os (ms)
      - **weighted time spent doing I/Os (ms)**
      
      cat /sys/block/<disk, e.g. sda>/queue/hw_sector_size --> bytes per sector.
      Needed for translating sectors read / written to kB.
      
      avgqu-sz is average concurrency overall during the interval.
      It is computed from the last field in the file—the weighted number of milliseconds 
      spent doing I/Os—divided by the milliseconds elapsed.
      As per Little’s Law, the units cancel out and you just get the average number of 
      operations in progress during the time period, which is a good measure of load or 
      backlog.
      The name, short for “average queue size”, is misleading. This value does not show
      how many operations were queued but not yet being serviced—it shows how many were 
      either in the queue waiting, or being serviced. The exact wording of the kernel
      documentation is
      “…as requests are given to appropriate struct request_queue and decremented as they 
      finish.”

* **memory (kB)** / *buffers cached free used*

      /proc/meminfo
      
      MemTotal: Total usable ram (i.e. physical ram minus a few reserved bits and the kernel binary code)
      MemFree:  The sum of LowFree+HighFree
      Buffers:  Relatively temporary storage for raw disk blocks shouldn't get tremendously large (20MB or so)
      Cached:  in-memory cache for files read from the disk (the pagecache).  Doesn't include SwapCached

* **network**. *rx (kB) .tx (kB)* / instances
      
      /proc/net/dev 
      
      Inter-|**Receive                                                   |[... 
       face |bytes**    packets errs drop fifo frame compressed multicast|[... 
            lo:  908188   5596     0    0    0     0          0         0 [...         
          ppp0:15475140  20721   410    0    0   410          0         0 [...  
          eth0:  614530   7085     0    0    0     0          0         1 [... 
   
      ...] **Transmit 
      ...] bytes**    packets errs drop fifo colls carrier compressed 
      ...]  908188     5596    0    0    0     0       0          0 
      ...] 1375103    17405    0    0    0     0       0          0 
      ...] 1703981     5535    0    0    0     3       0          0 
      
      lo device included in the monitor: Might be interesting to see traffic between two services on the same machine.

     
* **swap (kB)** / *in out*

      /proc/vmstat
      
      Number of swapins and swapouts (since the last boot):
      pswpin 2473
      pswpout 2995

<http://man7.org/linux/man-pages/man5/proc.5.html>  
<https://www.kernel.org/doc/Documentation/filesystems/proc.txt>  
<https://www.kernel.org/doc/Documentation/ABI/testing/procfs-diskstats>  
<https://www.kernel.org/doc/Documentation/iostats.txt>  
<http://linuxinsight.com/proc_vmstat.html>  
<https://www.xaprb.com/blog/2010/01/09/how-linux-iostat-computes-its-results/>
<https://en.wikipedia.org/wiki/Little%27s_law>