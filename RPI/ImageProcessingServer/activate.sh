#!/bin/bash
# Shell Script to activate/deactive virtual environment and to start Image Processing Service

if [ $# -eq 0 ]; then
    echo "Please provided an argument"
    exit 1
fi


#Activate virtualenv
source "env/Scripts/activate"
python app/main.py
read
#Deactivate virtualenv
#source "venv\Scripts\deactivate"

