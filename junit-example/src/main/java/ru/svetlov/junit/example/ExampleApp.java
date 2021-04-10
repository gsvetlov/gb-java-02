package ru.svetlov.junit.example;

import java.util.Arrays;

public class ExampleApp {

    public int[] takeLastAfterFour(int[] source) {
        int lastIndex = -1;
        for (int i = 0; i < source.length; i++)
            if (source[i] == 4)
                lastIndex = i;
        if (lastIndex == -1)
            throw new IllegalArgumentException("There's no 4's in array");
        return Arrays.copyOfRange(source, lastIndex + 1, source.length);
    }

    public boolean findOneOrFour(int[] source) {
        for (int v : source)
            if (v == 1 || v == 4)
                return true;
        return false;
    }
}
