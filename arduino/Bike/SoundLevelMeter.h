//
// Created by artem on 05.05.24.
//
#include <Arduino.h>
#include <math.h>
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
    float LsoundLevel_f, RsoundLevel_f;
public:
    float LsoundLevel, RsoundLevel;
    float averageLevel;
    float maxLevel;
    short Rlenght, Llenght;

    SoundLevelMeter(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int));

    void fhtSound();
};