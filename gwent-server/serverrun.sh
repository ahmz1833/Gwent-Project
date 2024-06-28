#!/bin/bash
cd .. && mvn clean package -pl gwent-common,gwent-server && cd gwent-server && ./_runinserver.sh