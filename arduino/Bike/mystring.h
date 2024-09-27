#pragma once
#define _CRT_SECURE_NO_WARNINGS

#include<stdlib.h>
#include<stdio.h>
#include<math.h>

void freeListStr(char **list, int n);

void freeTwiceList(void **list, int n);

int strToInt(const char *str);

char **getListStrFromFile(FILE *file, int *k);

char *getStrFromFile(FILE *file);

char *intToStr(int i);

char **delStrInList(char **list, int index, int n);

char **delThisStr(char **list, char *delStr, int *n);

char *delSymbols(char *s, char *symbols);

char *stdStr(char *str, char *OneSpaceLeft, char *OneSpaceRight);

char *getStr();

char *delSomeChar(char *str, int index, int count);

char *delExtraChars(char *str, char c);

int lessOrEqualStr(char *a, char *b);

void sortWords(char **words, int count);

char *concatWords(char *str, char **words, int count);//join

char *copyStr(char *str);

char *lowerStr(char *str);

char *upperStr(char *str);

char *toStr(char s);

char *addStr(char *str, char *addStr);

char *createNewString(int count, char c);

char *delChar(char *str, int index);

char *subStr(char *str, int start, int end);

int lenStr(const char *str);

bool compareStr(const char* a, const char *b);

char *addChar(char *str, int index, char addChar);

char **split(char *str, char *s, int *n);

char *charInSyms(char s, char *syms);

char *replace(char *str, char *lastValue, const char *newValue, int count);

char *join(char **list, int count, char *joiner);

char *addStrOnIndex(char *str, char *addStr, int index);

long int strToLongInt(const char *str);

char *longIntToStr(long int i);

void *reallocList(void *str, int count, int sizeOfType);

void *mallocList(int count, int sizeOfType);

void freeStr(char *str);
//gcc -c main.c libraries/mystring.c
//gcc -o main main.o mystring.o -lm
//fseek(file, +-step, cur_
//ftell()