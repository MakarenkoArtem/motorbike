#pragma once

#include "config.h"
#include "Parameters.h"
#include "SoundLevelMeter.h"
#include "SoundDecomposition.h"

class Animation {
    Parameters& params;
    SoundLevelMeter& sound;
    SoundDecomposition& fht;
    unsigned long timer = 0;
    unsigned long timerRunLine = 0;
    float average = 0;

    uint8_t strode(int period, uint8_t maxBright);
    void runningLineMode(uint8_t getValue());

    void convertAmplitudeToListOutput(uint8_t amplitude);

public:
    Animation(Parameters& params, SoundLevelMeter& sound, SoundDecomposition& fht);
    //verified 1.02.25
    bool processing();
};
