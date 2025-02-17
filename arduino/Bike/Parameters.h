#pragma once

#include "config.h"
#include <Arduino.h>

class Parameters {
public:
    bool gradient = true;
    bool movement = true;
    bool hsv = false;
    bool synchrony = true;
    long int strobePeriod = MIN_STROBE_PERIOD;
    byte step = 1;
    byte *colors;
    byte maxBright = 0;
    byte bright = 0;
    unsigned short mode = 11;
    byte frequency = 0;
    int inpCount = 15;
    byte *input;//могут запоминать прошлый максимумы или частотные
    int outCount = 15;
    byte *output;

    Parameters(byte *colors);

    void setFrequency(byte frequency);

    void setMaxBright(byte bright);

    void setMode(byte mode);
};//verified 1.02.25