#include <fstream>
#include <iostream>
#include <cstdlib>
#include <string>
#include <unordered_map>
#include <vector>
#include <queue>

using namespace std;

string alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

struct packet_info
{
    string label;
    int time;
};

/**
 * This function will give a labal for each input index (0-based)
*/
string get_label(int index)
{
    string label = "";
    label.push_back(alpha.at(index));
    return label;
}

/**
 * This function will print the map built in the main function
*/
void print_map(unordered_map<int, vector<packet_info>> &packet_time_map, int num_of_input)
{
    for (int i = 0; i < num_of_input; i++)
    {
        for (unsigned long j = 0; j < packet_time_map[i].size(); j++)
        {
            cout << packet_time_map[i][j].label << "" << packet_time_map[i][j].time;
            cout << " ";
        }
        cout << endl;
    }
}

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        cout << "Error: there must be two arguments: ./lab4.out [input file name]" << endl;
        return 1;
    }
    ifstream file(argv[1]);
    if (!file.is_open())
    {
        cout << "Error: the file can not be opened, please make sure the file name is correct." << endl;
        return 1;
    }

    int num_of_input, num_of_packet, packet_arrival_time;
    int total_num_packet = 0;
    int last_packet_time = 0;
    string label = "";
    unordered_map<int, vector<packet_info>> packet_time_map;
    file >> num_of_input;
    for (int i = 0; i < num_of_input; i++)
    {
        file >> num_of_packet;
        total_num_packet += num_of_packet;
        label = get_label(i);
        for (int j = 0; j < num_of_packet; j++)
        {
            file >> packet_arrival_time;
            if (packet_arrival_time > last_packet_time)
            {
                last_packet_time = packet_arrival_time;
            }
            packet_time_map[i].push_back(packet_info{label, packet_arrival_time});
        }
    }

    if (last_packet_time == 0)
    {
        cout << "There are no packet arrived in the input file" << endl;
        return 1;
    }

    int capacity = total_num_packet / last_packet_time + (total_num_packet % last_packet_time == 0 ? 0 : 1);

    queue<packet_info> q = {};

    int round_number = 1;
    unsigned long pointers[num_of_input];
    for (int i = 0; i < num_of_input; i++)
    {
        pointers[i] = 0;
    }

    for (; round_number <= last_packet_time; round_number++)
    {
        for (int i = 0; i < num_of_input; i++)
        {
            unsigned long curr_pointer = pointers[i];
            if (curr_pointer >= packet_time_map[i].size())
            {
                continue;
            }
            if (packet_time_map[i].at(curr_pointer).time > round_number)
            {
                continue;
            }
            q.push(packet_time_map[i].at(curr_pointer));
            pointers[i]++;
        }
        cout << round_number;
        for (int t = 0; t < capacity; t++)
        {
            if (q.size() > 0)
            {
                cout << " " << q.front().label << q.front().time;
                q.pop();
            }
        }
        cout << endl;
    }
}