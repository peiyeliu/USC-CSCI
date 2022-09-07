// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI 455 PA5
// Fall 2020


//*************************************************************************
// Node class definition 
// and declarations for functions on ListType

// Note: we don't need Node in Table.h
// because it's used by the Table class; not by any Table client code.

// Note2: it's good practice to not put "using" statement in *header* files.  Thus
// here, things from std libary appear as, for example, std::string

#ifndef LIST_FUNCS_H
#define LIST_FUNCS_H
  

struct Node {
   std::string key;
   int value;

   Node *next;

   Node(const std::string &theKey, int theValue);

   Node(const std::string &theKey, int theValue, Node *n);
};


typedef Node * ListType;

//*************************************************************************
//add function headers (aka, function prototypes) for your functions
//that operate on a list here (i.e., each includes a parameter of type
//ListType or ListType&).  No function definitions go in this file.

//remove a node from the linked list
bool listRemove(ListType& list, std::string target);

//insert a node from the linked list
bool listInsert(ListType& list, std::string target, int value);

//look up a node from the linked list
int* listLookup(const ListType& list, std::string target);

//get the length od the linked list
int getLength(const ListType& list);

//print out the node of the linked list
void printNode(const ListType& list);















// keep the following line at the end of the file
#endif
