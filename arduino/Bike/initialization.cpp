#include "initialization.h"

void initAssembly() {
    sbi(ADCSRA, ADPS2);
    cbi(ADCSRA, ADPS1);
    sbi(ADCSRA, ADPS0);
}

void initAudio() {
#if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
    analogReference(INTERNAL1V1);
#else
    analogReference(INTERNAL);
#endif
}

void initSerial() {
    Serial.begin(9600);
    Serial.println("Setup");
}

void initSwitchAudio() {
    pinMode(AUDIO_OFF, OUTPUT);
    digitalWrite(AUDIO_OFF, LOW);
}

void addLedsInFastLED(RGBLine &line) {
    switch (line.getPin()) {
        //FastLED.addLeds<WS2811, pin, BRG>(&line, count).setCorrection(TypicalLEDStrip);
        //найти реализацию addLeds с переменной pin, а не заданным при компиляции значением
        //https://community.alexgyver.ru/threads/fastled-nastraivaem-piny-i-porjadok-cvetov-na-letu-ili-kak-rabotat-s-nasledovaniem-klassov-v-c.9732/
        case LLine_pin: {
            FastLED.addLeds<WS2811, LLine_pin, BRG>(line.line, line.count);
            break;
        }
        case RLine_pin: {
            FastLED.addLeds<WS2811, RLine_pin, BRG>(line.line, line.count);
            break;
        }
    }
}

RGBLine* initLedLine(int pin, int count, byte *colors, byte id) {
    RGBLine line(pin, count, colors, id);
    addLedsInFastLED(line);
    line.setFastLED(&FastLED);
    return &line;
}

void initClock(iarduino_RTC &time) {
    time.settime(SECONDS, MINUTES, HOURS, DAY, MONTH, YEAR,
                 DAY_OF_WEEK);//(сек, мин, час, день, мес, год, день_недели)
    time.period(1);
}

