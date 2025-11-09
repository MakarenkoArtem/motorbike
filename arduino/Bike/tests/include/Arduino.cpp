#include "Arduino.h"
#include <chrono>
#include <thread>
#include <iostream>

unsigned long millis() {
    return static_cast<unsigned long>(
        std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::steady_clock::now().time_since_epoch()
        ).count()
    );
}

void delay(unsigned long ms) {
    std::this_thread::sleep_for(std::chrono::milliseconds(ms));
}
/*
struct DummySerial {
    void begin(int) {}
    void println(const char* msg) { std::cout << msg << std::endl; }
    void print(const char* msg) { std::cout << msg; }
} Serial;*/
