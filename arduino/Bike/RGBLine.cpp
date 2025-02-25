#include "RGBLine.h"

RGBLine::RGBLine(int pin, int count, Parameters& params, byte id) :
    pin(pin), count(count), params(params), id(id) {
    line = new CRGB[count];
    setColors(params.colors);
    show();
}

void RGBLine::setFastLED(CFastLED* fastLED) {
    this->fastLED = fastLED;
    setBrightness(params.maxBright);
}

int RGBLine::getPin() {
    return pin;
}

void RGBLine::setColors(byte* colors) {
    myPal.loadDynamicGradientPalette(colors);
}

void RGBLine::setBrightness(byte bright) {
    this->fastLED->setBrightness(bright);
}

void RGBLine::show() {
    setBrightness(params.bright);
    switch (params.mode) {
    case 11:
    case 12:
        renderStaticPattern();
        break;
    case 21: {
        renderFlashByAmplitude(params.output[0]);
        break;
    }
    case 22: {
        renderRunningFlashByAmplitude(params.outCount, params.output);
        break;
    }
    case 23: {
        renderColumn(params.output[0]);
        break;
    }
    case 31: {
        //renderFlashByFrequency(params.outCount, params.output);
    }
    default: {
        renderStaticPattern();
        break;
    }
    }
}

void RGBLine::data() {
    Serial.print("Mode: ");
    Serial.print(params.mode);
    Serial.print(" maxBright:");
    Serial.print(params.maxBright);
    Serial.print(" count:");
    Serial.println(count);
}

byte RGBLine::calculatePhase(byte phase, int index) {
    phase *= params.frequency;
    phase += index * 255 / (count - 1);
    if (!params.gradient) {
        phase = (phase-26)/ 51*51+26;
    }
    if (!params.synchrony && id % 2) {
        phase = 255 - phase;
    }
    return phase;
}

void RGBLine::renderStaticPattern() { //11, 12
    byte phase = 0;
    if (params.movement) {
        phase = millis();
    }
    for (int index = 0; index < count; index++) {
        if (params.hsv) {
            line[index] = CHSV(calculatePhase(phase, index), 255, params.bright);
        } else {
            line[index] = ColorFromPalette(myPal, calculatePhase(phase, index));
        }
    }
}


byte RGBLine::calculatePhaseByAmplitude(byte amplitude, int index) {
    byte phase = params.movement ? millis() : amplitude;
    if (params.gradient) {
        phase += index * 255 / (count - 1);
        if (!params.synchrony && id % 2) {
            phase = 255 - phase;
        }
    }
    return phase;
}

void RGBLine::renderFlashByAmplitude(byte amplitude) { //21
    if (!amplitude) return;
    for (int index = 0; index < count; ++index) {
        if (params.hsv) {
            line[index] = CHSV(calculatePhaseByAmplitude(amplitude, index), 255, params.bright);
        } else {
            line[index] = ColorFromPalette(myPal, calculatePhaseByAmplitude(amplitude, index));
        }
    }
}

void RGBLine::renderRunningFlashByAmplitude(int countAmp, byte* amplitudes) { //22
    for (int index = 0; index < count; index++) {
        byte amplitude = amplitudes[index * countAmp / (count - 1)];
        if (!amplitude) continue;
        if (params.hsv) {
            line[index] = CHSV(calculatePhaseByAmplitude(amplitude, 0), 255, params.bright);
        } else {
            line[index] = ColorFromPalette(myPal, calculatePhaseByAmplitude(amplitude, 0));
        }
    }
}

void RGBLine::renderColumn(byte amplitude) { //23
    if (!amplitude) return;
    for (int index = 0; index < amplitude * count / 255; index++) {
        if (params.hsv) {
            line[index] = CHSV(calculatePhaseByAmplitude(amplitude, index), 255, params.bright);
        } else {
            line[index] = ColorFromPalette(myPal, calculatePhaseByAmplitude(amplitude, index));
        }
    }
}

//verified 18.02.25

/*
void RGBLine::renderFlashByFrequency(int countFreq, byte* frequencies) { //22
    for (int index = 0; index < count; index++) {
        byte curIndex = index * countFreq / (count - 1);
        byte amplitude = frequencies[curIndex];
        if (!amplitude) continue;
        if (params.hsv) {
            line[index] = CHSV(curIndex, 255, amplitude);
        } else {
            line[index] = ColorFromPalette(myPal, curIndex * amplitude);
        }
    }
}*/
