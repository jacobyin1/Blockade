package org.cis1200.blockade;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

public class BotNet {
    private Map<Integer, float[]> biases;
    private Map<Integer, float[][]> weights;
    public BotNet() {
        biases = new HashMap<>();
        weights = new HashMap<>();
        Path p = Paths.get("files/state_dict.h5");
        try (HdfFile hdfFile = new HdfFile(p)) {
            Set<String> data_labels = hdfFile.getChildren().keySet();

            for (String data_label : data_labels) {
                Pattern pattern = Pattern.compile("\\d");
                Matcher matcher = pattern.matcher(data_label);
                if (!matcher.find()) {
                    throw new RuntimeException();
                }
                int value = Integer.parseInt(matcher.group());

                Dataset dataset = hdfFile.getDatasetByPath(data_label);
                Object data = dataset.getData();
                if (data instanceof float[]) {
                    biases.put(value, (float[]) data);
                } else if (data instanceof float[][]) {
                    weights.put(value, (float[][]) data);
                }
            }
        }
    }

    public Direction getMove(int[][] state, Coordinate c1, Coordinate c2) {
        state[c1.y()][c1.x()] = 0;
        state[c2.y()][c2.x()] = 0;
        int len = state.length;
        int wid = state[0].length;

        int binLength = Integer.toBinaryString(wid * len - 1).length();
        int[] bin1 = getBin(c1.y() * wid + c1.x(), binLength);
        int[] bin2 = getBin(c1.y() * wid + c1.x(), binLength);

        INDArray stateIND = Nd4j.create(state);
        INDArray binIND1 = Nd4j.create(new int[][]{bin1});
        INDArray binIND2 = Nd4j.create(new int[][]{bin2});

        INDArray stateFinal = Nd4j.concat(0, Nd4j.toFlattened('c', stateIND),
                Nd4j.toFlattened('c', binIND1), Nd4j.toFlattened('c', binIND2));
        INDArray qvals = forward(stateFinal.castTo(DataType.FLOAT));

        INDArray qmin_o = qvals.mul(-1).max(1).mul(-1);
        int action = (int) qmin_o.argMax(0).getDouble(0);
        return switch (action) {
            case 0 -> Direction.UP;
            case 1 -> Direction.DOWN;
            case 2 -> Direction.LEFT;
            case 3 -> Direction.RIGHT;
            default -> throw new RuntimeException();
        };
    }

    private INDArray forward(INDArray state) {
        for (int i = 0; i < weights.size(); i++) {
            INDArray w = Nd4j.create(weights.get(i));
            INDArray b = Nd4j.create(biases.get(i));
            state = w.mmul(state);
            state = state.add(b);
            if (i != weights.size() - 1) {
                state = Transforms.relu(state);
            }
        }
        return state.reshape(4, 4);
    }

    private int[] getBin(int num, int numDigits) {
        int[] bin = new int[numDigits];
        String bString = Integer.toBinaryString(num);
        for (int i = numDigits - bString.length(); i < numDigits; i++) {
            bin[i] = bString.charAt(i - numDigits + bString.length()) - '0';
        }
        return bin;
    }

    public static void main(String[] args) {
        INDArray nd2 = Nd4j.create(new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        INDArray b = nd2.mul(2);
        INDArray c = b.argMax(0);
        int a = (int) c.getDouble(0);
        System.out.println(a);
    }
}
