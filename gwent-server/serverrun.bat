@echo off
cd .. && mvn clean package -pl gwent-common,gwent-server && cd gwent-server && wsl ./_runinserver.sh