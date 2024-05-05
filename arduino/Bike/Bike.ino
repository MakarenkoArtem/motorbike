//#define OLDVERSION 1
#ifdef OLDVERSION
#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
// #include <c++/11/iostream>
#define LLine_pin 2
#define RLine_pin 3
#define TX_BLUETOOTH 4  //0
#define RX_BLUETOOTH 5  //1
#define BIKE_OFF A2
#define SOUND_R A6
#define SOUND_L A7
// --------------------------- НАСТРОЙКИ ---------------------------
// настройки радуги
int rainbow_speed = 3    // скорость движения радуги (чем меньше число, тем быстрее радуга)
#define RAINBOW_STEP 1     // шаг изменения цвета радуги
byte hue = 0;
unsigned long hue_timer = 0;
//----------------------Bluetooth--------------
#include <SoftwareSerial.h>

SoftwareSerial btSerial(RX_BLUETOOTH, TX_BLUETOOTH); // RX, TX
/*#include "AsyncStream.h"
AsyncStream<150> btserial(&btSerial,"\n", 200);*/
#define WAIT_BT 200;
#define DELAY_BT 1000;
unsigned long BTtimer;


//------------------------RGB---------------------
#define NUM_LEDS 20

#include "FastLED.h"

CRGB LLine[NUM_LEDS];
CRGB RLine[NUM_LEDS];
int RLen = NUM_LEDS;
int LLen = NUM_LEDS;
#ifdef FFFFFFFF

void setup() {
    Serial.begin(9600);
    btSerial.begin(9600);
    btSerial.setTimeout(WAIT_BT);
    sbi(ADCSRA, ADPS2);
    cbi(ADCSRA, ADPS1);
    //-------------audio------------------
#if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
    analogReference(INTERNAL1V1);
#else
    analogReference(INTERNAL);
#endif
    sbi(ADCSRA, ADPS0);

    FastLED.addLeds<WS2811, LLine_pin, BRG>(LLine, NUM_LEDS).setCorrection(
            TypicalLEDStrip);
    FastLED.addLeds<WS2811, RLine_pin, BRG>(RLine, NUM_LEDS).setCorrection(
            TypicalLEDStrip);
    FastLED.setBrightness(100);

    pinMode(BIKE_OFF, OUTPUT);
}


void blick() {
    if ((long) millis() - strobe_timer > STROBE_PERIOD) {
        strobe_timer = millis();
        strobeUp_flag = true;
        strobeDwn_flag = false;
    }
    if ((long) millis() - strobe_timer > light_time) {
        strobeDwn_flag = true;
    }
    if (strobeUp_flag) {                    // если настало время пыхнуть
        if (strobe_bright < 255)              // если яркость не максимальная
            strobe_bright += STROBE_SMOOTH;     // увелчить
        if (strobe_bright > 255) {            // если пробили макс. яркость
            strobe_bright = 255;                // оставить максимум
            strobeUp_flag = false;              // флаг опустить
        }
    }

    if (strobeDwn_flag) {                   // гаснем
        if (strobe_bright > 0)                // если яркость не минимальная
            strobe_bright -= STROBE_SMOOTH;     // уменьшить
        if (strobe_bright < 0) {              // если пробили мин. яркость
            strobeDwn_flag = false;
            strobe_bright = 0;                  // оставить 0
        }
    }
}


void regims() {
    int c = 0;
    byte j;
    byte colors_[] = {};
    FastLED.clear();          // очистить массив пикселей
    switch (this_mode / 10 == 4) {
        case 2:
            level_size();
            break;
        case 4:
            blick();
            break;
    }
    int t = 255 * (this_mode / 100);
    //if (this_mode/100==1){t=0;}
    switch (this_mode % 100) {
        case 11:
            myPal.loadDynamicGradientPalette(colors);
            for (int i = 0; i < NUM_LEDS; i++) {
                j = 255 - hue - i * 255 / NUM_LEDS;
                LLine[i] = ColorFromPalette(myPal, j - t);
                RLine[i] = ColorFromPalette(myPal, j - t);
            }
            FastLED.show();
            return;
        case 41:
            for (int i = 0; i < NUM_LEDS; i++) {
                LLine[i] = CHSV(hue, STROBE_SAT, strobe_bright);
                RLine[i] = CHSV(hue - t, STROBE_SAT, t - strobe_bright);
            }
            FastLED.show();
            return;
        case 12:
            for (int i = 0; i < num; i++) {
                RLine[i] = CRGB(colors[1], colors[2],
                                colors[3]);   // заливка по палитре " от зелёного к красному"
                LLine[i] = CRGB(colors[1], colors[2],
                                colors[3]);   // заливка по палитре " от зелёного к красному"
            }
            FastLED.show();
            return;
        case 13:
            //byte colors_[8];
            for (int i = 0; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 20; i < 24; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            break;
        case 14:
            //byte colors_[16]={};
            for (int i = 0; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 8; i < 16; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 20; i < 24; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            break;
        case 15:
            byte colors_ = colors;
            break;
    }
    myPal.loadDynamicGradientPalette(colors_);
    for (int i = 0; i < num; i++) {
        RLine[i] = ColorFromPalette(myPal, (i * 255 /
                                            NUM_LEDS));   // заливка по палитре " от зелёного к красному"
        LLine[i] = ColorFromPalette(myPal, (i * 255 /
                                            NUM_LEDS));   // заливка по палитре " от зелёного к красному"
    }
    FastLED.show();
}

String val = "";

void BlueTooth_socket() {
    val += btSerial.readString();
    while (true) {
        int k = val.indexOf("\n");
        if (k == -1) { return; }
        String text = val.substring(0, k);
        val = val.substring(k + 1);
        Serial.println("========================");
        Serial.print(text);
        Serial.println("----------------------");
        Serial.print(val);
        Serial.println("+++++++++++++++++++++++++");
        if (text == "OFF") {
            digitalWrite(BIKE_OFF, HIGH);
            btSerial.print("OK");
        } else if (text == "ON") {
            digitalWrite(BIKE_OFF, LOW);
            btSerial.print("OK");
        } else if (text == "END") {
        } else if (text.substring(0, 3) == "Br:") {
            FastLED.setBrightness(text.substring(3).toInt());
            btSerial.println("OK");
            FastLED.show();
        } else if (text.substring(0, 3) == "Ty:") {
            this_mode = text.substring(3, 6).toInt();
            btSerial.println("OK");
        } else if (text.substring(0, 3) == "Co:" && text.length() == 99) {
            for (int i = 0; i < 24; ++i) {
                /*Serial.print(7+i*4);
                Serial.print(" ");
                Serial.println(text.substring(7+i*4, 10+i*4));*/
                colors[i] = text.substring(3 + i * 4, 6 + i * 4).toInt();
            }
            BTtimer += DELAY_BT;
            myPal.loadDynamicGradientPalette(colors);
        } else if (text == "Con") {
            btSerial.print("OK ");
            for (int x = 0; x < 6; ++x) {
                btSerial.print(colors[x * 4]);
                btSerial.print(", ");
                btSerial.print(colors[x * 4 + 1]);
                btSerial.print(", ");
                btSerial.print(colors[x * 4 + 2]);
                btSerial.print(", ");
                btSerial.print(colors[x * 4 + 3]);
                btSerial.print(", ");
            }
            BTtimer += DELAY_BT;
        } else { BTtimer += DELAY_BT; }
        Serial.println("//////////////////////");
    }
}


void loop() {
    if (btSerial.available()) {
        BTtimer = millis() - DELAY_BT
        +WAIT_BT;
        BlueTooth_socket();
    }
    if (millis() - BTtimer < DELAY_BT) { return; }
    regims();
    if (millis() - hue_timer > rainbow_speed) {
        //hue=(hue + RAINBOW_STEP)%256;
        hue = hue + RAINBOW_STEP;
        hue_timer = millis();
    }
}
#endif

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
// --------------------------- НАСТРОЙКИ ---------------------------
// настройки радуги
#define RAINBOW_SPEED 6    // скорость движения радуги (чем меньше число, тем быстрее радуга)
#define RAINBOW_STEP 1     // шаг изменения цвета радуги

// отрисовка
#define MODE 11              // режим при запуске
#define MAIN_LOOP 5         // период основного цикла отрисовки (по умолчанию 5)
#define SMOOTH 0.5          // коэффициент плавности анимации VU (по умолчанию 0.5)
#define SMOOTH_FREQ 0.8     // коэффициент плавности анимации частот (по умолчанию 0.8)
#define MAX_COEF 1.8        // коэффициент громкости (максимальное равно срднему * этот коэф) (по умолчанию 1.8)
#define MAX_COEF_FREQ 1.2   // коэффициент порога для "вспышки" цветомузыки (по умолчанию 1.5)

// сигнал
#define MONO 0              // 1 - только один канал (ПРАВЫЙ!!!!! SOUND_R!!!!!), 0 - два канала
#define EXP 2             // степень усиления сигнала (для более "резкой" работы) (по умолчанию 1.4)
#define POTENT 1            // 1 - используем потенциометр, 0 - используется внутренний источник опорного напряжения 1.1 В

// нижний порог шумов
#define PASS_LOOP 5000        // период основного цикла отрисовки (по умолчанию 10000)
int LOW_PASS = 100;         // нижний порог шумов режим VU, ручная настройка
int SPEKTR_LOW_PASS = 40;   // нижний порог шумов режим спектра, ручная настройка
#define AUTO_LOW_PASS 1     // разрешить настройку нижнего порога шумов при запуске (по умолч. 0)
#define EEPROM_LOW_PASS 1   // порог шумов хранится в энергонезависимой памяти (по умолч. 1)
#define LOW_PASS_ADD  0    // "добавочная" величина к нижнему порогу, для надёжности (режим VU)
#define LOW_PASS_FREQ_ADD 3 // "добавочная" величина к нижнему порогу, для надёжности (режим частот)

// режим цветомузыки
#define SMOOTH_STEP 5          // шаг уменьшения яркости в режиме цветомузыки (чем больше, тем быстрее гаснет)
#define LOW_COLOR HUE_RED       // цвет низких частот
#define MID_COLOR HUE_GREEN     // цвет средних
#define HIGH_COLOR HUE_YELLOW   // цвет высоких

// режим стробоскопа
#define STROBE_PERIOD 150*2           // период вспышек, миллисекунды
#define STROBE_DUTY 50              // скважность вспышек (1 - 99) - отношение времени вспышки ко времени темноты
#define STROBE_COLOR HUE_YELLOW     // цвет стробоскопа
#define STROBE_SAT 255                // насыщенность. Если 0 - цвет будет БЕЛЫЙ при любом цвете (0 - 255)
#define STROBE_SMOOTH 255           // скорость нарастания/угасания вспышки (0 - 255)

// --------------------- ДЛЯ РАЗРАБОТЧИКОВ ---------------------
#define MODE_AMOUNT 6      // количество режимов

// цвета (устаревшие)
#define BLUE     0x0000FF
#define RED      0xFF0000
#define GREEN    0x00ff00
#define CYAN     0x00FFFF
#define MAGENTA  0xFF00FF
#define YELLOW   0xFFFF00
#define WHITE    0xFFFFFF
#define BLACK    0x000000

#define STRIPE NUM_LEDS / 5
#define NUM_LEDS 20
#define FHT_N 64         // ширина спектра х2
#define LOG_OUT 1
//#include <FHT.h>         // преобразование Хартли
//#include <EEPROMex.h>

#include "FastLED.h"

CRGB LLine[NUM_LEDS];
CRGB RLine[NUM_LEDS];

// градиент-палитра от зелёного к красному
byte colors[] = {
        0, 0, 255, 0,
        50, 0, 255, 100,
        100, 0, 200, 100,
        150, 0, 100, 200,
        200, 0, 100, 255,
        255, 0, 0, 255};
//DEFINE_GRADIENT_PALETTE(soundlevel_gp) colors;
CRGBPalette32 myPal;

int Rlenght, Llenght;
float RsoundLevel, RsoundLevel_f;
float LsoundLevel, LsoundLevel_f;

float averageLevel = 50;
int maxLevel = 100;
int hue;
unsigned long main_timer, hue_timer, strobe_timer, pass_timer;
float averK = 0.006, k = SMOOTH, k_freq = SMOOTH_FREQ;
byte count;
boolean lowFlag;
byte low_pass = 100;
int RcurrentLevel, LcurrentLevel;
int colorMusic[3];
float colorMusic_f[3], colorMusic_aver[3];
boolean colorMusicFlash[3], strobeUp_flag, strobeDwn_flag;
int this_mode = MODE;
int thisBright[3], strobe_bright = 0;
unsigned int light_time = STROBE_PERIOD * STROBE_DUTY / 100;


#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))

//--------------time------------------
//#include <iarduino_RTC.h>
//iarduino_RTC time(RTC_DS1302,RST_CLOCK,CLK_CLOCK,DATA_CLOCK);         // Задаем правильно название нашего модуля, а также указываем к каким цифровым пинам его подключаем(в нашем случае – 8,6,7)
int timer_audio = 1000, period_audio = 100;



//----------------------Bluetooth--------------
#include <SoftwareSerial.h>

SoftwareSerial btSerial(RX_BLUETOOTH, TX_BLUETOOTH); // RX, TX
/*#include "AsyncStream.h"
AsyncStream<150> btserial(&btSerial,"\n", 200);*/
unsigned long BTtimer;

int rainbow_speed = RAINBOW_SPEED;

void setup() {
    Serial.begin(9600);
    btSerial.begin(9600);
    btSerial.setTimeout(150);
    sbi(ADCSRA, ADPS2);
    cbi(ADCSRA, ADPS1);
    sbi(ADCSRA, ADPS0);
    //-------------audio------------------
#if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
    analogReference(INTERNAL1V1);
#else
    analogReference(INTERNAL);
#endif
    Serial.println(F("start"));
    myPal.loadDynamicGradientPalette(colors);
    FastLED.addLeds<WS2811, Lrgb, BRG>(LLine, NUM_LEDS).setCorrection(
            TypicalLEDStrip);
    FastLED.addLeds<WS2811, Rrgb, BRG>(RLine, NUM_LEDS).setCorrection(
            TypicalLEDStrip);
    FastLED.setBrightness(100);

    pinMode(BIKE_OFF, OUTPUT);
    //time.begin();
    //time.settime(0,35,21,13,4,23,1);
}

int Rlenght_last = 0;

void level_size() {
    for (int i = 0; i < 100; i++) {
        //try{                               // делаем 100 измерений
        RcurrentLevel = analogRead(
                SOUND_R);                            // с правого
        LcurrentLevel = analogRead(SOUND_L);                 // и левого каналов
        if (RsoundLevel < RcurrentLevel)
            RsoundLevel = RcurrentLevel;   // ищем максимальное
        if (LsoundLevel < LcurrentLevel)
            LsoundLevel = LcurrentLevel;   // ищем максимальное
    }// фильтруем по нижнему порогу шумов
    //Serial.print(RsoundLevel);
    //Serial.print(" ");
    //Serial.println(LOW_PASS);
    RsoundLevel = max(0, map(RsoundLevel, LOW_PASS, 1023, 0, 500));
    LsoundLevel = max(0, map(LsoundLevel, LOW_PASS, 1023, 0, 500));

    // возводим в степень (для большей чёткости работы)
    RsoundLevel = pow(RsoundLevel, EXP);
    LsoundLevel = pow(LsoundLevel, EXP);
    // ограничиваем диапазон
    RsoundLevel = constrain(RsoundLevel, 0, 500);
    LsoundLevel = constrain(LsoundLevel, 0, 500);

    // фильтр
    RsoundLevel_f = RsoundLevel * k + RsoundLevel_f * (1 - k);
    LsoundLevel_f = LsoundLevel * k + LsoundLevel_f * (1 - k);

    if (RsoundLevel_f > 15 || LsoundLevel_f > 15) {

        // расчёт общей средней громкости с обоих каналов, фильтрация.
        // Фильтр очень медленный, сделано специально для автогромкости
        averageLevel = (float) (RsoundLevel_f + LsoundLevel_f) / 2 * averK +
                       averageLevel * (1 - averK);

        // принимаем максимальную громкость шкалы как среднюю, умноженную на некоторый коэффициент MAX_COEF
        maxLevel = (float) averageLevel * MAX_COEF;
        Rlenght_last = Rlenght;
        // преобразуем сигнал в длину ленты (где NUM_LEDS это половина количества светодиодов)
        Rlenght = map(RsoundLevel_f, 0, maxLevel, 0, NUM_LEDS);
        Llenght = map(LsoundLevel_f, 0, maxLevel, 0, NUM_LEDS);

        // ограничиваем до макс. числа светодиодов
        Rlenght = constrain(Rlenght, 0, NUM_LEDS);
        Llenght = constrain(Llenght, 0, NUM_LEDS);
        if (Rlenght_last > Rlenght) {
            Rlenght = Rlenght_last - 1;
        }
    } else {
        Rlenght -= 5;

    }
    animation();       // отрисовать
}

void colorMusic_funct() {
    /*analyzeAudio();
    colorMusic[0] = 0;
    colorMusic[1] = 0;
    colorMusic[2] = 0;
    // низкие частоты, выборка с 3 по 5 тон
    for (byte i = 3; i < 6; i++) {
      if (fht_log_out[i] > SPEKTR_LOW_PASS) {
        if (fht_log_out[i] > colorMusic[0]) colorMusic[0] = fht_log_out[i];
      }
    }
    // средние частоты, выборка с 6 по 10 тон
    for (byte i = 6; i < 11; i++) {
      if (fht_log_out[i] > SPEKTR_LOW_PASS) {
        if (fht_log_out[i] > colorMusic[1]) colorMusic[1] = fht_log_out[i];
      }
    }
    // высокие частоты, выборка с 11 по 30 тон
    for (byte i = 11; i < 31; i++) {
      if (fht_log_out[i] > SPEKTR_LOW_PASS) {
        if (fht_log_out[i] > colorMusic[2]) colorMusic[2] = fht_log_out[i];
      }
    }
    for (byte i = 0; i < 3; i++) {
      colorMusic_aver[i] = colorMusic[i] * averK + colorMusic_aver[i] * (1 - averK);  // общая фильтрация
      colorMusic_f[i] = colorMusic[i] * k_freq + colorMusic_f[i] * (1 - k_freq);      // локальная
      if (colorMusic_f[i] > ((float)colorMusic_aver[i] * MAX_COEF_FREQ)) {
        thisBright[i] = 255;
        colorMusicFlash[i] = 1;
      } else colorMusicFlash[i] = 0;
      if (thisBright[i] >= 0) thisBright[i] -= SMOOTH_STEP;
      if (thisBright[i] < 0) thisBright[i] = 0;
    }*/
    animation();
}

void blick() {
    if ((long) millis() - strobe_timer > STROBE_PERIOD) {
        strobe_timer = millis();
        strobeUp_flag = true;
        strobeDwn_flag = false;
    }
    if ((long) millis() - strobe_timer > light_time) {
        strobeDwn_flag = true;
    }
    if (strobeUp_flag) {                    // если настало время пыхнуть
        if (strobe_bright < 255)              // если яркость не максимальная
            strobe_bright += STROBE_SMOOTH;     // увелчить
        if (strobe_bright > 255) {            // если пробили макс. яркость
            strobe_bright = 255;                // оставить максимум
            strobeUp_flag = false;              // флаг опустить
        }
    }

    if (strobeDwn_flag) {                   // гаснем
        if (strobe_bright > 0)                // если яркость не минимальная
            strobe_bright -= STROBE_SMOOTH;     // уменьшить
        if (strobe_bright < 0) {              // если пробили мин. яркость
            strobeDwn_flag = false;
            strobe_bright = 0;                  // оставить 0
        }
    }
    //animation();
}

void animation() {
    // согласно режиму
    FastLED.clear();          // очистить массив пикселей
    switch (this_mode) {
        case 0:
            count = 0;
            for (int i = 0; i < Rlenght; ++i) {
                RLine[i] = ColorFromPalette(myPal,
                                            (count * 255 /
                                             NUM_LEDS));   // заливка по палитре " от зелёного к красному"
                LLine[i] = ColorFromPalette(myPal,
                                            (count * 255 /
                                             NUM_LEDS));   // заливка по палитре " от зелёного к красному"
                count++;
            }
            /*count = 0;
            for (int i = (NUM_LEDS); i < (NUM_LEDS + Rlenght); i++ ) {
              LLine[i] = ColorFromPalette(myPal, (count * 255 / NUM_LEDS));   // заливка по палитре " от зелёного к красному"
              count++;
            }*/
            break;
        case 1:
            count = 0;
            for (int i = (NUM_LEDS - 1); i > ((NUM_LEDS - 1) - Rlenght); i--) {
                LLine[i] = ColorFromPalette(RainbowColors_p,
                                            (count * 255 / NUM_LEDS) / 2 -
                                            hue);  // заливка по палитре радуга
                count++;
            }
            count = 0;
            for (int i = (NUM_LEDS); i < (NUM_LEDS + Llenght); i++) {
                LLine[i] = ColorFromPalette(RainbowColors_p,
                                            (count * 255 / NUM_LEDS) / 2 -
                                            hue); // заливка по палитре радуга
                count++;
            }
            break;
        case 2:
            for (int i = 0; i < NUM_LEDS; i++) {
                if (i < STRIPE) LLine[i] = CHSV(HIGH_COLOR, 255, thisBright[2]);
                else if (i < STRIPE * 2)
                    LLine[i] = CHSV(MID_COLOR, 255, thisBright[1]);
                else if (i < STRIPE * 3)
                    LLine[i] = CHSV(LOW_COLOR, 255, thisBright[0]);
                else if (i < STRIPE * 4)
                    LLine[i] = CHSV(MID_COLOR, 255, thisBright[1]);
                else if (i < STRIPE * 5)
                    LLine[i] = CHSV(HIGH_COLOR, 255, thisBright[2]);
            }
            break;
        case 3:
            for (int i = 0; i < NUM_LEDS; i++) {
                if (i < NUM_LEDS / 3)
                    LLine[i] = CHSV(HIGH_COLOR, 255, thisBright[2]);
                else if (i < NUM_LEDS * 2 / 3)
                    LLine[i] = CHSV(MID_COLOR, 255, thisBright[1]);
                else if (i < NUM_LEDS)
                    LLine[i] = CHSV(LOW_COLOR, 255, thisBright[0]);
            }
            break;
        case 4:
            if (colorMusicFlash[2])
                for (int i = 0; i < NUM_LEDS; i++)
                    LLine[i] = CHSV(HIGH_COLOR, 255, thisBright[2]);
            else if (colorMusicFlash[1])
                for (int i = 0; i < NUM_LEDS; i++)
                    LLine[i] = CHSV(MID_COLOR, 255, thisBright[1]);
            else if (colorMusicFlash[0])
                for (int i = 0; i < NUM_LEDS; i++)
                    LLine[i] = CHSV(LOW_COLOR, 255, thisBright[0]);
            else for (int i = 0; i < NUM_LEDS; i++) LLine[i] = BLACK;
            break;
        case 5:
            for (int i = 0; i < NUM_LEDS; i++)
                LLine[i] = CHSV(STROBE_COLOR, STROBE_SAT, strobe_bright);
            break;
    }
    //Serial.println(this_mode);
    FastLED.show();
}

//char value[64];
//String tet="";
String val = "";

void BlueTooth_socket() {
    val += btSerial.readString();
    //Serial.println(btserial.buf[0]);
    //memcpy(value, btserial.buf, sizeof(btserial.buf));
    //val+=btserial.buf;//(String) value;
    /*Serial.print(tet);
    while(true){int k = tet.indexOf("\n");
      if (k==-1){return;}
      String text = tet.substring(0, k);
    switch (text[0]){
      case 'P':
        btSerial.print("OK ");
        for(int x=0;x<6;++x){
          btSerial.print(colors[x*4]);
          btSerial.print(", ");
          btSerial.print(colors[x*4+1]);
          btSerial.print(", ");
          btSerial.print(colors[x*4+2]);
          btSerial.print(", ");
          btSerial.print(colors[x*4+3]);
          btSerial.print(", ");}
        break;
      case 'C':
        for(int i=0;i<24;++i){
          /*Serial.print(7+i*4);
          Serial.print(" ");
          Serial.println(text.substring(7+i*4, 10+i*4));*/
    /*colors[i] = text.substring(1+i*4, 4+i*4).toInt();
    }
  myPal.loadDynamicGradientPalette(colors);
  break;
case 'F':
  digitalWrite(BIKE_OFF, HIGH);
  btSerial.print("OK");
  break;
case 'N':
  digitalWrite(BIKE_OFF, LOW);
  btSerial.print("OK");
  break;
case 'T':
  this_mode = text.substring(1,3).toInt();
  btSerial.println("OK");
  break;
}*/
    while (true) {
        int k = val.indexOf("\n");
        if (k == -1) { return; }
        String text = val.substring(0, k);
        val = val.substring(k + 1);
        Serial.println("========================");
        Serial.print(text);
        Serial.println("----------------------");
        Serial.print(val);
        Serial.println("+++++++++++++++++++++++++");
        if (text == "OFF") {
            digitalWrite(BIKE_OFF, HIGH);
            btSerial.print("OK");
        } else if (text == "ON") {
            digitalWrite(BIKE_OFF, LOW);
            btSerial.print("OK");
        } else if (text == "END") {
        } else if (text.substring(0, 3) == "Br:") {
            FastLED.setBrightness(text.substring(3).toInt());
            btSerial.println("OK");
            FastLED.show();
        } else if (text.substring(0, 3) == "Ty:") {
            this_mode = text.substring(3, 6).toInt();
            btSerial.println("OK");
        } else if (text.substring(0, 3) == "Co:" && text.length() == 99) {
            for (int i = 0; i < 24; ++i) {
                /*Serial.print(7+i*4);
                Serial.print(" ");
                Serial.println(text.substring(7+i*4, 10+i*4));*/
                colors[i] = text.substring(3 + i * 4, 6 + i * 4).toInt();
            }
            BTtimer += 1000;
            myPal.loadDynamicGradientPalette(colors);
        } else if (text == "Con") {
            btSerial.print("OK ");
            for (int x = 0; x < 6; ++x) {
                btSerial.print(colors[x * 4]);
                btSerial.print(", ");
                btSerial.print(colors[x * 4 + 1]);
                btSerial.print(", ");
                btSerial.print(colors[x * 4 + 2]);
                btSerial.print(", ");
                btSerial.print(colors[x * 4 + 3]);
                btSerial.print(", ");
            }
            BTtimer += 1000;
        } else { BTtimer += 1000; }
        Serial.println("//////////////////////");
    }
}

void regims() {
    rainbow_speed = RAINBOW_SPEED;
    int c = 0;
    byte colors_[] = {};
    FastLED.clear();          // очистить массив пикселей
    if (this_mode / 10 == 4) { blick(); }
    else if (this_mode / 10 == 2) { level_size(); }
    int t = 255;
    int num = NUM_LEDS;
    if (this_mode / 100 == 1) { t = 0; }
    switch (this_mode % 10) {
        case 1:
            switch (this_mode) {
                case 11:
                    rainbow_speed = rainbow_speed * 10;
                    myPal.loadDynamicGradientPalette(colors);
                    for (int i = 0; i < NUM_LEDS; i++) {
                        LLine[i] = ColorFromPalette(myPal,
                                                    (hue * 255 / NUM_LEDS));
                        RLine[i] = ColorFromPalette(myPal,
                                                    (hue * 255 / NUM_LEDS));
                    }
                    break;
                case 41:
                    /*STROBE_PERIOD=1000;
                    light_time = 20;
                    STROBE_SMOOTH=5;*/
                    for (int i = 0; i < NUM_LEDS; i++) {
                        LLine[i] = CHSV(hue, STROBE_SAT, strobe_bright);
                        RLine[i] = CHSV(hue, STROBE_SAT, t - strobe_bright);
                    }
                    break;
                case 22:
                    num = Rlenght;
                    myPal.loadDynamicGradientPalette(colors);
                    for (int i = 0; i < num; i++) {
                        LLine[i] = ColorFromPalette(myPal,
                                                    (hue * 255 / NUM_LEDS));
                        RLine[i] = ColorFromPalette(myPal,
                                                    (hue * 255 / NUM_LEDS));
                    }
                    break;
            }
            FastLED.show();
            return;
        case 2:
            //byte colors_[4];

            switch (this_mode) {
                case 42:
                    FastLED.setBrightness(strobe_bright);
                    break;
                case 22:
                    num = Rlenght;
                    break;
            }
            for (int i = 0; i < num; i++) {
                RLine[i] = CRGB(colors[1], colors[2],
                                colors[3]);   // заливка по палитре " от зелёного к красному"
                LLine[i] = CRGB(colors[1], colors[2],
                                colors[3]);   // заливка по палитре " от зелёного к красному"
            }
            FastLED.show();
            /*for (int i = 0s; i < 4; i++){// LLine[i] = colors[0];
              colors_[c]=colors[i];
              ++c;
            }
            break;*/
            return;
        case 3:
            //byte colors_[8];
            for (int i = 0; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 20; i < 24; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            switch (this_mode) {
                case 43:
                    FastLED.setBrightness(strobe_bright);
                    break;
                case 22:
                    num = Rlenght;
                    break;
            }
            break;
        case 4:
            //byte colors_[16]={};
            for (int i = 0; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 8; i < 16; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            for (int i = 20; i < 24; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            switch (this_mode) {
                case 44:
                    FastLED.setBrightness(strobe_bright);
            }
            break;
        case 5:
            switch (this_mode) {
                case 45:
                    FastLED.setBrightness(strobe_bright);
                    break;
                case 22:
                    num = Rlenght;
                    break;
            }
            byte colors_ = colors;
            break;
            /*case 41:
              /*STROBE_PERIOD=1000;
              light_time = 20;
              STROBE_SMOOTH=5;
              blick();
              for (int i = 0; i < NUM_LEDS; i++) {
                LLine[i] = CHSV(hue, STROBE_SAT, strobe_bright);
                RLine[i] = CHSV(hue, STROBE_SAT, strobe_bright);
              }
              FastLED.show();
              return;*/
    }
    myPal.loadDynamicGradientPalette(colors_);
    for (int i = 0; i < num; i++) {
        RLine[i] = ColorFromPalette(myPal, (i * 255 /
                                            NUM_LEDS));   // заливка по палитре " от зелёного к красному"
        LLine[i] = ColorFromPalette(myPal, (i * 255 /
                                            NUM_LEDS));   // заливка по палитре " от зелёного к красному"
    }
    FastLED.show();
}

void loop() {
    if (btSerial.available()) {
        BTtimer = millis() - 800;
        BlueTooth_socket();
    }
    if (millis() - BTtimer < 1000) { return; }
    regims();
    if (millis() - hue_timer > rainbow_speed) {
        hue = (hue + RAINBOW_STEP) % 256;
        hue_timer = millis();
    }
    // put your main code here, to run repeatedly:
    /*Serial.print(analogRead(L_AUDIO));
    Serial.print("   ");
    Serial.println(analogRead(R_AUDIO));*/

    //Serial.println(RsoundLevel);
    /*for (int i = 0; i < RsoundLevel; i++) {
      //RLine[i] = ColorFromPalette(RainbowColors_p, i*3.7);   // заливка по палитре " от зелёного к красному"
    }
    for (int i = 0; i < LsoundLevel; i++) {
      //LLine[i] = ColorFromPalette(RainbowColors_p, i*3.7);   // заливка по палитре " от зелёного к красному"
    }*/
    // кольцевое изменение положения радуги по таймеру
    /*
    if (millis() - hue_timer > RAINBOW_SPEED) {
      if (++hue >= 255) hue = 0;
      hue_timer = millis();
    }*/
    if (millis() - pass_timer > PASS_LOOP) {
        autoLowPass();            // измерить шумы
        pass_timer = millis();
    }
    /*
    if (millis() - main_timer > MAIN_LOOP) {
      // сбрасываем значения
      RsoundLevel = 0;
      LsoundLevel = 0;
      // перваые два режима - громкость (VU meter)
      base_rgb();
      return;
      if (this_mode == 0 || this_mode == 1) {
        level_size();}
      // 3-5 режим - цветомузыка
      else if (this_mode == 2 || this_mode == 3 || this_mode == 4) {
        colorMusic_funct();}
      else if (this_mode == 5) {

      }
      FastLED.show();           // отправить значения на ленту
      main_timer = millis();
    }*/
    //Serial.print(LsoundLevel);
    //Serial.print(' ');
    //Serial.println(LsoundLevel_f);
    //level(LsoundLevel_f);
    //FastLED.show();
    //Serial.println(time.gettime("d-m-Y, H:i:s, D"));   // выводим время
    //FastLED.clear();
}


void analyzeAudio() {
    /*
    for (int i = 0 ; i < FHT_N ; i++) {
      int sample = analogRead(SOUND_R);
      fht_input[i] = sample; // put real data into bins
  999  }
    fht_window();  // window the data for better frequency response
    fht_reorder(); // reorder the data before doing the fht
    fht_run();     // process the data in the fht
    fht_mag_log(); // take the output of the fht*/
}

void autoLowPass() {
    // для режима VU
    delay(10);                                // ждём инициализации АЦП
    int thisMax = 0;                          // максимум
    int thisLevel;
    for (byte i = 0; i < 100; i++) {
        thisLevel = analogRead(SOUND_R);        // делаем 200 измерений
        if (thisLevel > thisMax)                // ищем максимумы
            thisMax = thisLevel;                  // запоминаем
        delay(4);                               // ждём 4мс
    }
    LOW_PASS = (thisMax +
                LOW_PASS_ADD);        // нижний порог как максимум тишины + некая величина

    /*// для режима спектра
    thisMax = 0;
    for (byte i = 0; i < 100;s i++) {          // делаем 100 измерений
      analyzeAudio();                         // разбить в спектр
      for (byte j = 2; j < 32; j++) {         // первые 2 канала - хлам
        thisLevel = fht_log_out[j];
        if (thisLevel > thisMax)              // ищем максимумы
          thisMax = thisLevel;                // запоминаем
      }
      delay(4);                               // ждём 4мс
    }*/
    SPEKTR_LOW_PASS =
            thisMax + LOW_PASS_FREQ_ADD;  // нижний порог как максимум тишины

    /*if (EEPROM_LOW_PASS && !AUTO_LOW_PASS) {
      EEPROM.updateInt(0, LOW_PASS);
      EEPROM.updateInt(2, SPEKTR_LOW_PASS);
    }*/
}
#else


#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
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
#pragma once

#include "SoundLevelMeter.h"

SoundLevelMeter sound(SOUND_R, SOUND_L, pinMode, analogRead);


#include "IgnitionKey.h"

IgnitionKey ignKey(BIKE_OFF, pinMode, digitalWrite);


// градиент-палитра от зелёного к красному
byte colors[] = {
        0, 0, 255, 0,
        50, 0, 255, 100,
        100, 0, 200, 100,
        150, 0, 100, 200,
        200, 0, 100, 255,
        255, 0, 0, 255};

#include "RGBLine.h"
#include <FastLED.h>

RGBLine LeftLine(LLine_pin, NUM_LEDS, (byte*)&colors, &sound.LsoundLevel);//объект класса работы с лентой
RGBLine RightLine(RLine_pin, NUM_LEDS, (byte*)&colors, &sound.LsoundLevel);//объект класса работы с лентой

//----------------------Bluetooth--------------
#include "BTSerial.h"
BTSerial serial(RX_BLUETOOTH, TX_BLUETOOTH); // подключаем объект класса работы с блютуз

void setup() {
    sbi(ADCSRA, ADPS2);
    cbi(ADCSRA, ADPS1);
    //-------------audio------------------
#if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
    analogReference(INTERNAL1V1);
#else
    analogReference(INTERNAL);
#endif
    sbi(ADCSRA, ADPS0);
    Serial.begin(9600);
    FastLED.addLeds<WS2811, LLine_pin, BRG>(&LeftLine.line, LeftLine.count).setCorrection(
            TypicalLEDStrip);
    LeftLine.setFastLED(&FastLED);
    FastLED.addLeds<WS2811, RLine_pin, BRG>(&RightLine.line, RightLine.count).setCorrection(
            TypicalLEDStrip);
    RightLine.setFastLED(&FastLED);
};


void loop() {
    int resp = serial.getSocket(&LeftLine.bright, &LeftLine.mode, (byte * *) & colors);//проверяем блютуз
    if (resp != OK) {
        switch (resp) {
            case ON:
                ignKey.setVal(true);
            case OFF:
                ignKey.setVal(false);

        }
        return;
    }
    sound.fhtSound();
    FastLED.clear();//очищаем адресную ленту
    LeftLine.show();
    RightLine.show();
    FastLED.show();//обновляем адресную ленту
}

#endif