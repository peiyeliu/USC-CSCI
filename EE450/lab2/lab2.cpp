#include <iostream>
#include <fstream>
#include <string>
#include <chrono>
#include <ctime>

#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>

#include <arpa/inet.h>
#include <cstring>



#define PORT "8888"
#define BACKLOG 10
#define MAXDATASIZE 100

using namespace std;


struct Message {
    int  transaction_id;
    unsigned char ip[4];
    chrono::time_point<chrono::steady_clock> time;
};

/**
 * Peiye Liu (USCID: 5961770016)
 * Email: peiyeliu@usc.edu
 * References:
 * The server client connection setup at line 50-72, 131-188 was from our 
 * discussion 2 materials: https://colab.research.google.com/drive/1MGJI8w57Mq8CrX9qKfTYQ0WXV0TAIoE3?usp=sharing
*/
void client(int id) {
    cout << "client started" << endl;
    int sockfd;
    struct Message msg_read, msg_write;
    chrono::time_point<chrono::steady_clock> start_time, end_time;
    struct addrinfo hints, *servinfo;

    int rv, numbytes;

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    if((rv = getaddrinfo("localhost", PORT, &hints, &servinfo)) != 0){
        cerr << "getaddrinfo: error when getting the address info" << gai_strerror(rv) << endl;
        return;
    }

    sockfd = socket(servinfo->ai_family, servinfo->ai_socktype, servinfo->ai_protocol);
    if(sockfd == -1){
        cerr << "client: error when creating socket" << endl;
        return;
    }
    cout << "client: socket created, sockfd = " << sockfd << endl;

    int connect_status = connect(sockfd, servinfo->ai_addr, servinfo->ai_addrlen);
    if(connect_status == -1){
      cerr << "client: connect error" << endl;
      return;
    }
    cout << "client: socket connected" << endl;
    freeaddrinfo(servinfo);

    msg_write.transaction_id = id;
    msg_write.time = chrono::steady_clock::now();
    msg_write.ip[0] = (char)0;
    msg_write.ip[1] = (char)0;
    msg_write.ip[2] = (char)0;
    msg_write.ip[3] = (char)0;

    //client send discovery message
    if((rv = send(sockfd, (void*) &msg_write, sizeof(msg_write), 0)) == -1){
      printf("client: error code is %d \n", errno);
      cerr << strerror(errno) << endl;
      cerr << "client: error when sending discovery message" << endl;
      return;
    }
    
    //client receive offer message
    numbytes = recv(sockfd, (void*) &msg_read, sizeof(msg_read), 0);
    if(numbytes <= 1){
      printf("client: error code is %d \n", errno);
      cerr << strerror(errno) << endl;
      cout << "client: error when receiving offer message" << endl;
      return;
    }


    //client send request message

    msg_read.transaction_id++;
    start_time = msg_read.time;
    if((rv = send(sockfd, (void*) &msg_read, sizeof(msg_read), 0)) == -1){
      cerr << "client: error when sending request message" << endl;
      return;
    }


    //client receive ack message
    numbytes = recv(sockfd, (void*) &msg_read, sizeof(msg_read), 0);
    if(numbytes <= 1){
      printf("client: error code is %d \n", errno);
      cerr << strerror(errno) << endl;
      cout << "client: error when receiving acknowledge message" << endl;
      return;
    }

    end_time = msg_read.time;
    ofstream result("result-client.txt");

    result << chrono::duration_cast<chrono::microseconds>(end_time - start_time).count() << " ";
    result << (int)msg_read.ip[0] << "." << (int)msg_read.ip[1] << "." << (int)msg_read.ip[2] << "." << (int)msg_read.ip[3] << endl;
    result.close();

    close(sockfd); 
}


void server() {
    
    cout << "server started" << endl;
    struct Message msg_read, msg_write;

    int sockfd, new_socket;
    struct addrinfo hints, *servinfo;
    struct sockaddr_storage client_addr;
    socklen_t sin_size;
    int rv, byte_received;

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    if((rv = getaddrinfo(NULL, PORT, &hints, &servinfo)) != 0){
        cerr << "getaddrinfo: error when server getting the address info" << gai_strerror(rv) << endl;
        return;
    }


    sockfd = socket(servinfo->ai_family, servinfo->ai_socktype, servinfo->ai_protocol);
    if(sockfd == -1){
        cerr << "server: error when creating socket" << endl;
        return;
    }

    int yes = 1;
    setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

    rv = ::bind(sockfd, servinfo->ai_addr, servinfo->ai_addrlen);

    if(rv == -1){
        close(sockfd);
        cerr << "server: bind failed" << endl;
        return;
    }

    freeaddrinfo(servinfo);

    rv = listen(sockfd, BACKLOG);

    if(rv == -1){
        cerr << "server: listen failed" << endl;
        return;
    }

    cout << "server: waiting for connections......" << endl;

    sin_size = sizeof client_addr;

    new_socket = accept(sockfd, (struct sockaddr*) &client_addr, &sin_size);

    if(new_socket == -1){
        cout << "server: accept failed" << endl;
        return;
    }

    cout << "server: new socket created" << endl;


    //server receive discovery message
    byte_received = recv(new_socket, (void*) &msg_read, sizeof(msg_read), 0);
    if(byte_received <= 1){
        printf("client: error code is %d \n", errno);
        cerr << strerror(errno) << endl;
        cerr << "server: error when receiving discovery message" << endl;
        return;
    }

    //server send offer message
    //cout << msg_read.transaction_id << endl;
    msg_write.transaction_id = msg_read.transaction_id;
    msg_write.time = chrono::steady_clock::now();
    int ip_last = 1 + rand() % 255;
    msg_write.ip[0] = char(10);
    msg_write.ip[1] = char(0);
    msg_write.ip[2] = char(0);
    msg_write.ip[3] = char(ip_last);
    if((rv = send(new_socket, (void*) &msg_write, sizeof(msg_write), 0)) == -1){
      cerr << "server: error when sending offer message" << endl;
      return;
    }
    ofstream result("result-server.txt");
    cout << msg_read.transaction_id << endl;
    result << msg_read.transaction_id << " " << "10.0.0." << ip_last << endl;
    result.close();

    //server receive request message
    byte_received = recv(new_socket, (void*) &msg_read, sizeof(msg_read), 0);
    if(byte_received == -1){
        printf("client: error code is %d \n", errno);
        cerr << strerror(errno) << endl;
        cerr << "server: error when receiving request message" << endl;
        return;
    }

    //server send ack message
    msg_write.time = chrono::steady_clock::now();
    msg_write.transaction_id = msg_read.transaction_id;
    msg_write.ip[0] = msg_read.ip[0];
    msg_write.ip[1] = msg_read.ip[1];
    msg_write.ip[2] = msg_read.ip[2];
    msg_write.ip[3] = msg_read.ip[3];
    if((rv = send(new_socket, (void*) &msg_write, sizeof(msg_write), 0)) == -1){
      cerr << "server: error when sending acknowledge message" << endl;
      return;
    }

    close(new_socket);
    close(sockfd);
}

int main(int argc, char* argv[]) 
{ 
  if(argc > 1) {
    int id = 596; // This is the first 3 digits of my USCID
    if(argc == 3){
        id = stoi(argv[2]);
    }
    string argument(argv[1]);

    if(argument == "client"){
      client(id);
    } 

    if(argument == "server"){
      server();
    } 
      
  } else {
    cout << "run as: lab2 [client|server]" << endl;
  }
  return 0;
} 