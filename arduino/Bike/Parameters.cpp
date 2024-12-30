#include "Parameters.h"

Parameters::Parameters(RGBLine &line) :
        maxBright(line.maxBright), mode(line.mode),
        colors(line.colors), frequency(line.frequency) {};

Parameters::Parameters(byte maxBright, unsigned short mode, byte *colors, byte frequency) :
        maxBright(maxBright), mode(mode),
        colors(colors), frequency(frequency) {};
