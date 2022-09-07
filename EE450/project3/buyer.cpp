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

#define NO_BIDDING 0
#define OFFER 1
#define ACCEPTED 2
#define REJECTED 3
// add a new status for Phase 3 UDP messages;
#define MAIL_ADDR 4

#define UDP_PORT "8001"

#include <thread>
#include <mutex>

using namespace std;
using namespace chrono_literals;

int read_buyer_line(string &line, UDP_Message &msg)
{
  auto first_space_pos = line.find(' ', 0);
  if (first_space_pos == string::npos)
  {
    cerr << "Error: the seller input file is not valid." << endl;
    return -1;
  }
  msg.price = stoi(line.substr(0, first_space_pos));
  string name = line.substr(first_space_pos + 1);
  strncpy(msg.name, name.c_str(), 19);
  msg.name[19] = '\0';
  for (int i = 0; i < 20; i++)
  {
    if (msg.name[i] == '\r')
    {
      msg.name[i] = '\0';
    }
  }
  return 0;
}

/**
 * this function will print the bid result received from server
*/
void print_bid_result(UDP_Message &msg)
{
  time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
  cout << put_time(localtime(&curr_time), "%c %Z");
  switch (msg.action)
  {
  case NO_BIDDING:
    cout << ": Received bidding not open." << endl;
    break;
  case ACCEPTED:
    cout << ": Received bid " << msg.price << "$ on " << msg.name << " accepted." << endl;
    break;
  case REJECTED:
    cout << ": Received bid on " << msg.name << " rejected, current bid price " << msg.price << "$." << endl;
    break;
  default:
    cout << ": the message received is not valid " << msg.action << endl;
    break;
  }
}

void print_send_msg(UDP_Message &msg)
{
  time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
  cout << put_time(localtime(&curr_time), "%c %Z");
  cout << ": Sent bid " << msg.price << " on " << msg.name << "." << endl;
}

/**
 * Reference:
 * In the main function, the implementation of UDP connection was from our Discussion 4:
 * https://colab.research.google.com/drive/1SlZCgUdeO1KVlCRgC-ojIx3zbx043MXz?usp=sharing
*/
int main(int argc, char *argv[])
{
  setenv("TZ", "/usr/share/zoneinfo/America/Los_Angeles", 1);
  struct addrinfo hints, *servinfo;

  int sockfd;
  int rv;

  if (argc != 3)
  {
    // there should be 3 arguments for phase 3
    cout << "buyer: client hostname" << endl;
    return 1;
  }

  memset(&hints, 0, sizeof hints);
  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_DGRAM;

  rv = getaddrinfo("localhost", UDP_PORT, &hints, &servinfo);
  if (rv != 0)
  {
    cerr << "getaddrinfo: " << gai_strerror(rv) << endl;
    return 1;
  }

  sockfd = socket(servinfo->ai_family, servinfo->ai_socktype, servinfo->ai_protocol);
  if (sockfd == -1)
  {
    cerr << "buyer: socket" << endl;
    return 1;
  }

  freeaddrinfo(servinfo);

  ifstream file(argv[1]);
  int num_of_entry = -1;
  string line = "";
  if (!file.is_open())
  {
    cerr << "ERROR: The file could not be open, please type valid file name." << endl;
    file.close();
    return -1;
  }

  getline(file, line);
  num_of_entry = stoi(line);
  time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
  cout << put_time(localtime(&curr_time), "%c %Z") << ": Reading " << num_of_entry << " items from " << argv[1] << "." << endl;

  struct UDP_Message msg;
  msg.action = OFFER;
  struct UDP_Message msg_recv;
  struct sockaddr_storage recv_addr;
  socklen_t recv_addr_len = sizeof(recv_addr);
  //cout << "buyer: before the while loop" << endl;
  for (int i = 0; i < num_of_entry; i++)
  {
    getline(file, line);
    read_buyer_line(line, msg);
    int numbytes = sendto(sockfd, &msg, sizeof(msg), 0, servinfo->ai_addr, servinfo->ai_addrlen);
    if (numbytes == -1)
    {
      cerr << "buyer: failed to send message to server" << endl;
    }
    //cout << "buyer: bid message has been sent" << endl;
    print_send_msg(msg);

    while (1)
    {
      rv = recvfrom(sockfd, &msg_recv, sizeof(msg_recv), 0, (struct sockaddr *)&recv_addr, &recv_addr_len);
      if (rv <= 0 || msg_recv.action == NO_BIDDING)
      {
        if (rv > 0 && msg_recv.action == NO_BIDDING)
        {
          print_bid_result(msg_recv);
        }
        this_thread::sleep_for(0.5s);
        if (sendto(sockfd, &msg, sizeof(msg), 0, servinfo->ai_addr, servinfo->ai_addrlen) == -1)
        {
          cerr << "buyer: failed to send message to server" << endl;
          continue;
        }

        print_send_msg(msg);
      }
      else if (string(msg_recv.name) != string(msg.name))
      {
        print_bid_result(msg_recv);
      }
      else
      {
        break;
      }
    }
    print_bid_result(msg_recv);
    this_thread::sleep_for(0.5s);
  }

  file.close();

  while (1)
  {
    rv = recvfrom(sockfd, &msg_recv, sizeof(msg_recv), 0, (struct sockaddr *)&recv_addr, &recv_addr_len);
    if (rv <= 0)
    {
      cerr << "buyer: no message received" << endl;
      continue;
    }
    if (msg_recv.action == MAIL_ADDR)
    {
      time_t curr_time = chrono::system_clock::to_time_t(chrono::system_clock::now());
      cout << put_time(localtime(&curr_time), "%c %Z");
      cout << ": Received shipping address share request for " << msg_recv.name << " bought for " << msg_recv.price << "$." << endl;
      strncpy(msg_recv.name, argv[2], 20);
      if (sendto(sockfd, &msg_recv, sizeof(msg_recv), 0, servinfo->ai_addr, servinfo->ai_addrlen) == -1)
      {
        cerr << "buyer: failed to send message to server" << endl;
        break;
      }
    }
    else
    {
      print_bid_result(msg_recv);
    }
  }

  close(sockfd);

  return 0;
}