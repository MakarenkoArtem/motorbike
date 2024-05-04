//
// Created by artem on 02.05.24.
//

#include "RGBLine.h"

RGBLine::RGBLine(int pin, int count) : pin(pin), count(count) {
    line[count];
    switch (pin) {/*
            FastLED.addLeds<WS2811, pin, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            найти реализацию addLeds с переменной pin, а не заданным при компиляции значением*/
        case 1: {
            FastLED.addLeds<WS2811, 1, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 2: {
            FastLED.addLeds<WS2811, 2, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 3: {
            FastLED.addLeds<WS2811, 3, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 4: {
            FastLED.addLeds<WS2811, 4, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 5: {
            FastLED.addLeds<WS2811, 5, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 6: {
            FastLED.addLeds<WS2811, 6, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 7: {
            FastLED.addLeds<WS2811, 7, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 8: {
            FastLED.addLeds<WS2811, 8, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 9: {
            FastLED.addLeds<WS2811, 9, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 10: {
            FastLED.addLeds<WS2811, 10, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 11: {
            FastLED.addLeds<WS2811, 11, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 12: {
            FastLED.addLeds<WS2811, 12, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
        case 13: {
            FastLED.addLeds<WS2811, 13, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
    }
};

void RGBLine::setColors(byte *newColors) {
    /*if (colors != newColors) {
        free(colors);
    }
    colors = newColors;*/
    myPal.loadDynamicGradientPalette(newColors);
}

void RGBLine::load() {
    
}

void RGBLine::show() {
    int c = 0;
    byte j;
    byte colors_[] = {};
    FastLED.clear();          // очистить массив пикселей
    /*switch (mode / 10 == 4) {
        case 2:
            level_size();
            break;
        case 4:
            blick();
            break;
    }
    int t = 255 * (mode / 100);
    //if (this_mode/100==1){t=0;}
    switch (mode % 100) {
        case 11:
            myPal.loadDynamicGradientPalette(colors);
            for (int i = 0; i < NUM_LEDS; i++) {
                j = 255 - hue - i * 255 / NUM_LEDS;
                LLine[i] = ColorFromPalette(myPal, j - t);
                RLine[i] = ColorFromPalette(myPal, j - t);
            }
            FastLED.show();
            return;
        case 41:
            for (int i = 0; i < NUM_LEDS; i++) {
                LLine[i] = CHSV(hue, STROBE_SAT, strobe_bright);
                RLine[i] = CHSV(hue - t, STROBE_SAT, t - strobe_bright);
            }
            FastLED.show();
            return;
        case 12:
            for (int i = 0; i < num; i++) {
                RLine[i] = CRGB(colors[1], colors[2],
                                colors[3]);   // заливка по палитре " от зелёного к красному"
                LLine[i] = CRGB(colors[1], colors[2],
                                colors[3]);   // заливка по палитре " от зелёного к красному"
            }
            FastLED.show();
            return;
        case 13:
            //byte colors_[8];
            for (int i = 0; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 20; i < 24; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            break;
        case 14:
            //byte colors_[16]={};
            for (int i = 0; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 8; i < 16; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 20; i < 24; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            break;
        case 15:
            byte colors_ = colors;
            break;
    }
    myPal.loadDynamicGradientPalette(colors_);
    for (int i = 0; i < num; i++) {
        RLine[i] = ColorFromPalette(myPal, (i * 255 /
                                            NUM_LEDS));   // заливка по палитре " от зелёного к красному"
        LLine[i] = ColorFromPalette(myPal, (i * 255 /
                                            NUM_LEDS));   // заливка по палитре " от зелёного к красному"
    }*/
    FastLED.show();
}
