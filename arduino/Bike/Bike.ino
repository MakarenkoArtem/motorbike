#include "initialization.h"

void printMemoryUsage() {
    // Определение использования стека
    volatile char stack_dummy;
    char* stack_ptr = &stack_dummy;
    extern char __stack;  // Объявление переменной, указывающей на конец стека

    // Определение конца кучи
    extern char __heap_start, *__brkval;
    char* heap_ptr = (char*)__brkval == 0 ? (char*)&__heap_start : (char*)__brkval; //конец кучи

    // Находим адреса памяти
    int stack_usage = static_cast<int>((char*)&__stack - stack_ptr); // Использование стека
    int heap_usage = static_cast<int>(heap_ptr - (char*)&__heap_start); // Использование памяти кучи

    // Расчет общего размера стека и кучи
    int total_stack_size = static_cast<int>((char*)&__stack - (char*)0); // Размер стека
    int total_heap_size = 2048; // Примерный общий размер кучи (проверьте для вашего устройства)

    // Свободная память
    int free_heap = total_heap_size - heap_usage;
    int free_stack = total_stack_size - stack_usage;
    
    Serial.print(F("Stack Usage: "));
    Serial.print(stack_usage);
    Serial.print(F(" Free Stack: "));
    Serial.print(free_stack);
    Serial.print(F(" Heap Usage: "));
    Serial.print(heap_usage);
    Serial.print(F(" Free Heap: "));
    Serial.println(free_heap);
}


#include <Wire.h>         // Подключаем библиотеку для работ
#include <iarduino_RTC.h>

iarduino_RTC time(RTC_DS1302, RST_CLOCK, CLK_CLOCK, DATA_CLOCK);  // для модуля DS1302 - RST, CLK, DAT

#include "IgnitionKey.h"

IgnitionKey ignKey(BIKE_OFF, pinMode, digitalWrite);


//----------------------Bluetooth--создания------------
#include "BTSerial.h"

BTSerial serial(RX_BLUETOOTH, TX_BLUETOOTH); // подключаем объект класса работы с блютуз

RGBLine leftLine(LLine_pin, NUM_LEDS, 0);//&sound.LsoundLevel);//объект класса работы с лентой

RGBLine rightLine(RLine_pin, NUM_LEDS, 1);//&sound.LsoundLevel);//объект класса работы с лентой

void setup() {
    initAssembly();
    initAudio();
    initSerial();
    initSwitchAudio();
    initLedLine(leftLine);
    initLedLine(rightLine);
    initClock(time);
};

byte bright = leftLine.bright;
unsigned short mode =leftLine.mode;
byte frequency = leftLine.frequency;
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
                rightLine.setColors(colors);
                leftLine.setColors(colors);
                break;}
            case BRIGHT:{
                Serial.println(bright);
                rightLine.setBrightness(bright);
                leftLine.setBrightness(bright);
                break;}
            case MODE:{
                rightLine.setMode(mode);
                leftLine.setMode(mode);
                break;}
            case FREQUENCY:{
                rightLine.setFrequency(frequency);
                leftLine.setFrequency(frequency);
                break;}
            case WAIT_INPUT:{
                return;
            }
    }
    if(!iteration){
        //printMemoryUsage();
        FastLED.clear();//очищаем адресную ленту
        leftLine.show();
        rightLine.show();
        rightLine.data();
        FastLED.show();//обновляем адресную ленту
        //printMemoryUsage();
    }
    iteration = ++iteration%2500;
}