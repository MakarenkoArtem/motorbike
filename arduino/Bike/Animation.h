#pragma once

#include <Arduino.h>
#include "Parameters.h"
#include "SoundLevelMeter.h"
#include "SoundDecomposition.h"

class Animation {
    Parameters &params;
    SoundLevelMeter &sound;
    SoundDecomposition &fht;
    unsigned long timer = 0;

    byte strode(int period, byte maxBright);

    void convertAmplitudeToListOutput(byte amplitude);

public:
    Animation(Parameters &params, SoundLevelMeter &sound, SoundDecomposition &fht);
//verified 1.02.25
    bool processing();
};