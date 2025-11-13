package cn.harryh.arkpets.animations;

import java.util.stream.IntStream;


/** The stochastic matrix (markov matrix) to manage the transition of auto-played animations in a natural way.
 */
public class StochasticMatrix {
    protected final StochasticMatrixRow[] weights;
    protected final boolean[] disabled;
    protected final AnimData[] binds;

    /** One stochastic state corresponding to an auto-played animation.
     */
    public enum StochasticState {
        IDLE,
        SIT,
        SLEEP,
        MOVE_L,
        MOVE_R,
        SPECIAL;

        public StochasticState next() {
            int ord = (ordinal() + 1) % StochasticState.values().length;
            return StochasticState.values()[ord];
        }

        public StochasticState prev() {
            int ord = (ordinal() - 1 + StochasticState.values().length) % StochasticState.values().length;
            return StochasticState.values()[ord];
        }
    }

    /** One row of the stochastic matrix. Each element represents the weight of transition to the corresponding state.
     * @param weights The weights array of the states.
     * @param disabledRef The reference to the disabled states array. If a state is disabled, its weight is ignored.
     */
    public record StochasticMatrixRow(int[] weights, boolean[] disabledRef) {
        public StochasticMatrixRow {
            if (weights.length != StochasticState.values().length)
                throw new IllegalArgumentException("Weights length mismatch");
        }

        public StochasticState random() {
            int sum = IntStream.range(0, weights.length).filter(i -> !disabledRef[i]).map(i -> weights[i]).sum();
            int rnd = (int) (Math.random() * sum);
            int acc = 0;
            for (int i = 0; i < weights.length; i++) {
                if (disabledRef[i])
                    continue;
                acc += weights[i];
                if (rnd < acc)
                    return StochasticState.values()[i];
            }
            return null;
        }
    }

    /** Initializes the stochastic matrix with given weights.
     * @param weights The weights 2D array. Each row corresponds to a state,
     *                and each column corresponds to the weight of transition to another state.
     */
    public StochasticMatrix(int[][] weights) {
        if (weights.length != StochasticState.values().length)
            throw new IllegalArgumentException("Weights length mismatch");
        this.weights = new StochasticMatrixRow[weights.length];
        this.disabled = new boolean[StochasticState.values().length];
        this.binds = new AnimData[StochasticState.values().length];
        for (int i = 0; i < weights.length; i++)
            this.weights[i] = new StochasticMatrixRow(weights[i], this.disabled);
    }

    public static StochasticMatrix buildMatrixLv3() {
        return new StochasticMatrix(new int[][] {
                //IDLE   SIT   SLEEP   MOVE_L   MOVE_R   SPECIAL  //NEXT

                { 400,   200,  0,      150,     150,     100 },   // IDLE
                { 300,   400,  100,    75,      75,      50  },   // SIT
                { 200,   200,  600,    0,       0,       0   },   // SLEEP
                { 400,   100,  100,    200,     200,     0   },   // MOVE_L
                { 400,   100,  100,    200,     200,     0   },   // MOVE_R
                { 600,   300,  50,     20,      20,      10  }    // SPECIAL
                                                                  // CURRENT
        });
    }

    public AnimData nextAnimOf(StochasticState state) {
        StochasticState newState = state;
        for (int i = 0; i < StochasticState.values().length; i++) {
            newState = newState.next();
            if (!disabled[newState.ordinal()])
                return binds[newState.ordinal()];
        }
        return binds[state.ordinal()];
    }

    public AnimData prevAnimOf(StochasticState state) {
        StochasticState newState = state;
        for (int i = 0; i < StochasticState.values().length; i++) {
            newState = newState.prev();
            if (!disabled[newState.ordinal()])
                return binds[newState.ordinal()];
        }
        return binds[state.ordinal()];
    }

    public AnimData transitedAnimOf(StochasticState state) {
        StochasticState newState = weights[state.ordinal()].random();
        return newState == null ? binds[state.ordinal()] : binds[newState.ordinal()];
    }

    public void bind(StochasticState state, AnimData anim) {
        binds[state.ordinal()] = anim;
    }

    public void disable(StochasticState state) {
        disabled[state.ordinal()] = true;
    }

    public boolean isAllDisabled() {
        for (boolean d : disabled)
            if (!d)
                return false;
        return true;
    }
}
