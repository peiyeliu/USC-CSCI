#include <iostream>
#include <fstream>

#include <cstdlib>
#include <string>
#include <unordered_map>
#include <vector>

using namespace std;


/**
 * perform the Bellman_form algorithm to fine the shortest path in a weight graph.
 * return 1 if there is a negative cycle detected. return 0 if no negative cycle detected.
 * 
 * param:
 *      from: the start node of the path
 *      to: the end node of the path
 *      graph: the graph for the searching
 *      num_of_edges: the number of edges
 *      result: the shortes path will be saved in this string variable, for example:
 *          string "5 0 4 3 2" represents a path from 0 via 4, 5 to 2. And the sum of edges' weight is 5.
*/
int bellman_ford(int from, int to, 
                unordered_map<int, unordered_map<int, int> > &graph, 
                string &result){

    int num_of_nodes = graph.size();
    
    int distance[num_of_nodes];
    int parent[num_of_nodes];
    for(int i = 0; i < num_of_nodes; i++){
        parent[i] = -1;
        if(i == from){
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

            // How to iterate unordered map: https://thispointer.com/how-to-iterate-over-an-unordered_map-in-c11/
            for (std::pair<int, int> element : destination_map){
                int des = element.first;
                int length = element.second;
                if(distance[src] + length < distance[des]){
                    distance[des] = distance[src] + length;
                    parent[des] = src;
                    //cout << "des changed: " << des << " " << distance[des] << " " << parent[des] << endl;
                }
            }
        }
    }
    

    // check whether there is negative cycle
    for(int src = 0; src < num_of_nodes; src++){
        if(graph.find(src) == graph.end()){
            continue;
        }
        unordered_map<int, int> destination_map = graph[src];
        // How to iterate unordered map: https://thispointer.com/how-to-iterate-over-an-unordered_map-in-c11/
        for (std::pair<int, int> element : destination_map){
            int des = element.first;
            int length = element.second;
            // if there is a negative cycle, this function will return 1, and the path will not be created
            if(distance[src] + length < distance[des]){
                return 1;
            }
        }
    }

    // if no negative cycle, we will store the path and the cost information in a string
    int node_idx = to;
    int prev_node = -1;
    int cost = 0;
    while(parent[node_idx] != -1){
        result = to_string(node_idx) + " " + result;
        prev_node = node_idx;
        node_idx = parent[node_idx];
        cost += graph[prev_node][node_idx];
    }
    result = to_string(cost) + " " + to_string(node_idx) + " " + result;
    
    return 0;

}





int main(int argc, char* argv[]) 
{ 
    
    ifstream file(argv[1]);
    unordered_map<int, unordered_map<int, int> > delay_graph;
    unordered_map<int, unordered_map<int, int> > price_graph;

    int num_of_edges = 0;
    int from = 0;
    int to = 0;

    if(argc < 2){
        cout << "ERROR: You have to include the test file name in the command line" << endl;
        return -1;
    }
    if(argc > 2){
        cout << "ERROR: You have too much arguments." << endl;
        return -1;
    }

    if (!file.is_open()) {
        cout << "ERROR: The file could not be open, please type valid file name." << endl;
        file.close();
        return -1;
    }


    file >> num_of_edges >> from >> to;
    int src, des, delay, price;

    for(int i = 0; i < num_of_edges; i++){
        file >> src >> des >> delay >> price;

        //using map-of-map
        delay_graph[src][des] = delay;
        delay_graph[des][src] = delay;
        price_graph[src][des] = price;
        price_graph[des][src] = price;
    }

    string best_delay = "";
    string best_price = "";

    int delay_neg_cycle = bellman_ford(from, to, delay_graph, best_delay);
    int price_neg_cycle = bellman_ford(from, to, price_graph, best_price);


    ofstream result("result.txt");

    int has_neg_cycle = delay_neg_cycle | price_neg_cycle;
    result << has_neg_cycle << endl;
    if(has_neg_cycle){
        result.close();
        return 0;
    }


    int same_path = 0;
    if(best_delay.substr(2) == best_price.substr(2)){
        same_path = 1;
    }

    result << best_delay << endl;
    result << best_price << endl;
    result << same_path << endl;
    result.close();
    return 0;
} 