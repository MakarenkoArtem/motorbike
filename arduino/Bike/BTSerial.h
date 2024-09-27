#pragma once

#include <Arduino.h>
#include <SoftwareSerial.h>

#include "config.h"
#include "mystring.h"
#include "Parameters.h"

#ifndef CONFIG
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

#define TIMEOUT 200
#define SPEED 9600
#define DELAY 1000
#define MAXSZ 100
#endif

class BTSerial : public SoftwareSerial {
    char buf[MAXSZ];
    short sz = -1;
    unsigned long int timer;

public:
    BTSerial(int RX, int TX);

    short getCommands(Parameters &parameters);

private:
    short messageProcessing(Parameters &parameters);

    short changeColors(char *buf, byte *colors);
};

