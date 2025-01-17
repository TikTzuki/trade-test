#!/bin/bash

sudo yum update -y
sudo yum install docker -y
sudo systemctl enable docker.service
sudo systemctl start docker.service
sudo usermod -a -G docker ec2-user
sudo chmod 777 /var/run//docker.sock

sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-coo
mpose
sudo chmod +x /usr/local/bin/docker-compose
/usr/local/bin/docker-compose version

curl -O https://raw.githubusercontent.com/TikTzuki/nio-lab/refs/heads/develop/docker-compose.yaml

export GHP_TOKEN=<YOUR_TOKEN>
export CONFIG_REPO=https://x-access-token:$GHP_TOKEN@raw.githubusercontent.com/TikTzuki/config-repos/refs/heads/master/nio-lab/server

echo "repo: " $CONFIG_REPO;

mkdir -p ./nio-server/config/
mkdir -p ./nio-server/.aws
curl -O $CONFIG_REPO/application.yaml --output-dir ./nio-server/config
curl -O $CONFIG_REPO/keyspaces-application.conf --output-dir ./nio-server/config
curl -O $CONFIG_REPO/cassandra_truststore.jks --output-dir ./nio-server/config
curl -O $CONFIG_REPO/credentials --output-dir ./nio-server/.aws

/usr/local/bin/docker-compose up -d nio-server-<ARM/None>