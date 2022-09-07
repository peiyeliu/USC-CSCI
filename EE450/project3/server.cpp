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
#include <sys/time.h>
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

// add a new status for Phase 3 UDP messages;
#define MAIL_ADDR 4

mutex m;

struct item_info
{
    int seller;
    int buyer;
    unsigned int list_price;
    unsigned int curr_bid_price;
    struct sockaddr_storage buyer_addr;
    string seller_ip_and_port;
    int seller_sockfd;
    char item_name[20];
    char mail_address[20];
};

void print_curr_time()
{
    time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
    cout << put_time(localtime(&curr_time), "%c %Z");
}

void print_udp_reply(UDP_Message msg_recv)
{
    time_t thread_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
    cout << put_time(localtime(&thread_time), "%c %Z");
    if (msg_recv.action == OFFER || msg_recv.action == MAIL_ADDR)
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
 * get the ip address and port number in the following format:
 * ip:port
 * For example, 192.168.1.1:8080
 * Reference:
 * Getting IP address and port number was from our Discussion 4
 * https://colab.research.google.com/drive/1SlZCgUdeO1KVlCRgC-ojIx3zbx043MXz?usp=sharing
*/
string get_ip_and_port(struct sockaddr_storage sock_addr)
{
    char ip_address[INET6_ADDRSTRLEN];
    int port_num;
    if (sock_addr.ss_family == AF_INET)
    {
        struct sockaddr_in *addr = (struct sockaddr_in *)&sock_addr;
        inet_ntop(AF_INET, &(addr->sin_addr), ip_address, sizeof ip_address);
        port_num = ntohs(addr->sin_port);
    }
    else
    {
        struct sockaddr_in6 *addr = (struct sockaddr_in6 *)&sock_addr;
        inet_ntop(AF_INET, &(addr->sin6_addr), ip_address, sizeof ip_address);
        port_num = ntohs(addr->sin6_port);
    }
    string ip_and_port = string(ip_address) + ":" + to_string(port_num);
    return ip_and_port;
}

/**
 * This helper function is to setup up TCP connection with seller.
 * return -1 when the error occurs.
 * Reference:
 * The implementation of TCP connection was from our Discussion 2:
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
        cerr << "getaddrinfo: error when server getting the address info for tcp" << endl;
        return -1;
    }
    sockfd = socket(servinfo->ai_family, servinfo->ai_socktype, servinfo->ai_protocol);
    if (sockfd == -1)
    {
        cerr << "server: error when creating socket for tcp" << endl;
        return -1;
    }

    int yes = 1;
    setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

    if (::bind(sockfd, servinfo->ai_addr, servinfo->ai_addrlen) == -1)
    {
        close(sockfd);
        cerr << "server: bind failed for tcp" << endl;
        return -1;
    }
    freeaddrinfo(servinfo);
    if (-1 == listen(sockfd, BACKLOG))
    {
        cerr << "server: listen failed for tcp" << endl;
        return -1;
    }
    time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
    cout << put_time(localtime(&curr_time), "%c %Z") << ": Waiting for connection on port " << PORT << "." << endl;
    return 0;
}

/**
 * This function is to setup up UDP connection
 * return -1 when error occurs
 * Reference:
 * The implementation of UDP connection was from our Discussion 4:
 * https://colab.research.google.com/drive/1SlZCgUdeO1KVlCRgC-ojIx3zbx043MXz?usp=sharing
*/
int UDP_setup(int &sockfd, struct addrinfo &hints, struct addrinfo *servinfo)
{
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_DGRAM;
    hints.ai_flags = AI_PASSIVE;
    if (getaddrinfo(NULL, UDP_PORT, &hints, &servinfo) != 0)
    {
        cerr << "server: error when get address information for UDP connection" << endl;
        return -1;
    }
    sockfd = socket(servinfo->ai_family, servinfo->ai_socktype, servinfo->ai_protocol);
    if (sockfd == -1)
    {
        cerr << "server: udp socket failed" << endl;
        return -1;
    }

    /**
     * Reference: 
     * The setting of UDP timeout was from this StackOverFlow post:
     * https://stackoverflow.com/questions/13547721/udp-socket-set-timeout
     */
    struct timeval tv;
    tv.tv_sec = 3;
    tv.tv_usec = 0;
    if (setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (char *)&tv, sizeof tv))
    {
        cerr << "server: error when setting the sockopt for udp connection" << endl;
    }

    if (::bind(sockfd, servinfo->ai_addr, servinfo->ai_addrlen) == -1)
    {
        close(sockfd);
        cerr << "server: udp bind failed" << endl;
        return -1;
    }
    freeaddrinfo(servinfo);
    return 0;
}

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
        // cout << "previous bid price: " << list.at(index).curr_bid_price << ", current bid = " << msg.price << endl;
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
                    cerr << "server: failed to send message to buyer" << endl;
                }
                string ip = get_ip_and_port(prev_buyer_addr);
                print_udp_reply(msg_prev_buyer);
                cout << " to " << ip << "." << endl;
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
    string ip_and_port;

    TCP_setup(sockfd, hints, servinfo);
    UDP_setup(udp_sockfd, udp_hints, udp_servinfo);

    unordered_map<string, vector<item_info>> seller_map = {};

    vector<item_info> item_bid = {};

    chrono::time_point<chrono::steady_clock> last_TCP_arrival = chrono::steady_clock::now();

    auto save_server = [&seller_map, &last_TCP_arrival](int sockfd, int seller_id, string ip_and_port) -> void
    {
        struct TCP_Message msg_recv;
        time_t thread_time;

        while (1)
        {
            if (recv(sockfd, &msg_recv, sizeof(msg_recv), 0) <= 0)
            {
                return;
            }

            {
                lock_guard lg{m};
                string item_name(msg_recv.name);
                if (seller_map.find(item_name) == seller_map.end())
                {
                    seller_map[item_name] = {};
                }
                struct item_info i = {seller_id, 0, msg_recv.price, 0, {}, ip_and_port, sockfd, {}, {}};
                strncpy(i.item_name, msg_recv.name, 20);
                seller_map[item_name].push_back(i);
                last_TCP_arrival = chrono::steady_clock::now();
                thread_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
                cout << put_time(localtime(&thread_time), "%c %Z");
                cout << ": Received " << msg_recv.name << " for sale for " << msg_recv.price << "$";
                cout << " from " << ip_and_port << "(thread: " << this_thread::get_id() << ")"
                     << "." << endl;
            }
        }

        //The TCP connection should be kept connected
        while (1)
        {
        }
    };

    auto UDP_server = [&seller_map, &item_bid, &last_TCP_arrival](int udp_sockfd) -> void
    {
        //this_thread::sleep_for(3s);

        UDP_Message msg_recv, msg_send;
        struct sockaddr_storage udp_client_addr;
        socklen_t udp_client_addr_len = sizeof(udp_client_addr);

        while (recvfrom(udp_sockfd, &msg_recv, sizeof(msg_recv), 0, (struct sockaddr *)&udp_client_addr, &udp_client_addr_len) != -1)
        {
            string ip_and_port = get_ip_and_port(udp_client_addr);
            {
                lock_guard lg{m};
                time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
                cout << put_time(localtime(&curr_time), "%c %Z");
                cout << ": Received bid " << msg_recv.price << "$ on " << msg_recv.name;
                cout << " from " << ip_and_port << "." << endl;
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
                cout << " to " << ip_and_port << "." << endl;
            }
        }

        for (auto it = seller_map.begin(); it != seller_map.end(); it++)
        {
            vector<item_info> list = it->second;
            for (unsigned long i = 0; i < list.size(); i++)
            {
                if (list[i].curr_bid_price > 0)
                {
                    item_bid.push_back(list[i]);
                }
            }
        }
        UDP_Message addr_request;
        TCP_Message addr_to_seller;
        string item_name;
        for (unsigned long i = 0; i < item_bid.size(); i++)
        {
            sockaddr_storage buyer_addr = item_bid[i].buyer_addr;
            socklen_t buyer_addr_len = sizeof buyer_addr;
            string item_name = string(item_bid[i].item_name);
            addr_request.action = MAIL_ADDR;
            addr_request.price = item_bid[i].curr_bid_price;
            strncpy(addr_request.name, item_bid[i].item_name, 20);
            if (sendto(udp_sockfd, &addr_request, sizeof(addr_request), 0, (struct sockaddr *)&(buyer_addr), buyer_addr_len) == -1)
            {
                cerr << "server: failed to send mail address request message to buyer" << endl;
                continue;
            }
            print_curr_time();
            string ip_and_port = get_ip_and_port(buyer_addr);
            cout << ": Requesting shipping address from " << ip_and_port << " for " << item_name << " bought for " << addr_request.price << "$." << endl;

            if (recvfrom(udp_sockfd, &addr_request, sizeof(addr_request), 0, (struct sockaddr *)&(buyer_addr), &buyer_addr_len) > 0)
            {
                strncpy(item_bid[i].mail_address, addr_request.name, 20);
                print_curr_time();
                ip_and_port = get_ip_and_port(buyer_addr);
                cout << ": Received shipping address from " << ip_and_port << " for " << item_name << " bought for " << addr_request.price << "$." << endl;
                addr_to_seller.price = item_bid[i].curr_bid_price;
                strncpy(addr_to_seller.mail_address, item_bid[i].mail_address, 20);
                strncpy(addr_to_seller.name, item_bid[i].item_name, 20);
                if (send(item_bid[i].seller_sockfd, &addr_to_seller, sizeof(addr_to_seller), 0) == -1)
                {
                    cerr << "seller: failed to send message" << endl;
                    continue;
                }
                print_curr_time();
                cout << ": Sharing buyer shipping address, " << item_bid[i].mail_address << ", for ";
                cout << item_bid[i].item_name << " bought for " << item_bid[i].curr_bid_price << "$ with seller " << item_bid[i].seller_ip_and_port << "." << endl;
            }
        }
    };

    thread udp_t{UDP_server, udp_sockfd};
    udp_t.detach();

    while ((new_socket = accept(sockfd, (struct sockaddr *)&client_addr, &sin_size)) > 0)
    {
        if (new_socket == -1)
        {
            cerr << "server: accept failed" << endl;
            break;
        }
        string ip = get_ip_and_port(client_addr);

        thread t{save_server, new_socket, seller_idx, ip};
        seller_idx++;
        t.detach();
    }

    close(new_socket);
    close(sockfd);

    return 0;
}