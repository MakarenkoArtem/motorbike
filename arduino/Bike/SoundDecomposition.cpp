#include "SoundDecomposition.h"
#define LOG_OUT 1
#define FHT_N 128
#include<FHT.h>

void setADCPrescaler(uint8_t prescaler) {
    ADCSRA = (ADCSRA & 0b11111000) | prescaler;
}

SoundDecomposition::SoundDecomposition(int pinR, int pinL, void (*pinMode)(int, int),
                                       int (*analogRead)(int))
    : pinR(pinR), pinL(pinL), pinMode(pinMode), analogRead(analogRead) {
    pinMode(pinR, INPUT);
    pinMode(pinL, INPUT);
}

void SoundDecomposition::getSignals() {
    setADCPrescaler(ADC_PRESCALER_32); // Устанавливаем более высокую скорость дискретизации (меньший предделитель),
    for (int i = 0; i < FHT_N; i++) {
        fht_input[i] = analogRead(pinR) * 32; //(analogRead(pinR) - 512) * 64;
    }
    setADCPrescaler(ADC_PRESCALER_128); // Возвращаем предделитель на максимум (лучшая точность для других analogRead),
}

uint8_t* SoundDecomposition::fhtMethods() {
    getSignals();
    fht_window(); // window the data for better frequency response
    fht_reorder(); // reorder the data before doing the fht
    fht_run(); // process the data in the fht
    fht_mag_log(); // take the output of the fht
#if Sound_FHT_DEEP_DEBUG
        Serial.print("FHT:");
        for (byte i = 0;i<FHT_N/2;i++) {
            Serial.print(fht_log_out[i]);
            Serial.print(",");
        }
        Serial.println("");
#endif
    return fht_log_out;
}

int SoundDecomposition::maxFrequency() {
    uint8_t* input = fhtMethods();
    int maxVal = input[0];
    int index = 0;
    for (int i = 1; i < FHT_N; i++) {
        if (maxVal < input[i]) {
            maxVal = input[i];
            index = i;
        }
    }
    return index * 255 / (FHT_N - 1);
}

void SoundDecomposition::frequencyGrouping(uint8_t* input, uint8_t* output) {
    for (int level = 0; level != 5; ++level) {
        output[level] = 0;
        for (int i = groups[level][0]; i != groups[level][1]; ++i) {
            if (input[i] > output[level]) output[level] = input[i];
        }
        if (output[level] < 70) output[level] = 0;
    }
}

void applyCustomExponent(uint8_t* input, int size, float base) {
    for (int i = 0; i < size; i++) {
        input[i] = pow(base, input[i] / 10.0);
        input[i] = constrain(map(input[i], 1, pow(base, 10), 0, 255), 0, 255); // Ограничиваем диапазон
    }
}

void SoundDecomposition::getGroup(uint8_t* output) {
    uint8_t* input = fhtMethods();
    frequencyGrouping(input, output);
#if Sound_FHT_DEBUG
        Serial.print("FHT befor:");
        for (byte i = 0;i<5;i++) {
            Serial.print(output[i]);
            Serial.print(",");
        }
#endif
    applyCustomExponent(input, 5, 2.7);
#if Sound_FHT_DEBUG
        Serial.print(" FHT after:");
        for (byte i = 0;i<5;i++) {
            Serial.print(output[i]);
            Serial.print(",");
        }
        Serial.println("");
#endif
}

uint8_t* SoundDecomposition::analyzeAudio(uint8_t* output, int size) {
    uint8_t* input = fhtMethods();
    applyCustomExponent(input,FHT_N, 1.3);
    int startBin = 2; // Начинаем с 3-го бина, чтобы избежать шумов на 0 и 1
    int endBin = FHT_N / 2; // Максимальный индекс частоты (FHT_N/2 для реальной части спектра)

    // Логарифмическое разбиение частот
    float logStart = log10(startBin);
    float logEnd = log10(endBin);
    float logStep = (logEnd - logStart) / (size); // Шаг для каждого бина

    float base = 11.5;
    for (int i = 0; i < size; i++) {
        // Рассчитываем левую и правую границу для каждого бина
        int left = (int)pow(base, logStart + i * logStep);
        int right = (int)pow(base, logStart + (i + 1) * logStep);

        // Ограничиваем границы
        left = constrain(left, startBin, endBin);
        right = constrain(right, startBin, endBin);

        // Подсчитываем сумму амплитуд в этом диапазоне
        int sum = 0;
        for (int j = left; j < right; j++) {
            sum += input[j]; // Суммируем амплитуды в диапазоне
        }

        // Пропорционально разделяем сумму на количество частотных бинов
        output[i] = sum / (right - left);
        //if(output[i]<50){output[i]=0;}else{output[i]-=50;}
    }
#if Sound_FHT_DEBUG
    Serial.print("Output FHT:");
    for (byte i = 0; i < size; i++) {
        Serial.print(output[i]);
        Serial.print(",");
    }
    Serial.println("");
#endif
    return output;
}
