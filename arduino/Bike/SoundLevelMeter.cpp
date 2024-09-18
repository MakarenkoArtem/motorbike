//
// Created by artem on 05.05.24.
//
#include "SoundLevelMeter.h"

void setADCPrescaler(uint8_t prescaler){
  ADCSRA=(ADCSRA&0b11111000)|prescaler;
}

SoundLevelMeter::SoundLevelMeter(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int))
        : pinR(pinR), pinL(pinL), pinMode(pinMode), analogRead(analogRead) {
}

void SoundLevelMeter::fhtSound() {
    int RcurrentLevel, LcurrentLevel;
    setADCPrescaler(ADC_PRESCALER_2); //изменение времени анализа напряжения, сама операция 3 машинных операции(analogRead ~200мо)
    for (int i = 0; i < 100; i++) { // делаем 100 измерений
        RcurrentLevel = analogRead(pinR);                            // с правого
        LcurrentLevel = analogRead(pinL);                 // и левого каналов
        if (RsoundLevel < RcurrentLevel)
            RsoundLevel = RcurrentLevel;   // ищем максимальное
        if (LsoundLevel < LcurrentLevel)
            LsoundLevel = LcurrentLevel;   // ищем максимальное
    }// фильтруем по нижнему порогу шумов
    setADCPrescaler(ADC_PRESCALER_16);
    RsoundLevel = max(0, map(RsoundLevel, LOW_PASS, 1023, 0, 500));
    LsoundLevel = max(0, map(LsoundLevel, LOW_PASS, 1023, 0, 500));

    // возводим в степень (для большей чёткости работы)
    RsoundLevel = pow(RsoundLevel, EXP);
    LsoundLevel = pow(LsoundLevel, EXP);

    // фильтр
    RsoundLevel_f = RsoundLevel * SMOOTH + RsoundLevel_f * (1 - SMOOTH);
    LsoundLevel_f = LsoundLevel * SMOOTH + LsoundLevel_f * (1 - SMOOTH);

    if (RsoundLevel_f > 15 || LsoundLevel_f > 15) {

        // расчёт общей средней громкости с обоих каналов, фильтрация.
        // Фильтр очень медленный, сделано специально для автогромкости
        averageLevel = (RsoundLevel_f + LsoundLevel_f) / 2 * AVERK + averageLevel * (1 - AVERK);

        // принимаем максимальную громкость шкалы как среднюю, умноженную на некоторый коэффициент MAX_COEF
        maxLevel = averageLevel * MAX_COEF;
        // преобразуем сигнал в длину ленты (где NUM_LEDS это половина количества светодиодов)
        Rlenght = map(RsoundLevel_f, 0, maxLevel, 0, 100);
        Llenght = map(LsoundLevel_f, 0, maxLevel, 0, 100);
        Serial.print(F("Rlenght:"));
        Serial.println(Rlenght);
        // ограничиваем до макс. числа светодиодов
        Rlenght = constrain(Rlenght, 0, 100);
        Llenght = constrain(Llenght, 0, 100);
    }
}

