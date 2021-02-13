package ru.svetlov.hw01.obstacle;

import ru.svetlov.hw01.competitor.Competitor;
import ru.svetlov.hw01.competitor.Runner;

public class Track extends Obstacle{
    public Track(int length) {
        obstacleDescription = "Дорожка";
        obstacleSize = length;
    }
    @Override
    public boolean canPass(Competitor competitor){
        boolean result = false;
        if (competitor instanceof Runner){
            result = ((Runner) competitor).run(obstacleSize) >= obstacleSize;
        }
        printResult(competitor, result);
        return result;
    }
}
