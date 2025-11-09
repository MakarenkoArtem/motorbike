#pragma once
#include "Stream.h"
#include <iostream>
#include <string>

typedef unsigned char byte;

class SoftwareSerial : public Stream {
public:
    SoftwareSerial(int RX, int TX) {
        // Можно сохранить пины, если нужно
    }
    SoftwareSerial() {
    }

    void begin(int) {}
    void setTimeout(int) {}

    void print(const std::string &s) { std::cout << s; }
    void print(const char* s) { std::cout << s; }
    void print(int n) { std::cout << n; }
    void println(const std::string &s) { std::cout << s << std::endl; }
    void println(const char* s) { std::cout << s << std::endl; }
    void println() { std::cout << std::endl; }
    void println(int n) { std::cout << n << std::endl; }

};

extern SoftwareSerial Serial;

#define F(x) x
inline int map(int x, int in_min, int in_max, int out_min, int out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}