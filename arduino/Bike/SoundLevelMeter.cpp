//
// Created by artem on 05.05.24.
//
#include "SoundLevelMeter.h"

void setADCPrescaler(uint8_t prescaler){
  ADCSRA=(ADCSRA&0b11111000)|prescaler;
}

SoundLevelMeter::SoundLevelMeter(int pinR, int pinL, void (*pinMode)(int, int), int (*analogRead)(int))
        : pinR(pinR), pinL(pinL), pinMode(pinMode), analogRead(analogRead) {
          pinMode(pinR, INPUT);
          pinMode(pinL, INPUT);
}


int SoundLevelMeter::currentLevelOfSound(){
  int m=0,cur;
  for(int i=0;i<100;++i){
    cur=analogRead(pinR);
    if(cur>m){
      m=cur;
    }
  }
  averageLevel=(averageLevel*100+m*5)/105;
  return m;
}


float SoundLevelMeter::amplitudeLight(){
  int cur=currentLevelOfSound();
  #ifdef DEBUG_SOUND
  Serial.print(" cur:");
  Serial.print(cur);
  Serial.print(" averageLevel:");
  Serial.print(averageLevel);
  Serial.print(" filter:");
  Serial.println(averageLevel-filterValue);
  #endif
  if(averageLevel>100){
    if(avegareTimeLight<140){
      if(filterValue<10){
      ++filterValue;
      }
    }else if(filterValue>-5){
      --filterValue;
    }
    //Serial.print(" bef:");
    //Serial.print(amplitude*100);
    if(cur-averageLevel>filterValue){
          if(!amplitude){
            currentTimer=millis();
          }
          amplitude+=1;
          if(amplitude>1){
            amplitude=1;
          }
    } else{         
          if(amplitude==1){
            avegareTimeLight=(avegareTimeLight*100+(millis()-currentTimer)*2.5)/102.5;
          }
          amplitude-=1;
          if(amplitude<0){
              amplitude=0;
            }
    }
  }else{
    amplitude=0;
  }
  return amplitude;
}

void SoundLevelMeter::fhtSound() {
    int RcurrentLevel, LcurrentLevel;
    setADCPrescaler(ADC_PRESCALER_2); //изменение времени анализа напряжения, сама операция 3 машинных операции(analogRead ~200мо)
    for (int i = 0; i < 100; i++) { // делаем 100 измерений
        RcurrentLevel = analogRead(pinR);                            // с правого
        LcurrentLevel = analogRead(pinL);                 // и левого каналов
        if (RsoundLevel < RcurrentLevel)
            RsoundLevel = RcurrentLevel;   // ищем максимальное
        if (LsoundLevel < LcurrentLevel)
            LsoundLevel = LcurrentLevel;   // ищем максимальное
    }// фильтруем по нижнему порогу шумов
    setADCPrescaler(ADC_PRESCALER_16);
    RsoundLevel = max(0, map(RsoundLevel, LOW_PASS, 1023, 0, 500));
    LsoundLevel = max(0, map(LsoundLevel, LOW_PASS, 1023, 0, 500));

    // возводим в степень (для большей чёткости работы)
    RsoundLevel = pow(RsoundLevel, EXP);
    LsoundLevel = pow(LsoundLevel, EXP);

    // фильтр
    RsoundLevel_f = RsoundLevel * SMOOTH + RsoundLevel_f * (1 - SMOOTH);
    LsoundLevel_f = LsoundLevel * SMOOTH + LsoundLevel_f * (1 - SMOOTH);

    if (RsoundLevel_f > 15 || LsoundLevel_f > 15) {

        // расчёт общей средней громкости с обоих каналов, фильтрация.
        // Фильтр очень медленный, сделано специально для автогромкости
        averageLevel = (RsoundLevel_f + LsoundLevel_f) / 2 * AVERK + averageLevel * (1 - AVERK);

        // принимаем максимальную громкость шкалы как среднюю, умноженную на некоторый коэффициент MAX_COEF
        maxLevel = averageLevel * MAX_COEF;
        // преобразуем сигнал в длину ленты (где NUM_LEDS это половина количества светодиодов)
        Rlenght = map(RsoundLevel_f, 0, maxLevel, 0, 100);
        Llenght = map(LsoundLevel_f, 0, maxLevel, 0, 100);
        Serial.print(F("Rlenght:"));
        Serial.println(Rlenght);
        // ограничиваем до макс. числа светодиодов
        Rlenght = constrain(Rlenght, 0, 100);
        Llenght = constrain(Llenght, 0, 100);
    }
}

