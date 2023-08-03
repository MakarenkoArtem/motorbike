#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))

#define LLine_pin 2
#define RLine_pin 3
#define TX_BLUETOOTH 4  //0
#define RX_BLUETOOTH 5  //1
#define BIKE_OFF A2
#define SOUND_R A6
#define SOUND_L A7
// --------------------------- НАСТРОЙКИ ---------------------------
int this_mode = 111;
// настройки радуги
int rainbow_speed = 12;    // скорость движения радуги (чем меньше число, тем быстрее радуга)
#define RAINBOW_STEP 3     // шаг изменения цвета радуги
byte hue = 0;
unsigned long hue_timer=0;

// режим стробоскопа
#define STROBE_PERIOD 150*2           // период вспышек, миллисекунды
#define STROBE_DUTY 50              // скважность вспышек (1 - 99) - отношение времени вспышки ко времени темноты
#define STROBE_SAT 255                // насыщенность. Если 0 - цвет будет БЕЛЫЙ при любом цвете (0 - 255)
#define STROBE_SMOOTH 255           // скорость нарастания/угасания вспышки (0 - 255)
unsigned long main_timer, strobe_timer, pass_timer;
int thisBright[3], strobe_bright = 0;
unsigned int light_time = STROBE_PERIOD * STROBE_DUTY / 100;
boolean colorMusicFlash[3], strobeUp_flag, strobeDwn_flag;

//----------------------Bluetooth--------------
#include <SoftwareSerial.h>
SoftwareSerial btSerial(RX_BLUETOOTH, TX_BLUETOOTH); // RX, TX
/*#include "AsyncStream.h"
AsyncStream<150> btserial(&btSerial,"\n", 200);*/
#define WAIT_BT 200
#define DELAY_BT 1000
unsigned long BTtimer;


//------------------------RGB---------------------
#define NUM_LEDS 20
#include "FastLED.h"
CRGB LLine[NUM_LEDS];
CRGB RLine[NUM_LEDS];
int RLen = NUM_LEDS;
int LLen = NUM_LEDS;
// градиент-палитра от зелёного к красному
byte colors[]={
    0,    0,  255,  0,
  100,    0,  255, 100,
  100,    0,  200, 100,
  150,    0,  100, 200,
  200,    0,  100, 255,
  255,    0,    0, 255  };
//DEFINE_GRADIENT_PALETTE(soundlevel_gp) colors;
CRGBPalette32 myPal;

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
  FastLED.setBrightness(255);
  
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
    //animation();
}




void regims(){ 
  int c=0;
  byte j;
  byte colors_[]={};
  FastLED.clear();          // очистить массив пикселей
  int t = 255*(this_mode/100);
  myPal.loadDynamicGradientPalette(colors);
  blick();
  for (int i = 0; i < NUM_LEDS; i++) {
    LLine[i] = CHSV(hue, STROBE_SAT, strobe_bright);
    RLine[i] = CHSV(hue-t, STROBE_SAT, t - strobe_bright);
  }
  FastLED.show();
  return;
}
void loop() {
  if (millis()-BTtimer< DELAY_BT){return;}
  regims();
  if (millis() - hue_timer > rainbow_speed) {
    //hue=(hue + RAINBOW_STEP)%256;
    hue=hue + RAINBOW_STEP;
    hue_timer = millis();
  }
}

