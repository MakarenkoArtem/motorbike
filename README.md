# Bluetooth LED and Music Controller

A smart Arduino-based system to control LED lighting and music playback via Bluetooth. This project combines hardware and software for an engaging and interactive experience, perfect for DIY enthusiasts or anyone who wants a customized multimedia setup.

## Features

- **Bluetooth Control:**
    - Adjust LED patterns and colors using a mobile app.
    - Control music playback seamlessly.
- **Real-time Audio Visualization:**
    - LEDs synchronize with music for dynamic effects.
- **Customizable Settings:**
    - Modify LED patterns and audio settings directly from the app.

---

## How It Works

1. **Bluetooth Communication:**
    - The mobile app sends commands to the Arduino via Bluetooth.
    - The device processes these commands to change LED patterns or manage music playback.

2. **LED Synchronization:**
    - The device uses audio signals to create mesmerizing light patterns.

3. **Hardware Components:**
    - Arduino board.
    - Bluetooth module (e.g., HC-05 or HC-06).
    - LED strips (e.g., WS2812B).
    - Audio input module for sound analysis.

---

## Getting Started

### Prerequisites

- **Hardware Requirements:**
    - Arduino Uno/Nano/ESP32.
    - LED strips.
    - Bluetooth module.
    - Breadboard and jumper wires.

- **Software Requirements:**
    - [Arduino IDE](https://www.arduino.cc/en/software).
    - Required libraries:
        - `FastLED`
        - `FHT` (for audio processing)

### Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/MakarenkoArtem/motorbike.git
   ```
2. Open the project in Arduino IDE.
3. Install necessary libraries via Arduino Library Manager.
4. Upload the sketch to your Arduino board.

---

## Usage

1. **Setup the Hardware:**
    - Connect the components as per the provided diagram (see below).
2. **Pair with Bluetooth:**
    - Pair your smartphone with the device using the Bluetooth module.
3. **Control via Mobile App:**
    - Use the app to change LED patterns and control music playback.

---

## Circuit Diagram

*Insert an image or ASCII art representation of the hardware setup here.*

---

## Mobile App

- Download the companion app: [Download Link]
- Features:
    - Toggle between LED patterns.
    - Control audio playback.
    - Adjust settings like brightness and color.

---

## Example

*Add a video or GIF of the project in action to make it visually appealing.*

