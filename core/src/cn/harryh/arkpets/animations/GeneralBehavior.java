/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.animations.AnimClip.AnimStage;
import cn.harryh.arkpets.animations.AnimClip.AnimType;
import cn.harryh.arkpets.animations.StochasticMatrix.StochasticState;

import java.util.*;


public class GeneralBehavior extends Behavior {
    protected ArkConfig config;
    protected AnimStage stageCur;
    protected AnimClipGroup stageAnimList;
    protected Iterator<AnimStage> stageItr;
    protected final ArrayList<AnimStage> stageList;
    protected final HashMap<AnimStage, AnimClipGroup> stageAnimMap;
    protected final HashMap<AnimStage, StochasticMatrix> stageAnimWeightMap;

    public GeneralBehavior(ArkConfig config, AnimClipGroup animList) {
        super();

        this.config = config;
        stageAnimMap = animList.clusterByStage();
        stageAnimWeightMap = new HashMap<>();

        for (AnimStage key : stageAnimMap.keySet()) {
            stageAnimWeightMap.put(key, getMatrix(stageAnimMap.get(key)));
        }

        stageList = new ArrayList<>(stageAnimWeightMap.keySet().stream().toList());
        stageList.sort(Comparator.comparing(AnimStage::id));
        if (stageList.isEmpty())
            throw new NoSuchElementException("Animation stage map was empty because no animation's name was matched.");
        stageItr = stageList.iterator();

        nextStage();
    }

    private StochasticMatrix getMatrix(AnimClipGroup animClips) {
        StochasticMatrix mat = new StochasticMatrix(StochasticMatrix.DEFAULT_WEIGHTS);

        // Bind and disable states based on config
        AnimData sitAnim, sleepAnim, moveAnim, specialAnim;
        if (!(sitAnim = animClips.getLoopAnimData(AnimType.SIT)).isEmpty() && config.behavior_allow_sit) {
            mat.bind(StochasticState.SIT, sitAnim);
        } else {
            mat.disable(StochasticState.SIT);
        }
        if (!(sleepAnim = animClips.getLoopAnimData(AnimType.SLEEP)).isEmpty() && config.behavior_allow_sleep) {
            mat.bind(StochasticState.SLEEP, sleepAnim);
        } else {
            mat.disable(StochasticState.SLEEP);
        }
        if (!(moveAnim = animClips.getLoopAnimData(AnimType.MOVE)).isEmpty() && config.behavior_allow_walk) {
            mat.bind(StochasticState.MOVE_L, moveAnim.derive(-1));
            mat.bind(StochasticState.MOVE_R, moveAnim.derive(+1));
        } else {
            mat.disable(StochasticState.MOVE_L);
            mat.disable(StochasticState.MOVE_R);
        }
        if (!(specialAnim = animClips.getLoopAnimData(AnimType.SPECIAL)).isEmpty() && config.behavior_allow_special) {
            mat.bind(StochasticState.SPECIAL, specialAnim);
        } else {
            mat.disable(StochasticState.SPECIAL);
        }

        // Scale idle weights based on activation value
        float factor = 1 + (8 - Math.max(0, Math.min(16, config.behavior_ai_activation))) / 8f;
        mat.scale(StochasticState.IDLE, factor);

        return mat;
    }

    public void nextStage() {
        if (!stageItr.hasNext())
            stageItr = stageList.iterator();
        stageCur = stageItr.next();
        stageAnimList = stageAnimMap.get(stageCur);
        currentMatrix = stageAnimWeightMap.get(stageCur);
        currentState = StochasticState.IDLE;
        actionAutoGetter.removeCachedValue();
    }

    public Set<AnimStage> getStages() {
        return stageAnimMap.keySet();
    }

    public AnimStage getCurrentStage() {
        return stageCur;
    }

    @Override
    public AnimData defaultAnim() {
        return stageAnimList.getLoopAnimData(AnimType.IDLE);
    }

    @Override
    public AnimData walkAnim(int mobility) {
        return stageAnimList.getLoopAnimData(AnimType.MOVE).derive(mobility);
    }

    @Override
    public AnimData clickEnd() {
        AnimData a1 = stageAnimList.getStreamedAnimData(AnimType.ATTACK);
        AnimData a2 = stageAnimList.getStreamedAnimData(AnimType.INTERACT);
        AnimData a3 = a2.isEmpty() ? a1 : a2;
        return new AnimData(a3.animClip(), a3.animNext(), false, true, a3.mobility()).join(defaultAnim());
    }

    @Override
    public AnimData dropped() {
        return clickEnd();
    }
}
