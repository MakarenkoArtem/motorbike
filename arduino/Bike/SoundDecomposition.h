#pragma once
#include <Arduino.h>
#include "config.h"
#define FHT_N 128
#define COUNT_FREQ 60

//============варианты предделителя(количество тактов для analogRead)========
#define ADC_PRESCALER_2 0x01    //лучшая скорость, но малое качество
#define ADC_PRESCALER_4 0x02
#define ADC_PRESCALER_8 0x03
#define ADC_PRESCALER_16 0x04
#define ADC_PRESCALER_32 0x05
#define ADC_PRESCALER_64 0x06
#define ADC_PRESCALER_128 0x07  //default лучшее качество, но малая скорость

class SoundDecomposition {
    int pinR, pinL;

    void (*pinMode)(int, int);

    int (*analogRead)(int);

    void getSignals();

    uint8_t* fhtMethods();

    void frequencyGrouping(uint8_t* input, uint8_t* output);

    int groups[5][2] = { //для FHT_N=128
            {2, 4}, // Очень низкие
            {4, 6}, // Низкие
            {6, 10}, // Средние
            {10, 19}, // Высокие
            {19, COUNT_FREQ} // Очень высокие (60 примерно 15 кГц)
        };

    void lowLevelFiltration(uint8_t* output);

    float averageMinLevel = 0 ; //средний уровень минимальной амплитуды из значений group(обязательно с плавающей точкой, тк иначени из-за округления вниз неполучается среднее значение)

public:
    SoundDecomposition(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int));
    uint8_t frequencyWithMaxAmplitude(); //вернет примерное расположение макс чатсоты
    uint8_t* analyzeAudio(uint8_t* output, int size);
    void getGroup(uint8_t* output);
};
