//
// Created by artem on 02.05.24.
//

#include "BTSerial.h"

BTSerial::BTSerial(int RX, int TX) : SoftwareSerial(RX, TX) {
    this->begin(SPEED);
    this->setTimeout(TIMEOUT);
    timer=millis();
}

short BTSerial::getSocket(byte &bright, unsigned short &curMode, byte (&colors)[24], byte &frequency) {
    /*не ставить тайм ауты, тк по умолчанию размер буфера составляет 64 байта и при переполнении старые данные будут зетерты
    if(!available() || (sz==-1 && millis()-timer<100)){//если ввод пуст или время с начала ввода меньше тайм аута
        return OK;      
    } else if(sz==-2){ //если начался прием
        sz=-1;
        timer = millis();
        return OK;     
    } else*/
    if(!available()){
        return OK;
    }
    if (millis()-timer > DELAY){ //если пора очищать ввод
        sz=-1;
    }
    if (sz==-1) {
        timer = millis();
    }
     /*
    Serial.print("Size:");
    Serial.print(sz);
    Serial.print(" buf:");
    Serial.print(buf);
    Serial.print(" avaliable:");
    Serial.println(available());*/
    do {
        buf[++sz] = this->read();
    } while (available() and buf[sz]!='\n' and sz < MAXSZ);
    if (MAXSZ == sz) {
        sz=-1;
        return ERROR; 
    }
    if(buf[sz]=='\n'){
        buf[sz]=0;
    }else{
        buf[sz+1]=0;
        /*Serial.print(F("Part: "));
        Serial.print(buf);
        Serial.print(" avaliable:");
        Serial.println(available());*/
        timer = millis()-TIMEOUT;
        return WAIT_INPUT;
    }
    Serial.print(F("Command:"));
    Serial.print(buf);
    Serial.print(F(" size:"));
    Serial.print(sz);
    Serial.print(F(" avaliable:"));
    Serial.println(available());
    short ans = OK;
    if (compareStr(buf, "GC")) {
          for (int x = 0; x < 6; ++x) {
                this->print(colors[x * 4]);
                this->print(F(","));
                this->print(colors[x * 4 + 1]);
                this->print(F(","));
                this->print(colors[x * 4 + 2]);
                this->print(F(","));
                this->print(colors[x * 4 + 3]);
                this->print(F(","));
          }
    }else if (compareStr(buf, "Con")) {
        this->print(F("OK"));
    } else if (compareStr(buf, "OFF")) {
        this->print(F("OK"));
        ans = OFF;
    } else if (compareStr(buf, "ON")) {
        this->print(F("OK"));
        ans = ON;
    } else if (compareStr(buf, "LOW")) {
        this->print(F("OK"));
        ans = SOUND_OFF;
    } else if (compareStr(buf, "HIGH")) {
        this->print(F("OK"));
        ans = SOUND_ON;
    } else if (compareStr(buf, "END")) {
        ans = END;
    } else {
        char *t = subStr(buf, 0, 3);
        if (compareStr(t, "Br:")) {
            bright = static_cast<byte>(strToLongInt(buf+3));
            ans = BRIGHT;
        } else if (compareStr(t, "Ty:")) {
            curMode = static_cast<unsigned short>(strToLongInt(buf+3));
            ans = MODE;
        } else if (compareStr(t, "CF:")) {
            frequency = static_cast<unsigned short>(strToLongInt(buf+3));
            ans = FREQUENCY;
        } else if (compareStr(t, "Co:")){
            if(sz != 99) {
                Serial.println(F("Damaged message"));
            }
            char *val;
            for (int i = 0; i < 24; ++i) {
                val = subStr(buf+3 + i * 4, 0, 3);
                colors[i] = static_cast<byte>(strToLongInt(val));
                free(val);
            }
            ans = COLORS;
        }
        free(t);
    }
    sz=-1;
    return ans;
}