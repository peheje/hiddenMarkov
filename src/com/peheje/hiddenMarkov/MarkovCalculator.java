package com.peheje.hiddenMarkov;

import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class MarkovCalculator {

    class Edge {
        private int fromRow;
        private int toRow;
        private double value;

        public Edge(int fromRow, int toRow, double value) {
            this.fromRow = fromRow;
            this.toRow = toRow;
            this.value = value;
        }
    }

    public double JointLogProbability(IMarkovModel m, String observedList, String states) {

        // Map character that represent 'hidden state' or 'observable state' to the index in our model.
        Map<Character, Integer> hiddenMap = m.getHiddenMap();
        Map<Character, Integer> observablesMap = m.getObservablesMap();

        Character preState;
        Character curState = states.charAt(0);
        Character observed = observedList.charAt(0);

        int preHiddenIdx;
        int hiddenIdx = hiddenMap.get(curState);
        int observedIdx = observablesMap.get(observed);

        double p = Math.log(m.getInitial().get(hiddenIdx)) +
                Math.log(m.getEmissions().get(hiddenIdx, observedIdx));
        for (int i = 1; i < observedList.length(); i++) {

            preState = states.charAt(i - 1);
            curState = states.charAt(i);
            observed = observedList.charAt(i);

            preHiddenIdx = hiddenMap.get(preState);
            hiddenIdx = hiddenMap.get(curState);
            observedIdx = observablesMap.get(observed);

            //p *= m.getTransitions().get(preHiddenIdx, hiddenIdx) * m.getEmissions().get(hiddenIdx, observedIdx);
            p += Math.log(m.getTransitions().get(preHiddenIdx, hiddenIdx)) +
                    Math.log(m.getEmissions().get(hiddenIdx, observedIdx));
        }

        return p;
    }

    // Returns list of most likely path given by rows.
    // E.g. [0, 3, 1] would mean column 0 row 0, column 1 row 3, column 2 row 1.
    public List<Integer> viterbi(IMarkovModel m, String observed) {
        int N = observed.length();              // N observations.
        int D = m.getObservables().size();      // D different symbols.
        int K = m.getHidden().size();           // K hidden states.

        String x = observed;
        List<String> z = m.getHidden();
        List<Double> pi = m.getInitial();       // K vector
        SimpleMatrix A = m.getTransitions();    // KxK matrix.
        SimpleMatrix O = m.getEmissions();      // KxD matrix.

        SimpleMatrix w = new SimpleMatrix(K, N);

        Map<Character, Integer> xmap = m.getObservablesMap();
        List<Integer> path = new ArrayList<>();
        List<Double> nodeVertices = new ArrayList<>();

        System.out.println("N Observations: " + N);

        // First step
        int firstMaxRowIdx = -1;
        double firstMaxVal = -Double.MAX_VALUE;
        for (int r = 0; r < K; r++) {
            double sta = pi.get(r);
            double emi = O.get(r, xmap.get(x.charAt(0)));
            double res = Math.log(sta) + Math.log(emi);
            //double res = sta * emi;
            w.set(r, 0, res);

            if (res > firstMaxVal) {
                firstMaxVal = res;
                firstMaxRowIdx = r;
            }
        }
        path.add(firstMaxRowIdx);

        // Next steps
        List<Edge> columnVertices = new ArrayList<>();
        for (int c = 1; c < N; c++) {
            columnVertices.clear();
            for (int r = 0; r < K; r++) {
                nodeVertices.clear();
                for (int k = 0; k < K; k++) {
                    double pre = w.get(k, c - 1);
                    double tra = A.get(k, r);
                    double emi = O.get(r, xmap.get(x.charAt(c)));
                    double res = pre + Math.log(tra) + Math.log(emi);
                    //double res = pre * tra * emi;
                    nodeVertices.add(res);
                    columnVertices.add(new Edge(k, r, res));
                }
                Double max = Collections.max(nodeVertices);
                w.set(r, c, max);
            }
            Edge max = columnVertices.stream().max(Comparator.comparingDouble(e -> e.value)).get();
            path.add(max.fromRow);
        }

        w.print();

        return path;
    }
}
