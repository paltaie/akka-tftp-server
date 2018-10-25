#`akka-tftp-server`

## Background
This is an Akka implementation of a TFTP server as specified by [RFC 1350](https://tools.ietf.org/html/rfc1350), initially
created for [this blog entry](https://iconsolutions.com/blog/reactive-systems-lets-write-tftp-server-with-akka/), and later
presented at Reactive Summit 2018.

TFTP is a lockstep protocol. This means that receipt of a message is the trigger for sending of the next message. This
is the reason why it's _trivial_, because it doesn't have the concept of state: receiving a message triggers you to
send the next one, and so on until one of you is done sending messages!

## Design goals/motivation
TFTP is a "fast data system" before fast data was even a concept! It's pull-based, so the client signals demand to the
server for the next piece of the requested file. This lent itself nicely to a demonstration of two Akka features:
clustering and distributed data.

## Architecture
Here's the architecture of `akka-tftp-server`:
![a](https://i.imgur.com/scBreLs.png)

The `Server` is a cluster singleton: only one instance of it is running per actor system cluster. The reason for this in
this demonstration case is that it binds to a port, and there are multiple instances of the actor system running on the
same machine, so if multiple servers were to start then we'd have a `BindException`. So we create a single server per
cluster.

Obviously in real life you'd be running across multiple machines or even multiple DCs, and you'd be putting all of these
different TFTP servers behind a load balancer, but if you reach that stage with this application then it's perhaps time
to re-evaluate your architecture, and life!

The Server acts as a rudimentary router: depending on the opcode (type) of message received, it routes said message to
one of the relevant actors which are capable of handling that type of message. For example, if the `Server` receives a
read request (RRQ), it forwards it to the `ReadRequestActor`, WRQ to the `WriteRequestActor` and so on.

## Building
Simply run `mvn clean install`. This will create a fat/uberjar with all dependencies.

## Running
The uberjar can be run with the   `node1.sh`, `node2.sh` or `nodeN.sh` scripts. They're super rudimentary!

The difference between the scripts is the bind port for Akka clustering. `node1.sh` runs on the default Akka remoting
defined in `application.conf`. The `node2.sh` script overrides the port to 2552, and `nodeN.sh` sets the port to zero,
which means that Akka will find any free port to bind to instead of a specific one. You can run as many `nodeN.sh`
instances of `akka-tftp-server` as you like, and they'll all join the seed nodes running on 2551 and/or 2552.

## Caveats/limitations
This server currently doesn't offer any security (i.e. you can request any file from the server and it'll happily serve
it!) There's no root where it can be run from like a real life [T]FTP server, so don't run this in production.

It also doesn't implement the Write Request (WRQ). If you feel like you want to implement that, then go ahead!

Finally, it only runs in active/passive: only the node which is running the cluster singleton does any actual work.
We can implement a cluster-aware router to spread the load between other `akka-tftp-server` nodes, and move to an
active/active configuration.