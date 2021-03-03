package ru.svetlov.hw05;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class Hw05App {
    static final int size = 10_000_000;

    public static void main(String[] args) throws InterruptedException {
        processArraySync();
        processArrayParallel();
    }

    private static void processArraySync() {
        float[] arr = new float[size];
        Arrays.fill(arr, 1f);

        long swStart = System.currentTimeMillis();
        processArray(arr);
        long swRunningTime = System.currentTimeMillis() - swStart;

        System.out.println("Synchronous processing...");
        System.out.printf("Processing time: %d ms\n", swRunningTime);
    }

    private static void processArray(float[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (float) (arr[i]
                    * Math.sin(0.2f + i / 5f)
                    * Math.cos(0.2f + i / 5f)
                    * Math.cos(0.4f + i / 2f));
        }
    }

    private static void processArraySplit(float[] part, int originalIndex) {
        for (int i = 0; i < part.length; i++) {
            part[i] = (float) (part[i]
                    * Math.sin(0.2f + (i + originalIndex) / 5f)
                    * Math.cos(0.2f + (i + originalIndex) / 5f)
                    * Math.cos(0.4f + (i + originalIndex) / 2f));
        }
    }

    private static void processArrayParallel() throws InterruptedException {
        float[] arr = new float[size];
        Arrays.fill(arr, 1f);

        long swStart = System.currentTimeMillis();
        float[] partOne = Arrays.copyOfRange(arr, 0, arr.length / 2);
        float[] partTwo = Arrays.copyOfRange(arr, arr.length / 2, arr.length);
        long swSplit = System.currentTimeMillis();

        AtomicLong swPartOneProcessing = new AtomicLong();
        Thread t1 = new Thread(() -> {
            processArraySplit(partOne, 0);
            swPartOneProcessing.set(System.currentTimeMillis());
        });

        AtomicLong swPartTwoProcessing = new AtomicLong();
        Thread t2 = new Thread(() -> {
            processArraySplit(partTwo, arr.length / 2);
            swPartTwoProcessing.set(System.currentTimeMillis());
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        long swArrayJoinStart = System.currentTimeMillis();
        System.arraycopy(partOne, 0, arr, 0, partOne.length);
        System.arraycopy(partTwo, 0, arr, arr.length / 2, partTwo.length);
        long swArrayJoinEnd = System.currentTimeMillis();

        System.out.println("Parallel processing...");
        System.out.printf("Array split: %d ms\n", swSplit - swStart);
        System.out.printf("First thread processing: %d ms\n", swPartOneProcessing.get() - swSplit);
        System.out.printf("Second thread processing: %d ms\n", swPartTwoProcessing.get() - swSplit);
        System.out.printf("Total threads processing: %d ms\n", swArrayJoinStart - swSplit);
        System.out.printf("Array join: %d ms\n", swArrayJoinEnd - swArrayJoinStart);
        System.out.printf("Total processing: %d ms\n", swArrayJoinEnd - swStart);
    }
}
