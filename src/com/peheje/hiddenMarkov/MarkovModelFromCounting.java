package com.peheje.hiddenMarkov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ejml.simple.SimpleMatrix;

public class MarkovModelFromCounting implements IMarkovModel {

  private final Character[] observableOrder;
  private final Character[] stateOrder;
  private IObservations observations;


  public MarkovModelFromCounting(IObservations observations, Character[] stateOrder,
      Character[] observableOrder) {
    this.observations = observations;
    this.stateOrder = stateOrder;
    this.observableOrder = observableOrder;
  }

  @Override
  public List<String> getHidden() {
    return null;
  }

  @Override
  public Map<Character, Integer> getHiddenMap() {
    return null;
  }

  @Override
  public List<String> getObservables() {
    return null;
  }

  @Override
  public Map<Character, Integer> getObservablesMap() {
    return null;
  }

  @Override
  public List<Double> getInitial() {
    return null;
  }

  @Override
  public SimpleMatrix getTransitions() {

    // Map from character -> index, count
    Map<Character, IdxCount> stateMap = new HashMap<>();
    for (int i = 0; i < stateOrder.length; i++) {
      stateMap.put(stateOrder[i], new IdxCount(i, 0));
    }

    final int K = stateMap.size();
    SimpleMatrix m = new SimpleMatrix(K, K);

    // Count
    for (String z : observations.getStates()) {
      for (int i = 0; i < z.length() - 1; i++) {
        char cur = z.charAt(i);
        char nex = z.charAt(i + 1);
        IdxCount curIc = stateMap.get(cur);
        IdxCount nextIc = stateMap.get(nex);
        m.set(nextIc.idx, curIc.idx, m.get(nextIc.idx, curIc.idx) + 1);
        stateMap.get(cur).count++;
      }
    }

    // Divide
    for (int i = 0; i < m.numCols(); i++) {
      for (int j = 0; j < m.numRows(); j++) {
        int count = stateMap.get(stateOrder[j]).count;
        m.set(j, i, m.get(j, i) / (double) count);
      }
    }

    m.print();
    return m;
  }

  @Override
  public SimpleMatrix getEmissions() {

    // Map from character -> index, count
    Map<Character, IdxCount> observableMap = new HashMap<>();
    for (int i = 0; i < observableOrder.length; i++) {
      observableMap.put(observableOrder[i], new IdxCount(i, 0));
    }

    Map<Character, IdxCount> stateMap = new HashMap<>();
    for (int i = 0; i < stateOrder.length; i++) {
      stateMap.put(stateOrder[i], new IdxCount(i, 0));
    }

    final int K = stateOrder.length;
    final int D = observableOrder.length;

    SimpleMatrix m = new SimpleMatrix(K, D);

    // Count
    List<String> sequences = observations.getSequences();
    List<String> states = observations.getStates();

    for (int i = 0; i < sequences.size(); i++) {
      String seq = sequences.get(i);
      String sta = states.get(i);

      for (int j = 0; j < seq.length(); j++) {
        char x = seq.charAt(j);
        char z = sta.charAt(j);

        IdxCount xIc = observableMap.get(x);
        IdxCount zIc = stateMap.get(z);

        m.set(zIc.idx, xIc.idx, m.get(zIc.idx, xIc.idx) + 1);
        zIc.count++;
      }
    }

    // Divide
    for (int i = 0; i < m.numCols(); i++) {
      for (int j = 0; j < m.numRows(); j++) {
        int count = stateMap.get(stateOrder[j]).count;
        m.set(j, i, m.get(j, i) / (double) count);
      }
    }

    m.print();
    return m;
  }

  private class IdxCount {

    int idx;
    int count;

    public IdxCount(int idx, int count) {
      this.idx = idx;
      this.count = count;
    }
  }

  private List<Character> uniques(List<String> list) {
    List<Character> ret = new ArrayList<>();
    Map<Character, Boolean> counted = new HashMap<>();

    for (String s : list) {
      for (Character c : s.toCharArray()) {
        if (!counted.containsKey(c)) {
          ret.add(c);
          counted.put(c, true);
        }
      }
    }

    return ret;
  }
}
