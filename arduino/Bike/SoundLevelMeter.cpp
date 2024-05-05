//
// Created by artem on 05.05.24.
//
#include "SoundLevelMeter.h"
SoundLevelMeter::SoundLevelMeter(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int))
        : pinR(pinR), pinL(pinL), pinMode(pinMode), analogRead(analogRead) {
}

void SoundLevelMeter::fhtSound() {
    int RcurrentLevel, LcurrentLevel;
    for (int i = 0; i < 100; i++) { // делаем 100 измерений
        RcurrentLevel = analogRead(pinR);                            // с правого
        LcurrentLevel = analogRead(pinL);                 // и левого каналов
        if (RsoundLevel < RcurrentLevel)
            RsoundLevel = RcurrentLevel;   // ищем максимальное
        if (LsoundLevel < LcurrentLevel)
            LsoundLevel = LcurrentLevel;   // ищем максимальное
    }// фильтруем по нижнему порогу шумов
    //Serial.print(RsoundLevel);
    //Serial.print(" ");
    //Serial.println(LOW_PASS);
    RsoundLevel = max(0, map(RsoundLevel, LOW_PASS, 1023, 0, 500));
    LsoundLevel = max(0, map(LsoundLevel, LOW_PASS, 1023, 0, 500));

    // возводим в степень (для большей чёткости работы)
    RsoundLevel = pow(RsoundLevel, EXP);
    LsoundLevel = pow(LsoundLevel, EXP);
    // ограничиваем диапазон
    RsoundLevel = constrain(RsoundLevel, 0, 500);
    LsoundLevel = constrain(LsoundLevel, 0, 500);

    // фильтр
    RsoundLevel_f = RsoundLevel * SMOOTH + RsoundLevel_f * (1 - SMOOTH);
    LsoundLevel_f = LsoundLevel * SMOOTH + LsoundLevel_f * (1 - SMOOTH);

    if (RsoundLevel_f > 15 || LsoundLevel_f > 15) {

        // расчёт общей средней громкости с обоих каналов, фильтрация.
        // Фильтр очень медленный, сделано специально для автогромкости
        averageLevel = (float) (RsoundLevel_f + LsoundLevel_f) / 2 * AVERK + averageLevel * (1 - AVERK);

        // принимаем максимальную громкость шкалы как среднюю, умноженную на некоторый коэффициент MAX_COEF
        maxLevel = (float) averageLevel * MAX_COEF;
        /*Rlenght_last = Rlenght;
        // преобразуем сигнал в длину ленты (где NUM_LEDS это половина количества светодиодов)
        Rlenght = map(RsoundLevel_f, 0, maxLevel, 0, NUM_LEDS);
        Llenght = map(LsoundLevel_f, 0, maxLevel, 0, NUM_LEDS);

        // ограничиваем до макс. числа светодиодов
        Rlenght = constrain(Rlenght, 0, NUM_LEDS);
        Llenght = constrain(Llenght, 0, NUM_LEDS);
        if (Rlenght_last > Rlenght) {
            Rlenght = Rlenght_last - 1;
        }
    } else {
        Rlenght -= 5;
*/
    }
}

