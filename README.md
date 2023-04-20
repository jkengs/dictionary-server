# Dictionary Server
A dictionary server that is built using a thread-per-request  and client/server architecture. It allows concurrent users to query, add, remove and update words from a dictionary JSON file.
## Prerequisites

## Build
`mvn clean compile assembly:single`

## Usage
 
Server: `java -jar <jar-file> <port> <dictionary-file>` 
<br></br>
Client: `java -jar <jar-file> <host-address> <port>`


<br><br/>
 > Note: This is a copy of the original private repo, with all sensitive info removed.
