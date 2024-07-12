//
// Created by artem on 02.05.24.
//

#include "BTSerial.h"

BTSerial::BTSerial(int RX, int TX) : SoftwareSerial(RX, TX) {
    this->begin(9600);
    this->setTimeout(TIMEOUT);
    timer=millis();
}

short BTSerial::getSocket(byte &bright, unsigned short &curMode, byte (&colors)[24]) {
    if (sz!=this->available() || !sz){
        sz=this->available();
        return OK;//(timer>millis())?WAIT_INPUT:OK;
    }   
    Serial.print(sz!=this->available());
    Serial.print(" ");
    Serial.print(!sz);
    Serial.print(" ");
    Serial.print(sz);
    Serial.print(" ");
    Serial.println(available());
    //this->flush();//ожидать конца передачи данных
    sz = -1;
    *buf=0;
    do {
        buf[++sz] = this->read();
    } while (this->available() and buf[sz] and sz <= MAXSZ);
    /*if (!this->available() && buf[sz]){
        timer=millis()+delay;
        return WAIT_INPUT;
    }*/
    if (MAXSZ < sz) {
        return ERROR;
    }
    short ans = OK;
    if (compareStr(buf, "OFF")) {
        this->println(F("OK"));
        ans = OFF;
    } else if (compareStr(buf, "ON")) {
        ans = ON;
    } else if (compareStr(buf, "END")) {
        ans = END;
    } else {
        char *t = subStr(buf, 0, 3);
        if (compareStr(t, "Br:")) {
            bright = static_cast<byte>(strToLongInt(buf+3));
        } else if (compareStr(t, "Ty:")) {
            curMode = static_cast<unsigned short >(strToLongInt(buf+3));
        } else if (compareStr(t, "Co:") && sz == 99) {
            char *val;
            for (int i = 0; i < 24; ++i) {
                val = subStr(buf+3 + i * 4, 0, 3);
                colors[i] = static_cast<byte>(strToLongInt(val));
                free(val);
            }
            //timer += DELAY;
        } else if (compareStr(t, "Con")) {
            this->println(F("OK "));
            for (int x = 0; x < 6; ++x) {
                this->print(colors[x * 4]);
                this->print(F(", "));
                this->print(colors[x * 4 + 1]);
                this->print(F(", "));
                this->print(colors[x * 4 + 2]);
                this->print(F(", "));
                this->print(colors[x * 4 + 3]);
                this->print(F(", "));
            }
            //timer += DELAY;
        }// else { BTtimer += DELAY_BT; }
        free(t);
    }
    Serial.println(buf);
    return ans;
}