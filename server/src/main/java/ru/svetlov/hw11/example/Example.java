package ru.svetlov.hw11.example;

public class Example {
    public static final Object lock = new Object();
    public static char letter = 'A';
    public static int count = 5;

    public static void main(String[] args) {
        new Thread(()->print('C','A', true)).start();
        new Thread(()->print('A','B', false)).start();
        new Thread(()->print('B','C', false)).start();
    }

    private static void print(char currentLetter, char nextLetter, boolean isLast){
        synchronized (lock) {
            for (int i = 0; i < count; i++) {
                while (letter != currentLetter) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String out = isLast ? letter + " " : String.valueOf(letter);
                System.out.print(out);
                letter = nextLetter;
                lock.notifyAll();
            }
        }
    }
}
