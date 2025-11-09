#include "../BTSerial.h"
#include <cassert>
#include <cstring>
#include <iostream>

void fillInput(BTSerial &bt, const char *cmd) {
    for (char *c = (char *) cmd; *c; ++c) {
        bt.input.push(*c);
    }
}

void tests(BTSerial &bt, Parameters &params, char *text, short answer) {
    std::cout << "Input: "<< text << std::endl;
    fillInput(bt, text);
    assert(answer == bt.getCommands(params));
    bt.input = std::queue<char>();
}

Parameters params(colors);
BTSerial bt(0, 0); // подключаем объект класса работы с блютуз
int main() {
    const short N = 5;
    char *inputs[N] = {"ON\n", "CON\n", "Con\ndfs", "HighAmp", "HighAm"};
    short answers[N] = {ON, ERROR,OK, AMPLIFIER_ON, WAIT_INPUT};
    for (int i = 0; i != N; ++i) {
        tests(bt, params, inputs[i], answers[i]);
        std::cout << "\nTest " << inputs[i] << " passed" << std::endl;
    }
    return 0;
}
