//
// Created by artem on 02.05.24.
//

#include "BTSerial.h"

BTSerial::BTSerial(int RX, int TX) : SoftwareSerial(RX, TX) {
    this->setTimeout(timeOut);
}

int BTSerial::getSocket(byte *bright, int *curMode, byte **colors) {
    if (!this->available()) { return OK; }
    char ch;
    do {
        ch = this->read();
        if (ch == '\n') { break; }
        buf[sz++] = ch;
    } while (ch and sz <= maxSz);
    if (maxSz < sz) {
        sz = 0;
        //throw std::out_of_range("");
        return ERROR;
    }
    int ans = OK;
    buf[sz] = '\0';
    if (compareStr((char*)&buf, (char*)&"OFF")) {
        ans = OFF;
    } else if (compareStr((char*)&buf, (char*)&"ON")) {
        ans = ON;
    } else if (compareStr((char*)&buf, (char*)&"END")) {
        ans = END;
    } else {
        char *t = subStr((char*)&buf, 0, 3);
        if (compareStr(t, (char*)&"Br:")) {
            *bright = strToInt((char*)&buf[3]);
        } else if (compareStr(t, (char*)&"Ty:")) {
            *curMode = strToInt((char*)&buf[3]);
        } else if (compareStr(t, (char*)&"Co:") && sz == 99) {
            char *val;
            for (int i = 0; i < 24; ++i) {
                val = subStr(&buf[3 + i * 4], 0, 3);
                *colors[i] = strToInt(val);
                free(val);
            }
            timer += delay;
        } else if (compareStr(t, (char*)&"Con")) {
            timer += delay;
        }// else { BTtimer += DELAY_BT; }
        free(t);
    }
    sz = 0;
    return ans;
}