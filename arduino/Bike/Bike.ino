#include "BTSerial.h"
#include "IgnitionKey.h"
#include "initialization.h"
#include "SoundLevelMeter.h"
#include "SoundDecomposition.h"
#include "Animation.h"



BTSerial serial(RX_BLUETOOTH, TX_BLUETOOTH); // подключаем объект класса работы с блютуз

RGBLine *leftLine;//указатель на объект класса работы с лентой
RGBLine *rightLine;

iarduino_RTC time(RTC_DS1302, RST_CLOCK, CLK_CLOCK, DATA_CLOCK);  // для модуля DS1302 - RST, CLK, DAT

IgnitionKey ignKey(BIKE_OFF, pinMode, digitalWrite);

Parameters params(colors);//объект, где хранятся все параметры для взаиводействия разных частей кода


SoundLevelMeter sound(SOUND_R, SOUND_L, pinMode, analogRead);
SoundDecomposition fht(SOUND_R, SOUND_L, pinMode, analogRead);

Animation animation(params, sound, fht);

void setup() {
    initAssembly();
    initAudio();
    initSerial();
    initSwitchAudio(AUDIO_OFF, AUDIO_BT_OFF);
    leftLine = initLedLine(LLine_pin, NUM_LEDS, params, 0);
    rightLine = initLedLine(RLine_pin, NUM_LEDS, params, 1);
    initClock(time);
};


int resultProcessing(int resp) {
    switch (resp) {
        case OFF: {
            Serial.println(F("OFF"));
            ignKey.setVal(false);
            break;
        }
        case ON: {
            Serial.println(F("ON"));
            ignKey.setVal(true);
            break;
        }
        case SOUND_AMPLIFIER_OFF: {
            Serial.println(F("Amplifier: LOW"));
            digitalWrite(AUDIO_OFF, LOW);
            break;
        }
        case SOUND_AMPLIFIER_ON: {
            Serial.println(F("Amplifier: HIGH"));
            digitalWrite(AUDIO_OFF, HIGH);
            break;
        }
        case SOUND_BT_OFF: {
            Serial.println(F("BT: inactiv"));
            digitalWrite(AUDIO_BT_OFF, HIGH);
            break;
        }
        case SOUND_BT_ON: {
            Serial.println(F("BT: activ"));
            digitalWrite(AUDIO_BT_OFF, LOW);
            break;
        }
        case COLORS: {
            rightLine->setColors(params.colors);
            leftLine->setColors(params.colors);
            break;
        }
        case WAIT_INPUT: {
            return 1;
        }
    }
    return 0;
}

void updateRGBLine() {
    FastLED.clear();//очищаем адресную ленту
    leftLine->show();
    rightLine->show();
    FastLED.show();//обновляем адресную ленту
}

void loop() {
    int resp = serial.getCommands(params);
    if (resultProcessing(resp)) {
        return;
    }
    if (animation.processing()){
        updateRGBLine();
    }
}
//verified 1.02.25
