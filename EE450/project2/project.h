#ifndef PROJECT_H
#define PROJECT_H

#define TCP_PORT "8000"

struct TCP_Message
{
  uint32_t price;
  char name[20];
};

struct UDP_Message
{
  uint32_t action;
  uint32_t price;
  char name[20];
};

#endif /* PROJECT_H */