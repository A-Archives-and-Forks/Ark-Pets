/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;


import cn.harryh.arkpets.utils.Cached;


abstract public class Behavior {
    protected AnimClipGroup animList;
    protected AnimDataWeight[] actionList;
    protected Cached<AnimData> actionAutoGetter;
    protected int idxRec;

    /** Character Behavior Controller Instance.
     * @param animList The animation clip list.
     */
    public Behavior(AnimClipGroup animList) {
        actionList = null;
        this.animList = animList;
        actionAutoGetter = new Cached<>() {
            @Override
            protected AnimData produce() {
                idxRec = getRandomAction();
                return actionList[idxRec].anim();
            }
        };
    }

    /** Gets a random animation.
     * @return AnimData object.
     */
    public final AnimData autoCtrl() {
        AnimData value = actionAutoGetter.get();
        actionAutoGetter.setCacheAge(Math.max(0.5f, value.animClip().duration));
        return value;
    }

    /** Selects an action to play randomly.
     * @return The index of the action.
     */
    protected final int getRandomAction() {
        // Calculate the sum of all action's weight
        int weight_sum = 0;
        for (AnimDataWeight i : actionList) {
            weight_sum += i.weight();
        }
        // Random select a weight
        int weight_select = (int) Math.ceil(Math.random() * weight_sum);
        // Figure out which action the weight referred
        weight_sum = 0;
        for (int j = 0; j < actionList.length; j++) {
            weight_sum += actionList[j].weight();
            if (weight_select <= weight_sum)
                return j;
        }
        return -1;
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

    /** Selects the next animation.
     * @return AnimData object.
     */
    public AnimData nextAnim() {
        return new AnimData(null);
    }

    /** Selects the previous animation.
     * @return AnimData object.
     */
    public AnimData prevAnim() {
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
