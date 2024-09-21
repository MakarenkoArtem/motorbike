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

void initLedLine(RGBLine& line) {
    float sound;
    FastLED.addLeds<WS2811, LLine_pin, BRG>(line.line, line.count)
            .setCorrection(TypicalLEDStrip);
    line.setFastLED(&FastLED);
}

void initClock(iarduino_RTC& time) {
    time.settime(SECONDS, MINUTES, HOURS, DAY, MONTH, YEAR,
                 DAY_OF_WEEK);//(сек, мин, час, день, мес, год, день_недели)
    time.period(1);
}

