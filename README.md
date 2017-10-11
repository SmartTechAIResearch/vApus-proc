An agent that is written in Java that collects counters from dstat.  
Extends from the vApus-agent Netbeans packages and vApus-vApus-agent communication protocol compliant. See the vApus-agent readme.

Available counters:

* cpu#/ hiq idl siq sys usr wai
* dsk/<label>/ read writ avgqu-sz
* memory usage/ buff cach free used
* net/<label>/ recv send
* paging/ in out
* procs/ blk new run
* system/ csw int
* total cpu usage/ hiq idl siq sys usr wai