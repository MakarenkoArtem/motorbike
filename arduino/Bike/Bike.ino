#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
#pragma once

#define LLine_pin 2
#define RLine_pin 3
#define TX_BLUETOOTH 4  //0
#define RX_BLUETOOTH 5  //1
#define AUDIO_OFF 6
#define CLK_CLOCK 11
#define DATA_CLOCK 12
#define RST_CLOCK 13
#define BIKE_OFF A2
#define SOUND_R A6
#define SOUND_L A7
#define NUM_LEDS 50

#define YEAR  ((__DATE__[12] - '0') * 1000 + (__DATE__[13] - '0') * 100 + (__DATE__[14] - '0') * 10 + (__DATE__[15] - '0')) 
#define MONTH ( \
    (__DATE__[0] == 'J' && __DATE__[1] == 'a') ? 1 : \
    (__DATE__[0] == 'F' ? 2 : \
    (__DATE__[0] == 'M' && __DATE__[2] == 'r') ? 3 : \
    (__DATE__[0] == 'A' && __DATE__[1] == 'p') ? 4 : \
    (__DATE__[0] == 'M' && __DATE__[2] == 'y') ? 5 : \
    (__DATE__[0] == 'J' && __DATE__[1] == 'u' && __DATE__[2] == 'n') ? 6 : \
    (__DATE__[0] == 'J' && __DATE__[1] == 'u' && __DATE__[2] == 'l') ? 7 : \
    (__DATE__[0] == 'A' && __DATE__[1] == 'u') ? 8 : \
    (__DATE__[0] == 'S' ? 9 : \
    (__DATE__[0] == 'O' ? 10 : \
    (__DATE__[0] == 'N' ? 11 : \
    (__DATE__[0] == 'D' ? 12 : 0))))))
#define DAY  (__DATE__[7] - '0') * 10 + (__DATE__[8] - '0')
#define HOURS (__TIME__[0] - '0') * 10 + (__TIME__[1] - '0')
#define MINUTES (__TIME__[3] - '0') * 10 + (__TIME__[4] - '0')
#define SECONDS (__TIME__[6] - '0') * 10 + (__TIME__[7] - '0')
// Макрос для вычисления дня недели (0 = Воскресенье, 6 = Суббота)
#define DAY_OF_WEEK ((DAY + (((13 * (MONTH + 1)) / 5) + YEAR + (YEAR / 4) - (YEAR / 100) + (YEAR / 400)) % 7 + 6) % 7)

#include <Wire.h>         // Подключаем библиотеку для работ
#include <iarduino_RTC.h>
iarduino_RTC time(RTC_DS1302,RST_CLOCK,CLK_CLOCK,DATA_CLOCK);  // для модуля DS1302 - RST, CLK, DAT

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
RGBLine LeftLine(LLine_pin, NUM_LEDS, colors, sound, 0);//&sound.LsoundLevel);//объект класса работы с лентой
RGBLine RightLine(RLine_pin, NUM_LEDS, colors, sound, 1);//&sound.RsoundLevel);//объект класса работы с лентой

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

    Serial.begin(9600);
    Serial.println("Setup");
    
    pinMode(AUDIO_OFF, OUTPUT);
    digitalWrite(AUDIO_OFF, LOW);

    FastLED.addLeds<WS2811, LLine_pin, BRG>(LeftLine.line, LeftLine.count).setCorrection(
            TypicalLEDStrip);
    LeftLine.setFastLED(&FastLED);
    FastLED.addLeds<WS2811, RLine_pin, BRG>(RightLine.line, RightLine.count).setCorrection(
            TypicalLEDStrip);
    RightLine.setFastLED(&FastLED);

    time.begin();
    time.settime(SECONDS, MINUTES, HOURS, DAY, MONTH, YEAR, DAY_OF_WEEK);//(сек, мин, час, день, мес, год, день_недели)
    time.period(1);
};

byte bright = LeftLine.bright;
unsigned short mode =LeftLine.mode;
byte frequency = LeftLine.frequency;
unsigned int iteration = 0;
void loop() {
    int resp = serial.getSocket(bright, mode, colors, frequency);//проверяем блютуз
    switch (resp) {
            case ON:{
                Serial.println(F("ON"));
                ignKey.setVal(true);
                break;}
            case OFF:{
                Serial.println(F("OFF"));
                ignKey.setVal(false);
                break;}
            case SOUND_OFF:{
                Serial.println(F("LOW"));
                digitalWrite(AUDIO_OFF, LOW);
                break;}
            case SOUND_ON:{
                Serial.println(F("HIGH"));
                digitalWrite(AUDIO_OFF, HIGH);
                break;}
            case COLORS:{
                RightLine.setColors(colors);
                LeftLine.setColors(colors);
                break;}
            case BRIGHT:{
                Serial.println(bright);
                RightLine.setBrightness(bright);
                LeftLine.setBrightness(bright);
                break;}
            case MODE:{
                RightLine.setMode(mode);
                LeftLine.setMode(mode);
                break;}
            case FREQUENCY:{
                RightLine.setFrequency(frequency);
                LeftLine.setFrequency(frequency);
                break;}
            case WAIT_INPUT:{
                return;
            }
    }
    if(!iteration){
        FastLED.clear();//очищаем адресную ленту
        LeftLine.show();
        RightLine.show();
        //RightLine.data();
        FastLED.show();//обновляем адресную ленту
    }
    iteration = ++iteration%2500;
    //Serial.println("5555555555555555555555555555555555555");
}