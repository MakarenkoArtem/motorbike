//
// Created by artem on 05.05.24.
//

#ifndef BIKE_SOUNDLEVELMETER_H
#define BIKE_SOUNDLEVELMETER_H
#include <Arduino.h>
#include <math.h>
#define AVERK 0.006
#define SMOOTH_FREQ
#define SMOOTH 0.5          // коэффициент плавности анимации VU (по умолчанию 0.5)
#define LOW_PASS 100
#define MAX_COEF 1.8 
#define EXP 2.7             // степень усиления сигнала (для более "резкой" работы) (по умолчанию 1.4)

class SoundLevelMeter {

    int pinR, pinL;

    void (*pinMode)(int, int);

    int (*analogRead)(int);
    float LsoundLevel_f, RsoundLevel_f;
public:
    float LsoundLevel, RsoundLevel;
    float averageLevel;
    float maxLevel;

    SoundLevelMeter(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int));

    void fhtSound();
};


#endif //BIKE_SOUNDLEVELMETER_H
