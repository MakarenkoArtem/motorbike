#include "BTSerial.h"
#include "IgnitionKey.h"
#include "initialization.h"
#include "SoundLevelMeter.h"


void printMemoryUsage() {
    // Определение использования стека
    volatile char stack_dummy;
    char *stack_ptr = &stack_dummy;
    extern char __stack;  // Объявление переменной, указывающей на конец стека

    // Определение конца кучи
    extern char __heap_start, *__brkval;
    char *heap_ptr = (char *) __brkval == 0 ? (char *) &__heap_start : (char *) __brkval; //конец кучи

    // Находим адреса памяти
    int stack_usage = static_cast<int>((char *) &__stack - stack_ptr); // Использование стека
    int heap_usage = static_cast<int>(heap_ptr - (char *) &__heap_start); // Использование памяти кучи

    // Расчет общего размера стека и кучи
    int total_stack_size = static_cast<int>((char *) &__stack - (char *) 0); // Размер стека
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


BTSerial serial(RX_BLUETOOTH, TX_BLUETOOTH); // подключаем объект класса работы с блютуз

RGBLine *leftLine;//указатель на объект класса работы с лентой
RGBLine *rightLine;

iarduino_RTC time(RTC_DS1302, RST_CLOCK, CLK_CLOCK, DATA_CLOCK);  // для модуля DS1302 - RST, CLK, DAT

IgnitionKey ignKey(BIKE_OFF, pinMode, digitalWrite);

Parameters *parameters;


SoundLevelMeter sound(SOUND_R, SOUND_L, pinMode, analogRead);

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

unsigned int timer = 0;
float amplitude=1.0;

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
        case MODE: {
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

void updateRGBLine(){
    FastLED.clear();//очищаем адресную ленту
    if(leftLine->needAmplitude) {
        amplitude = sound.amplitudeLight();
    }
    leftLine->show(amplitude);
    rightLine->show(amplitude);
    //rightLine->data();
    FastLED.show();//обновляем адресную ленту
}

void loop() {
    int resp = serial.getCommands(*parameters);
    if(resultProcessing(resp)){
        return;
    }
    if (timer < millis()-50 || timer > millis()) {
        //printMemoryUsage();
        updateRGBLine();
        timer = millis();
    }
}