###A detailed instruction with pictures can be found in the report PDF "Installation" part

###PLEASE READ BEFORE RUNNING THE CODE
#####In “app.py” file, there are three string variables in the beginning (url_read, id_head, file_path). These 3 variables need to be adjusted to match the Solr configuration in your local machine:

**url_head**:
    
    the format should be http://localhost:8983/solr/<core name>/select?q=
    If your Solr port number is not 8983, you need to replace it with the port number you used.

**id_head**:

this is the path to the crawl folder
all crawled pages should be in this folder

**file_path**:

this is the path of url-to-html.csv file, I have already put the file I used  "URLtoHTML_nytimes_news.csv" in this folder. You can replace it with your csv file.
		
-----------
		
		
Before running the program, make sure Python (>=3.6) and the latest pip are installed.

Reference on running the Flask project: https://flask.palletsprojects.com/en/2.0.x/installation/


-----------

###On Mac OS

First, make sure python (>=3.6) and pip has been installed in the computer.

After install python, the procedure is the same as in Ubuntu System (See step 2- 5 in “On Ubuntu System”)

###On Windows

Since Solr/post method is not directly supported in Windows, it is not recommended to running the programming in a Windows System.

First, make sure python( >=3.6) and pip has been installed in the computer.

Type the following two commends to activate the environment
    
    py -3 -m venv venv
    venv\Scripts\activate

Type the following two commends to install required packages
    
    pip install Flask
    pip install requests
    
Type “python3 app.py” to run the program

###On Ubuntu System

This program has been tested on Ubuntu 16.04.7, 18.04.6 and 20.04.3 installed in a VirtualBox.

Reference on installing Python3.8 on Ubuntu 16.04: https://gist.github.com/ptantiku/aca8d955296d5dee01bd9ed1c3027d8c

**Step1**: In the terminal, type the following commands to install python3 and pip:
	
	Ubuntu 18.04.6 and 20.04.3:
	    sudo apt update
	    sudo apt install python3-pip
	    sudo apt install python3 (might not be required if python3.6 or python3.8 is already installed)
	    sudo apt install python3-venv

	Ubuntu 16.04.7:
        sudo apt update
        sudo apt install python3-pip
        # Do the following steps instead to install python3
        
        # install PPA
        sudo add-apt-repository ppa:deadsnakes/ppa
        
        # update and install
        sudo apt update
        sudo apt install python3.8 python3.8-dev python3.8-venv
        
        # setup alternatives
        sudo update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.5 1
        sudo update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.8 2
        
        # show menu for selecting the version
        sudo update-alternatives --config python3
        
        
**Step2**: Enter the project root folder, then type the command “python3 -m venv venv”

**Step3**: type “. venv/bin/activate” to activate the environment (There is a space between “.” and “venv/bin/activate”)
**Step4**: type the following two commends to install required packages
	
	pip install Flask
	pip install requests
**Step5**: Type “python3 app.py” to run the program, and open http://127.0.0.1:5000/ in a web browser


