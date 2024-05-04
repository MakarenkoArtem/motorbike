//
// Created by artem on 02.05.24.
//

#ifndef BIKE_IGNITIONKEY_H
#define BIKE_IGNITIONKEY_H
#define OUTPUT 0
#define LOW 0
#define HIGH 1

class IgnitionKey {
    int pin;
    bool val = false;
    unsigned long int timer;

    void (*pinMode)(int, int);

    void (*digitalWrite)(int, int);

public:
    IgnitionKey(int pin, void (*pinMode)(int, int),
                void (*digitalWrite)(int, int));


    void setVal(bool val);
};


#endif //BIKE_IGNITIONKEY_H
