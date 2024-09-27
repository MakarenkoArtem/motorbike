#pragma once

#include <FastLED.h>

#include "config.h"

#ifndef CONFIG
#define MIN_STROBE_PERIOD 150
#endif

extern float fStubLink;
extern byte bStubLink;

class RGBLine {
    unsigned short oldMode = 11;
    const int pin;
    CRGBPalette32 myPal;
    float &sound = fStubLink;
    long int strobePeriod = MIN_STROBE_PERIOD;
    int STROBE_SAT = 255;
    byte strobeBright = 0;
    byte id;
    byte hue = 0;
    byte hueStep = 2;
    int hueSpeed = 3;
    int hueTimer = millis();
public:
    byte *colors = &bStubLink;
    CRGB *line;
    byte bright = 0;
    int count;
    unsigned short mode = 11;
    byte frequency = 0;
    CFastLED *fastLED = nullptr;

    RGBLine(int pin, int count, byte *colors, byte id);

    void setFastLED(CFastLED *fastLED);

    int getPin();

    void setMode(unsigned short mode);

    void setBrightness(byte bright);

    void setColors(byte *newColors);

    void setFrequency(byte frequency);

    void changeMode();

    void regGradient();

    void strobeHSV();

    void strobe();

    void moveEffect();

    void regHSV();

    void show();

    void regAA();

    void regAB();

    void regDA();

    void changeGradientAB();

    void changeGradientAC();

    void changeGradientAD();

    void blick();

    void data();
};