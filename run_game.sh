#!/bin/sh

set -e

javac -cp .:ColorfightII.jar skynet.java
java -cp .:ColorfightII.jar skynet
