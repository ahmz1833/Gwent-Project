#!/bin/bash

# Define variables
KEY_FILE="$HOME/.ssh/apgrp10_key"
PASSPHRASE="14032024apgrp10"
REMOTE_USER="ubuntu"
REMOTE_IP="37.152.178.57"
SCREEN_SESSION_NAME="gwent-server"

MAIN_CLASS="org.apgrp10.gwent.server.ServerMain"
LIB_DIR="../gwent-common/target"
REMOTE_DIR="/home/ubuntu/gwent"

JAR_NAMES=($(find "$LIB_DIR" -name "*.jar" -exec basename {} \;))
CLASS_PATH=""
LOCAL_FILES=""
for jar in "${JAR_NAMES[@]}"; do
  CLASS_PATH="$CLASS_PATH$jar:"
  LOCAL_FILES="$LOCAL_FILES$LIB_DIR/$jar "
done
LOCAL_FILES=$LOCAL_FILES"../gwent-server/target/gwent-server-1.0-SNAPSHOT.jar"
CLASS_PATH=$CLASS_PATH"gwent-server-1.0-SNAPSHOT.jar"

echo
echo "Uploading files to the server..."
echo

eval $(ssh-agent -s) > /dev/null
sshpass -P assphrase -p $PASSPHRASE ssh-add "$KEY_FILE" > /dev/null

# First, kill all java processes on the server
( ssh $REMOTE_USER@$REMOTE_IP "pkill java"; \

# Use rsync to upload the jar file to the server
rsync -avz -e "ssh" --progress $LOCAL_FILES $REMOTE_USER@$REMOTE_IP:$REMOTE_DIR/ && \

clear

# Run java in a screen session
ssh -t $REMOTE_USER@$REMOTE_IP "
  if screen -list | grep -q \"$SCREEN_SESSION_NAME\"; then
    screen -S $SCREEN_SESSION_NAME -X quit; # Quit existing session
  fi
  echo \"#!/bin/bash\" > $REMOTE_DIR/run.sh
  echo \"clear\" >> $REMOTE_DIR/run.sh
  echo \"echo Starting the server... Press Ctrl-A + D for detaching the screen\" >> $REMOTE_DIR/run.sh
  echo \"echo\" >> $REMOTE_DIR/run.sh
  echo \"echo\" >> $REMOTE_DIR/run.sh
  echo \"cd '$REMOTE_DIR' && java -cp '$CLASS_PATH' $MAIN_CLASS\" >> $REMOTE_DIR/run.sh
  echo \"echo\" >> $REMOTE_DIR/run.sh
  echo \"echo\" >> $REMOTE_DIR/run.sh
  echo \"read -p 'Press [Enter] key to exit...'\" >> $REMOTE_DIR/run.sh
  chmod +x $REMOTE_DIR/run.sh
  screen -h 10000 -S $SCREEN_SESSION_NAME $REMOTE_DIR/run.sh
  " )