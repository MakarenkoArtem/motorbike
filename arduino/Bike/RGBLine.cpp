#include "RGBLine.h"

RGBLine::RGBLine(int pin, int count, byte *colors, byte id) :
        pin(pin), count(count), colors(colors), id(id) {
    line = new CRGB[count];
    changeMode();
    /*switch (pin) {
            //FastLED.addLeds<WS2811, pin, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            //найти реализацию addLeds с переменной pin, а не заданным при компиляции значением
            //https://community.alexgyver.ru/threads/fastled-nastraivaem-piny-i-porjadok-cvetov-na-letu-ili-kak-rabotat-s-nasledovaniem-klassov-v-c.9732/
        case 1: {
            FastLED.addLeds<WS2811, 1, BRG>(&line, count).setCorrection(TypicalLEDStrip);
            break;
        }
    }*/
}

void RGBLine::setFastLED(CFastLED *fastLED) {
    this->fastLED = fastLED;
    setBrightness(bright);
}

int RGBLine::getPin() {
    return pin;
}

void RGBLine::setFrequency(byte frequency) {
    strobePeriod = MIN_STROBE_PERIOD + frequency * 1.5;
}

void RGBLine::setColors(byte *newColors) {
    if (colors != newColors) {
        free(colors);         //опасный момент
        colors = newColors;
    }
    myPal.loadDynamicGradientPalette(colors);
}

void RGBLine::setMode(unsigned short mode) {
    this->mode = mode;
    changeMode();
}

void RGBLine::setBrightness(byte bright) {
    this->bright = bright;
    this->fastLED->setBrightness(bright);
}

void RGBLine::changeMode() {
    switch (mode % 100) {
        case 12:
            this->changeGradientAB();
            break;
        case 13:
            this->changeGradientAC();
            break;
        case 14:
            this->changeGradientAD();
            break;
        default:
            this->setColors(colors);
    }
    switch (mode / 10 % 10) {
        case 4:
            this->fastLED->setBrightness(255);
            break;
    }
    oldMode = mode;
}

void RGBLine::strobeHSV() {
    byte t = 255 * (mode / 100) * (id % 2);
    for (int i = 0; i < count; i++) {
        line[i] = CHSV(hue - t, STROBE_SAT, strobeBright);
    }
}

void RGBLine::strobe() {
    for (int i = 0; i < count; i++) {
        line[i] = ColorFromPalette(myPal, i * 255 / count);
    }
    this->fastLED->setBrightness(strobeBright);
}

void RGBLine::changeGradientAB() {
    byte colors_[8];
    int c = 0;
    for (int j = 2; j; --j) {
        for (int i = 0; i < 4; i++) {
            colors_[c++] = colors[i];
        }
    }
    colors_[--c] = colors[23];
    myPal.loadDynamicGradientPalette(colors_);
}

void RGBLine::changeGradientAC() {
    byte colors_[8];
    int c = 0;
    for (int i = 0; i < 4; i++) {
        colors_[c++] = colors[i];
    }
    for (int i = 20; i < 24; i++) {
        colors_[c++] = colors[i];
    }
    myPal.loadDynamicGradientPalette(colors_);
}

void RGBLine::changeGradientAD() {
    byte colors_[16];
    int c = 0;
    for (int i = 0; i < 4; i++) {
        colors_[c++] = colors[i];
    }
    for (int i = 8; i < 16; i++) {
        colors_[c++] = colors[i];
    }
    for (int i = 20; i < 24; i++) {
        colors_[c++] = colors[i];
    }
    myPal.loadDynamicGradientPalette(colors_);
}


void RGBLine::regGradient() {
    byte j, t = 255 * (mode / 100) * (id % 2);
    for (int i = 0; i < count; i++) {
        j = 255 - (byte) millis() - i * 255 / count;
        line[i] = ColorFromPalette(myPal, j - t);
    }
}

void RGBLine::regHSV() {
    byte t = 255 * (mode / 100) * (id % 2);
    for (int i = 0; i < count; i++) {
        line[i] = CHSV(static_cast<byte>(millis()) - t, STROBE_SAT, t - bright);
    }
}

void RGBLine::blick() {
    /*if (millis() - strobe_timer > STROBE_PERIOD) {
        strobe_timer = millis();
        strobeDwn_flag = false;
    }
    strobeDwn_flag = (millis() - strobe_timer > light_time);
    if (strobeDwn_flag) {                   // гаснем
        if (bright < STROBE_SMOOTH) {              // если пробили мин. яркость
            strobeDwn_flag = false;
            bright = STROBE_SMOOTH;                  // оставить 0
        }else{
          bright -= STROBE_SMOOTH;
          }
    }else {       
        if (bright > 255-STROBE_SMOOTH) {            // если пробили макс. яркость
            bright = 255-STROBE_SMOOTH;                // оставить максимум
            strobeDwn_flag = true;              // флаг опустить
        }else{
          bright += STROBE_SMOOTH;
        }
    }*/
    int t = strobePeriod / 2 * (mode / 100) * (id % 2);
    strobeBright = (((t + millis()) % strobePeriod > 100) * 255);
}

void RGBLine::moveEffect() {
    byte j, t = 255 * (mode / 100) * (id % 2);
    for (int i = 0; i < count; i++) {
        j = 255 - hue - i * 255 / count;
        line[i] = ColorFromPalette(myPal, j - t);
    }
}

void RGBLine::show() {
    switch (mode / 10 % 10) {
        /*case 2:
            level_size();
            break;*/
        case 4:
            blick();
            break;
    }
    switch (mode % 100) {
        case 11:
            this->moveEffect();
            break;
        case 41:
            this->strobeHSV();
            break;
        case 42:
            this->strobe();
            break;
        default:
            for (int i = 0; i < count; i++) {
                line[i] = ColorFromPalette(myPal, (i * 255 / count));
            }
            break;
    }
    if (millis() - hueTimer > hueSpeed) {
        hue += hueStep;
        hueTimer = millis();
    }
}

void RGBLine::data() {
    Serial.print(mode);
    Serial.print(" ");
    Serial.print(bright);
    Serial.print(" ");
    Serial.println(count);
}

float fStubLink = 0;
byte bStubLink = 0;