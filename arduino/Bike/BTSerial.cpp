//
// Created by artem on 02.05.24.
//

#include "BTSerial.h"

BTSerial::BTSerial(int RX, int TX) : SoftwareSerial(RX, TX) {
    this->begin(9600);
    this->setTimeout(timeOut);
    timer=millis();
}

int BTSerial::getSocket(byte &bright, int &curMode, byte (&colors)[24], char* &buf, int &sz) {
    /*Serial.print(this->available());
    Serial.print(" ");
    Serial.print(timer);
    Serial.print(" ");
    Serial.println(millis());*/
    //Serial.println(timer);
    if (!this->available()){
        return (timer>millis())?WAIT_INPUT:OK;
    }
    Serial.print(this->available());

    //this->flush();//ожидать конца передачи данных
    /*Serial.print(1);
    Serial.print(this->readBytesUntil('\n', buf, maxSz));
    Serial.print(2);
    Serial.println(buf);
    Serial.println("adasdsf!!!!!!!");*/
    /*char g[maxSz];
    *str="buf:"+String(this->readBytesUntil('\n', (char*)&g, maxSz));
    *str+=String(g)+"\n";
    Serial.println(*str);
    return WAIT_INPUT;*/
    char ch;
    Serial.println("BT");
    do {
        ch = this->read();
        Serial.print((byte)ch);
        Serial.print(" ");
        Serial.println(ch);
        /*Serial.print(ch);
        Serial.print(" ");*/
        //Serial.println(buf);
        if (ch == '\n' || !ch) {
            buf[sz] = '\0';
            break; }
        buf[sz++] = ch;
        //Serial.print(this->available());
        //Serial.print(ch);
        //Serial.println(sz);
    } while (this->available() and ch and sz <= maxSz);
    //*str=String(buf)+" sz:"+String(sz);
    if (!this->available() && buf[sz]){
        timer=millis()+delay;
        return WAIT_INPUT;
    }
    if (maxSz < sz) {
        sz = 0;
        //throw std::out_of_range("");
        return ERROR;
    }
    int ans = OK;
    for(int i=0;i!=sz+1;Serial.print(buf[i++]));
    Serial.println(F("VAL"));
    if (compareStr(buf, (char*)&"OFF")) {
        this->println("OK");
        ans = OFF;
    } else if (compareStr(buf, (char*)&"ON")) {
        this->println("OK");
        ans = ON;
    } else if (compareStr(buf, (char*)&"END")) {
        ans = END;
    } else {
        char *t = subStr(buf, 0, 3);
        Serial.println("TTTTTTTTTTt");
        for(int i=0;i!=3;Serial.print(t[i++]));
        if (compareStr(t, (char*)&"Br:")) {
            bright = strToInt((char*)&buf[3]);
        } else if (compareStr(t, (char*)&"Ty:")) {
            curMode = strToInt((char*)&buf[3]);
        } else if (compareStr(t, (char*)&"Co:") && sz == 99) {
            char *val;
            for (int i = 0; i < 24; ++i) {
                val = subStr(&buf[3 + i * 4], 0, 3);
                colors[i] = strToInt(val);
                free(val);
            }
            timer += delay;
        } else if (compareStr(t, (char*)&"Con")) {
            this->println("OK ");
            for (int x = 0; x < 6; ++x) {
                this->print(colors[x * 4]);
                this->print(", ");
                this->print(colors[x * 4 + 1]);
                this->print(", ");
                this->print(colors[x * 4 + 2]);
                this->print(", ");
                this->print(colors[x * 4 + 3]);
                this->print(", ");
            }
            timer += delay;
        }// else { BTtimer += DELAY_BT; }
        free(t);
    }
    Serial.println("Repeat");
    sz = 0;
    *buf=0;
    return ans;
}