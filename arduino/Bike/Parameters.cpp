#include "Parameters.h"

Parameters::Parameters(byte* colors) :
    colors(colors),
    input(malloc(inpCount * sizeof(byte))), output(malloc(outCount * sizeof(byte))) {
}

void Parameters::setFrequency(byte frequency) {
    this->frequency = frequency;
    switch (mode / 10) {
        case 4: {
            strobePeriod = MIN_STROBE_PERIOD + frequency * 2;
            break;
        }
        default: {
            strobePeriod = MIN_STROBE_PERIOD * pow(100.0, (100 - frequency) / 100.0);
        }
    }
    step = 1 + frequency;
}

void Parameters::setMaxBright(byte bright) {
    maxBright = bright;
}

void Parameters::setMode(byte mode) {
    this->mode = mode;
    setFrequency(frequency);
    switch (mode) {
        case 21: {
            outCount = 1;
            break;
        }
        case 22:
        case 23:
        case 24: {
            outCount = 9;
            break;
        }
        case 31: {
            outCount = 5;
            break;
        }
        default: {
            outCount = 0;
            break;
        }
    }
}
//verified 1.02.25