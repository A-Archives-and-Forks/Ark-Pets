/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.utils.Cached;


abstract public class Behavior {
    protected AnimClipGroup animList;
    protected StochasticMatrix animMatrix;
    protected Cached<AnimData> actionAutoGetter;
    private int idxRec;

    private static final double minAnimCacheAge = 0.5;

    /** Character Behavior Controller Instance.
     * @param animList The animation clip list.
     */
    public Behavior(AnimClipGroup animList) {
        animMatrix = null;
        this.animList = animList;
        actionAutoGetter = new Cached<>();
        actionAutoGetter.setValueProducer(this::markovSelect);
        actionAutoGetter.setCacheAgeProducer(() -> {
            AnimData cache = actionAutoGetter.getCachedValue();
            return cache == null ? minAnimCacheAge : Math.max(minAnimCacheAge, cache.animClip().duration);
        });
        idxRec = 0;
    }

    /** Checks whether the random animation is expired or empty.
     * @return True if expired or empty, false otherwise.
     */
    public final boolean isAutoAnimExpired() {
        return actionAutoGetter.isExpired();
    }

    /** Gets a random animation.
     * @return AnimData object.
     */
    public final AnimData autoAnim() {
        return actionAutoGetter.getValue();
    }

    /** Gets the next animation.
     * @return AnimData object.
     */
    public final AnimData nextAnim() {
        if (animMatrix.getLength() > 0) {
            idxRec = idxRec <= 0 ? animMatrix.getLength() - 1 : idxRec - 1;
            return animMatrix.getAnim(idxRec);
        }
        return new AnimData(null);
    }

    /** Gets the previous animation.
     * @return AnimData object.
     */
    public final AnimData prevAnim() {
        if (animMatrix.getLength() > 0) {
            idxRec = idxRec <= 0 ? animMatrix.getLength() - 1 : idxRec - 1;
            return animMatrix.getAnim(idxRec);
        }
        return new AnimData(null);
    }

    private AnimData markovSelect() {
        if (animMatrix.getLength() > 0) {
            // Calculate the sum of current action's weight
            int weightSum = animMatrix.sum(idxRec);
            // Random select a weight
            int weightSelect = (int) Math.ceil(Math.random() * weightSum);
            // Figure out which action is selected
            int weight = 0;
            int[] weightGroup = animMatrix.get(idxRec);
            for (int i = 0; i < weightGroup.length; i++) {
                weight += weightGroup[i];
                if (weight >= weightSelect) {
                    idxRec = i;
                    return animMatrix.getAnim(i);
                }
            }
        }
        return new AnimData(null);
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
}
