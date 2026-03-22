#!/bin/bash
set -e

mkdir -p /usr/local/hadoop/hdfs/namenode
chmod -R 777 /usr/local/hadoop/hdfs/namenode

chmod -R 777 /hdfs/datanode

hdfs namenode -format -force -nonInteractive
exec hdfs datanode
