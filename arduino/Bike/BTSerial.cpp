#include "BTSerial.h"

BTSerial::BTSerial(int RX, int TX) : SoftwareSerial(RX, TX) {
    this->begin(SPEED);
    this->setTimeout(TIMEOUT);
    timer = millis();
}

short BTSerial::getCommands(Parameters &parameters) {
    if (!available()) {
        return OK;
    }
    //не ставить тайм ауты, тк по умолчанию размер буфера составляет 64 байта и при переполнении старые данные будут зетерты
    if (millis() - timer > DELAY) { //если пора очищать ввод
        sz = -1;
    }
    if (sz == -1) {
        timer = millis();
    }
    do {
        buf[++sz] = this->read();
    } while (available() and buf[sz] != '\n' and sz < MAXSZ);
    if (MAXSZ == sz) {
        sz = -1;
        return ERROR;
    }
    if (buf[sz] == '\n') {
        buf[sz] = 0;
    } else {
        buf[sz + 1] = 0;
        timer = millis() - TIMEOUT;
        if (buf[0] == COMMAND[0] && buf[1] == COMMAND[1]) { delay(10); }
        return WAIT_INPUT;
    }
    Serial.print(F("Command:"));
    Serial.print(buf);
    Serial.print(F(" size:"));
    Serial.print(sz);
    Serial.print(F(" avaliable:"));
    Serial.println(available());
    return messageProcessing(parameters);
}

short BTSerial::messageProcessing(Parameters &parameters) {
    short ans = OK;
    if (compareStr(buf, "GC")) {
        for (int x = 0; x < 6 * 4; ++x) {
            this->print(parameters.colors[x]);
            this->print(F(","));
        }
    } else if (compareStr(buf, "Con")) {
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
        char *firstPart = subStr(buf, 0, 3);
        if (compareStr(firstPart, "Br:")) {
            parameters.maxBright = static_cast<byte>(strToLongInt(buf + 3));
            ans = BRIGHT;
        } else if (compareStr(firstPart, "Ty:")) {
            parameters.mode = static_cast<unsigned short>(strToLongInt(buf + 3));
            ans = LINE_MODE;
        } else if (compareStr(firstPart, "CF:")) {
            parameters.frequency = static_cast<unsigned short>(strToLongInt(buf + 3));
            ans = FREQUENCY;
        } else if (compareStr(firstPart, "Co:")) {
            ans = changeColors(buf + 3, parameters.colors);
        }
        free(firstPart);
    }
    sz = -1;
    return ans;
}

short BTSerial::changeColors(char *buf, byte *colors) {
    if (sz != 99) {
        Serial.println(F("Damaged message"));
        this->print(F("Damaged message"));
        return ERROR;
    }
    char *val;
    for (int i = 0; i < 24; ++i) {
        val = subStr(buf + i * 4, 0, 3);
        colors[i] = static_cast<byte>(strToLongInt(val));
        free(val);
    }
    this->print(F("OK"));
    return COLORS;
}

