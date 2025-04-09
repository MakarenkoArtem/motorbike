#include "SoundDecomposition.h"
#define LOG_OUT 1
#define FHT_N 32//256
#include<FHT.h>


SoundDecomposition::SoundDecomposition(int pinR, int pinL, void (*pinMode)(int, int),
                                       int (*analogRead)(int))
    : pinR(pinR), pinL(pinL), pinMode(pinMode), analogRead(analogRead) {
    pinMode(pinR, INPUT);
    pinMode(pinL, INPUT);
}

void SoundDecomposition::getSignals() {
    for (int i = 0; i < FHT_N; i++) {
        fht_input[i] = analogRead(pinR) - 512;
    }
}

uint8_t* SoundDecomposition::fhtMethods() {
    getSignals();
    fht_window(); // window the data for better frequency response
    fht_reorder(); // reorder the data before doing the fht
    fht_run(); // process the data in the fht
    fht_mag_log(); // take the output of the fht
    return fht_log_out;
}
int SoundDecomposition::maxFrequency() {
    uint8_t* input = fhtMethods();
    int maxVal = input[0];
    int index=0;
    for (int i = 1; i < FHT_N; i++) {
        if (maxVal < input[i]) {
            maxVal = input[i];
            index = i;
        }
    }
    return index*255/(FHT_N-1);
}

uint8_t* SoundDecomposition::analyzeAudio(uint8_t* output, int size) {
    uint8_t* input = fhtMethods();
    float pos, frac;
    int left, right, oldSize = FHT_N;
    for (int i = 0; i < size; i++) {
        pos = i * (oldSize - 1) / (size - 1);
        left = pos;
        right = pos + 1;
        frac = pos - left;
        if (right >= oldSize) { right = oldSize - 1; }
        output[i] = input[left] * (1 - frac) + input[right] * frac;
    }
    return output;
}
