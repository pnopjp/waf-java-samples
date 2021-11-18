#!/bin/bash
(cd ./frontend-webapp && mvn clean package)
(cd ./backend-function && mvn clean package)

