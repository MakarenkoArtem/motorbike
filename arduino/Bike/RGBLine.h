#pragma once

#include "config.h"
#include <FastLED.h>

extern float fStubLink;
extern byte bStubLink;

class RGBLine {
    unsigned short oldMode = 11;
    const int pin;
    CRGBPalette32 myPal;
    byte *colors = &bStubLink;
    float &sound = fStubLink;
    long int strobePeriod = StrobePeriod;
    //int STROBE_SMOOTH = 75;
    //long int strobeTimer = 0;
    //bool strobeUp_flag = true, strobeDwn_flag = false;
    //long int light_time = STROBE_PERIOD / 2;
    int STROBE_SAT = 255;
    byte strobeBright = 0;
    byte id;
    byte hue = 0;
    byte hueStep = 2;
    int hueSpeed = 3;
    int hueTimer = millis();
public:
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