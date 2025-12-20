/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.animations.StochasticMatrix.StochasticState;
import cn.harryh.arkpets.utils.Cached;


abstract public class Behavior {
    protected final Cached<AnimData> actionAutoGetter;
    protected StochasticMatrix currentMatrix;
    protected StochasticState currentState;

    private static final double minAnimCacheAge = 0.5;

    /** Character Behavior Controller Instance.
     */
    public Behavior() {
        actionAutoGetter = new Cached<>();
        actionAutoGetter.setValueProducer(() -> {
            StochasticState newState = currentMatrix.transitedAnimOf(currentState);
            if (newState == null) {
                return currentMatrix.getStateAnim(currentState);
            } else {
                currentState = newState;
                return currentMatrix.getStateAnim(newState);
            }
        });
        actionAutoGetter.setCacheAgeProducer(() -> {
            AnimData cache = actionAutoGetter.getCachedValue();
            return cache == null ? minAnimCacheAge : Math.max(minAnimCacheAge, cache.animClip().duration);
        });
        currentMatrix = null;
        currentState = null;
    }

    /** Checks whether the random animation is expired or empty.
     * @return True if expired or empty, false otherwise.
     */
    public final boolean isAutoAnimExpired() {
        return actionAutoGetter.isExpired();
    }

    /** Gets a random animation. This method has caching mechanism.
     * @return AnimData object.
     */
    public final AnimData autoAnim() {
        return actionAutoGetter.getValue();
    }

    /** Gets the next animation.
     * @return AnimData object.
     */
    public final AnimData nextAnim() {
        return currentMatrix.isAllDisabled() ? new AnimData(null) : currentMatrix.nextAnimOf(currentState);
    }

    /** Gets the previous animation.
     * @return AnimData object.
     */
    public final AnimData prevAnim() {
        return currentMatrix.isAllDisabled() ? new AnimData(null) : currentMatrix.prevAnimOf(currentState);
    }

    /** Gets the default animation.
     * @return AnimData object.
     */
    public AnimData defaultAnim() {
        return new AnimData(null);
    }

    /** Gets the walk animation.
     * @param mobility 1=GoRight, -1=GoLeft.
     * @return AnimData object.
     */
    public AnimData walkAnim(int mobility) {
        return new AnimData(null);
    }

    /** Gets the animation when mouse-down.
     * @return AnimData object.
     */
    public AnimData clickStart() {
        return new AnimData(null);
    }

    /** Gets the animation when mouse-up.
     * @return AnimData object.
     */
    public AnimData clickEnd() {
        return new AnimData(null);
    }

    /** Gets the animation when the user starts dragging.
     * @return AnimData object.
     */
    public AnimData dragging() {
        return new AnimData(null);
    }

    /** Gets the animation when character dropped.
     * @return AnimData object.
     */
    public AnimData dropped() {
        return new AnimData(null);
    }

    public int[][] getDebugMatrix() {
        return currentMatrix.getDebugMatrix();
    }

    public StochasticState getCurrentMatrixState() {
        return currentState;
    }
}
