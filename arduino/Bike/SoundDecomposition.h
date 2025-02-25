#pragma once
#include <Arduino.h>

class SoundDecomposition {
    int pinR, pinL;

    void (*pinMode)(int, int);

    int (*analogRead)(int);

    void getSignals();

    uint8_t* fhtMethods();

public:
    SoundDecomposition(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int));
    int maxFrequency();
    uint8_t* analyzeAudio(uint8_t* output, int size);
};
