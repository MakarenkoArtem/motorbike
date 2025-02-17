#include "BTSerial.h"

BTSerial::BTSerial(int RX, int TX) : SoftwareSerial(RX, TX) {
    this->begin(SPEED);
    this->setTimeout(TIMEOUT);
    timer = millis();
}

short BTSerial::getCommands(Parameters& parameters) {
    if (!available()) {
        return OK;
    }
    //не ставить тайм ауты, тк по умолчанию размер буфера составляет 64 байта и при переполнении старые данные будут зетерты
    if (millis() - timer > DELAY) {
        //если пора очищать ввод
#if DEBUGBT
        whatDel();
#endif
        sz = -1;
    }
    if (sz == -1) {
        timer = millis();
    }
    do {
        buf[++sz] = this->read();
    } while (available() and buf[sz] != '\n' and sz < MAXSZ);
    if (MAXSZ == sz) {
#if DEBUGBT
        whatDel();
#endif
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

short BTSerial::messageProcessing(Parameters& parameters) {
    short ans = OK;
    if (compareStr(buf, "GC")) {
        ans = GET_COLOR;
        for (int x = 0; x < 6 * 4; ++x) {
            this->print(parameters.colors[x]);
            this->print(F(","));
        }
    } else if (compareStr(buf, "Con")) {
    } else if (compareStr(buf, "OFF")) {
        ans = OFF;
    } else if (compareStr(buf, "ON")) {
        ans = ON;
    } else if (compareStr(buf, "LowAmp") || compareStr(buf, "LOW")) {
        ans = AMPLIFIER_OFF;
    } else if (compareStr(buf, "HighAmp") || compareStr(buf, "HIGH")) {
        ans = AMPLIFIER_ON;
    } else if (compareStr(buf, "OffBT")) {
        ans = AUDIO_BT_OFF;
    } else if (compareStr(buf, "OnBT")) {
        ans = AUDIO_BT_ON;
    } else if (compareStr(buf, "OnHSV")) {
        parameters.hsv = true;
    } else if (compareStr(buf, "OffHSV")) {
        parameters.hsv = false;
    } else if (compareStr(buf, "OnMov")) {
        parameters.movement = true;
    } else if (compareStr(buf, "OffMov")) {
        parameters.movement = false;
    } else if (compareStr(buf, "OnSync")) {
        parameters.synchrony = true;
    } else if (compareStr(buf, "OffSync")) {
        parameters.synchrony = false;
    } else if (compareStr(buf, "OnGrad")) {
        parameters.gradient = true;
    } else if (compareStr(buf, "OffGrad")) {
        parameters.gradient = false;
    } else if (compareStr(buf, "END")) {
        ans = END;
    } else {
        char* firstPart = subStr(buf, 0, 3);
        if (compareStr(firstPart, "Br:")) {
            parameters.setMaxBright(static_cast<byte>(strToLongInt(buf + 3)));
        } else if (compareStr(firstPart, "Ty:")) {
            parameters.setMode(static_cast<unsigned short>(strToLongInt(buf + 3)));
        } else if (compareStr(firstPart, "CF:")) {
            parameters.setFrequency(static_cast<unsigned short>(strToLongInt(buf + 3)));
        } else if (compareStr(firstPart, "Cr:")) {
            ans = changeColor(buf + 3, parameters.colors);
        } else if (compareStr(firstPart, "Co:")) {
            ans = changeColors(buf + 3, parameters.colors);
        } else {
            Serial.println(F("Damaged message"));
            this->print(F("Damaged message"));
            ans = ERROR;
        }
        free(firstPart);
    }
    if (ans != ERROR) {
        this->print(F("OK"));
    } else {
#if DEBUGBT
        whyError();
#endif
    }
    sz = -1;
    return ans;
}

short BTSerial::changeColor(char* buf, byte* colors) {
    if (sz != 19) {
        Serial.println(F("Damaged message"));
        this->print(F("Damaged message"));
        return ERROR;
    }
    char* val = subStr(buf, 0, 3);
    int index = static_cast<int>(strToLongInt(val));
    free(val);
    for (int i = 1; i < 4; ++i) {
        val = subStr(buf + i * 4, 0, 3);
        colors[index * 4 + i] = static_cast<byte>(strToLongInt(val));
        free(val);
    }
    return COLORS;
}

short BTSerial::changeColors(char* buf, byte* colors) {
    if (sz != 99) {
        Serial.println(F("Damaged message"));
        this->print(F("Damaged message"));
        return ERROR;
    }
    char* val;
    for (int i = 0; i < 24; ++i) {
        val = subStr(buf + i * 4, 0, 3);
        colors[i] = static_cast<byte>(strToLongInt(val));
        free(val);
    }
    return COLORS;
}

void BTSerial::whatDel() {
    if (sz > 2) {
        Serial.print(F("Deleting damaged message:"));
        Serial.print(buf);
    }
}

void BTSerial::whyError() {
    Serial.print(F("Error due to this message:"));
    Serial.print(buf);
}

//verified 11.02.25
