package com.peheje.hiddenMarkov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    List<Edge> nodeVertices = new ArrayList<>();            // Temporary for each node to determine its propability.
    List<Edge> path = new LinkedList<>();

    System.out.println("N Observations: " + N);

    // First step
    for (int r = 0; r < K; r++) {
      double ini = pi.get(r);
      double emi = O.get(r, xmap.get(x.charAt(0)));
      double res = Math.log(ini) + Math.log(emi); //double res = ini * emi;
      w.set(r, 0, res);
    }

    // Next steps
    Edge colMax;
    for (int c = 1; c < N; c++) {
      colMax = new Edge(-1, -Double.MAX_VALUE);
      for (int r = 0; r < K; r++) {
        nodeVertices = new ArrayList<>();
        for (int k = 0; k < K; k++) {
          double pre = w.get(k, c - 1);
          double tra = A.get(k, r);
          double emi = O.get(r, xmap.get(x.charAt(c)));
          double res = pre + Math.log(tra) + Math.log(emi); //double res = pre * tra * emi;
          nodeVertices.add(new Edge(k, res));
        }
        Edge nodeMax = Collections.max(nodeVertices);
        w.set(r, c, nodeMax.value);

        if (nodeMax.value > colMax.value)
          colMax = nodeMax;
      }
      path.add(0, colMax);
    }

    double last = -Double.MAX_VALUE;
    int lastIdx = -1;
    for (int i = 0; i < K; i++) {
      double v = w.get(i, w.numCols() - 1);
      if (v > last) {
        last = v;
        lastIdx = i;
      }
    }

    path.add(0, new Edge(lastIdx, last));

    w.print(1, 2);

    Collections.reverse(path);
    return path.stream().map(p -> p.cameFrom).collect(Collectors.toList());
  }

  class Edge implements Comparable {

    private int cameFrom;
    private double value;

    public Edge(int cameFrom, double value) {
      this.cameFrom = cameFrom;
      this.value = value;
    }

    @Override
    public int compareTo(Object o) {
      return Double.compare(value, ((Edge) o).value);
    }
  }
}
