#pragma once

#include <Arduino.h>
#include <math.h>

#include "config.h"

//===================================================================
#define AVERK 0.006
#define SMOOTH 0.5          // коэффициент плавности анимации VU (по умолчанию 0.5)
#define LOW_PASS 100
#define MAX_COEF 1.8
#define EXP 2.7             // степень усиления сигнала (для более "резкой" работы) (по умолчанию 1.4)

class SoundLevelMeter {
    int pinR, pinL;

    void (*pinMode)(int, int);

    int (*analogRead)(int);

    float filterValue = 1.1;        // пороговый коэффициент пропускает только сигналы которые в filterValue раз больше среднего
    long long currentTimer = 0;     // время начала пика сигнала(когда он стал больше фильтрующего значения)
    float avegareTimeLight = 200.0; // обязательно с плавающей точкой, тк иначени из-за округления вниз неполучается среднее значение
    float averageLevel = 0;         // средний уровень входного сигнала(обязательно с плавающей точкой, тк иначени из-за округления вниз неполучается среднее значение)
    byte currentAmplitude = 0;      // максимальная амплитуда полученная в рез-те измерений (значения в границах [0, 255])
    byte levelAmplitude = 0;        // в границах [0, 255]
    byte smoothedAmplitude = 0;     // в границах [0, 255]
    float _fastAverageTop=0.0;      // примерная верхняя граница по полученным данным(в границах [0, 255])
    float _fastAverageBottom=0.0;   // примерная нижняя  граница по полученным данным(в границах [0, 255])
    byte expLikeAmplitude = 0;      // уровень после функции по типу экспоненцирования, но медленнее для больших скачков на больших амплитудах

    byte currentLevelOfSound();

    void amplitudeUpdate();
public:
    SoundLevelMeter(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int));

    byte getCurAmplitude();

    byte getLevelAmplitude();

    byte getSmoothedAmplitude();

    // применяем показательную функцию для большей чувствительности на больших амплитудах
    // coef в границах [0, 100] иначел либо переполнение(f(coef)^ampl->inf)
    byte getExpLikeAmplitude(byte coef);

    void whichCurrentLevel(int curVal);

    void whichAvegareTimeLight(int avegareTimeLight);

    //verified 9,04.25
};
