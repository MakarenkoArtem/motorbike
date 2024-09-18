//
// Created by artem on 02.05.24.
//
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
#define SOUND_OFF 7
#define SOUND_ON 8
#define BRIGHT 9
#define MODE 10
#define FREQUENCY 11
#define WAIT_INPUT 101

#define TIMEOUT 200//200
#define SPEED 9600
#define DELAY 1000
#define MAXSZ 100


class BTSerial : public SoftwareSerial {
    char buf[MAXSZ];
    short sz = -1;
    unsigned long int timer;
    
public:
    BTSerial(int RX, int TX);

    short getSocket(byte &bright, unsigned short &curMode, byte (&colors)[24], byte &frequency);
};
