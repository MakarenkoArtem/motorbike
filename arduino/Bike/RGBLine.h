//
// Created by artem on 02.05.24.
//


//#include <StandardCplusplus.h>//https://roboticsbackend.com/arduino-stl-library/
//#include <vector>
//#include <map>
#include <FastLED.h>
#define StrobePeriod 150
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
    long int strobePeriod = StrobePeriod;
    //int STROBE_SMOOTH = 75;
    //long int strobeTimer = 0;
    //bool strobeUp_flag = true, strobeDwn_flag = false;
    //long int light_time = STROBE_PERIOD / 2;
    int STROBE_SAT = 255;
    byte strobeBright=0;
    byte id;
    byte hue=0;
    byte hueStep=2;
    int hueSpeed=3;
    int hueTimer = millis();
public:
    CRGB* line;
    byte bright = 0;
    int count;
    unsigned short mode = 11;
    byte frequency=0;
    CFastLED *fastLED = nullptr;

    RGBLine(int pin, int count, byte (&colors)[24], float &sound, byte id);

    void setFastLED(CFastLED *fastLED) { 
      this->fastLED = fastLED;
      setBrightness(bright); };

    void setMode(unsigned short mode);

    void setBrightness(byte bright);

    void setColors(byte *newColors);

    void setFrequency(byte frequency);

    void changeMode();

    void regGradient();

    void strobeHSV();

    void strobe();

    void moveEffect();

    void regHSV();

    void show();

    void regAA();

    void regAB();

    void regDA();

    void changeGradientAB();

    void changeGradientAC();

    void changeGradientAD();

    void blick();

    void data(){
    Serial.print(mode);
    Serial.print(" ");
    Serial.print(bright);
    Serial.print(" ");
    Serial.println(count);
    }
};