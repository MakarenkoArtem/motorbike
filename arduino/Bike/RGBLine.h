#pragma once

#include <FastLED.h>
#include "Parameters.h"
#include "config.h"

class RGBLine {
    const int pin;
    CRGBPalette32 myPal;
    int STROBE_SAT = 255;
    byte id;
    Parameters &params;
    CFastLED *fastLED = nullptr;
public:
    int count;
    CRGB *line;

    RGBLine(int pin, int count, Parameters &params, byte id);

    void setFastLED(CFastLED *fastLED);

    int getPin();

    void setBrightness(byte bright);

    void setColors(byte *colors);

    void show();

    void data();
//verified 1.02.25
private:
    byte calculatePhase(byte phase, int index);

    void renderStaticPattern();

    byte calculatePhaseByAmplitude(byte amplitude, int index);

    void renderFlashByAmplitude(byte amplitude);

    void renderRunningFlashByAmplitude(int countAmp, byte *amplitudes);

    void renderColumn(byte amplitude);
};