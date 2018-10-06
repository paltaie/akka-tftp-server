#!/bin/bash
java -Dakka.remote.netty.tcp.port=0 -jar target/akka-tftp-server-1.0-SNAPSHOT-allinone.jar
