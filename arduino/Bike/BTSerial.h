//
// Created by artem on 02.05.24.
//
#ifndef BIKE_BTSERIAL_H
#define BIKE_BTSERIAL_H
//#include "c++/11/execution"
//#include "exception"
#include <Arduino.h>
#include <SoftwareSerial.h>
#include "mystring.h"
//#include <string.h>
#define ERROR -1
#define OK 0
#define OFF 1
#define ON 2
#define END 3
#define BRIGHT 4
#define TYPE 5
#define COLORS 6
#define WAIT_INPUT 101

class BTSerial : public SoftwareSerial {
    const unsigned int maxSz = 100;
    char buf[100];
    unsigned int sz = 0;
    char endChar = '\n';
    unsigned int timeOut = 200;//200;
    unsigned int delay = 1000;
    unsigned long int timer;
public:
    BTSerial(int RX, int TX);

    int getSocket(byte &bright, int &curMode, byte (&colors)[24], char* &buf, int &sz);
};

#endif //BIKE_BTSERIAL_H
