#pragma once
#include <Arduino.h>
#define CONFIG true

#define LLine_pin 2
#define RLine_pin 3
#define TX_BLUETOOTH 4  //0
#define RX_BLUETOOTH 5  //1
#define AUDIO_OFF 6
#define CLK_CLOCK 11
#define DATA_CLOCK 12
#define RST_CLOCK 13
#define BIKE_OFF A2
#define SOUND_R A6
#define SOUND_L A7
#define NUM_LEDS 50

//BTSerial part
//      Messages
#define ERROR -1
#define OK 0
#define OFF 1
#define ON 2
#define END 3
#define BRIGHT 4
#define TYPE 5
#define COLORS 6
#define SOUND_OFF 7
#define SOUND_ON 8
#define BRIGHT 9
#define LINE_MODE 10
#define FREQUENCY 11
#define WAIT_INPUT 101

//      SETTINGS
#define TIMEOUT 200
#define SPEED 9600
#define DELAY 1000
#define MAXSZ 100


//RGBLine part
#define MIN_STROBE_PERIOD 150




#define YEAR  ((__DATE__[12] - '0') * 1000 + (__DATE__[13] - '0') * 100 + (__DATE__[14] - '0') * 10 + (__DATE__[15] - '0'))
#define MONTH ( \
    (__DATE__[0] == 'J' && __DATE__[1] == 'a') ? 1 : \
    (__DATE__[0] == 'F' ? 2 : \
    (__DATE__[0] == 'M' && __DATE__[2] == 'r') ? 3 : \
    (__DATE__[0] == 'A' && __DATE__[1] == 'p') ? 4 : \
    (__DATE__[0] == 'M' && __DATE__[2] == 'y') ? 5 : \
    (__DATE__[0] == 'J' && __DATE__[1] == 'u' && __DATE__[2] == 'n') ? 6 : \
    (__DATE__[0] == 'J' && __DATE__[1] == 'u' && __DATE__[2] == 'l') ? 7 : \
    (__DATE__[0] == 'A' && __DATE__[1] == 'u') ? 8 : \
    (__DATE__[0] == 'S' ? 9 : \
    (__DATE__[0] == 'O' ? 10 : \
    (__DATE__[0] == 'N' ? 11 : \
    (__DATE__[0] == 'D' ? 12 : 0))))))
#define DAY  (__DATE__[7] - '0') * 10 + (__DATE__[8] - '0')
#define HOURS (__TIME__[0] - '0') * 10 + (__TIME__[1] - '0')
#define MINUTES (__TIME__[3] - '0') * 10 + (__TIME__[4] - '0')
#define SECONDS (__TIME__[6] - '0') * 10 + (__TIME__[7] - '0')
// Макрос для вычисления дня недели (0 = Воскресенье, 6 = Суббота)
#define DAY_OF_WEEK ((DAY + (((13 * (MONTH + 1)) / 5) + YEAR + (YEAR / 4) - (YEAR / 100) + (YEAR / 400)) % 7 + 6) % 7)


extern byte colors[24];