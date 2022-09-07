// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI 455 PA5
// Fall 2020

// pa5list.cpp
// a program to test the linked list code necessary for a hash table chain

// You are not required to submit this program for pa5.

// We gave you this starter file for it so you don't have to figure
// out the #include stuff.  The code that's being tested will be in
// listFuncs.cpp, which uses the header file listFuncs.h

// The pa5 Makefile includes a rule that compiles these two modules
// into one executable.

#include <iostream>
#include <string>
#include <cassert>

using namespace std;

#include "listFuncs.h"


int main() {
    string n = "first";
    string a = "join";
    string b = "hello";
    string c = "world";
    string d = "get";
    string e = "not";

    ListType empty = NULL;
    listRemove(empty, n);
    listInsert(empty, n, 0);
    cout << "the result after insertion on an empty node" << endl;
    printNode(empty);


    ListType list = new Node(a, 1);
    list->next = new Node(b, 2);
    list->next->next = new Node(c, 3);
    listInsert(list, d, 4);
    cout << "the result after insertion on a defined node" << endl;
    printNode(list);


    listRemove(list, b);
    cout << getLength(list) << endl;
    listRemove(list, c);
    listRemove(list, b);
    listRemove(list, d);

    listInsert(list, e, 100);
    cout << "the length after several operations = " << getLength(list) << endl;
    printNode(list);

    return 0;
}
