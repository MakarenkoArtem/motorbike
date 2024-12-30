#define LOW 0x0
#define HIGH 0x1

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