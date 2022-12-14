#include <iostream>
#include <iomanip>
#include <ctime>
#include <chrono>
#include <thread>
#include <mutex>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <signal.h>

#include <unordered_map>
#include <vector>
#include "project.h"

using namespace std;
using namespace chrono_literals;

#define PORT "8000"
#define UDP_PORT "8001"
#define BACKLOG 10
#define MAXDATASIZE 100

#define NO_BIDDING 0
#define OFFER 1
#define ACCEPTED 2
#define REJECTED 3

mutex m;

struct item_info
{
    int seller;
    int buyer;
    unsigned int list_price;
    unsigned int curr_bid_price;
    struct sockaddr_storage buyer_addr;
};

/**
 * This fuction will print the message replied to buyer
*/
void print_udp_reply(UDP_Message msg_recv)
{
    time_t thread_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
    cout << put_time(localtime(&thread_time), "%c %Z");
    if (msg_recv.action == OFFER)
    {
        return;
    }

    if (msg_recv.action == NO_BIDDING)
    {
        cout << ": Sent bidding not open";
    }
    else
    {
        string result = msg_recv.action == ACCEPTED ? "accepted" : "rejected";
        cout << ": Sent bid " << msg_recv.price << "$ on " << msg_recv.name;
        cout << " " << result;
    }
}

/**
 * This helper function is to setup up TCP connection with seller.
 * return -1 when the error occurs.
 * Reference: The implement of TCP connection was introduced in Discussion 2: 
 * https://colab.research.google.com/drive/1MGJI8w57Mq8CrX9qKfTYQ0WXV0TAIoE3?usp=sharing
 * 
*/
int TCP_setup(int &sockfd, struct addrinfo &hints, struct addrinfo *servinfo)
{
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    if (getaddrinfo(NULL, PORT, &hints, &servinfo) != 0)
    {
        cout << "getaddrinfo: error when server getting the address info" << endl;
        return -1;
    }
    sockfd = socket(servinfo->ai_family, servinfo->ai_socktype, servinfo->ai_protocol);
    if (sockfd == -1)
    {
        cout << "server: error when creating socket" << endl;
        return -1;
    }
    int yes = 1;
    setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));
    if (::bind(sockfd, servinfo->ai_addr, servinfo->ai_addrlen) == -1)
    {
        close(sockfd);
        cout << "server: bind failed" << endl;
        return -1;
    }
    freeaddrinfo(servinfo);
    if (-1 == listen(sockfd, BACKLOG))
    {
        cout << "server: listen failed" << endl;
        return -1;
    }
    time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
    cout << put_time(localtime(&curr_time), "%c %Z") << ": Waiting for connection on port " << PORT << "." << endl;
    return 0;
}

/**
 * This function is to setup up UDP connection
 * return -1 when error occurs
 * Reference: The implement of UDP connection is introduced in Discussion 4:
 * https://colab.research.google.com/drive/1SlZCgUdeO1KVlCRgC-ojIx3zbx043MXz?usp=sharing
 * 
*/
int UDP_setup(int &sockfd, struct addrinfo &hints, struct addrinfo *servinfo)
{
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_DGRAM;
    hints.ai_flags = AI_PASSIVE;
    if (getaddrinfo(NULL, UDP_PORT, &hints, &servinfo) != 0)
    {
        cout << "server: error when get address information for UDP connection" << endl;
        return -1;
    }
    sockfd = socket(servinfo->ai_family, servinfo->ai_socktype, servinfo->ai_protocol);
    if (sockfd == -1)
    {
        cerr << "server: socket" << endl;
        return -1;
    }
    int yes = 1;
    setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

    if (::bind(sockfd, servinfo->ai_addr, servinfo->ai_addrlen) == -1)
    {
        close(sockfd);
        cerr << "server: bind" << endl;
        return -1;
    }
    freeaddrinfo(servinfo);
    return 0;
}

/**
 * This fuction will process the bid received from buyer
*/
void process_bid(UDP_Message msg, UDP_Message &msg_send, unordered_map<string, vector<item_info>> &seller_map, int udp_sockfd, struct sockaddr_storage &udp_client_addr)
{
    string item_name = string(msg.name);
    msg_send.action = NO_BIDDING;
    msg_send.price = msg.price;
    strncpy(msg_send.name, msg.name, 20);

    if (seller_map.find(item_name) != seller_map.end() && seller_map[item_name].size() > 0)
    {
        vector<item_info> &list = seller_map[item_name];
        unsigned int min_price = 1000000;
        int index = -1;
        for (unsigned long i = 0; i < list.size(); i++)
        {
            unsigned int curr_price = list.at(i).list_price > list.at(i).curr_bid_price ? list.at(i).curr_bid_price : list.at(i).list_price;
            if (curr_price < min_price)
            {
                min_price = list.at(i).list_price;
                index = i;
            }
        }
        if (msg.price > list.at(index).curr_bid_price)
        {
            if (list.at(index).curr_bid_price != 0)
            {
                // tell the previous buyer
                struct UDP_Message msg_prev_buyer;
                msg_prev_buyer.action = REJECTED;
                msg_prev_buyer.price = msg.price;
                strncpy(msg_prev_buyer.name, msg.name, 20);
                struct sockaddr_storage prev_buyer_addr = list.at(index).buyer_addr;
                socklen_t prev_buyer_addr_len = sizeof(prev_buyer_addr);
                if (sendto(udp_sockfd, &msg_prev_buyer, sizeof(msg_prev_buyer), 0, (struct sockaddr *)&prev_buyer_addr, prev_buyer_addr_len) == -1)
                {
                    cout << "server: failed to send message to buyer" << endl;
                }
                char ip_address[INET6_ADDRSTRLEN];
                int port_num;
                if (prev_buyer_addr.ss_family == AF_INET)
                {
                    struct sockaddr_in *addr = (struct sockaddr_in *)&prev_buyer_addr;
                    inet_ntop(AF_INET, &(addr->sin_addr), ip_address, sizeof ip_address);
                    port_num = ntohs(addr->sin_port);
                }
                else
                {
                    struct sockaddr_in6 *addr = (struct sockaddr_in6 *)&prev_buyer_addr;
                    inet_ntop(AF_INET, &(addr->sin6_addr), ip_address, sizeof ip_address);
                    port_num = ntohs(addr->sin6_port);
                }
                print_udp_reply(msg_prev_buyer);
                cout << " to " << ip_address << ":" << port_num << "." << endl;
            }
            item_info &p = list[index];
            p.curr_bid_price = msg.price;
            p.buyer_addr = udp_client_addr;
            msg_send.action = ACCEPTED;
        }
        else
        {
            msg_send.price = list.at(index).curr_bid_price;
            msg_send.action = REJECTED;
        }
    }
}

int main()
{

    setenv("TZ", "/usr/share/zoneinfo/America/Los_Angeles", 1);

    int sockfd, new_socket;
    struct addrinfo hints, *servinfo;
    servinfo = nullptr;

    int udp_sockfd;
    struct addrinfo udp_hints, *udp_servinfo;
    udp_servinfo = nullptr;

    struct sockaddr_storage client_addr;
    socklen_t sin_size = sizeof client_addr;

    int seller_idx = 1;

    TCP_setup(sockfd, hints, servinfo);
    UDP_setup(udp_sockfd, udp_hints, udp_servinfo);

    unordered_map<string, vector<item_info>> seller_map = {};
    chrono::time_point<chrono::steady_clock> last_TCP_arrival = chrono::steady_clock::now();
    //Reference: The time manipulation was introduce in Discussion 2
    auto save_server = [&seller_map, &last_TCP_arrival](int sockfd, int seller_id, char *ip_address, unsigned short int port_num) -> void
    {
        struct TCP_Message msg_recv;
        time_t thread_time;
        while (1)
        {

            if (recv(sockfd, &msg_recv, sizeof(msg_recv), 0) <= 0)
            {
                continue;
            }

            {
                lock_guard lg{m};
                string item_name(msg_recv.name);
                if (seller_map.find(item_name) == seller_map.end())
                {
                    seller_map[item_name] = {};
                }
                seller_map[item_name].push_back({seller_id, 0, msg_recv.price, 0, {}});
                last_TCP_arrival = chrono::steady_clock::now();
                thread_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
                cout << put_time(localtime(&thread_time), "%c %Z");
                cout << ": Received " << msg_recv.name << " for sale for " << msg_recv.price << "$";
                cout << " from " << ip_address << ":" << port_num << "(thread: " << this_thread::get_id() << ")"
                     << "." << endl;
            }
        }
    };

    auto UDP_server = [&seller_map, &last_TCP_arrival](int udp_sockfd) -> void
    {
        UDP_Message msg_recv, msg_send;
        struct sockaddr_storage udp_client_addr;
        socklen_t udp_client_addr_len = sizeof(udp_client_addr);

        while (1)
        {
            if (recvfrom(udp_sockfd, &msg_recv, sizeof(msg_recv), 0, (struct sockaddr *)&udp_client_addr, &udp_client_addr_len) == -1)
            {
                cout << "server: failed to receive UDP message from buyer" << endl;
                continue;
            }

            char ip_address[INET6_ADDRSTRLEN];
            int port_num;
            if (udp_client_addr.ss_family == AF_INET)
            {
                struct sockaddr_in *addr = (struct sockaddr_in *)&udp_client_addr;
                inet_ntop(AF_INET, &(addr->sin_addr), ip_address, sizeof ip_address);
                port_num = ntohs(addr->sin_port);
            }
            else
            {
                struct sockaddr_in6 *addr = (struct sockaddr_in6 *)&udp_client_addr;
                inet_ntop(AF_INET, &(addr->sin6_addr), ip_address, sizeof ip_address);
                port_num = ntohs(addr->sin6_port);
            }
            {
                lock_guard lg{m};
                time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
                cout << put_time(localtime(&curr_time), "%c %Z");
                cout << ": Received bid " << msg_recv.price << "$ on " << msg_recv.name;
                cout << " from " << ip_address << ":" << port_num << "." << endl;
                chrono::time_point<chrono::steady_clock> end = chrono::steady_clock::now();
                if (chrono::duration_cast<chrono::seconds>(end - last_TCP_arrival).count() < 3)
                {
                    msg_send.action = NO_BIDDING;
                }
                else
                {
                    process_bid(msg_recv, msg_send, seller_map, udp_sockfd, udp_client_addr);
                }
                if (sendto(udp_sockfd, &msg_send, sizeof(msg_send), 0, (struct sockaddr *)&udp_client_addr, udp_client_addr_len) == -1)
                {
                    cout << "server: failed to send message to buyer" << endl;
                }

                print_udp_reply(msg_send);
                cout << " to " << ip_address << ":" << port_num << "." << endl;
            }
        }
    };

    thread udp_t{UDP_server, udp_sockfd};
    udp_t.detach();

    while ((new_socket = accept(sockfd, (struct sockaddr *)&client_addr, &sin_size)) > 0)
    {
        if (new_socket == -1)
        {
            cout << "server: accept failed" << endl;
            break;
        }
        //Reference: Getting IP address was introduced in Discussion 2
        char ip_address[INET6_ADDRSTRLEN];
        int port_num;
        if (client_addr.ss_family == AF_INET)
        {
            struct sockaddr_in *addr = (struct sockaddr_in *)&client_addr;
            inet_ntop(AF_INET, &(addr->sin_addr), ip_address, sizeof ip_address);
            port_num = ntohs(addr->sin_port);
        }
        else
        {
            struct sockaddr_in6 *addr = (struct sockaddr_in6 *)&client_addr;
            inet_ntop(AF_INET, &(addr->sin6_addr), ip_address, sizeof ip_address);
            port_num = ntohs(addr->sin6_port);
        }

        thread t{save_server, new_socket, seller_idx, ip_address, port_num};
        seller_idx++;
        t.detach();
    }

    close(new_socket);
    close(sockfd);

    return 0;
}