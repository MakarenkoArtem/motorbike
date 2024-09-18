//
// Created by artem on 02.05.24.
//

#include "RGBLine.h"

RGBLine::RGBLine(int pin, int count, byte (&colors)[24], float &sound, byte id) : 
                                                              pin(pin), count(count),
                                                              colors(colors), sound(sound), 
                                                              id(id) {
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
    }*/
};
void RGBLine::setFrequency(byte frequency){
    strobePeriod= StrobePeriod + frequency*1.5;
}
void RGBLine::setColors(byte *newColors) {
    if (colors != newColors) {
        free(colors);
        colors = newColors;
    }
    myPal.loadDynamicGradientPalette(colors);
}
void RGBLine::setMode(unsigned short mode) { 
      this->mode = mode;
      changeMode();
};
void RGBLine::setBrightness(byte bright) { 
      this->bright = bright;
      this->fastLED->setBrightness(bright);
     }

void RGBLine::changeMode() {
    switch (mode%100) {
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
    switch (mode/10%10){
      case 4:
            this->fastLED->setBrightness(255);
            break;
    }
    oldMode = mode;
}

void RGBLine::strobeHSV(){
    byte t = 255 * (mode / 100) * (id%2);
    for (int i = 0; i < count; i++) {
        line[i] = CHSV(hue-t, STROBE_SAT, strobeBright);
    }
}

void RGBLine::strobe(){
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
    byte j, t = 255 * (mode / 100) * (id%2);
    for (int i = 0; i < count; i++) {
        j = 255 - (byte) millis() - i * 255 / count;
        line[i] = ColorFromPalette(myPal, j - t);        
    }
}

void RGBLine::regHSV() {
    byte t = 255 * (mode / 100) * (id%2);
    for (int i = 0; i < count; i++) {
        line[i] = CHSV(static_cast<byte>(millis()) - t, STROBE_SAT, t - bright);
        //RLine[i] = CHSV((byte)millis() - t, STROBE_SAT, t - bright);
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
    int t = strobePeriod/2 * (mode / 100) * (id%2);
    strobeBright = (((t+millis())%strobePeriod>100)*255);
}

void RGBLine::moveEffect(){
    byte j, t = 255 * (mode / 100) * (id%2);
        /*j = 255 - hue ;
        Serial.print("Start: ");
        Serial.println( j - t);*/
    for (int i = 0; i < count; i++) {
        j = 255 - hue - i * 255 / count;
        line[i] = ColorFromPalette(myPal, j - t);
    }
}

void RGBLine::show() {
    /*Serial.print(oldMode);
    Serial.print(" ");
    Serial.print(mode);
    if (oldMode != mode) {
        this->changeMode();
    }*/
    switch (mode / 10%10) {
        /*case 2:
            level_size();
            break;*/
        case 4:
            blick();
            break;
    }
    switch (mode%100) {
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
          for (int i = 0; i < count; i++) {//!!!!!
              line[i] = ColorFromPalette(myPal,
                                        (i * 255 / count));   // заливка по палитре " от зелёного к красному"
          }
          break;
    }
    if(millis() - hueTimer > hueSpeed){
      hue+=hueStep;
      hueTimer = millis();
    }
    //hue += hueStep;
}
