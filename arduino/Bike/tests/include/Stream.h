#pragma once
#include <string>
#include <queue>
#include <iostream>

class Stream {
public:
    std::queue<char> input;
    std::string output ="Test";

    Stream() {}
    Stream(int RX, int TX){}

    void begin(int) {}
    void write(char c) { output += c; }

    int read() {
        if (input.empty()) return -1;
        char c = input.front();
        input.pop();
        return c;
    }

    int available() { return input.size(); }

    template<typename T>
    void print(T val) { output += std::to_string(val); }

    void print(const char* s) { output += s; }
    void print(const std::string& s) { output += s; }

    void println(const char* s) { output += s; output += "\n"; }
    void println(const std::string& s) { output += s; output += "\n"; }
};
