#!/bin/bash
cd .. && mvn clean compile && mvn package assembly:single -pl gwent-server && cd gwent-server && ./_runinserver.sh