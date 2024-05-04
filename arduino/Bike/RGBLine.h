//
// Created by artem on 02.05.24.
//

#ifndef BIKE_RGBLINE_H
#define BIKE_RGBLINE_H

#include <FastLED.h>

class RGBLine {
    const int pin;
    CRGBPalette32 myPal;
    CRGB line;
public:
    byte bright = 255;
    //byte* colors;
    int count;
    int mode = 11;
    RGBLine(int pin, int count);

    void setMode(int mode) { this->mode = mode; };

    void setBright(int bright) { this->bright = bright; };

    void setColors(byte *newColors);

    void load();

    void show();
};


#endif //BIKE_RGBLINE_H
