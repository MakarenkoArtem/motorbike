#pragma once

#include <Arduino.h>
#include <math.h>

#include "config.h"

//#define DEBUG_SOUND
//============варианты предделителя(количество тактов для analogRead)========
#define ADC_PRESCALER_2 0x01    //лучшая скорость, но малое качество
#define ADC_PRESCALER_4 0x02
#define ADC_PRESCALER_8 0x03
#define ADC_PRESCALER_16 0x04
#define ADC_PRESCALER_32 0x05
#define ADC_PRESCALER_64 0x06
#define ADC_PRESCALER_128 0x07  //default лучшее качество, но малая скорость
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

    float filterValue = 1.1;
    long long currentTimer = 0;
    float avegareTimeLight = 200.0;// обязательно с плавающей точкой, тк иначени из-за округления вниз неполучается среднее значение
    float averageLevel = 0;// обязательно с плавающей точкой, тк иначени из-за округления вниз неполучается среднее значение
    byte currentAmplitude = 0;
    byte levelAmplitude = 0;
    byte smoothedAmplitude = 0;
    float fastAverageTop=0.0;
    float fastAverageBottom=0.0;

    byte currentLevelOfSound();

    void amplitudeUpdate();
public:
    SoundLevelMeter(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int));

    byte getCurAmplitude();

    byte getLevelAmplitude();

    byte getSmoothedAmplitude();

    void whichCurrentLevel(int curVal);

    void whichAvegareTimeLight(int avegareTimeLight);

//verified 11.02.25
    float LsoundLevel, RsoundLevel;
    float LsoundLevel_f, RsoundLevel_f;
    float maxLevel;
    short Rlenght, Llenght;
    void fhtSound();
};
