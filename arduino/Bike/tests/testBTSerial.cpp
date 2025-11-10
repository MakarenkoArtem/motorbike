#include "../BTSerial.h"
#include <cassert>
#include <iostream>

void fillInput(BTSerial &bt, const char *cmd) {
    for (char *c = (char *) cmd; *c; ++c) {
        bt.input.push(*c);
    }
}

void tests(Parameters &params, const char *text, short answer) {
    std::cout << "================\nInput: " << text << std::endl;
    BTSerial bt(0, 0);
    fillInput(bt, text);
    assert(answer == bt.getCommands(params));
    char *val = replace(copyStr((char *) text), (char *) "\n", "\\", -1);
    std::cout << "\nPassed: test " << val << std::endl;
    free(val);
}

Parameters params(colors);

int main() {
    const char *inputs[] = {"ON\n", "CON\n", "CON", "Con\ndfs", "HighAmp\n", "HighAm"};
    short answers[] = {ON, ERROR,WAIT_INPUT,OK, AMPLIFIER_ON, WAIT_INPUT};
    for (int i = 0; i != std::size(inputs); ++i) {
        tests(params, inputs[i], answers[i]);
    }
    return 0;
}

//g++.exe arduino\Bike\tests\testBTSerial.cpp ..\BTSerial.cpp ..\Parameters.cpp ..\mystring.cpp ..\config.cpp -isystem include include\SoftwareSerial.cpp include\Arduino.cpp -o testBTSerial
