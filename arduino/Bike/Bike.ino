#include "BTSerial.h"
#include "IgnitionKey.h"
#include "initialization.h"
#include "SoundLevelMeter.h"
#include "SoundDecomposition.h"
#include "Animation.h"



BTSerial serial(RX_BLUETOOTH_PIN, TX_BLUETOOTH_PIN); // подключаем объект класса работы с блютуз

RGBLine *leftLine;//указатель на объект класса работы с лентой
RGBLine *rightLine;

iarduino_RTC time(RTC_DS1302, RST_CLOCK_PIN, CLK_CLOCK_PIN, DATA_CLOCK_PIN);  // для модуля DS1302 - RST, CLK, DAT

IgnitionKey ignKey(BIKE_PIN, pinMode, digitalWrite);

Parameters params(colors);  //объект, где хранятся все параметры для взаиводействия разных частей кода


SoundLevelMeter sound(SOUND_R_PIN, SOUND_L_PIN, pinMode, analogRead);
SoundDecomposition fht(SOUND_R_PIN, SOUND_L_PIN, pinMode, analogRead);

Animation animation(params, sound, fht);

void setup() {
    initAssembly();
    initSerial();
    initAudio();
    initSwitchAudio(AMPLIFIER_PIN, AUDIO_BT_PIN);
    leftLine = initLedLine(LLINE_PIN, NUM_LEDS, params, 0);
    rightLine = initLedLine(RLINE_PIN, NUM_LEDS, params, 1);
    initClock(time);
    params.setMode(params.mode);
    Serial.println("End setup");
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
        case AMPLIFIER_OFF: {
            Serial.println(F("Amplifier: LOW"));
            digitalWrite(AMPLIFIER_PIN, LOW);
            break;
        }
        case AMPLIFIER_ON: {
            Serial.println(F("Amplifier: HIGH"));
            digitalWrite(AMPLIFIER_PIN, HIGH);
            break;
        }
        case AUDIO_BT_OFF: {
            Serial.println(F("BT: inactiv"));
            digitalWrite(AUDIO_BT_PIN, HIGH);
            break;
        }
        case AUDIO_BT_ON: {
            Serial.println(F("BT: activ"));
            digitalWrite(AUDIO_BT_PIN, LOW);
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
