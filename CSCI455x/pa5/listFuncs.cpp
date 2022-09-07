// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI 455 PA5
// Fall 2020


#include <iostream>

#include <cassert>

#include "listFuncs.h"

using namespace std;

Node::Node(const string &theKey, int theValue) {
    key = theKey;
    value = theValue;
    next = NULL;
}

Node::Node(const string &theKey, int theValue, Node *n) {
    key = theKey;
    value = theValue;
    next = n;
}


//*************************************************************************
// put the function definitions for your list functions below

/**
 * remove a node from a linked list
 * @param list: the pointer of the linked list
 * @param target: the key of the target node that will be removed
 * @return true if the target is found and removed
 */
bool listRemove(ListType &list, string target) {
    if (list == NULL) {
        return false;
    }

    ListType pointer = list;
    if (pointer->key == target) {
        list = pointer->next;
        return true;
    }

    while (pointer->next != NULL) {
        if (pointer->next->key == target) {
            pointer->next = pointer->next->next;
            return true;
        }
        pointer = pointer->next;
    }
    return false;
}

/**
 * insert a node into the linked list
 * @param list: the pointer of the linked list
 * @param target: the key of the node that will be inserted
 * @param value: the value of the node that will be inserted
 * @return true if the new node is inserted. If the node is already present, return false
 */
bool listInsert(ListType &list, string target, int value) {
    if (list == NULL) {
        list = new Node(target, value);
        return true;
    }
    ListType pointer = list;
    while (pointer->next != NULL) {
        if (pointer->key == target) {
            return false;
        }
        pointer = pointer->next;
    }
    if (pointer->key == target) {
        return false;
    }
    pointer->next = new Node(target, value);
    return true;
}

/**
 * lookup a node in the linked list
 * @param list: the pointer of the linked list
 * @param target: the key of the target node that need to be searched
 * @return the address of value if the key is found, otherwise return NULL
 */
int *listLookup(const ListType &list, string target) {
    if (list == NULL) {
        return NULL;
    }

    ListType pointer = list;
    while (pointer != NULL) {
        if (pointer->key == target) {
            return &pointer->value;
        }
        pointer = pointer->next;
    }

    return NULL;
}

/**
 * get the length of the linked list
 * @param list: the pointer of the linked list
 * @return the length of the linked list
 */
int getLength(const ListType &list) {
    int length = 0;
    if (list == NULL) {
        return 0;
    }
    ListType pointer = list;
    while (pointer != NULL) {
        length++;
        pointer = pointer->next;
    }
    return length;
}

/**
 * print out a linked list
 * @param list: the pointer of the linked list
 */
void printNode(const ListType &list) {
    Node *pointer = list;
    if (pointer == NULL) {
        return;
    }
    while (pointer != NULL) {
        cout << pointer->key << " " << pointer->value << endl;
        pointer = pointer->next;
    }
}