#pragma once
#include <Arduino.h>
/*extern int fht_input[];
extern uint8_t fht_log_out[];
extern void fht_window(void);
extern void fht_reorder(void);
extern void fht_run(void);
extern void fht_mag_log(void);*/
class SoundDecomposition {
    int pinR, pinL;

    void (*pinMode)(int, int);

    int (*analogRead)(int);
public:
    SoundDecomposition(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int));
    void getSignals();

    uint8_t* analyzeAudio();
};