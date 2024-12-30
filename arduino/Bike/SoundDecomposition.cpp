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
        fht_input[i] = analogRead(pinR)-512;
    }
}

uint8_t* SoundDecomposition::analyzeAudio() {
    getSignals();
    fht_window();  // window the data for better frequency response
    fht_reorder(); // reorder the data before doing the fht
    fht_run();     // process the data in the fht
    fht_mag_log(); // take the output of the fht
    return fht_log_out;
}