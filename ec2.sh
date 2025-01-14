#!/bin/bash

sudo yum update -y
sudo yum install docker
sudo systemctl enable docker.service
sudo systemctl start docker.service
sudo usermod -a -G docker ec2-user

sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose version

curl -O https://github.com/TikTzuki/nio-lab/blob/feature/develop/docker-compose.yaml
docker-compose up -d nio-server