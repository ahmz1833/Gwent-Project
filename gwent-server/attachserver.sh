#!/bin/bash
KEY_FILE="$HOME/.ssh/apgrp10_key"
PASSPHRASE="14032024apgrp10"
REMOTE_USER="ubuntu"
REMOTE_IP="37.152.181.45"
SCREEN_SESSION_NAME="gwent-server"
sshpass -P assphrase -p $PASSPHRASE ssh -t -i $KEY_FILE $REMOTE_USER@$REMOTE_IP "screen -r $SCREEN_SESSION_NAME"