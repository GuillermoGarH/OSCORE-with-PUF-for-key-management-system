# PUFwithOSCORE
Implementation of the protocol OSCORE with Java via Sockets, integrating the key managment system with a Physical Unclonable Funcition

Used for the clients a Raspberry Pi 3 Model B with a SRAM 23LC1024

For the server part of all codes the path used should work in any Ubuntu OS, double check either way, for Client ***YOU MUST*** change the paths for your own to make it work. 
Was distributed like this for organization reasons.
***YOU MUST*** also change the IP to your Server device IP. 

The process of execution is:

- Generation of the PUF
  **Both in Raspberry Pi:**
  
    python puf.py
    
    python SerialNumber.py

  For this two you can just execute the bash file as ./puf.sh, is faster.

- Enrolmment and Authentication If using the java files, use eclipse, create Maven project, and drop the both ClientEA and ServerEA to src/main/java and Otros archivos to resource. POM is also provided
  - **New Device**

  *Enrollment*

  **In Ubuntu:**
    java -jar ServerEA.java

  **In Raspbery Pi:**
    java -jar ClientEA.java

  *Authentication*

  **In Ubuntu:**
    java -jar ServerEA.java

  **In Raspbery Pi:**
    java -jar ClientEA.java

  - **Device already enrolled**

  *Authentication*
  
  **In Ubuntu:**
    java -jar ServerEA.java

  **In Raspbery Pi:**
    java -jar ClientEA.java

- **OSCORE Communications** only provided my codes, for testing with the .java you must first download the Californium repository. Original POM modified

  **In Ubuntu:**
    java -jar ServidorOSCORE.java

  **In Raspbery Pi:**
    java -jar ClienteOSCORE.java


