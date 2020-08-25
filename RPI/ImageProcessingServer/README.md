# Image Processing Server 

## Prerequisites
###  Ensure VirtualEnv is installed 
`pip install virtualen`
## Set Up

- change directory to 'image_processing_server': `cd ImageProcessingServer`
- create a new virtual environment named 'venv' in the 'image_processing_server' directory: `python -m venv venv` (or `python3 -m venv venv --python=python3.7.4` if your python version is different)
- activate the virtual environment: `source venv/bin/activate`
  - for Windows (inclusive of quotes): `"C:\...\image_processing_server\venv\Scripts\activate"`
- update pip and setuptools to latest version: `python -m pip install --upgrade pip setuptools`
- install dependencies: `pip install -r requirements.txt`
- once done, you can deactivate the virtual environment: `deactivate`
  - for Windows (inclusive of quotes): `"C:\...\image_processing_server\venv\Scripts\deactivate"`
