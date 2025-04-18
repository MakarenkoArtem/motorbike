#include "SoundLevelMeter.h"


SoundLevelMeter::SoundLevelMeter(int pinR, int pinL, void (*pinMode)(int, int),
                                 int (*analogRead)(int))
    : pinR(pinR), pinL(pinL), pinMode(pinMode), analogRead(analogRead) {
    pinMode(pinR, INPUT);
    pinMode(pinL, INPUT);
}


byte SoundLevelMeter::currentLevelOfSound() {
    int top = 0, bottom = 1023, cur; //при m<240 считаем его шумом
    for (int i = 0; i < 100; ++i) {
        cur = analogRead(pinR);
        top = cur > top ? cur : top;
        bottom = cur < bottom ? cur : bottom;
    }
    _fastAverageTop = (_fastAverageTop * 9 + top * 1) / 10;
    _fastAverageBottom = (_fastAverageBottom * 9 + bottom * 1) / 10;
#if DEBUG_LEVEL_SOUND
    Serial.print(" bottom:");
    Serial.print(bottom);
    Serial.print(" top:");
    Serial.print(top);
    Serial.print(" _bottomAver:");
    Serial.print(_fastAverageBottom);
    Serial.print(" _topAver:");
    Serial.println(_fastAverageTop);
#endif
    if (_fastAverageTop - _fastAverageBottom < 40) { //отсекаем шумы при отсутсвии сигнала
        return 0;
    }
    top = map(top, (_fastAverageBottom * 4 + _fastAverageTop) / 5, max(_fastAverageTop * 1.3, top), 0, 255); //ver2
    //300-800, т.к <250-шумы, >800-aux не выдает большее напряжение
    averageLevel = (averageLevel * 20 + top * 1) / 21; //(averageLevel * 100 + top * 5) / 105;
    return top;
}


void SoundLevelMeter::amplitudeUpdate() {
    currentAmplitude = currentLevelOfSound();
#if DEBUG_LEVEL_SOUND
    whichCurrentLevel(currentAmplitude);
    whichAvegareTimeLight(avegareTimeLight);
#endif
    if (currentAmplitude) {
        if (avegareTimeLight < 350) { //если пики слишком короткие по времени
            if (filterValue > 0.80) { //понижаем уровень среза
                filterValue -= 0.01;
            }
        } else if (filterValue < 1.10) {
            filterValue += 0.008;
        }
        if (currentAmplitude > averageLevel * filterValue) {
            if (!levelAmplitude) { //если придыдущая амплитуда была ниже порога
                currentTimer = millis();
            }
            levelAmplitude = currentAmplitude; // map(currentAmplitude, averageLevel*filterValue, 255, 0, 255);
        } else if (levelAmplitude) { //если пик закончился
            avegareTimeLight = (avegareTimeLight * 50 + (millis() - currentTimer)) / 51;
            levelAmplitude = 0;
        } else {
            filterValue -= 0.025;
        }
    } else {
        levelAmplitude = 0;
        currentAmplitude = 0;
    }
    if (currentAmplitude < smoothedAmplitude) {
        smoothedAmplitude -= 20;
    } else { smoothedAmplitude = currentAmplitude; }
}

byte SoundLevelMeter::getExpLikeAmplitude(byte coef) { // значения должны быть в рамках [0, 100]
    amplitudeUpdate();
    //задачем основание показательной функции в пределах [1.1, 1.9]
    float e = 1.1 + coef * 0.008;
    float ampl = map(pow(e, static_cast<float>(currentAmplitude) * 20 / 255), 1, pow(e, 20), 0, 255);
    if (ampl < expLikeAmplitude) {
        if (expLikeAmplitude > 20) { expLikeAmplitude -= 20; } else { expLikeAmplitude = 0; }
    } else {
        expLikeAmplitude = ampl;
    }
    return expLikeAmplitude;
}

byte SoundLevelMeter::getCurAmplitude() {
    amplitudeUpdate();
    return currentAmplitude;
}

byte SoundLevelMeter::getLevelAmplitude() {
    amplitudeUpdate();
    return levelAmplitude;
}

byte SoundLevelMeter::getSmoothedAmplitude() {
    amplitudeUpdate();
    return smoothedAmplitude;
}

void SoundLevelMeter::whichCurrentLevel(int curVal) {
    Serial.print("Current sound level:");
    Serial.print(curVal);
    Serial.print(" averageLevel:");
    Serial.print(averageLevel);
    Serial.print(" filter:");
    Serial.println(averageLevel * filterValue);
}

void SoundLevelMeter::whichAvegareTimeLight(int avegareTimeLight) {
    Serial.print("Time interval:");
    Serial.println(avegareTimeLight);
} //verified 9.04.25