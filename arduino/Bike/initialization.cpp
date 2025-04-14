#include "initialization.h"

void initAssembly() {//увеличение частоты оцифровки
    //предположительно её можно заменить setADCPrescaler установив вместо ADC_PRESCALER_128 ADC_PRESCALER_32
    /*sbi(ADCSRA, ADPS2);
    cbi(ADCSRA, ADPS1);
    sbi(ADCSRA, ADPS0);*/
}

void initAudio() {
    analogReference(EXTERNAL);
/*#if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
    analogReference(INTERNAL1V1);
#else
    analogReference(INTERNAL);
#endif*/
}

void initSerial() {
    Serial.begin(9600);
    Serial.println("Setup");
}

void initSwitchAudio(int amplifier, int btSource) {
    pinMode(amplifier, OUTPUT);
    digitalWrite(amplifier, LOW);
    pinMode(btSource, OUTPUT);
    digitalWrite(btSource, LOW);
}

void addLedsInFastLED(RGBLine &line) {
    switch (line.getPin()) {
        //FastLED.addLeds<WS2811, pin, BRG>(&line, count).setCorrection(TypicalLEDStrip);
        //найти реализацию addLeds с переменной pin, а не заданным при компиляции значением
        //https://community.alexgyver.ru/threads/fastled-nastraivaem-piny-i-porjadok-cvetov-na-letu-ili-kak-rabotat-s-nasledovaniem-klassov-v-c.9732/
        case LLINE_PIN: {
            FastLED.addLeds<WS2811, LLINE_PIN, BRG>(line.line, line.count);
            break;
        }
        case RLINE_PIN: {
            FastLED.addLeds<WS2811, RLINE_PIN, BRG>(line.line, line.count);
            break;
        }
    }
}

RGBLine *initLedLine(int pin, int count, Parameters &params, byte id) {
    RGBLine *line = new RGBLine(pin, count, params, id);
    addLedsInFastLED(*line);
    line->setFastLED(&FastLED);
    return line;
}

void initClock(iarduino_RTC &time) {
    time.settime(SECONDS, MINUTES, HOURS, DAY, MONTH, YEAR,
                 DAY_OF_WEEK);//(сек, мин, час, день, мес, год, день_недели)
    time.period(1);
}
//verified 1.02.25
