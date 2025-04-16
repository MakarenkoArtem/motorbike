#pragma once
#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))

#include <avr/io.h>
#include <Arduino.h>
#include <iarduino_RTC.h>

#include "config.h"
#include "RGBLine.h"

void initAudio();

void initSerial();

void initSwitchAudio(int amplifier, int btSource);

RGBLine *initLedLine(int pin, int count, Parameters &params, byte id);

//void initClock(iarduino_RTC &time);
//verified 1.02.25