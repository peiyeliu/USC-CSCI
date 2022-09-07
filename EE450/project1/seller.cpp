#include <iostream>
#include <fstream>
#include <iomanip>
#include <ctime>
#include <chrono>

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

#include "project.h"
using namespace std;
#define PORT "8000"

/**
 * This function is used to read each line into TCP_Message data structure 
 */
int read_seller_line(string &line, TCP_Message &msg){
    
    auto first_space_pos = line.find(' ', 0);
    if(first_space_pos ==  string::npos){
        cerr << "Error: the seller input file is not valid." << endl;
        return -1;
    }
    msg.price = stoi(line.substr(0, first_space_pos));
    string name = line.substr(first_space_pos + 1);
    strncpy(msg.name, name.c_str(), 19);
    msg.name[19] = '\0';
    for(int i = 0; i < 20; i++){
        if(msg.name[i] == '\r'){
           msg.name[i] = '\0'; 
        }
    }
    return 0;
}

int main(int argc, char *argv[])
{

    if(argc != 2){
        cout << "seller error: the command should be './seller.out [input file name]'" << endl;
        return -1;
    }
    setenv("TZ", "/usr/share/zoneinfo/America/Los_Angeles", 1);
    time_t curr_time;


    //create connection
    int sockfd;
    struct addrinfo hints, *servinfo;
    int rv;
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    if((rv = getaddrinfo("localhost", PORT, &hints, &servinfo)) != 0){
        cerr << "getaddrinfo: error when getting the address info" << gai_strerror(rv) << endl;
        return -1;
    }

    /**
     * Reference: (Line 76 - 90)
     * This part was from "Beej's guide to network programming", Section 6.2 "A Simple Stream Client"
     * Website: http://www.beej.us/guide/bgnet/html/index-wide.html#a-simple-stream-client
     */
    struct addrinfo *p;
    for(p = servinfo; p != NULL; p = p->ai_next) {
        if ((sockfd = socket(p->ai_family, p->ai_socktype,
                p->ai_protocol)) == -1) {
            cout << "seller: error when creating socket" << endl;
            continue;
        }

        if (connect(sockfd, p->ai_addr, p->ai_addrlen) == -1) {
            close(sockfd);
            cout << "seller: error when connecting" << endl;
            continue;
        }
        break;
    }


    //print connections information
    /**
     * Reference: Line 101 - 108
     * This part is from from "Beej's guide to network programming", Section 5.1 "getaddrinfo()—Prepare to launch!"
     * http://www.beej.us/guide/bgnet/html/index-wide.html#getaddrinfoprepare-to-launch 
     * 
     */
    char ip_address[INET6_ADDRSTRLEN];
    void *addr;
    if(servinfo->ai_family == AF_INET){
        addr = &(((struct sockaddr_in *)servinfo->ai_addr)->sin_addr);
    }
    else{
        addr = &(((struct sockaddr_in6 *)servinfo->ai_addr)->sin6_addr);
    }
    inet_ntop(servinfo->ai_family, addr, ip_address, sizeof ip_address);
    curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
    cout << put_time(localtime(&curr_time), "%c %Z") << ": Connected to " << ip_address << " on port " << PORT << "." << endl;
    freeaddrinfo(servinfo);


    // read file
    ifstream file(argv[1]);
    int num_of_entry = -1;
    string line = "";
    if (!file.is_open()) {
        cout << "ERROR: The file could not be open, please type valid file name." << endl;
        file.close();
        return -1;
    }
    getline(file, line);
    num_of_entry = stoi(line);
    curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
    cout << put_time(localtime(&curr_time), "%c %Z") << ": Reading " << num_of_entry << " items from "<< argv[1] << "." << endl;
    
    struct TCP_Message msg;
    for(int i = 0; i < num_of_entry; i++){
        getline(file, line);
        read_seller_line(line, msg);
        if(send(sockfd, &msg, sizeof(msg), 0) == -1){
            cout << "seller: failed to send message" << endl;
            continue;
        }
        curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
        cout << put_time(localtime(&curr_time), "%c %Z") << ": Sent " << msg.name << " for sale for " << msg.price << "$." << endl;
    }
    file.close();

    return 0;
}