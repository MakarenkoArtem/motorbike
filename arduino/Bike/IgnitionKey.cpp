//
// Created by artem on 02.05.24.
//

#include "IgnitionKey.h"

IgnitionKey::IgnitionKey(int pin, void (*pinMode)(int, int),
                         void (*digitalWrite)(int, int)) : pin(pin), pinMode(pinMode),
                                                           digitalWrite(digitalWrite) {
    pinMode(pin, 0x1);
    digitalWrite(pin, val ? 0x1 : 0x0);
}


void IgnitionKey::setVal(bool val) {
    this->val = val;
    digitalWrite(pin, val ? 0x1 : 0x0);
}