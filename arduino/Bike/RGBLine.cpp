#include "RGBLine.h"

RGBLine::RGBLine(int pin, int count, Parameters &params, byte id) :
        pin(pin), count(count), params(params), id(id) {
    line = new CRGB[count];
}

void RGBLine::setFastLED(CFastLED *fastLED) {
    this->fastLED = fastLED;
    setBrightness(params.maxBright);
}

int RGBLine::getPin() {
    return pin;
}

void RGBLine::setColors(byte *colors) {
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
        phase = phase / 43 * 51;
    }
    if (!params.sync && id % 2) {
        phase = 255 - phase;
    }
    return phase;
}

void RGBLine::renderStaticPattern() {//11, 12
    byte phase = 0;
    if (params.movement) {
        phase = millis();
    }
    for (int index = 0; index < count; index++) {
        if (params.hsv) {
            line[index] = CHSV(calculatePhase(phase, index), STROBE_SAT, params.bright);
        } else {
            line[index] = ColorFromPalette(myPal, calculatePhase(phase, index));
        }
    }
}
//verified 1.02.25

byte RGBLine::calculatePhaseByAmplitude(byte amplitude, int index) {
    byte phase = amplitude;
    if (!params.movement) {
        byte phase = index * 255 / (count - 1);
        if (params.gradient) {
            phase = phase / 43 * 51;
        }
        if (!params.sync && id % 2) {
            phase = 255 - phase;
        }
    }
    return (params.gradient) ? phase : phase / 43 * 51;
}

void RGBLine::renderFlashByAmplitude(byte amplitude) {//21
    if (!amplitude) return;
    setBrightness(params.bright);
    for (int index = 0; index < count; ++index) {
        if (params.hsv) {
            line[index] = CHSV(calculatePhaseByAmplitude(amplitude, index), STROBE_SAT, params.bright);
        } else {
            line[index] = ColorFromPalette(myPal, calculatePhaseByAmplitude(amplitude, index));
        }
    }
}

void RGBLine::renderRunningFlashByAmplitude(int countAmp, byte *amplitudes) {//22
    setBrightness(params.bright);
    for (int index = 0; index < count; index++) {
        byte amplitude = amplitudes[index * (countAmp) / (count - 1)];
        if (!amplitude) continue;
        amplitude = (params.gradient) ? amplitude : amplitude / 43 * 51;
        if (params.hsv) {
            line[index] = CHSV(amplitude, STROBE_SAT, params.bright);
        } else {
            line[index] = ColorFromPalette(myPal, amplitude);
        }
    }

}

void RGBLine::renderColumn(byte amplitude) {//23
    if (!amplitude) return;
    setBrightness(params.bright);
    for (int index = 0; index < amplitude * count / 255; index++) {
        if (params.hsv) {
            line[index] = CHSV(calculatePhaseByAmplitude(amplitude, index), STROBE_SAT, params.bright);
        } else {
            line[index] = ColorFromPalette(myPal, calculatePhaseByAmplitude(amplitude, index));
        }
    }
}