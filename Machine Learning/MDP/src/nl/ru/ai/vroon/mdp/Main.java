package nl.ru.ai.vroon.mdp;

/**
 *
 * @author Piotr Leszmann, s4771826 Denis Karakoc, s4835093
 *
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {

        MarkovDecisionProblem mdp = new MarkovDecisionProblem();

        boolean deterministic = true;

        System.out.println("Value Iteration");
        mdp.valueIteration(10000, 5000, deterministic);

        System.out.println("Total Reward: " + mdp.getTotalReward());
        mdp.setTotalReward(0);

        System.out.println("Q-Learning");
        mdp.qLearning(5000, deterministic);
        System.out.println("Total Reward: " + mdp.getTotalReward());
    }
}
