#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))

#define LLine_pin 2
#define RLine_pin 3
#define TX_BLUETOOTH 4  //0
#define RX_BLUETOOTH 5  //1
#define AUDIO_OFF 6
#define BIKE_OFF A2
#define SOUND_R A6
#define SOUND_L A7
// --------------------------- НАСТРОЙКИ ---------------------------
//-----------------------------СОХРАНЕНИЕ---------------------------
#include <EEPROM.h>
#define INIT_ADDR 1023  // номер резервной ячейки
#define INIT_KEY 17     // ключ первого запуска. 0-254, на выбор

// отрисовка
int this_mode = 45;
byte async = 1; 
float coef=1;
unsigned long MainTimer = 0;
#define MainLoop 100

// настройки радуги
int rainbow_speed = 3;    // скорость движения радуги (чем меньше число, тем быстрее радуга)
#define RAINBOW_STEP 1     // шаг изменения цвета радуги
byte hue = 0;
unsigned long hue_timer = 0;

// режим стробоскопа
#define STROBE_PERIOD 400           // период вспышек, миллисекунды
#define STROBE_DUTY 10              // скважность вспышек (1 - 99) - отношение времени вспышки ко времени темноты
#define STROBE_SAT 255                // насыщенность. Если 0 - цвет будет БЕЛЫЙ при любом цвете (0 - 255)
#define STROBE_SMOOTH 255           // скорость нарастания/угасания вспышки (0 - 255)
boolean strobeUp_flag, strobeDwn_flag, strobeUp_flag_async, strobeDwn_flag_async;
int strobe_bright = 0, strobe_bright_async=0;
unsigned int light_time = STROBE_PERIOD * STROBE_DUTY / 100;
unsigned long strobe_timer=0,strobe_timer_async=0;

//---------------------светомузыка----------------
// нижний порог шумов
#define EXP 2             // степень усиления сигнала (для более "резкой" работы) (по умолчанию 1.4)
#define PASS_LOOP 5000        // период основного цикла отрисовки (по умолчанию 10000)
int LOW_PASS = 100;         // нижний порог шумов режим VU, ручная настройка
int SPEKTR_LOW_PASS = 40;   // нижний порог шумов режим спектра, ручная настройка
#define AUTO_LOW_PASS 1     // разрешить настройку нижнего порога шумов при запуске (по умолч. 0)
#define EEPROM_LOW_PASS 1   // порог шумов хранится в энергонезависимой памяти (по умолч. 1)
#define LOW_PASS_ADD  0    // "добавочная" величина к нижнему порогу, для надёжности (режим VU)
#define LOW_PASS_FREQ_ADD 3 // "добавочная" величина к нижнему порогу, для надёжности (режим частот)
#define LowPassTime 5000
unsigned long LowPass_Timer=0;
int up_level[50]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
int counter_up_level=0;
// отрисовка
#define SMOOTH 0.5          // коэффициент плавности анимации VU (по умолчанию 0.5)
#define SMOOTH_FREQ 0.8     // коэффициент плавности анимации частот (по умолчанию 0.8)
#define MAX_COEF 1.8        // коэффициент громкости (максимальное равно срднему * этот коэф) (по умолчанию 1.8)
#define MAX_COEF_FREQ 1.2   // коэффициент порога для "вспышки" цветомузыки (по умолчанию 1.5)


int Llenght, Rlenght, Llenght_last, Rlenght_last, maxLevel = 100, RcurrentLevel, LcurrentLevel;
float RsoundLevel, RsoundLevel_f, LsoundLevel, LsoundLevel_f, averageLevel = 50;
float averK = 0.006, k = SMOOTH, k_freq = SMOOTH_FREQ;
bool sound_bool=false;

//----------------------Bluetooth--------------
#include <SoftwareSerial.h>
SoftwareSerial btSerial(RX_BLUETOOTH, TX_BLUETOOTH); // RX, TX
/*#include "AsyncStream.h"
AsyncStream<150> btserial(&btSerial,"\n", 200);*/
#define WAIT_BT 200
#define DELAY_BT 1000
unsigned long BTtimer = 0;

//------------------------RGB---------------------
#include "FastLED.h"
// градиент-палитра от зелёного к красному
byte colors[] = {
    0, 0, 255, 0,
    50, 0, 255, 100,
    100, 0, 200, 100,
    150, 0, 100, 200,
    200, 0, 100, 255,
    255, 0, 0, 255};
CRGBPalette32 myPal;
#define NUM_LEDS 20
CRGB LLine[NUM_LEDS];
CRGB RLine[NUM_LEDS];
int RLen = NUM_LEDS;
int LLen = NUM_LEDS;
bool RActive = false;

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

    FastLED.addLeds<WS2811, LLine_pin, BRG>(LLine, NUM_LEDS).setCorrection(TypicalLEDStrip);
    FastLED.addLeds<WS2811, RLine_pin, BRG>(RLine, NUM_LEDS).setCorrection(TypicalLEDStrip);
    FastLED.setBrightness(0);
    pinMode(BIKE_OFF, OUTPUT);
    pinMode(AUDIO_OFF, OUTPUT);
    digitalWrite(AUDIO_OFF, LOW);
    if (EEPROM.read(INIT_ADDR) != INIT_KEY) { // первый запуск
        EEPROM.write(INIT_ADDR, INIT_KEY);    // записали ключ
        EEPROM.put(0, this_mode);
        EEPROM.put(1, sound_bool);
        EEPROM.put(2, coef);
        EEPROM.put(3, 0);
        EEPROM.put(4, async);
    }else{
        EEPROM.get(0, this_mode);
        EEPROM.get(1, sound_bool);
        EEPROM.get(2, coef);
        int h;
        EEPROM.get(3, h);
        EEPROM.get(4, async);
        FastLED.setBrightness(h);
        if (sound_bool){
          delay(10000);
          digitalWrite(AUDIO_OFF, HIGH);
        }else {digitalWrite(AUDIO_OFF, LOW);}
    }
    if (this_mode/10 == 12 || this_mode/10 == 13){
      not_spektr();
    }else{ spektr();}
}
void level_size() {
  RsoundLevel =0;
    for (int i = 0; i < 100; i++) {//100
        //try{                               // делаем 100 измерений
        RcurrentLevel = analogRead(SOUND_R);                            // с правого
        //LcurrentLevel = analogRead(SOUND_L);                 // и левого каналов
        if (RsoundLevel < RcurrentLevel) RsoundLevel = RcurrentLevel;   // ищем максимальное
        //if (LsoundLevel < LcurrentLevel) LsoundLevel = LcurrentLevel;   // ищем максимальное
    }// фильтруем по нижнему порогу шумов
    int h=0;
    for (int i =0; i<50;i++){
      h = max(h, up_level[i]);
    }
    counter_up_level=(++counter_up_level)%50;
    up_level[counter_up_level] = RsoundLevel;
    //Serial.print(RsoundLevel);
    //Serial.print(" ");
    //Serial.println(LOW_PASS);
    /*Serial.print("h:");
    Serial.println(h);
    Serial.print("LOW_PASS:");
    Serial.println(LOW_PASS);
    Serial.print("LOW_PASS*coef:");
    Serial.println(LOW_PASS*coef);
    Serial.print("Lev:");
    Serial.println(RcurrentLevel);*/
    RsoundLevel = max(0, map(RsoundLevel, LOW_PASS*coef/5, h*1.1, 0, 500));
    //LsoundLevel = max(0, map(LsoundLevel, LOW_PASS*coef, 1023, 0, 500));
    Serial.print("Lev1:");
    Serial.println(RsoundLevel);

    // возводим в степень (для большей чёткости работы)
    //RsoundLevel = pow(RsoundLevel, EXP);
    Serial.print("Lev2:");
    Serial.println(RsoundLevel);
    //LsoundLevel = pow(LsoundLevel, EXP);
    // ограничиваем диапазон
    RsoundLevel = constrain(RsoundLevel, 0, 500);
    //LsoundLevel = constrain(LsoundLevel, 0, 500);
    //Serial.print("Lev3:");
    //Serial.println(RsoundLevel);

    // фильтр
    RsoundLevel_f = RsoundLevel * k + RsoundLevel_f * (1 - k);
    //LsoundLevel_f = LsoundLevel * k + LsoundLevel_f * (1 - k);
    Serial.print("Lev4:");
    Serial.println(RsoundLevel_f);

    if (RsoundLevel_f > 15){// || LsoundLevel_f > 15) {
        // расчёт общей средней громкости с обоих каналов, фильтрация.
        // Фильтр очень медленный, сделано специально для автогромкости
        //averageLevel = (float) (RsoundLevel_f + LsoundLevel_f) / 2 * averK + averageLevel * (1 - averK);
        averageLevel = (float) (RsoundLevel_f + RsoundLevel_f) / 2 * averK + averageLevel * (1 - averK);

        // принимаем максимальную громкость шкалы как среднюю, умноженную на некоторый коэффициент MAX_COEF
        maxLevel = (float) averageLevel * MAX_COEF;
        Rlenght_last = Rlenght;
        // преобразуем сигнал в длину ленты (где NUM_LEDS это половина количества светодиодов)
        Rlenght = map(RsoundLevel_f, 0, maxLevel, 0, NUM_LEDS);
        //Llenght = map(LsoundLevel_f, 0, maxLevel, 0, NUM_LEDS);

        // ограничиваем до макс. числа светодиодов
        Rlenght = constrain(Rlenght, 0, NUM_LEDS);
        //Llenght = constrain(Llenght, 0, NUM_LEDS);
        if (Rlenght_last > Rlenght) {
            Rlenght = Rlenght_last - 1;
        }
        /*if (Llenght_last > Llenght) {
            Llenght = Llenght_last - 1;
        }*/
    } else {
        Rlenght = min(Rlenght-4, 0);
        //Llenght-=3;
    }
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
        if (strobe_bright >= 255) {            // если пробили макс. яркость
            strobe_bright = 255;                // оставить максимум
            strobeUp_flag = false;              // флаг опустить
        }
    }

    if (strobeDwn_flag) {                   // гаснем
        if (strobe_bright > 0)                // если яркость не минимальная
            strobe_bright -= STROBE_SMOOTH;     // уменьшить
        if (strobe_bright <= 0) {              // если пробили мин. яркость
            strobeDwn_flag = false;
            strobe_bright = 0;                  // оставить 0
        }
    }
    if (async){
      if ((long) millis() - strobe_timer_async > STROBE_PERIOD && (long) millis()-strobe_timer>STROBE_PERIOD/2) {
        strobe_timer_async = millis();
          //strobe_timer_async = millis();
          strobeUp_flag_async = true;
          strobeDwn_flag_async = false;
      }
      if ((long) millis() - strobe_timer_async > light_time) {
          strobeDwn_flag_async = true;
      }
      if (strobeUp_flag_async) {                    // если настало время пыхнуть
          if (strobe_bright_async < 255)              // если яркость не максимальная
              strobe_bright_async += STROBE_SMOOTH;     // увелчить
          if (strobe_bright_async >= 255) {            // если пробили макс. яркость
              strobe_bright_async = 255;                // оставить максимум
              strobeUp_flag_async = false;              // флаг опустить
          }
      }

      if (strobeDwn_flag_async) {                   // гаснем
          if (strobe_bright_async > 0)                // если яркость не минимальная
              strobe_bright_async -= STROBE_SMOOTH;     // уменьшить
          if (strobe_bright_async <= 0) {              // если пробили мин. яркость
              strobeDwn_flag_async = false;
              strobe_bright_async = 0;                  // оставить 0
          }
      }
    }else{strobe_bright_async=strobe_bright;}
}

void not_spektr(){
  int c = 0;
  byte colors_[] = {};
  switch (this_mode) {
        case 45:
        case 11:
        case 15:
            myPal.loadDynamicGradientPalette(colors);
            return;
        case 12:
            for (int j = 0; j < 2; j++) {
              colors_[c] = 126*j;
              c++;
              for (int i = 1; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
              }}
            break;
        case 13:
            for (int j = 0; j < 2; j++) {
              colors_[c] = 126*j;
              c++;
              for (int i = 1; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
              }}
            for (int j = 0; j < 2; j++) {
              colors_[c] = 127*j;
              c++;
              for (int i = 21; i < 4; i++) {
                  colors_[c] = colors[i];
                  ++c;
              }}
            break;
        case 14:
            for (int j = 0; j < 2; j++) {
              colors_[c] = 83*j;
              c++;
              for (int i = 1; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
              }}
            for (int j = 0; j < 2; j++) {
              colors_[c] = 84*j;
              c++;
              for (int i = 9; i < 12; i++) {
                  colors_[c] = colors[i];
                  ++c;
              }}
            for (int j = 2; j < 4; j++) {
              colors_[c] = 85*j;
              c++;
              for (int i = 21; i < 4; i++) {
                  colors_[c] = colors[i];
                  ++c;
              }}
            break;
    }
    myPal.loadDynamicGradientPalette(colors_);
}

void spektr(){
  int c = 0;
  byte colors_[] = {};
  switch (this_mode) {
        case 45:
        case 11:
        case 15:
            myPal.loadDynamicGradientPalette(colors);
            return;
        case 12:
            for (int i = 0; i < 4; i++) {
                colors_[c] = colors[i];
                ++c;
            }
            break;
        case 13:
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
    }
    myPal.loadDynamicGradientPalette(colors_);
}

void regims() {
    MainTimer = millis();
    byte j;
    FastLED.clear();          // очистить массив пикселей
    switch (this_mode / 10) {
        case 2:
            level_size();
            break;
        case 4:
            blick();
        default:
          Rlenght = NUM_LEDS;
    }
    switch (this_mode) {
        case 21:
        case 11:
            for (int i = 0; i < Rlenght; i++) {
                j = async - hue - i * 255 / NUM_LEDS;
                LLine[i] = ColorFromPalette(myPal, j);
                RLine[i] = ColorFromPalette(myPal, j);
            }
            FastLED.show();
            return;
        case 41:
            for (int i = 0; i < Rlenght; i++) {
                LLine[i] = CHSV(hue, STROBE_SAT, strobe_bright);
                RLine[i] = CHSV(hue - async, STROBE_SAT, strobe_bright_async);
            }
            FastLED.show();
            return;
        case 12:
            break;
        case 13:
            break;
        case 14:
            break;
        case 15:
            break;
        case 45:
            for (int i = 0; i < Rlenght; i++) {
                LLine[i] = CHSV(hue, STROBE_SAT, strobe_bright);
                RLine[i] = CHSV(hue - async, STROBE_SAT, strobe_bright_async);
            }
            FastLED.show();
            return;
    }
    for (int i = 0; i < Rlenght; i++) {
        RLine[i] = ColorFromPalette(myPal, (i * 255 / NUM_LEDS));   // заливка по палитре " от зелёного к красному"
        LLine[i] = ColorFromPalette(myPal, (i * 255 / NUM_LEDS));   // заливка по палитре " от зелёного к красному"
    }
    FastLED.show();
}

String val = "";
void BlueTooth_socket() {
    BTtimer = millis() - DELAY_BT+WAIT_BT;
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
        }else if (text == "LOW") {
            digitalWrite(AUDIO_OFF, LOW);
            btSerial.print("OK");
            EEPROM.put(1, false);
        } else if (text == "HIGH"){
            digitalWrite(AUDIO_OFF, HIGH);
            btSerial.print("OK");
            EEPROM.put(1, true);
        } else if (text == "END") {
          return;
        } else if (text.substring(0, 3) == "CF:") {
          coef= (float)text.substring(3).toInt()/10;
          btSerial.print("OK");
          EEPROM.put(2, coef);
        } else if (text.substring(0, 3) == "Br:") {
            int h = text.substring(3).toInt();
            FastLED.setBrightness(h);
            btSerial.println("OK");
            FastLED.show();
            EEPROM.put(3, h);
        } else if (text.substring(0, 3) == "Ty:") {
            this_mode = text.substring(3, 6).toInt();
            btSerial.println("OK");
            async = 255 * (this_mode / 100);
            this_mode %=100;    // записали ключ
            EEPROM.put(0, this_mode);
            EEPROM.put(5, async);
            if (this_mode/10 == 12 || this_mode/10 == 13){
              not_spektr();
            }else{ spektr();}
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
    if (btSerial.available()) { BlueTooth_socket(); }//?
    if (millis() - BTtimer < DELAY_BT) { return; }
    if (millis() - MainTimer > MainLoop) { regims(); }
    if (millis() - hue_timer > rainbow_speed) {
        hue += RAINBOW_STEP*coef;
        hue_timer = millis();
    }
    if (millis() - LowPass_Timer > LowPassTime ){ autoLowPass();}
}

void autoLowPass() {
    // для режима VU
    LowPass_Timer = millis();
    delay(10);                                // ждём инициализации АЦП
    int thisMax = 0;                          // максимум
    int thisLevel;
    for (byte i = 0; i < 100; i++) {
        thisLevel = analogRead(SOUND_R);        // делаем 200 измерений
        if (thisLevel > thisMax)                // ищем максимумы
            thisMax = thisLevel;                  // запоминаем
        delay(4);                               // ждём 4мс
    }
    LOW_PASS = (thisMax + LOW_PASS_ADD);        // нижний порог как максимум тишины + некая величина
    SPEKTR_LOW_PASS = thisMax + LOW_PASS_FREQ_ADD;  // нижний порог как максимум тишины
}