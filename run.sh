#!/bin/bash

# run - this script enters a infinite loop to ensure that
# the socket server does not go down
#
# 7/14/18 Jeffery Russell

while true
do java -cp SteamFriendsGraph-0.1-jar-with-dependencies.jar net.jrtechs.www.App
done