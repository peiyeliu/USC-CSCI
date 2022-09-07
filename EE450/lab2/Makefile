all: lab2

install:
	echo "nothing to install"

lab2: lab2.cpp
	g++ lab2.cpp -lstdc++ -Wall -Wextra -std=c++17 -o lab2.out

test-client:
	python3 lab2_test.py test-client

test-server:
	python3 lab2_test.py test-server

test-all:
	python3 lab2_test.py test-all

clean:
	rm -rf lab2.out