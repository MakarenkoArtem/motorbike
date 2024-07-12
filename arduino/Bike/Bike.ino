#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
#pragma once
#define TX_BLUETOOTH 4  //0
#define RX_BLUETOOTH 5  //1
#define LLine_pin 2
#define RLine_pin 3
//#define MISO_STICK 4
//#define CLK_STICK 5
#define MOSI_STICK 6
#define CS_STICK 7
#define VRx_STICK 8
#define VRy_STICK 9
#define BTN_STICK 10
#define CLK_CLOCK 11
#define DATA_CLOCK 12
#define RST_CLOCK 13
#define BIKE_OFF A2
#define SOUND_R A6
#define SOUND_L A7
#define NUM_LEDS 50

#define WAIT_INPUT 101

#include "IgnitionKey.h"

IgnitionKey ignKey(BIKE_OFF, pinMode, digitalWrite);

// градиент-палитра от зелёного к красному
byte colors[24] = {
        0, 0, 255, 0,
        50, 0, 255, 100,
        100, 0, 200, 100,
        150, 0, 100, 200,
        200, 0, 100, 255,
        255, 0, 0, 255};

#include "RGBLine.h"
#include <FastLED.h>
float sound;
RGBLine LeftLine(LLine_pin, NUM_LEDS, colors, sound);//&sound.LsoundLevel);//объект класса работы с лентой
RGBLine RightLine(RLine_pin, NUM_LEDS, colors, sound);//&sound.RsoundLevel);//объект класса работы с лентой

//----------------------Bluetooth--------------
#include "BTSerial.h"

BTSerial serial(RX_BLUETOOTH, TX_BLUETOOTH); // подключаем объект класса работы с блютуз

void setup() {
    sbi(ADCSRA, ADPS2);
    cbi(ADCSRA, ADPS1);
    sbi(ADCSRA, ADPS0);
    //-------------audio------------------
    #if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
        analogReference(INTERNAL1V1);
    #else
        analogReference(INTERNAL);
    #endif
    FastLED.addLeds<WS2811, LLine_pin, BRG>(&LeftLine.line, LeftLine.count).setCorrection(
            TypicalLEDStrip);
    LeftLine.setFastLED(&FastLED);
    FastLED.addLeds<WS2811, RLine_pin, BRG>(&RightLine.line, RightLine.count).setCorrection(
            TypicalLEDStrip);
    RightLine.setFastLED(&FastLED);
    Serial.begin(9600);
    Serial.println("Setup");
};

byte f;
unsigned short g;
void loop() {
    int resp = serial.getSocket(LeftLine.bright, LeftLine.mode, colors);//проверяем блютуз
    if (resp != OK) {
        switch (resp) {
            case ON:
                Serial.println("ON");
                ignKey.setVal(true);
                break;
            case OFF:
                Serial.println("OFF");
                ignKey.setVal(false);
                break;
        }
    }
    return;
    //Serial.println("1111111111111111111111111111111111111");
    FastLED.clear();//очищаем адресную ленту
    LeftLine.show();
    RightLine.show();
    FastLED.show();//обновляем адресную ленту
    //Serial.println("5555555555555555555555555555555555555");
}