#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))

#include <avr/io.h>
#include <Arduino.h>
#include "RGBLine.h"
#include <FastLED.h>
#include <iarduino_RTC.h>

#pragma once
#include "config.h"



void initAssembly();

void initAudio();

void initSerial();

void initSwitchAudio();

void initLedLine(RGBLine& line);

void initClock(iarduino_RTC& time);
