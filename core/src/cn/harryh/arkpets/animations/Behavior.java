/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;


abstract public class Behavior {
    protected AnimDataWeight[] actionList;
    protected AnimClipGroup animList;
    protected float deltaMin;
    protected float timeRec;
    protected float duraRec;
    protected int idxRec;

    /** Character Behavior Controller Instance.
     * @param animList The animation clip list.
     */
    public Behavior(AnimClipGroup animList) {
        actionList = null;
        this.animList = animList;
        deltaMin = 0.5f;
        autoCtrlReset();
    }

    /** Gets a random animation.
     * @param deltaTime The delta time.
     * @return AnimData object.
     */
    public final AnimData autoCtrl(float deltaTime) {
        duraRec += deltaTime;
        timeRec += deltaTime;
        if (timeRec >= deltaMin) {
            timeRec = 0f;
            if (duraRec >= actionList[idxRec].duration()) {
                // Now try to change action
                duraRec = 0f;
                idxRec = getRandomAction();
                return actionList[idxRec].anim();
            }
        }
        return null;
    }

    /** Resets the random animation getter.
     */
    public final void autoCtrlReset() {
        timeRec = 0;
        duraRec = 0;
        idxRec = 0;
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
