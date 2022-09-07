#include <iostream>
#include <cstdlib>
#include <fstream>
#include <string>
#include <unordered_map>
#include <vector>
#include <queue>
#include <set>

using namespace std;


/**
 * This function will read the file and store the graph information in a unordered map.
 * 
*/
void build_graph(ifstream &file, unordered_map<int, unordered_map<int, int>> &graph, int num_of_edges){
    int src, des, distance;
    for(int i = 0; i < num_of_edges; i++){
        file >> src >> des >> distance;
        //cout << src << " " << des << " " << distance << endl; 
        graph[src][des] = distance;
        graph[des][src] = distance;
    }
}

/**
 * This function will read a series of number that representing the people who got covid positive.
 * 
 * 
*/
void read_positive_nodes(ifstream &file, set<int> &positive_nodes){
    int num_of_positive, node_index;
    file >> num_of_positive;
    for(int i = 0; i < num_of_positive; i++){
        file >> node_index;
        positive_nodes.insert(node_index);
    }
}

/**
 * This function will perform graph search from the give node using bellmam-ford alogrithm.
 * 
 * param:
 *      start --- the starting node for the graph search (the person who got COVID positive)
 *      graph --- the graph for the search
 *      num_of_edges --- the number of edges in the graph
 *      result_set --- this set will store the nodes that are relatively close to the start node (the people who need to stay at home)
 *      benchmark --- if the distance between two nodes are smaller than this value, then these two nodes are close enough.
*/
void bellman_ford(int start, unordered_map<int, unordered_map<int, int>> &graph,
                int num_of_nodes, set<int> &result_set, int benchmark){
    
    int distance[num_of_nodes];

    for(int i = 0; i < num_of_nodes; i++){
        if(i == start){
            distance[i] = 0;
        }
        else{
            distance[i] = 1000000;
        }
    }

    for(int t = 0; t < num_of_nodes - 1; t++){
        for(int src = 0; src < num_of_nodes; src++){
            if(graph.find(src) == graph.end()){
                continue;
            }
            unordered_map<int, int> destination_map = graph[src];

            for(pair<int, int> element : destination_map){
                int des = element.first;
                int length = element.second;

                if(distance[src] + length < distance[des]){
                    distance[des] = distance[src] + length;
                }

            }
        }
    }

    for(int i = 0; i < num_of_nodes; i++){
        if(distance[i] <= benchmark){
            result_set.insert(i);
        }
    }
}

void random_positive_nodes(set<int> &positive_nodes, int num_of_nodes){
    int node;
    while(positive_nodes.size() < 100){
        node = rand() % num_of_nodes;
        if(positive_nodes.find(node) == positive_nodes.end()){
            positive_nodes.insert(node);
        }
    }
}



/**
 * the main function
*/
int main(int argc, char* argv[]){
    ifstream graph_file;
    ifstream positive_nodes_file;

    unordered_map<int, unordered_map<int, int> > graph;
    set<int> positive_nodes;
    set<int> close_contact_nodes;

    int num_of_edges, num_of_nodes;
    int benchmark = 5;

    if(argc < 2 || argc > 3){
        cout << "ERROR: wrong command argument number! The correct argument should be:" << endl;
        cout << "./hw1extra [graph input file name] [positive nodes file name]" << endl;
        return -1;
    }

    graph_file.open(argv[1], ios::in);
    if(graph_file.fail()){
        cout << "ERROR: the file " << argv[1] << " can not be opened." << endl;
        return -1;
    }
    graph_file >> num_of_nodes >> num_of_edges;

    build_graph(graph_file, graph, num_of_edges);
    graph_file.close();

    cout << "A graph has been created with " << num_of_nodes << " nodes and " << num_of_edges << " edges." << endl;
    if(argc == 3){
        positive_nodes_file.open(argv[2], ios::in);
        if(positive_nodes_file.fail()){
            cout << "ERROR: the file " << argv[2] << " can not be opened." << endl;
            return -1;
        }
        read_positive_nodes(positive_nodes_file, positive_nodes);
        positive_nodes_file.close();
    }
    else{
        random_positive_nodes(positive_nodes, num_of_nodes);
        cout << "100 random positive node created." << endl; 
    }

    cout << "There are " << positive_nodes.size() << " positive cases." << endl;

    for(int node: positive_nodes){
        bellman_ford(node, graph, num_of_edges, close_contact_nodes, benchmark);
        //cout << "Node " << node << " finished." << endl; 
    }

    ofstream result("result.txt");
    for(int x: close_contact_nodes){
        result << x << "\n";
    }
    cout << "The result has been saved into 'result.txt' file."<< endl;
    
    return 0;
}