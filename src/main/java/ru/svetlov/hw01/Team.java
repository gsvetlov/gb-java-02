package ru.svetlov.hw01;

import ru.svetlov.hw01.competitor.Competitor;

public class Team {
    private final String name;
    private final Competitor[] teamMembers;
    private final boolean[] results;

    public Team(String name, Competitor[] teamMembers) {
        this.name = name;
        this.teamMembers = teamMembers;
        results = new boolean[teamMembers.length];
    }

    public Competitor[] getMembers() {
        return teamMembers;
    }

    public void printResults() {
        System.out.println("Результаты:");
        for (int i = 0; i < teamMembers.length; i++) {
            String negation = results[i] ? "" : " не";
            System.out.printf("%s%s финишировал\n", teamMembers[i].getName(), negation);
        }
    }

    public void setResult(Competitor c, boolean isFinished) {
        for (int i = 0; i < teamMembers.length; i++) {
            if (c == teamMembers[i]) {
                results[i] = isFinished;
                break;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void printMembersInfo(){
        System.out.printf("Команда %s\n", name);
        for (Competitor c : teamMembers){
            c.printInfo();
        }
    }
}
