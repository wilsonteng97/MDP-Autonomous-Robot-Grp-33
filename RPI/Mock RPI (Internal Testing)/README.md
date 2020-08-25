# Image Processing Server 
Python version: 3.7.2

## Prerequisites
Ensure VirtualEnv is Installed : `pip install virtualenv`

## Set Up
- change directory to 'ImageProcessingServer': `cd ImageProcessingServer`
- create a new virtual environment: `virtualenv env`(or `virtualenv venv --python=python3.7.2` if your python version is different)
- activate the virtual environment: `env\bin\activate`
- update pip and setuptools to latest version: `python -m pip install --upgrade pip setuptools`
- install dependencies: `pip install -r requirements.txt`
- once done, you can deactivate the virtual environment: `env\bin\deactivate`

## Initializing the Server
- change directory to 'ImageProcessingServer\app': cd `ImageProcessingServer\app`
- run the server: `python main.py` (or `python -m main`)
- once done, you can deactivate the virtual environment: `env\bin\deactivate`
