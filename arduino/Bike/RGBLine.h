//
// Created by artem on 02.05.24.
//


//#include <StandardCplusplus.h>//https://roboticsbackend.com/arduino-stl-library/
//#include <vector>
//#include <map>
#include <FastLED.h>

class RGBLine {
    unsigned short oldMode = 11;
    const int pin;
    CRGBPalette32 myPal;
    byte *colors;
    float sound;
    /*std::vector<void (RGBLine::*)()> changeGradient = {nullptr, nullptr, nullptr, nullptr, nullptr,
                                                       nullptr, nullptr, nullptr, nullptr, nullptr,
                                                       nullptr,};
    std::vector<void (RGBLine::*)()> funcs = {nullptr, nullptr, nullptr, nullptr, nullptr,
                                              nullptr, nullptr, nullptr, nullptr, nullptr,
                                              nullptr,};
    /*std::map<int, void (RGBLine::*)()> funcs = {{11, &RGBLine::regAA}};/*,
                                             {2, &RGBLine::params}};*/
    long int STROBE_PERIOD = 100;
    int STROBE_SMOOTH = 10;
    long int strobe_timer = 0;
    bool strobeUp_flag = true, strobeDwn_flag = false;
    long int light_time = STROBE_PERIOD / 2;
    int STROBE_SAT = 255;
public:
    CRGB line;
    byte bright = 0;
    //byte* colors;
    int count;
    unsigned short mode = 11;
    CFastLED *fastLED = nullptr;

    RGBLine(int pin, int count, byte (&colors)[24], float &sound);

    void setFastLED(CFastLED *fastLED) { this->fastLED = fastLED; };

    void setMode(unsigned short mode) { this->mode = mode; };

    void setBright(int bright) { this->bright = bright; };

    void setColors(byte *newColors);

    void changeMode();

    void regGradient();

    void regHSV();

    void show();

    void regAA();

    void regAB();

    void regDA();

    void changeGradientAB();

    void changeGradientAC();

    void changeGradientAD();

    void blick();
};