all: lab1

install:
	pip3 install networkx numpy matplotlib

lab1: lab1.cpp
	g++ lab1.cpp -lstdc++ -Wall -Wextra -std=c++17 -o lab1.out

test-negativecycle:
	python3 lab1_test.py test-negativecycle

test-cost:
	python3 lab1_test.py test-cost

test-shortestpath:
	python3 lab1_test.py test-shortestpath

test-sameshortestpath:
	python3 lab1_test.py test-sameshortestpath

test-all:
	python3 lab1_test.py test-all

clean:
	rm -rf lab1.out