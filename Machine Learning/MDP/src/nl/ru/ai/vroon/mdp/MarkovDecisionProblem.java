package nl.ru.ai.vroon.mdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

/**
 * Basic class that contains and displays a Markov Decision Problem with grid
 * positions in a landscape as states. Most stuff can be set manually to create
 * the MDP desired.
 *
 * Also contains and updates an agent that can roam around in the MDP.
 *
 * @author Jered Vroon
 * 
 * @author adaptations and additions by Piotr Leszmann_s4771826,  Denis Karakoc_s4835093
 *
 */
public class MarkovDecisionProblem {
    /////////////////////////////////////////////////////////
    /// FIELDS
    /////////////////////////////////////////////////////////

    // The collection of grid positions that can be visited:
    private Field[][] landscape;
    private int width = 4,
            height = 3;

    // The current position of the agent
    private int xPosition = 0,
            yPosition = 0;

    // The positions of the agent in state 0:
    private int initXPos = 0,
            initYPos = 0;

    // Boolean determining if Actions are performed deterministically or not
    private boolean deterministic = false;

    // Random number generator for doing the Actions stochastically:
    private static Random rand = new Random();
    // ... and the probabilities for each (mis)interpretation of each Action:
    private double pPerform = 0.8, // probability of action being executed as planned 
            pSidestep = 0.2, // probability of a sidestep being executed
            pBackstep = 0, // probability of the inverse action being executed
            pNoStep = 0;		// probability of no action being executed 
    // These four probabilities should add up to 1

    // The rewards given for each state:
    private double posReward = 1, // reward for positive end state
            negReward = -1, // reward for negative end state
            noReward = -0.04;	// reward for the other states

    // Boolean maintaining if an end state has been reached:
    private boolean terminated = false;

    // The DrawFrame this MDP uses to draw itself
    private DrawFrame frame = null;
    // The time that is waited between drawing each action performed:
    private int waittime = 500;
    private boolean showProgress = true;

    // Counts the number of actions that has been performed
    private int actionsCounter = 0;

    /////////////////////////////////////////////////////////
    /// FUNCTIONS
    /////////////////////////////////////////////////////////
    /**
     * Constructor. Constructs a basic MDP (the one described in Chapter 17 of
     * Russell & Norvig)
     */
    public MarkovDecisionProblem() {
        defaultSettings();

        width = 4;
        height = 3;

        // Make and fill the fields:
        landscape = new Field[width][height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                landscape[j][i] = Field.EMPTY;
            }
        }
        setField(1, 1, Field.OBSTACLE);
        setField(3, 1, Field.NEGREWARD);
        setField(3, 2, Field.REWARD);

        // Draw yourself:
        pDrawMDP();
    }

    /**
     * Constructs a basic MDP with the given width and height. All fields are
     * set to Field.EMPTY. All other settings are the same as in the MDP
     * described in Chapter 17 of Russell & Norvig
     *
     * @param width
     * @param height
     */
    public MarkovDecisionProblem(int width, int height) {
        defaultSettings();

        this.width = width;
        this.height = height;

        // Make and fill the fields:
        landscape = new Field[this.width][this.height];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                landscape[j][i] = Field.EMPTY;
            }
        }
        pDrawMDP();
    }

    /**
     * Sets most parameters (except for the landscape, its width and height) to
     * their default value
     */
    public void defaultSettings() {
        xPosition = 0;
        yPosition = 0;

        initXPos = 0;
        initYPos = 0;

        deterministic = false;

        pPerform = 0.8;
        pSidestep = 0.2;
        pBackstep = 0;
        pNoStep = 0;

        posReward = 1;
        negReward = -1;
        noReward = -0.04;

        terminated = false;

        waittime = 1;
        showProgress = true;

        actionsCounter = 0;
    }

    /**
     * Performs the given action and returns the reward that action yielded.
     *
     * However, keep in mind that, if this MDP is non-deterministic, the given
     * action need not be executed - another action could be executed as well.
     *
     * @param action, the Action that is intended to be executed
     * @return the reward the agent gains at its new state
     */
    public double performAction(Action action) {
        // If we are working deterministic, the action is performed
        if (deterministic) {
            doAction(action);
        } else {
            double prob = rand.nextDouble();
            if (prob < pPerform) {
                doAction(action);
            } else if (prob < pPerform + pSidestep / 2) {
                doAction(Action.previousAction(action));
            } else if (prob < pPerform + pSidestep) {
                doAction(Action.nextAction(action));
            } else if (prob < pPerform + pSidestep + pBackstep) {
                doAction(Action.backAction(action));
            }
            // else: do nothing (i.e. stay where you are)
        }
        actionsCounter++;
        pDrawMDP();
        return getReward();
    }

    /**
     * Executes the given action as is (i.e. translates Action to an actual
     * function being performed)
     *
     * @param action
     */
    private void doAction(Action action) {
        switch (action) {
            case UP:
                moveUp();
                break;
            case DOWN:
                moveDown();
                break;
            case LEFT:
                moveLeft();
                break;
            case RIGHT:
                moveRight();
                break;
        }
    }

    /**
     * Moves the agent up (if possible).
     */
    private void moveUp() {
        if (yPosition < (height - 1) && landscape[xPosition][yPosition + 1] != Field.OBSTACLE) {
            yPosition++;
        }
    }

    /**
     * Moves the agent down (if possible).
     */
    private void moveDown() {
        if (yPosition > 0 && landscape[xPosition][yPosition - 1] != Field.OBSTACLE) {
            yPosition--;
        }
    }

    /**
     * Moves the agent left (if possible).
     */
    private void moveLeft() {
        if (xPosition > 0 && landscape[xPosition - 1][yPosition] != Field.OBSTACLE) {
            xPosition--;
        }
    }

    /**
     * Moves the agent right (if possible).
     */
    private void moveRight() {
        if (xPosition < (width - 1) && landscape[xPosition + 1][yPosition] != Field.OBSTACLE) {
            xPosition++;
        }
    }

    /**
     * sets the agent back to its default state and sets terminated to false.
     */
    public void restart() {
        terminated = false;
        xPosition = initXPos;
        yPosition = initYPos;
        actionsCounter = 0;
        pDrawMDP();
    }

    /**
     * Returns the reward the field in which the agent currently is yields
     *
     * @return a double (can be negative)
     */
    public double getReward() {
        // If we are terminated, no rewards can be gained anymore (i.e. every action is futile):
        if (terminated) {
            return 0;
        }

        switch (landscape[xPosition][yPosition]) {
            case EMPTY:
                return noReward;
            case REWARD:
                terminated = true;
                return posReward;
            case NEGREWARD:
                terminated = true;
                return negReward;
        }

        // If something went wrong:
        System.err.println("ERROR: MDP: getReward(): agent is not in an empty, reward or negreward field...");
        return 0;
    }

    /**
     * This works the same way as getReward() without setting terminated to true
     * @return reward in the current state
     */
    public double getReward4Display() {
        switch (landscape[xPosition][yPosition]) {
            case EMPTY:
                return noReward;
            case REWARD:
                return posReward;
            case NEGREWARD:
                return negReward;
        }
        System.err.println("ERROR: MDP: getReward(): agent is not in an empty, reward or negreward field...");
        return 0;
    }

    public double getTotalReward() {
        return totalReward;
    }
    
    public void setTotalReward(int tr) {
        this.totalReward = tr;
    }

    /////////////////////////////////////////////////////////
    /// SETTERS
    /////////////////////////////////////////////////////////
    /**
     * Sets the field with the given x and y coordinate to the given field.
     * Updates the visual display.
     *
     * @param xpos
     * @param ypos
     * @param field
     */
    public void setField(int xpos, int ypos, Field field) {
        if (xpos >= 0 && xpos < width && ypos >= 0 && ypos < height) {
            landscape[xpos][ypos] = field;
        }
        pDrawMDP();
    }

    /**
     * Moves the agent to the given state (x and y coordinate)
     *
     * @param xpos
     * @param ypos
     */
    public void setState(int xpos, int ypos) {
        xPosition = xpos;
        yPosition = ypos;
        pDrawMDP();
    }

    /**
     * sets the default state for the agent (used in restart() )
     *
     * @param xpos
     * @param ypos
     */
    public void setInitialState(int xpos, int ypos) {
        initXPos = xpos;
        initYPos = ypos;
    }

    /**
     * makes this MDP deterministic (i.e. actions have certain outcomes)
     */
    public void setDeterministic() {
        deterministic = true;
    }

    /**
     * makes this MDP stochastic (i.e. actions do not have certain outcomes)
     */
    public void setStochastic() {
        deterministic = false;
    }

    /**
     * Setter to set the probabilities for all (mis)interpretations of a
     * to-be-performed action. The given probabilities should add up to 1.
     *
     * @param pPerform, the probability an action is performed as is (e.g. UP is
     * executed as UP)
     * @param pSidestep, the probability a sidestep is performed (e.g. UP is
     * executed as LEFT or RIGHT)
     * @param pBackstep, the probability a backstep is performed (e.g. UP is
     * executed as DOWN)
     * @param pNoStep, the probability an action is not performed at all (e.g.
     * UP is not executed)
     */
    public void setProbsStep(double pPerform, double pSidestep, double pBackstep, double pNoStep) {
        double total = pPerform + pSidestep + pBackstep + pNoStep;
        if (total == 1.0) {
            System.err.println("ERROR: MDP: setProbsStep: the given probabilities do not add up to 1. I will normalize to compensate.");
        }
        this.pPerform = pPerform / total;
        this.pSidestep = pSidestep / total;
        this.pBackstep = pBackstep / total;
        this.pNoStep = pNoStep / total;
    }

    /**
     * Setter to set the reward given when a Field.REWARD is reached
     *
     * @param posReward
     */
    public void setPosReward(double posReward) {
        this.posReward = posReward;
    }

    /**
     * Setter to set the reward given when a Field.NEGREWARD is reached
     *
     * @param posReward
     */
    public void setNegReward(double negReward) {
        this.negReward = negReward;
    }

    /**
     * Setter to set the reward given when a Field.EMPTY is reached
     *
     * @param posReward
     */
    public void setNoReward(double noReward) {
        this.noReward = noReward;
    }

    /////////////////////////////////////////////////////////
    /// GETTERS
    /////////////////////////////////////////////////////////
    /**
     * Returns the x-position of the current state
     *
     * @return a number between 0 and width
     */
    public int getStateXPosition() {
        return xPosition;
        //return height - 1 - yPosition;
    }

    /** 
     * this returns the X index in conventional java array logic, i.e. left to right, top to bottom
     * @return index X
     */
    public int getNaturalXIndex() {
        return height - 1 - getStateYPosition();
    }

    /**
     * this returns the Y index in conventional java array logic, i.e. left to right in a 2D array
     * @return index Y
     */
    public int getNaturalYIndex() {
        return getStateXPosition();
    }

    /**
     * Returns the y-position of the current state
     *
     * @return a number between 1 and height
     */
    public int getStateYPosition() {
        //return width - 1 - xPosition;
        return yPosition;
    }

    /**
     * Returns if the MDP has been terminated (i.e. a final state has been
     * reached)
     *
     * @return
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * Returns the width of the landscape
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the landscape
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns if this MDP is deterministic
     *
     * @return
     */
    public boolean isDeterministic() {
        return deterministic;
    }

    /**
     * Returns the number of actions that has been performed since the last
     * (re)start.
     *
     * @return
     */
    public int getActionsCounter() {
        return actionsCounter;
    }

    /**
     * Returns the field with the given x and y coordinates
     *
     * @param xpos, should fall within the landscape
     * @param ypos, should fall within the landscape
     * @return
     */
    public Field getField(int xpos, int ypos) {
        if (xpos >= 0 && xpos < width && ypos >= 0 && ypos < height) {
            return landscape[xpos][ypos];
        } else {
            System.err.println("ERROR:MDP:getField:you request a field that does not exist!");
            return Field.OUTOFBOUNDS;
        }
    }

    /////////////////////////////////////////////////////////
    /// DISPLAY STUFF
    /////////////////////////////////////////////////////////
    /**
     * Private method used to have this MDP draw itself only if it should show
     * its progress.
     */
    private void pDrawMDP() {
        if (showProgress) {
            drawMDP();
        }
    }

    /**
     * Draws this MDP. If showProgress is set to true called by MDP every time
     * something changes. In that case also waits the waittime.
     */
    public void drawMDP() {
        // (1) sleep
        if (showProgress) {
            Thread.currentThread();
            try {
                Thread.sleep(waittime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // (2) repaint
        if (frame == null) {
            frame = new DrawFrame(this);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } else {
            frame.drawContent();
            frame.repaint();
        }
    }

    /**
     * Setter to set the speed with which the display is updated at maximum
     *
     * @param waittime in ms
     */
    public void setWaittime(int waittime) {
        if (waittime >= 0) {
            this.waittime = waittime;
        } else {
            System.err.println("ERROR:MDP:setWaittime: no negative waittime alowed.");
        }
    }

    /**
     * Setter to enable/disable the showing of the progress on the display
     *
     * @param show
     */
    public void setShowProgress(boolean show) {
        showProgress = show;
    }

    /////             \\\\\\
    /////  Q-Learning   \\\\\\
    /////                 \\\\\\
    // probability the agent will exploit instead of explore 
    private final double exploit = 0.8; 
    private final double learningRate = 0.7;
    private final double discount = 0.7;
    private double totalReward = 0;

    /**
     * constructs a 2D HashMap of the same dimensions as the field, with each possible action in each state mapped to a value of 0.
     * @return 2D HashMap mapping the 4 actions to 0.
     */
    public ArrayList<ArrayList<HashMap<Action, Double>>> intitializeValueTable() {
        ArrayList<ArrayList<HashMap<Action, Double>>> table = new ArrayList<ArrayList<HashMap<Action, Double>>>();
        for (int i = 0; i < height; i++) {
            table.add(initializeRows());
        }
        return table;
    }

    /**
     * initializes the rows of the valueTabe
     * @return HashMap ArrayList mapping actions to 0.
     */
    private ArrayList<HashMap<Action, Double>> initializeRows() {
        ArrayList<HashMap<Action, Double>> row = new ArrayList<HashMap<Action, Double>>();
        for (int j = 0; j < width; j++) {
            HashMap<Action, Double> m = new HashMap<Action, Double>();
            m.put(Action.UP, 0.0);
            m.put(Action.DOWN, 0.0);
            m.put(Action.RIGHT, 0.0);
            m.put(Action.LEFT, 0.0);
            row.add(m);
        }
        return row;
    }

    /**
     * picks best action in the current state with probability exploit, picks a random action with probability 1-exploit
     * @param valueTable
     * @return best or random action
     */
    private Action pickAction(ArrayList<ArrayList<HashMap<Action, Double>>> valueTable) {

        if (exploreOrExploit()) {
            return pickBestAction(valueTable);
        } else {
            return pickRandomAction();
        }
    }

    /**
     *
     * @return true if explore, false if exploit
     */
    private boolean exploreOrExploit() {

        double prob = rand.nextDouble();
        return prob < exploit;

    }

    /**
     * picks the best action at the agent's current position
     * @param valueTable
     * @return highest valued action at current state
     */
    private Action pickBestAction(ArrayList<ArrayList<HashMap<Action, Double>>> valueTable) {
        int x = getNaturalXIndex();
        int y = getNaturalYIndex();

        Action bestAction;

        if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.DOWN)) {
            if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.LEFT)) {
                if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.RIGHT)) {
                    return pickRandomAction();
                }
            }
        }

        bestAction = (valueTable.get(x).get(y).get(Action.UP) > valueTable.get(x).get(y).get(Action.DOWN)) ? Action.UP : Action.DOWN;
        bestAction = (valueTable.get(x).get(y).get(bestAction) > valueTable.get(x).get(y).get(Action.LEFT)) ? bestAction : Action.LEFT;
        bestAction = (valueTable.get(x).get(y).get(bestAction) > valueTable.get(x).get(y).get(Action.RIGHT)) ? bestAction : Action.RIGHT;

        return bestAction;

    }

    /**
     * picks a random action
     * @return one of 4 possible actions
     */
    private Action pickRandomAction() {

        Action[] actions = new Action[]{Action.UP, Action.DOWN, Action.LEFT, Action.RIGHT};
        int index = rand.nextInt(4);
        return actions[index];

    }

    /**
     * The essential function in q-learning where the value of an action in a state is updated by its experience
     * @param a action that is performed and later updated
     * @param valueTable 
     * @return the reward of having done the action, used elsewhere to compute the total reward
     */
    private double updateQ(Action a, ArrayList<ArrayList<HashMap<Action, Double>>> valueTable) {
        int x1 = getNaturalXIndex();
        int y1 = getNaturalYIndex();
        double r = performAction(a);
        int x2 = getNaturalXIndex();
        int y2 = getNaturalYIndex();

        double newValue = (1 - learningRate) * valueTable.get(x1).get(y1).get(a) + learningRate * (r + discount * maxAction(x2, y2, valueTable));
        valueTable.get(x1).get(y1).put(a, newValue);

        if (terminated) {
            restart();
        }
        return r;
    }

    /**
     * picks the value of the highest valued action in any state
     * @param x natural X index of the state
     * @param y natural Y index of the state
     * @param valueTable
     * @return value of best action in state [X][Y]
     */
    private double maxAction(int x, int y, ArrayList<ArrayList<HashMap<Action, Double>>> valueTable) {
        Action bestAction;

        if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.DOWN)) {
            if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.LEFT)) {
                if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.RIGHT)) {
                    return valueTable.get(x).get(y).get(Action.UP);
                }
            }
        }

        bestAction = (valueTable.get(x).get(y).get(Action.UP) > valueTable.get(x).get(y).get(Action.DOWN)) ? Action.UP : Action.DOWN;
        bestAction = (valueTable.get(x).get(y).get(bestAction) > valueTable.get(x).get(y).get(Action.LEFT)) ? bestAction : Action.LEFT;
        bestAction = (valueTable.get(x).get(y).get(bestAction) > valueTable.get(x).get(y).get(Action.RIGHT)) ? bestAction : Action.RIGHT;

        return valueTable.get(x).get(y).get(bestAction);
    }

    /**
     * the implementation of the q-learning algorithm
     * @param nrOfIterations how many steps the agent takes
     * @param d boolean value to set if deterministic or stochastic
     */
    public void qLearning(int nrOfIterations, boolean d) {

        deterministic = d;

        ArrayList<ArrayList<HashMap<Action, Double>>> valueTable = intitializeValueTable();
        for (int i = 0; i < nrOfIterations; i++) {
            Action a = pickAction(valueTable);
            totalReward += updateQ(a, valueTable);
        }
        printPolicies(valueTable);
    }

    //////                \\\\\\
    ////// Value Iteration  \\\\\\
    //////                    \\\\\\
    
    /**
     * the essential function in the value iteration algorithm to determine the value of each action in each state
     * @param nrOfIterations how often to iterate throughout the entire world. also known as K in the lecture slides
     * @return the learned valueTable with values for each action in each state
     */
    public ArrayList<ArrayList<HashMap<Action, Double>>> iterations(int nrOfIterations) {

        ArrayList<ArrayList<HashMap<Action, Double>>> valueTable = intitializeValueTable();
        int iterations = nrOfIterations;
        Action[] actions = new Action[]{Action.UP, Action.DOWN, Action.RIGHT, Action.LEFT};
        for (int k = 0; k <= iterations; k++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    for (Action a : actions) {

                        setState(i, j);

                        double r = performAction(a);
                        terminated = false;
                        setState(i, j);

                        doAction(a);
                        double v1 = computeV(valueTable, a, i, j);
                        doAction(Action.previousAction(a));

                        double v2 = computeV(valueTable, a, i, j);

                        doAction(Action.backAction(a));
                        double v3 = computeV(valueTable, a, i, j);

                        doAction(Action.backAction(a));
                        double v4 = computeV(valueTable, a, i, j);

                        int x = getNaturalXIndex();
                        int y = getNaturalYIndex();

                        double newValue = r + discount * (pPerform * v1 + (pSidestep / 2) * v2 + (pSidestep / 2) * v3 + pBackstep * v4);
                        valueTable.get(x).get(y).put(a, newValue);

                    }
                }
            }
        }
        return valueTable;
    }

    /**
     * computes the value of the best action in a state after having done action a in the value iteration algorithm
     * @param valueTable
     * @param a the action that was performed
     * @param i the agent's current X index in the Cartesian coordinate system 
     * @param j the agent's current Y index in the Cartesian coordinate system 
     * @return the value of the highest valued action after having done action a
     */
    private double computeV(ArrayList<ArrayList<HashMap<Action, Double>>> valueTable, Action a, int i, int j) {
        int x = getNaturalXIndex();
        int y = getNaturalYIndex();
        double v1 = Math.max(valueTable.get(x).get(y).get(a), Math.max(valueTable.get(x).get(y).get(Action.nextAction(a)), Math.max(valueTable.get(x).get(y).get(Action.previousAction(a)), valueTable.get(x).get(y).get(Action.backAction(a)))));
        setState(i, j);
        return v1;
    }

    /**
     * extracts the optimal policy from the value table, meaning the highest valued action in each state
     * @param valueTable
     * @return 2D array corresponding to the dimensions of the world, with the optimal action at each state
     */
    private Action[][] policies(ArrayList<ArrayList<HashMap<Action, Double>>> valueTable) {
        Action[][] policies = new Action[height][width];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                policies[i][j] = highestAction(i, j, valueTable);
            }
        }
        return policies;
    }

    /**
     * returns the highest valued action in a state [X][Y]
     * @param x X index in common java array logic
     * @param y Y index in common java array logic
     * @param valueTable
     * @return the highest valued action
     */
    private Action highestAction(int x, int y, ArrayList<ArrayList<HashMap<Action, Double>>> valueTable) {

        Action bestAction;

        if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.DOWN)) {
            if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.LEFT)) {
                if (valueTable.get(x).get(y).get(Action.UP) == valueTable.get(x).get(y).get(Action.RIGHT)) {
                    return pickRandomAction();
                }
            }
        }

        bestAction = (valueTable.get(x).get(y).get(Action.UP) > valueTable.get(x).get(y).get(Action.DOWN)) ? Action.UP : Action.DOWN;
        bestAction = (valueTable.get(x).get(y).get(bestAction) > valueTable.get(x).get(y).get(Action.LEFT)) ? bestAction : Action.LEFT;
        bestAction = (valueTable.get(x).get(y).get(bestAction) > valueTable.get(x).get(y).get(Action.RIGHT)) ? bestAction : Action.RIGHT;

        return bestAction;

    }

    /**
     * the implementation of the value iteration algorithm
     * @param train how often to iterate throughout the world for approximating an optimal policy
     * @param test how many steps to make the agent take after training
     * @param d boolean setting deterministic or stochastic
     */
    public void valueIteration(int train, int test, boolean d) {

        deterministic = d;
        setShowProgress(false);

        ArrayList<ArrayList<HashMap<Action, Double>>> values = iterations(train);
        Action[][] policies = policies(values);
        printPolicies(values);
        setShowProgress(true);

        setState(0, 0);
        for (int i = 0; i < test; i++) {
            if (terminated) {
                restart();
            }
            int x = getNaturalXIndex();
            int y = getNaturalYIndex();
            totalReward += performAction(policies[x][y]);
        }
    }

    /**
     * prints the agent's learned policy
     * @param valueTable 
     */
    public void printPolicies(ArrayList<ArrayList<HashMap<Action, Double>>> valueTable) {

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //System.out.println("Values at Index " + i + "," + j + " " + valueTable.get(i).get(j).entrySet());
                System.out.println("Best Action at Index " + i + "," + j + ": " + highestAction(i, j, valueTable));
            }
        }

    }

}
