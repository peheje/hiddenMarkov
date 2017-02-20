package com.peheje.hiddenMarkov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ejml.simple.SimpleMatrix;

public class MarkovCalculator {

  public double jointLogProbability(IMarkovModel m, String observedList, String states) {

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
    List<Integer> path = new LinkedList<>();                  // Final path.
    List<Double> nodeVertices = new ArrayList<>();            // Temporary for each node to determine its propability.
    List<Edge> columnVertices;                                // Temporary vertices in a column.
    List<List<Edge>> matrixVertices = new ArrayList<>(N); // All vertices for matrix.

    System.out.println("N Observations: " + N);

    // First step
    for (int r = 0; r < K; r++) {
      double ini = pi.get(r);
      double emi = O.get(r, xmap.get(x.charAt(0)));
      double res = Math.log(ini) + Math.log(emi); //double res = ini * emi;
      w.set(r, 0, res);
    }

    // Next steps
    for (int c = 1; c < N; c++) {
      columnVertices = new ArrayList<>(K * K);
      for (int r = 0; r < K; r++) {
        nodeVertices.clear();
        for (int k = 0; k < K; k++) {
          double pre = w.get(k, c - 1);
          double tra = A.get(k, r);
          double emi = O.get(r, xmap.get(x.charAt(c)));
          double res = pre + Math.log(tra) + Math.log(emi); //double res = pre * tra * emi;
          nodeVertices.add(res);
          columnVertices.add(new Edge(k, r, res));
        }
        Double max = Collections.max(nodeVertices);
        w.set(r, c, max);
      }
      matrixVertices.add(columnVertices);
    }

    Edge maxLast = Collections.max(matrixVertices.get(matrixVertices.size() - 1));
    path.add(0, maxLast.toRow);

    for (int i = matrixVertices.size() - 1; i >= 0; i--) {
      List<Edge> edges = matrixVertices.get(i);
      Edge max = Collections.max(edges);
      path.add(0, max.toRow);
    }

    w.print(1, 2);

    return path;
  }

  class Edge implements Comparable {

    private int fromRow;
    private int toRow;
    private double value;

    public Edge(int fromRow, int toRow, double value) {
      this.fromRow = fromRow;
      this.toRow = toRow;
      this.value = value;
    }

    @Override
    public int compareTo(Object o) {
      return Double.compare(value, ((Edge) o).value);
    }
  }
}
