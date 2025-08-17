/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.animations.AnimClip.AnimStage;
import cn.harryh.arkpets.animations.AnimClip.AnimType;

import java.util.*;

import static cn.harryh.arkpets.Const.behaviorBaseWeight;


public class GeneralBehavior extends Behavior {
    protected ArkConfig config;
    protected AnimStage stageCur;
    protected AnimClipGroup stageAnimList;
    protected Iterator<AnimStage> stageItr;
    protected final ArrayList<AnimStage> stageList;
    protected final HashMap<AnimStage, AnimClipGroup> stageAnimMap;
    protected final HashMap<AnimStage, StochasticMatrix> stageAnimWeightMap;

    public GeneralBehavior(ArkConfig config, AnimClipGroup animList) {
        super(animList);

        this.config = config;
        stageAnimMap = this.animList.clusterByStage();
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
        StochasticMatrix mat = StochasticMatrix.buildMatrixLv3();
        AnimData anim;
        mat.bind(0, animClips.getLoopAnimData(AnimType.IDLE));
        anim = animClips.getLoopAnimData(AnimType.SIT);
        if (anim == null || !config.behavior_allow_sit) {
            mat.removeRow(1);
            mat.removeCol(1, true);
        } else {
            mat.bind(1, anim);
        }
        anim = animClips.getLoopAnimData(AnimType.MOVE);
        if (anim == null || !config.behavior_allow_walk) {
            mat.removeRow(3);
            mat.removeCol(3, true);
            mat.removeRow(4);
            mat.removeCol(4, true);
        } else {
            mat.bind(3, anim.derive(-1));
            mat.bind(4, anim.derive(+1));
        }
        anim = animClips.getLoopAnimData(AnimType.SLEEP);
        if (anim == null || !config.behavior_allow_sleep) {
            mat.removeRow(2);
            mat.removeCol(2, true);
        } else {
            mat.bind(2, anim);
        }
        anim = animClips.getStrictAnimData(AnimType.SPECIAL);
        if (anim == null || !config.behavior_allow_special) {
            mat.removeRow(5);
            mat.removeCol(5, true);
        } else {
            mat.bind(5, anim);
        }
        return mat;
    }

    public void nextStage() {
        if (!stageItr.hasNext())
            stageItr = stageList.iterator();
        stageCur = stageItr.next();
        stageAnimList = stageAnimMap.get(stageCur);
        animMatrix = stageAnimWeightMap.get(stageCur);
        actionAutoGetter.removeCachedValue();
    }

    public Set<AnimStage> getStages() {
        return stageAnimMap.keySet();
    }

    public AnimStage getCurrentStage() {
        return stageCur;
    }

    private AnimDataWeight[] getActionList(AnimClipGroup animList) {
        ArrayList<AnimDataWeight> actionList = new ArrayList<>(List.of(
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.IDLE),
                        Math.round(behaviorBaseWeight / (float) Math.sqrt(config.behavior_ai_activation))
                ),
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.SIT),
                        config.behavior_allow_sit ? 1 << 6 : 0
                ),
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.SLEEP),
                        config.behavior_allow_sleep ? 1 << 5 : 0
                ),
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.MOVE).derive(+1),
                        config.behavior_allow_walk ? 1 << 5 : 0
                ),
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.MOVE).derive(-1),
                        config.behavior_allow_walk ? 1 << 5 : 0),
                new AnimDataWeight(
                        animList.getStrictAnimData(AnimType.SPECIAL)
                                .join(animList.getLoopAnimData(AnimType.IDLE)),
                        config.behavior_allow_special ? 1 << 4 : 0
                )
        ));
        actionList.removeIf(e -> e.anim().isEmpty());
        return actionList.toArray(new AnimDataWeight[0]);
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
