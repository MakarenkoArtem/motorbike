//
// Created by artem on 02.05.24.
//

#include "IgnitionKey.h"

IgnitionKey::IgnitionKey(int pin, void (*pinMode)(int, int), void (*digitalWrite)(int, int)) :
        pin(pin), pinMode(pinMode), digitalWrite(digitalWrite) {
    pinMode(pin, HIGH);
    digitalWrite(pin, val ? HIGH : LOW);
}


void IgnitionKey::setVal(bool val) {
    this->val = val;
    digitalWrite(pin, val ? HIGH : LOW);
}