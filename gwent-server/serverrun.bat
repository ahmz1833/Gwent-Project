@echo off

:: Define variables
set "KEY_FILE=$HOME/.ssh/apgrp10_key"
set "PASSPHRASE=14032024apgrp10"
set "LOCAL_FILE=target/gwent-server-1.0-SNAPSHOT-jar-with-dependencies.jar"
set "REMOTE_USER=ubuntu"
set "REMOTE_IP=37.152.181.45"
set "REMOTE_DIR=/home/ubuntu"
set "REMOTE_FILE=%REMOTE_DIR%/gwent-server.jar"
set "SCREEN_SESSION_NAME=gwent-server"

:: Build the project (and package it into a jar file)
cd .. && mvn clean compile && cd gwent-server && mvn package

:: Call WSL for the following commands
wsl bash -c "(
:: First, kill all java processes on the server
sshpass -p $PASSPHRASE ssh -i "$KEY_FILE" $REMOTE_USER@$REMOTE_IP "pkill java"; \

:: Use rsync to upload the file with progress
sshpass -p $PASSPHRASE rsync -avz -e "ssh -i $KEY_FILE" --progress $LOCAL_FILE $REMOTE_USER@$REMOTE_IP:$REMOTE_FILE && \

:: Run the jar file in a screen session
sshpass -p $PASSPHRASE ssh -t -i $KEY_FILE $REMOTE_USER@$REMOTE_IP '
  if screen -list | grep -q \"$SCREEN_SESSION_NAME\"; then
    screen -S $SCREEN_SESSION_NAME -X quit; # Quit existing session
  fi
  echo \"#!/bin/bash\" > $REMOTE_DIR/run.sh
  echo \"echo Starting the server... Press Ctrl-A + D for detaching the screen\" >> $REMOTE_DIR/run.sh
  echo \"echo\" >> $REMOTE_DIR/run.sh
  echo \"echo\" >> $REMOTE_DIR/run.sh
  echo \"java -jar $REMOTE_FILE\" >> $REMOTE_DIR/run.sh
  echo \"echo\" >> $REMOTE_DIR/run.sh
  echo \"echo\" >> $REMOTE_DIR/run.sh
  echo \"read -p \'Press [Enter] key to exit...\'\" >> $REMOTE_DIR/run.sh
  chmod +x $REMOTE_DIR/run.sh
  screen -h 10000 -S $SCREEN_SESSION_NAME $REMOTE_DIR/run.sh
  '
)"