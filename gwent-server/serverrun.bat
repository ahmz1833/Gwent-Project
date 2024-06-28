@echo off
cd .. && mvn clean package assembly:single -pl gwent-common,gwent-server && cd gwent-server && wsl ./_runinserver.sh