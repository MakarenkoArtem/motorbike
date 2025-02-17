#include "Animation.h"

Animation::Animation(Parameters &params, SoundLevelMeter &sound, SoundDecomposition &fht) :
        params(params), sound(sound), fht(fht) {}


byte Animation::strode(int period, byte maxBright) {
    int halfPeriod = period / 2;
    int curBright = ((millis() % period / halfPeriod) ?     //0-возрастает, 1-убывает
                     halfPeriod - millis() % halfPeriod :
                     millis() % halfPeriod) *
                    255 * 2 / period;
    return map(curBright, 0, halfPeriod, 0, maxBright);
}

void Animation::convertAmplitudeToListOutput(byte amplitude) {
    for (byte i = 0; i < params.outCount; ++i) {
        if (i < map(amplitude, 0, 255, 0, params.outCount)) {
            params.output[0] = amplitude;
        } else {
            params.output[0] = 0;
        }
    }
}

bool Animation::processing() {
    if (!(timer < millis() - 50 || timer > millis())) {
        return false;
    }
    timer = millis();
    switch (params.mode) {
        case 11: {
            params.bright = params.maxBright;
            break;
        }
        case 12: {
            params.bright = strode(params.strobePeriod * 10, params.maxBright);
            break;
        }
        case 21: {
            params.bright = sound.getLevelAmplitude();
            break;
        }
        case 22: {
            for (byte i = 1; i < params.outCount; ++i) {
                params.output[i] = params.output[i - 1];
            }
            params.output[0] = sound.getLevelAmplitude();
            break;
        }
        case 23: {
            byte amplitude = sound.getSmoothedAmplitude();
            convertAmplitudeToListOutput(amplitude);
            break;
        }//verified 1.02.25
        case 31: {
            break;
        }
        case 32: {
            break;
        }
        case 33: {
            break;
        }
        case 41: {
            params.bright = strode(params.strobePeriod, params.maxBright);
            break;
        }
    }
    return true;
}