#!/bin/bash
set -e

mkdir -p /usr/local/hadoop/hdfs/namenode
chmod -R 777 /usr/local/hadoop/hdfs/namenode

mkdir -p /data
chmod -R 777 /data

mkdir -p /data/allowed-products
chmod -R 777 /data/allowed-products

mkdir -p /data/client-requests
chmod -R 777 /data/client-requests

hdfs namenode -format -force -nonInteractive
exec hdfs namenode
