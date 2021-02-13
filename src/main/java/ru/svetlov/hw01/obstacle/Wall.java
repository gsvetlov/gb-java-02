package ru.svetlov.hw01.obstacle;

import ru.svetlov.hw01.competitor.Competitor;
import ru.svetlov.hw01.competitor.Jumper;

public class Wall extends Obstacle {
    public Wall(int height) {
        obstacleDescription = "Барьер";
        obstacleSize = height;
    }
@Override
    public boolean canPass(Competitor competitor){
        boolean result = false;
        if (competitor instanceof Jumper){
            result = ((Jumper) competitor).jump(obstacleSize) >= obstacleSize;
        }
        printResult(competitor, result);
        return result;
    }
}
