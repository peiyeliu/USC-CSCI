import os
import glob
from bs4 import BeautifulSoup

'''
CSCI572 HW5
This program is to produce 'big.text' file
all html files will be parsed and text will be extracted
'''
source_folder = "/Users/pyl/solr-7.7.3/nytimes"


if __name__ == '__main__':
    with open("bigdata/big.txt", "w") as f:
        for filename in glob.glob(os.path.join(source_folder, '*.html')):
            soup = BeautifulSoup(open(filename, encoding="UTF-8"), features="html.parser")
            text = str(soup.get_text())
            f.write(text)
            f.write('\n')
        f.close()
