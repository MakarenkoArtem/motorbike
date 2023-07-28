#define RX_BLUETOOTH 0
#define TX_BLUETOOTH 1
#define L_RGB 2
#define R_RGB 3
#define MISO_STICK 4
#define CLK_STICK 5
#define MOSI_STICK 6
#define CS_STICK 7
#define VRx_STICK 8
#define VRy_STICK 9
#define BTN_STICK 10
#define CLK_CLOCK 11
#define DATA_CLOCK 12
#define RST_CLOCK 13
#define R_AUDIO A7
#define L_AUDIO A6

#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
//--------------time------------------
 #include <iarduino_RTC.h>
iarduino_RTC time(RTC_DS1302,RST_CLOCK,CLK_CLOCK,DATA_CLOCK);         // Задаем правильно название нашего модуля, а также указываем к каким цифровым пинам его подключаем(в нашем случае – 8,6,7)
int timer_audio=0, period_audio=120;
//-------------audio------------------
// ----- нижний порог шумов
uint16_t LOW_PASS = 0;          // нижний порог шумов режим VU, ручная настройка
uint16_t SPEKTR_LOW_PASS = 40;    // нижний порог шумов режим спектра, ручная настройка
#define AUTO_LOW_PASS 0           // разрешить настройку нижнего порога шумов при запуске (по умолч. 0)
#define EEPROM_LOW_PASS 1         // порог шумов хранится в энергонезависимой памяти (по умолч. 1)
#define LOW_PASS_ADD 13           // "добавочная" величина к нижнему порогу, для надёжности (режим VU)
#define LOW_PASS_FREQ_ADD 3       // "добавочная" величина к нижнему порогу, для надёжности (режим частот)

// ----- режим шкала громкости
float SMOOTH = 0.3;               // коэффициент плавности анимации VU (по умолчанию 0.5)
#define MAX_COEF 1.8              // коэффициент громкости (максимальное равно срднему * этот коэф) (по умолчанию 1.8)

// ----- сигнал
#define EXP 1.4                   // степень усиления сигнала (для более "резкой" работы) (по умолчанию 1.4)



//--------------RGB-------------------
#define NUMPIXELS 16 // Popular NeoPixel ring size

#include "FastLED.h" // подключаем библиотеку фастлед
CRGB RGB_L[NUMPIXELS];
CRGB RGB_R[NUMPIXELS];
//// градиент-палитра от зелёного к красному

DEFINE_GRADIENT_PALETTE(palette_1) {
  0,    0,    255,  0,  // blue
  50,    255,    0,  0,  // green
  100,  255,  255,  0,  // yellow
  150,  255,  100,  0,  // orange
  200,  255,  50,   0,  // red
  255,  0,  255,   255,  // red
};
CRGBPalette32 Palette_1 = palette_1;







void setup() {  
  Serial.begin(9600);
  sbi(ADCSRA, ADPS2);
  cbi(ADCSRA, ADPS1);
  sbi(ADCSRA, ADPS0);
  //-------------audio------------------
  #if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
      analogReference(INTERNAL1V1);
  #else
      analogReference(INTERNAL);
  #endif
  Serial.println("start");
  
  FastLED.addLeds <WS2812, L_RGB, GRB>(RGB_L, NUMPIXELS).setCorrection(TypicalLEDStrip);
  FastLED.addLeds <WS2812, R_RGB, GRB>(RGB_R, NUMPIXELS).setCorrection(TypicalLEDStrip);
  FastLED.setBrightness(255);
  
  time.begin();     
  time.settime(0,35,21,13,4,23,1);  
}

int RcurrentLevel, LcurrentLevel;
float RsoundLevel=0,LsoundLevel=0;
float RsoundLevel_f=0,LsoundLevel_f=0;
float RsoundLevel_Max=100,LsoundLevel_Max=100;
float RsoundLevel_Min=0,LsoundLevel_Min=0;
void level_sound(){
  if ((millis()-timer_audio)>period_audio){
    timer_audio = millis();
    RsoundLevel=0;
    LsoundLevel=0;  
    for (byte i = 0; i < 100; i ++) {                                 // делаем 100 измерений
      RcurrentLevel = analogRead(R_AUDIO);                            // с правого
      LcurrentLevel = analogRead(L_AUDIO);                 // и левого каналов
      if (RsoundLevel < RcurrentLevel) RsoundLevel = RcurrentLevel;   // ищем максимальное
      if (LsoundLevel < LcurrentLevel) LsoundLevel = LcurrentLevel;   // ищем максимальное
    }
    // фильтруем по нижнему порогу шумов
    RsoundLevel_Min=min(RsoundLevel, RsoundLevel_Min+0.05);
    LsoundLevel_Min=min(LsoundLevel, LsoundLevel_Min+0.05);
    RsoundLevel_Max=max(RsoundLevel, RsoundLevel_Max-0.05);
    LsoundLevel_Max=max(LsoundLevel, LsoundLevel_Max-0.05);
    RsoundLevel = map(RsoundLevel, RsoundLevel_Min, RsoundLevel_Max, 0, 100);
    LsoundLevel = map(LsoundLevel, LsoundLevel_Min, LsoundLevel_Max, 0, 100);

    // ограничиваем диапазон
    RsoundLevel = constrain(RsoundLevel, 0, 100);
    LsoundLevel = constrain(LsoundLevel, 0, 100);

    // возводим в степень (для большей чёткости работы)
    RsoundLevel = pow(RsoundLevel, EXP);
    LsoundLevel = pow(LsoundLevel, EXP);

    // фильтр
    RsoundLevel_f = RsoundLevel * SMOOTH + RsoundLevel_f * (1 - SMOOTH);
    LsoundLevel_f = LsoundLevel * SMOOTH + LsoundLevel_f * (1 - SMOOTH);
  }
}


float max_level=100;
float min_level=0;

void level(int level){
  max_level=max(level, max_level-0.05);
  min_level=min(level, min_level+0.05);
  level =  map(level, min_level, max_level, 0, NUMPIXELS);
  FastLED.clear();
  //Serial.print(' ');
  for (int i=0; i<level; i++){
    RGB_L[i]=CRGB(255-(255*i/NUMPIXELS),0,255*i/NUMPIXELS);      
    RGB_R[i]=CRGB(255*i/NUMPIXELS,0,255-(255*i/NUMPIXELS));     
  }
  FastLED.show();
}  


String message;
void bt(){
  if(!Serial.available()){
    return ;
  }
  message=Serial.readString();
  Serial.println(message);
  if (message.substring(0, 6)=="Period"){
    period_audio=message.substring(7).toInt();
    Serial.println(period_audio);
  }
}



int paletteIndex=0;
void loop() {
  // put your main code here, to run repeatedly:
  /*Serial.print(analogRead(L_AUDIO));
  Serial.print("   ");
  Serial.println(analogRead(R_AUDIO));*/
  
  //Serial.println(RsoundLevel);
  /*for (int i = 0; i < RsoundLevel; i++) {
    //RGB_R[i] = ColorFromPalette(RainbowColors_p, i*3.7);   // заливка по палитре " от зелёного к красному"
  }
  for (int i = 0; i < LsoundLevel; i++) {
    //l_rgb[i] = ColorFromPalette(RainbowColors_p, i*3.7);   // заливка по палитре " от зелёного к красному"
  }*/
  bt();
  level_sound();
  //Serial.print(' ');
  //Serial.println(RsoundLevel_f);
  
  /*
  Serial.println(NUMPIXELS);
  c++;
  c%=NUMPIXELS;
  for(int i=0;i<NUMPIXELS;i++){Serial.println(i*255/NUMPIXELS); 
  RGB_L[i].setHue((c+i)%NUMPIXELS*255/NUMPIXELS);
  }*/
  fill_palette(RGB_L, NUMPIXELS, paletteIndex, 255/NUMPIXELS, Palette_1, 255, LINEARBLEND);
  EVERY_N_MILLISECONDS(10){
    paletteIndex++; 
  }
  FastLED.show();
  delay(100);
  //level(RsoundLevel_f);
  //FastLED.show();
  //Serial.println(time.gettime("d-m-Y, H:i:s, D"));   // выводим время
  //FastLED.clear();
}
