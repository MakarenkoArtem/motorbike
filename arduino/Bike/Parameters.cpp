#include "Parameters.h"

Parameters::Parameters(byte *colors) :
        maxBright(0), mode(11), colors(colors), frequency(0),
        input(malloc(inpCount * sizeof(byte))), output(malloc(outCount * sizeof(byte))) {}

void Parameters::setFrequency(byte frequency) {
    this->frequency=frequency;
    switch (mode/10) {
        case 4:{
            strobePeriod = MIN_STROBE_PERIOD + frequency * 2;
            break;
        }default:{
            strobePeriod = MIN_STROBE_PERIOD + frequency * 10;
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
        case 22: {
            outCount = 1;
            break;
        }
        case 23:
        case 24: {
            outCount = 15;
            break;
        }
        default: {
            outCount = 0;
            break;
        }
    }
}
//verified 1.02.25