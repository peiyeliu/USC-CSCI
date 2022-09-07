// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI 455 PA5
// Fall 2020

// Table.cpp  Table class implementation


#include "Table.h"

#include <iostream>
#include <string>
#include <cassert>

using namespace std;


// listFuncs.h has the definition of Node and its methods.  -- when
// you complete it it will also have the function prototypes for your
// list functions.  With this #include, you can use Node type (and
// Node*, and ListType), and call those list functions from inside
// your Table methods, below.

#include "listFuncs.h"


//*************************************************************************

/**
 * initialized the table when the hashsize is not specified
 */
Table::Table() {
    numOfEntries = 0;
    hashSize = Table::HASH_SIZE;
    hashArray = new ListType[hashSize];
    for (int i = 0; i < hashSize; i++) {
        hashArray[i] = NULL;
    }
}

/**
 * initialized the table when the hashsize is specified
 */
Table::Table(unsigned int hSize) {
    numOfEntries = 0;
    hashSize = hSize;
    hashArray = new ListType[hashSize];
    for (int i = 0; i < hashSize; i++) {
        hashArray[i] = NULL;
    }
}

/**
 * look up the entry with giving key
 * @param key: the key that will be searched
 * @return the address of value if the key is found
 */
int *Table::lookup(const string &key) {
    int hashNum = this->hashCode(key);
    return listLookup(hashArray[hashNum], key);
}

/**
 * remove the entry with giving key
 * @param key
 * @return true if the entry is found and removed
 */
bool Table::remove(const string &key) {
    int hashNum = this->hashCode(key);
    bool isRemoved = listRemove(hashArray[hashNum], key);
    if (isRemoved) {
        numOfEntries--;
    }
    return isRemoved;

}

/**
 * insert an entry into the table
 * @param key: the key for that new entry
 * @param value: the value for that new entry
 * @return true if the entry is inserted
 */
bool Table::insert(const string &key, int value) {
    int hashNum = this->hashCode(key);
    bool isInserted = listInsert(hashArray[hashNum], key, value);
    if (isInserted) {
        numOfEntries++;
    }
    return isInserted;
}

/**
 * return the number of entries in the table
 * @return the number of entries in the table
 */
int Table::numEntries() const {
    return numOfEntries;      // dummy return value for stub
}

/**
 * print all entries in the table
 */
void Table::printAll() const {
    for (int i = 0; i < hashSize; i++) {
        printNode(hashArray[i]);
    }
}


//   number of buckets: 997
//   number of entries: 10
//   number of non-empty buckets: 9
//   longest chain: 2
void Table::hashStats(ostream &out) const {
    cout << "number of buckets: " << hashSize << endl;
    cout << "number of entries: " << Table::numEntries() << endl;
    cout << "number of non-empty buckets: " << Table::getFilledBuckets(hashArray) << endl;
    cout << "longest chain: " << Table::getLongestChain(hashArray) << endl;
}


// add definitions for your private methods here

/**
 * get the length of the longest chain in the table
 * @param hashArray the hashArray that will be counted
 * @return the length of the longest chain in the table
 */
int Table::getLongestChain(ListType *hashArray) const {
    int longestChain = 0;
    for (int i = 0; i < hashSize; i++) {
        int currLength = getLength(hashArray[i]);
        if (currLength > longestChain) {
            longestChain = currLength;
        }
    }
    return longestChain;
}

/**
 * get the number of filled buckets in the table
 * @param hashArray the hashArray that will be counted
 * @return the number of filled buckets in the table
 */
int Table::getFilledBuckets(ListType *hashArray) const {
    int filledBuckets = 0;
    for (int i = 0; i < hashSize; i++) {
        if (getLength(hashArray[i]) > 0) {
            filledBuckets++;
        }
    }
    return filledBuckets;
}