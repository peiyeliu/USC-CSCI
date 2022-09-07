#include <iostream>
#include <cstdlib>
#include <fstream>
#include <string>
#include <unordered_map>
#include <vector>
#include <queue>
using namespace std;


/**
 * This function check whether the string is a valid number format.
 * 
*/

bool isNumber(string s){
    if(s == ""){
        return false;
    }

    for(size_t i = 0; i < s.length(); i++){
        if(s.at(i) - '0' < 0 || s.at(i) - '0' > 9){
            return false;
        }
    }
    return true;
}




/**
 * Build the graph from the input file
 * Return 0 if the graph was built successfully
 * Return -1 if the input file is invalid
 *   
 * param:
 * file --- the input file
 * graph --- the result graph
 * num_of_nodes --- the number of nodes in the graph
 * 
 * Reference:
 * In this function, the string split method (line 61 to 62) was inspired by this stackoverflow post: https://stackoverflow.com/questions/14265581/parse-split-a-string-in-c-using-string-delimiter-standard-c
 * Reading file line by line (line 49 and line 55): https://stackoverflow.com/questions/7868936/read-file-line-by-line-using-ifstream-in-c
 * 
*/
int buildGraph(ifstream &file, unordered_map<int, vector<int>> &graph, int &num_of_nodes){
    string line = "";
    getline(file, line);
    num_of_nodes = stoi(line);
    for(int i = 1; i <= num_of_nodes; i++){
        graph[i] = {};
    }

    while(getline(file, line)){
        int space_pos = line.find(" ", 0);
        if(space_pos == string::npos){
            cout << "ERROR (No space detected): In each line, there should be two integers separated by space." << endl;
            return -1;
        }
        string before_space = line.substr(0, space_pos);
        string after_space = line.substr(space_pos + 1, line.length() - 1 - space_pos);

        if(!(isNumber(before_space) && isNumber(after_space))){
            cout << "ERROR (Invalid character): the input should contains numbers only" << endl;
            return -1;
        }
        int from_node = stoi(before_space);
        int to_node = stoi(after_space);
        graph[from_node].push_back(to_node);
        graph[to_node].push_back(from_node);
    }

    return 0;
}




/**
 * Perform breadth-first-search algorithm on the graph to find the distance between source and destination nodes
 * Return the distance between two nodes (it is also the number of edges between two nodes)
 * For example, node A, B and C are connected linearly: A-B-C. The distance between A and C will be 2.
 * Return -1 if these two nodes are not connnected
 * 
 * param:
 * from --- the index of source node
 * to --- the index of destination node
 * graph --- the graph of the node
 * num_of_nodes --- the number of the node
 * 
 * Reference: the breadth-first-search algorithm was implemented by following the pseudocode in Wikipedia: https://en.wikipedia.org/wiki/Breadth-first_search
 * 
*/
int bfs(int from, int to, unordered_map<int, vector<int>> &graph, int num_of_nodes){
    if(from == to){
        return 0;
    }

    bool visited[num_of_nodes + 1];
    for(int i = 1; i <= num_of_nodes; i++){
        visited[i] = false;
    }

    unordered_map<int, int> distance;
    visited[from] = true;
    distance[from] = 0;
    queue<int> q;
    q.push(from);

    while(q.size() > 0){
        int curr_node = q.front();
        q.pop();
        vector<int> child_list = graph[curr_node];
        for(int i = 0; i < child_list.size(); i++){
            int child = child_list[i];
            if(!visited[child]){
                visited[child] = true;
                distance[child] = 1 + distance[curr_node];
                q.push(child);
                if(child == to){
                    return distance[child];
                }
            }
        }
    }
    return -1;
}


/**
 * Print the information of the graph
 * 
 * param:
 * graph --- the graph of the node
 * num_of_nodes --- the number of the node
*/
void print_graph(unordered_map<int, vector<int>> &graph, int num_of_nodes){
    for(int i = 1; i <= num_of_nodes; i++){
        cout << "The node " << i << " has " << graph[i].size() << " childrens." << endl; 
    }
}

/**
 * The main function
 * 
 * Reference:
 * In this function, the handling and reading of file (line 173 to 177) was inspired by this post: https://stackoverflow.com/questions/7868936/read-file-line-by-line-using-ifstream-in-c
 * Using adjacency list to store graph was explained in Discussion 0: https://colab.research.google.com/drive/1bkdrOhyrmw0lcUb2jnc2HqBRlD5vP-ld?usp=sharing
*/
int main(){
    string filename = "";
    string query = "";
    bool file_loaded = false;
    
    cout <<"Type 'exit' or 'quit' to leave the program."<< endl;
    ifstream file;
    unordered_map<int, vector<int>> graph;
    int num_of_nodes = 0;

    while(!file_loaded){
        cout << "Please type the name of the test file: ";
        getline(cin, filename);
        if(filename == ""){
            cout <<"ERROR: The file name is empty, please type the file name again."<< endl;
        }
        else if(filename == "quit" || filename == "exit"){
            cout << "Program terminated." << endl;
            return 0;
        }
        else{
            
            file.open(filename, ios::in);
            if (file.fail()) {
                cout << "ERROR: The file could not be open, please type valid file name." << endl;
                file.close();
            }
            else{
                if(buildGraph(file, graph, num_of_nodes) < 0){
                    cout << "The input file can not be converted to a valid graph, please check your input file." << endl;
                    file.close();
                    continue;
                }
                cout << "The graph has been initialized successfully" << endl;
                // print_graph(graph, num_of_nodes);
                cout << "The number of nodes is: " << num_of_nodes << endl;
                cout << "To make the query, type the index of two nodes separated by a space"<< endl;
                cout << "Type 'exit' or 'quit' to leave the program."<< endl;
                file_loaded = true;
                file.close();
            }
        }
    }

    while(1){
        cout << "Please type the index of source and destination node (separate by a space): ";
        getline(cin, query);
        if(query == "quit" || query == "exit"){
            cout << "Program terminated." << endl;
            return 0;
        }
        int space_pos = query.find(" ", 0);
        if(space_pos == string::npos){
            cout << "ERROR (No space detected): To make the query, type two integers separated by space." << endl;
            continue;
        }
        string before_space = query.substr(0, space_pos);
        string after_space = query.substr(space_pos + 1, query.length() - 1 - space_pos);
        if(!(isNumber(before_space) && isNumber(after_space))){
            cout << "ERROR (Invalid character): To make the query, the input should be number." << endl;
            continue;
        }
        int from_node = stoi(query.substr(0, space_pos));
        int to_node = stoi(query.substr(space_pos + 1, query.length() - 1 - space_pos));
        if(from_node > num_of_nodes || to_node > num_of_nodes || from_node < 1 || to_node < 1){
            cout << "ERROR: The index of node should be large than 0 and smaller than " << (num_of_nodes + 1) << endl;
        }
        else{
            int result = bfs(from_node, to_node, graph, num_of_nodes);
            if(result == -1){
                cout << "The node " << from_node << " and the node " << to_node << " are not connected." << endl;
            }
            else{
                double significance = 1.0 / (result);
                cout << "There are " << result << " edges between node " << from_node << " and node " << to_node << endl;
                cout << "The significance factor between node " << from_node << " and node " << to_node << " is " << significance << endl;
            }
        }
    }
    

    return 0;
}