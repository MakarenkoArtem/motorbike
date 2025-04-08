#include "Animation.h"

Animation::Animation(Parameters& params, SoundLevelMeter& sound, SoundDecomposition& fht) :
    params(params), sound(sound), fht(fht) {
}


byte Animation::strode(int period, byte maxBright) {
    int halfPeriod = period / 2;
    int curBright = ((millis() % period > halfPeriod) ?
                         //0-возрастает, 1-убывает
                         halfPeriod - millis() % halfPeriod :
                         millis() % halfPeriod);
    return map(curBright, 0, halfPeriod, 0, maxBright);
}

void Animation::convertAmplitudeToListOutput(byte amplitude) {
    for (byte i = 0; i < params.outCount; ++i) {
        if (i < map(amplitude, 0, 255, 0, params.outCount)) {
            params.output[i] = map(i, 0, params.outCount-1, 0, 255);//amplitude;
        } else {
            params.output[i] = 0;
        }
    }
}

void Animation::runningLineMode() {
    if (timerRunLine < millis()) {
        for (byte i = params.outCount - 1; i; --i) {
            params.output[i] = params.output[i - 1];
        }
        timerRunLine = millis();
        int step = (100 - params.frequency)+25;
        timerRunLine = timerRunLine + step >= timerRunLine ? timerRunLine + step : 0;
        params.output[0] = sound.getLevelAmplitude();
        if (params.output[0] != 0) {
            average = (average * 10 + params.output[0]) / 11;
            params.output[0] = map(params.output[0], average * 0.7, min(average * 1.45, 255), 0, 255);
        }
    }
    sound.getLevelAmplitude();
}

bool Animation::processing() {
    if (timer+50 > millis()) {
        return false;
    }
    timer = millis();
    switch (params.mode) {
        case 11: {
            params.bright = params.maxBright;
            break;
        }
        case 12: {
            params.bright = strode(params.strobePeriod, params.maxBright);
            break;
        }
        case 21: {
            params.output[0] = sound.getLevelAmplitude();
            params.bright = params.maxBright;
            break;
        }
        case 22: {
            params.bright = params.maxBright;
            runningLineMode();
            break;
        }
        case 23: {
            params.output[0] = sound.getExpLikeAmplitude(params.frequency);
            params.bright = params.maxBright;
            #if DEBUG_ANIMATION
                Serial.print("Exp amplitude:");
                Serial.println(params.output[0]);
            #endif
            break;
        } //verified 8.04.25
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
