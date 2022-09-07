// Name: Peiye Liu
// USC NetID: peiyeliu
// CSCI 455 PA5
// Fall 2020

/*
 * grades.cpp
 * A program to test the Table class.
 * How to run it:
 *      grades [hashSize]
 * 
 * the optional argument hashSize is the size of hash table to use.
 * if it's not given, the program uses default size (Table::HASH_SIZE)
 *
 */

#include "Table.h"

// cstdlib needed for call to atoi
#include <cstdlib>

// vector need in the "getComponent" function
#include <vector>

using namespace std;

//process the input user typed
int process(Table *grades, string input);

//for a given input, split it by space and save them into a string vector
vector <string> getComponents(string input);

//process the insert request
void insertEntry(Table *grades, string name, int score);

//process the change request
void changeEntry(Table *grades, string name, int score);

//process the remove request
void removeEntry(Table *grades, string name);

//process the lookup request
void lookupEntry(Table *grades, string name);

//print out the help message
void printHelperMessage();



int main(int argc, char *argv[]) {

    // gets the hash table size from the command line
    int hashSize = Table::HASH_SIZE;

    Table *grades;  // Table is dynamically allocated below, so we can call
    // different constructors depending on input from the user.

    if (argc > 1) {
        hashSize = atoi(argv[1]);  // atoi converts c-string to int
        if (hashSize < 1) {
            cout << "Command line argument (hashSize) must be a positive number"
                 << endl;
            return 1;
        }
        grades = new Table(hashSize);
    } else {   // no command line args given -- use default table size
        grades = new Table();
    }

    grades->hashStats(cout);

    string input;

    //the signal that indicate when to terminate the program
    //when terminated == 1, leave the while loop and the program will be terminated
    int terminated = 0;
    while (terminated != 1) {
        cout << "cmd> ";
        getline(cin, input);
        terminated = process(grades, input);
    }
    return 0;
}

/**
 * split the input string by space
 * For example, the input "insert Mike 95" will return a vector containing "insert", "Mike", "95"
 * @param input: the line command user typed
 * @return a vector containing these strings
 */
vector <string> getComponents(string input) {
    //make sure there is at least a valid value when calling find function below
    string str = input.append(" ");
    vector <string> components;
    for (int i = 0; i < str.length(); i++) {
        int spacePos = str.find(" ", i);
        if (spacePos < str.length()) {
            string substr = str.substr(i, spacePos - i);
            components.push_back(substr);
            i = spacePos;
        }
    }
    return components;
}

/**
 * process user's command
 * @param grades: the table that will be modified
 * @param input: the command typed by the user
 * @return return 1 if the program should be terminated, return 0 if the program should be continued
 */
int process(Table *grades, string input) {
    vector <string> components = getComponents(input);
    if (components[0] == "insert") {
        insertEntry(grades, components[1], atoi(components[2].c_str()));
    } else if (components[0] == "change") {
        changeEntry(grades, components[1], atoi(components[2].c_str()));
    } else if (components[0] == "lookup") {
        lookupEntry(grades, components[1]);
    } else if (components[0] == "remove") {
        removeEntry(grades, components[1]);
    } else if (components[0] == "print") {
        grades->printAll();
    } else if (components[0] == "size") {
        cout << "There are " << grades->numEntries() << " entries in the record." << endl;
    } else if (components[0] == "stats") {
        grades->hashStats(cout);
    } else if (components[0] == "help") {
        printHelperMessage();
    } else if (components[0] == "quit") {
        return 1;
    } else {
        cout << "ERROR: invalid command" << endl;
    }
    return 0;
}

/**
 * process the insert request, if the record is already present, no insertion will be done
 * @param grades: the table that will be modified
 * @param name: the name that will be inserted
 * @param score: the score for that name
 */
void insertEntry(Table *grades, string name, int score) {
    if (grades->lookup(name)) {
        cout << "The name is already present: " << name << endl;
    } else {
        grades->insert(name, score);
    }
}


/**
 * process the change request, if the record is not present, no change will be done
 * @param grades: the table that will be modified
 * @param name: the record's name
 * @param score: the new score
 */
void changeEntry(Table *grades, string name, int score) {
    int *result = grades->lookup(name);
    if (result != NULL) {
        *result = score;
    } else {
        cout << "No record for this student: " << name << endl;
    }
}

/**
 * process the remove request, if the record is already present, no remove operation will be done
 * @param grades: the table that will be modified
 * @param name: the name that will be removed
 */
void removeEntry(Table *grades, string name) {
    if (!grades->remove(name)) {
        cout << "No record for this student: " << name << endl;
    }
}

/**
 * process the lookup request and print the corresponding message
 * @param grades: the table that will be searched
 * @param name: the target name
 */
void lookupEntry(Table *grades, string name) {
    int *lookupRes = grades->lookup(name);
    if (lookupRes != NULL) {
        cout << "name: " << name << "; score: " << *lookupRes << endl;
    } else {
        cout << "No record for this student: " << name << endl;
    }
}

/**
 * print out the instruction message when the user type "help"
 */
void printHelperMessage() {
    cout << "Here are all valid commands:" << endl;
    cout << "   insert name score: insert this name and score into the table" << endl;
    cout << "   change name score: change the score for this student" << endl;
    cout << "   lookup name: look up the name. If the name is found, the name and score will be printed"
         << endl;
    cout << "   remove name: remove the record for that student" << endl;
    cout << "   print: print out all the names and scores" << endl;
    cout << "   size: print out the number of entries in the table" << endl;
    cout << "   stats: print out statistics about the hashtable" << endl;
    cout << "   help: print out a brief command summary" << endl;
    cout << "   quit: exit the program" << endl;
}


