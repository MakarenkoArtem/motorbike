#pragma once
#include <Arduino.h>

#include "RGBLine.h"

class Parameters {
public:
    byte bright;
    unsigned short mode;
    byte frequency;
    byte *colors;

    Parameters(RGBLine &line);

    Parameters(byte bright, unsigned short mode, byte *colors, byte frequency);
};