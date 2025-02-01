#pragma once

#include <Arduino.h>
#include <SoftwareSerial.h>

#include "config.h"
#include "mystring.h"
#include "Parameters.h"

#define COMMAND "Co"

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
//verified 1.02.25