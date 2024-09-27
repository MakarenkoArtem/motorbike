#include "Parameters.h"

Parameters::Parameters(RGBLine &line) :
        bright(line.bright), mode(line.mode),
        colors(line.colors), frequency(line.frequency) {};

Parameters::Parameters(byte bright, unsigned short mode, byte *colors, byte frequency) :
        bright(bright), mode(mode),
        colors(colors), frequency(frequency) {};
