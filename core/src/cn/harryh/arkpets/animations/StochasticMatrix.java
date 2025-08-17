package cn.harryh.arkpets.animations;

import com.badlogic.gdx.utils.IntMap;

import java.util.Arrays;


public class StochasticMatrix {
    private final int[][] weight;
    private final IntMap<AnimData> anim = new IntMap<>();

    public StochasticMatrix(int[][] weight) {
        this.weight = weight;
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

    public int sum(int i) {
        return Arrays.stream(weight[i]).sum();
    }

    public int[] get(int i) {
        return weight[i];
    }

    public void removeRow(int i) {
        Arrays.fill(weight[i], 0);
    }

    public void removeCol(int i, boolean spare) {
        for (int j = 0; j < weight.length; j++) {
            int v = weight[j][i];
            weight[j][i] = 0;
            if (spare) {
                long count = Arrays.stream(weight[j]).filter(q -> q != 0).count();
                if (count == 0) continue;
                int div = Math.toIntExact(v / count);
                for (int k = 0; k < weight[j].length; k++) {
                    if (k == i) continue;
                    if (weight[j][k] == 0) continue;
                    weight[j][k] += div;
                }
            }
        }
    }

    public void bind(int i, AnimData anim) {
        this.anim.put(i, anim);
    }

    public int getLength() {
        return this.anim.size;
    }

    public AnimData getAnim(int idxRec) {
        return this.anim.get(idxRec);
    }
}
