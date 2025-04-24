#include "mystring.h"

#define min(a, b) (((a)<(b))?(a):(b))
#define max(a, b) (((a)>(b))?(a):(b))
#define OK 0
#define Error -1
#define IndexOutOfRange -2
#define MemoryError -3


void freeTwiceList(void **list, int n) {
    for (; n; freeStr(static_cast<char*>(list[--n])));
    free(list);
}

void freeListStr(char **list, int n) {
    freeTwiceList((void **) list, n);
}

void freeStr(char *str) {
    free(str);
}

void *reallocList(void *str, int count, int sizeOfType) {
    void *s = realloc(str, sizeOfType * count);
    return s;
}


void *mallocList(int count, int sizeOfType) {
    return malloc(sizeOfType * count);
}

static void swap(void **a, void **b) {
    void *tmp = *a;
    *a = *b;
    *b = tmp;
}

char *createNewString(int count, char c) {
    if (count < 0) { count = 0; }
    char *str = (char *) mallocList(++count, sizeof(char));
    str[--count] = '\0';
    for (; count; str[--count] = c);
    return str;
}

char *copyStr(char *str) {
    char *linkStr, *newStr = (char *) mallocList(lenStr(str) + 1, sizeof(char));
    linkStr = newStr;
    for (; *str; *(newStr++) = *(str++));
    *newStr = '\0';
    return linkStr;
}

char *addStr(char *str, char *addStr) {
    char *p = str;
    str += lenStr(str);
    for (; *addStr; *(str++) = *(addStr++));
    *str = '\0';
    return p;
}

char *addStrOnIndex(char *str, char *addStr, int index) {
    char *p = str;
    int lenstr = lenStr(str), lenadd = lenStr(addStr);
    for (int i = 0;
         i <= lenstr - index; str[lenstr + lenadd - (i++)] = str[lenstr - i]);
    for (str += index; *addStr; *(str++) = *(addStr++));
    return p;
}

char *lowerStr(char *str) {
    int i;
    char *p = str, up[93] = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ";
    char low[93] = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyz";
    for (; *str; ++str) {
        if (charInSyms(*str, up)) {
            for (i = 0; up[i] != *str && up[i]; ++i);
            if (up[i]) {
                *str = low[i];
            }
        }
    }
    return p;
}

char *upperStr(char *str) {
    int i;
    char *p = str, up[93] = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ";
    char low[93] = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyz";
    for (; *str; ++str) {
        if (charInSyms(*str, low)) {
            for (i = 0; low[i] != *str && low[i]; ++i);
            if (low[i]) {
                *str = up[i];
            }
        }
    }
    return p;
}

char *delChar(char *str, int index) {
    if (index < 0) {
        return (char *) Error;
    } else if (index >= lenStr(str)) {
        return (char *) IndexOutOfRange;
    }
    for (; str[index]; str[index++] = str[index + 1]);
    return str;
}

char *delSomeChar(char *str, int index, int count) {
    if (index < 0 || count < 0) {
        return (char *) Error;
    } else if (index >= lenStr(str)) {
        return (char *) IndexOutOfRange;
    } else if (!count) { return str; }
    count = min(lenStr(str) - index, count);
    for (; str[index]; str[index++] = str[index + count]);
    return str;
}

char *addChar(char *str, int index, char addChar) {
    if (index < 0) {
        return (char *) Error;
    } else if (index > lenStr(str)) {
        return (char *) IndexOutOfRange;
    }
    for (int i = lenStr(str) + 1; i != index; --i) {
        str[i] = str[i - 1];
    }
    str[index] = addChar;
    return str;
}

char *toStr(char s) {
    return createNewString(1, s);
}

int lenStr(char *str) {
    int i = -1;
    for (; str[++i];);
    return i;
}

char *subStr(char *str, int start, int end) {
    char *slice = (char *) mallocList(end - start + 1, sizeof(char));
    for (int i = start; i < end; ++i) {
        slice[i - start] = str[i];
    }
    slice[end - start] = '\0';
    return slice;
}

bool compareStr(const char *a, const char *b) {
    for (; *a && *a == *b; ++a) {
        ++b;
    }
    return !(*a || *b);
}

char **delStrInList(char **list, int index, int n) {
    for (--n; index != n; *(list + index++) = *(list + index + 1));
    return list;
}

char **delThisStr(char **list, char *delStr, int *n) {
    int c = *n;
    for (; c--;) {
        if (compareStr((list)[c], delStr)) {
            delStrInList(list, c, (*n)--);
        }
    }
    return list;
}

int strToInt(const char *str) {
    return static_cast<int>(strToLongInt(str));
}

char *intToStr(int i) {
    return longIntToStr((long int) i);
}

char **split(char *str, char *s, int *n) {
    int i = 0, c = 0, lenSplitStr = lenStr(s);
    *n = 0;
    char *help_, **list = (char **) mallocList(lenStr(str) / 2 + 1,
                                               sizeof(void *));
    do {
        help_ = subStr(str, i, i + lenSplitStr);
        if (compareStr(help_, s) || !str[i]) {
            //list[*n] = (char*)mallocList(c + 1, sizeof(char));
            list[(*n)++] = subStr(str, i - c, i);
            c = 0;
        } else { c++; }
        freeStr(help_);
    } while (str[i++]);
    return list;
}

char *charInSyms(char s, char *syms) {
    for (; *syms && *syms != s; ++syms);
    return *syms == s ? syms : 0;
}

char *replace(char *str, char *lastValue, char *newValue, int count) {
    int k, h, i = 0, lenLastValue = lenStr(lastValue), lenNewValue = lenStr(
            newValue);
    char *help_;
    do {
        help_ = subStr(str, i, i + lenLastValue);
        if (compareStr(help_, lastValue)) {
            --count;
            k = lenLastValue - lenNewValue;
            if (k >= 0) {
                for (int j = 0; j < lenNewValue; str[i + j] = newValue[j], j++);
                //str[i + j] = newValue[j];}
                delSomeChar(str, i + lenNewValue, k);
                //for (; k--; delChar(str, i + lenNewValue));
                k = 0;
            } else {
                addStrOnIndex(str, subStr(newValue, 0, -k), i);
                //for (h = -k; h--; addChar(str, i, newValue[h]));//check the buffer size for the line
                for (int j = -k;
                     j < lenLastValue - k; str[i + j] = newValue[j++]);
            }
            i -= lenLastValue - lenNewValue;
        }
        freeStr(help_);
    } while (str[++i] && count);
    return str;
}

char *join(char **list, int count, char *joiner) {
    int n = 0, sizeStr = lenStr(joiner) * (count - 1);
    for (int i = 0; i < count; sizeStr += lenStr(list[i++]));
    char *str = createNewString(sizeStr, '\0');
    for (int i = 0; i < count; i++) {
        addStr(str + n, list[i]);
        n += lenStr(list[i]);
        if (i + 1 < count) {
            addStr(str + n, joiner);
            n += lenStr(joiner);
        }
    }
    return str;
}

int lessOrEqualStr(char *a, char *b) {
    int i = 0;
    for (; a[i] && a[i] == b[i]; ++i);
    if (a[i] == 0 || b[i] == 0) { return a[i]; }
    return a[i] <= b[i];

}

void sortWords(char **words, int count) {
    for (int i = 0; i < count - 1; ++i) {
        for (int j = 1; j < count - i; ++j) {//(int j = i + 1; j < count; ++j) {
            if (!lessOrEqualStr(*(words + j - 1), *(words + j))) {
                swap((void **) words + j - 1, (void **) words + j);
            }
        }
    }
}

char *concatWords(char *str, char **words, int count) {
    return join(words, count, str);
}

char *delExtraChars(char *str, char c) {
    int count = 0;
    for (int i = 0; str[i]; ++i) {
        while (str[count++ + i] == c);
        if (--count) {
            delSomeChar(str, i,
                        count - 1 + (str[0] == c || str[count + i] == '\0'));
        }
        count = 0;
    }
    return str;
}

char *getStrFromFile(FILE *file) {
    int count = 0, size = 10;
    char *str = (char *) mallocList(size, sizeof(char));
    if (str == NULL) { return (char *) Error; }
    str[count] = fgetc(file);
    count += *str != '\n';
    while ((str[count] = fgetc(file)) != EOF && str[count] != '\n' &&
           str[count] != '\0' && str[count] != '\r') {
        if (++count == size) {
            size *= 2;
            str = (char *) reallocList(str, size, sizeof(char));
            if (str == NULL) { return (char *) Error; }
        }
    }
    str[count] = '\0';
    return str;
}

char *getStr() {
    return getStrFromFile(stdin);
}

char *stdStr(char *str, char *OneSpaceLeft, char *OneSpaceRight) {
    delExtraChars(str, ' ');
    int i = 0;
    while (str[i]) {
        if (str[i] == ' ' && charInSyms(str[i + 1], OneSpaceRight)) {
            delChar(str, i);
            //continue;
        } else if (charInSyms(str[i], OneSpaceLeft) && str[i + 1] == ' ') {
            delChar(str, i + 1);
            //continue;
        }
        i++;
    }
    return str;
}

char *delSymbols(char *str, char *symbols) {
    for (int i = 0; i < lenStr(symbols); ++i) {
        if (symbols[i] == ' ') { continue; }
        replace(str, createNewString(1, symbols[i]), " ", -1);
    }
    return str;
}

char **getListStrFromFile(FILE *file, int *k) {
    int c = 5;
    *k = 0;
    char **list = (char **) mallocList(c, sizeof(void *));
    while (!feof(file)) {
        if (*k == c) {
            c *= 2;
            list = (char **) reallocList(list, c, sizeof(void *));
        }
        list[(*k)++] = getStrFromFile(file);
    }
    return list;
}

long int strToLongInt(const char *str) {
    long int absolut = 0, sign = 1;
    if (*str == '-') {
        ++str;
        sign = -1;
    }
    while (*str) {
        absolut = absolut * 10 + (*str++ - '0');
    }
    return absolut * sign;
}

char *longIntToStr(long int i) {
    if (!i) { return createNewString(1, '0'); }
    int c = 0;
    if (i < 0) {
        c = 1;
        i *= -1;
    }
    char *str = createNewString(log10(i) + 1 + c, '-');
    str += (int) (log10(i) + 1 + c);
    while (i) {
        *(--str) = i % 10 + '0';
        i /= 10;
    }
    return str - c;
}