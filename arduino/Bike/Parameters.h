#pragma once
#include <Arduino.h>

#include "RGBLine.h"

class Parameters {
public:
    byte maxBright;
    unsigned short mode;
    byte frequency;
    byte *colors;

    Parameters(RGBLine &line);

    Parameters(byte maxBright, unsigned short mode, byte *colors, byte frequency);
};