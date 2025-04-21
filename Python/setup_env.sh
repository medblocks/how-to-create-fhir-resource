#!/bin/bash

# chmod +x setup_env.sh  <- run this command if you are getting permission denied

# Create virtual environment
python3 -m venv venv

# Activate virtual environment
source venv/bin/activate

# Upgrade pip
pip install --upgrade pip

# Install dependencies
pip install -r requirements.txt
