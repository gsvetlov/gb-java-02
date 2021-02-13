package ru.svetlov.hw01.obstacle;

import ru.svetlov.hw01.competitor.Competitor;

public abstract class Obstacle {
    protected String obstacleDescription;
    protected int obstacleSize;

    public abstract boolean canPass(Competitor competitor);

    protected void printResult(Competitor competitor, boolean result) {
        String output = result ? "" : " не";
        System.out.printf("%s%s преодолел %s(%d)\n",
                competitor.getName(),
                output,
                obstacleDescription,
                obstacleSize);
    }
}

