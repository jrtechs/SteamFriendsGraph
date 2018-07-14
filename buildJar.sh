#!/bin/bash

# Builds a jar file and places it in the target folder.
# For this to work, you need to have maven installed.
#
# 7/14/18 Jeffery Russell

mvn clean compile assembly:single