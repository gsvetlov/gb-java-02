package ru.svetlov.hw01.competitor;

// для разнообразия сделаем робота, который не умеет прыгать,
// а то классы участников уж слишком одинаковые.
// прыгающий робот - в комментариях в коде ниже

public class Robot implements Runner {
    private final int runLength;
    private final String name;

    public Robot(String name, int runLength) {
        this.name = name;
        this.runLength = runLength;
    }

    @Override
    public int run(int distance) {
        int result = Math.min(distance, runLength);
        System.out.printf("Робот %s пробежал %d\n", name, result);
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void printInfo() {
        System.out.printf("%s; бег: %d\n", name, runLength);
    }
}
/* имплементация робота, который умеет прыгать

public class Robot implements Runner, Jumper{
    private final int runLength;
    private final int jumpHeight;
    private final String name;

    public Robot(String name, int runLength, int jumpHeight) {
        this.name = name;
        this.runLength = runLength;
        this.jumpHeight = jumpHeight;
    }

    @Override
    public int jump(int height) {
        int result = Math.min(height, jumpHeight);
        System.out.printf("Робот %s прыгнул на %d", name, result);
        return result;
    }

    @Override
    public int run(int distance) {
        int result = Math.min(distance, runLength);
        System.out.printf("Робот %s пробежал %d", name, result);
        return result;
    }

    @Override
    public String getName() {
        return name;
    }
}
*/
