#include "BTSerial.h"
#include "IgnitionKey.h"
#include "initialization.h"
#include "SoundLevelMeter.h"
//#include "SoundDecomposition.h"



BTSerial serial(RX_BLUETOOTH, TX_BLUETOOTH); // подключаем объект класса работы с блютуз

RGBLine *leftLine;//указатель на объект класса работы с лентой
RGBLine *rightLine;

iarduino_RTC time(RTC_DS1302, RST_CLOCK, CLK_CLOCK, DATA_CLOCK);  // для модуля DS1302 - RST, CLK, DAT

IgnitionKey ignKey(BIKE_OFF, pinMode, digitalWrite);

Parameters *parameters;

SoundLevelMeter sound(SOUND_R, SOUND_L, pinMode, analogRead);
//SoundDecomposition FHT(SOUND_R, SOUND_L, pinMode, analogRead);

void setup() {
    initAssembly();
    initAudio();
    initSerial();
    initSwitchAudio();
    leftLine = initLedLine(LLine_pin, NUM_LEDS, colors, 0);
    rightLine = initLedLine(RLine_pin, NUM_LEDS, colors, 1);
    initClock(time);
    parameters = new Parameters(*leftLine);
};

unsigned long timer = 0;
float amplitude = 1.0;

int resultProcessing(int resp) {
    switch (resp) {
        case ON: {
            Serial.println(F("ON"));
            ignKey.setVal(true);
            break;
        }
        case OFF: {
            Serial.println(F("OFF"));
            ignKey.setVal(false);
            break;
        }
        case SOUND_OFF: {
            Serial.println(F("LOW"));
            digitalWrite(AUDIO_OFF, LOW);
            break;
        }
        case SOUND_ON: {
            Serial.println(F("HIGH"));
            digitalWrite(AUDIO_OFF, HIGH);
            break;
        }
        case COLORS: {
            rightLine->setColors(parameters->colors);
            leftLine->setColors(parameters->colors);
            break;
        }
        case BRIGHT: {
            rightLine->setMaxBrightness(parameters->maxBright);
            leftLine->setMaxBrightness(parameters->maxBright);
            break;
        }
        case LINE_MODE: {
            rightLine->setMode(parameters->mode);
            leftLine->setMode(parameters->mode);
            break;
        }
        case FREQUENCY: {
            rightLine->setFrequency(parameters->frequency);
            leftLine->setFrequency(parameters->frequency);
            break;
        }
        case WAIT_INPUT: {
            return 1;
        }
    }
    return 0;
}

void updateRGBLine() {
    if (leftLine->needAmplitude) {
        amplitude = sound.amplitudeLight();
    } 
    /*    uint8_t* fht = FHT.analyzeAudio();
        for (int i = 0; i < 64; ++i) {
            Serial.print(fht[i]);
            Serial.print(",");
        }
        Serial.println("");
    */
    FastLED.clear();//очищаем адресную ленту
    leftLine->show(amplitude);
    rightLine->show(amplitude);
    //rightLine->data();
    FastLED.show();//обновляем адресную ленту
}

void loop() {
    int resp = serial.getCommands(*parameters);
    if (resultProcessing(resp)) {
        return;
    }
    if (timer < millis() - 50 || timer > millis()) {
        updateRGBLine();
        timer = millis();
    }
}