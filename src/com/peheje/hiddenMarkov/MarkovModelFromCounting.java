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
    List<String> ret = new ArrayList<>();
    for (Character c : stateOrder) {
      ret.add(c.toString());
    }
    return ret;
  }

  @Override
  public Map<Character, Integer> getHiddenMap() {
    return Lookup(getHidden());
  }

  @Override
  public List<String> getObservables() {
    List<String> ret = new ArrayList<>();
    for (Character c : observableOrder) {
      ret.add(c.toString());
    }
    return ret;
  }

  @Override
  public Map<Character, Integer> getObservablesMap() {
    return Lookup(getObservables());
  }

  @Override
  public List<Double> getInitial() {
    List<Double> pi = new ArrayList<>();
    List<String> states = observations.getStates();

    Map<Character, IdxCount> stateMap = new HashMap<>();
    for (int i = 0; i < stateOrder.length; i++) {
      stateMap.put(stateOrder[i], new IdxCount(i, 0));
    }

    // Count
    for (int i = 0; i < states.size(); i++) {
      String z = states.get(i);
      char c = z.charAt(0);
      stateMap.get(c).count++;
    }

    // Divide
    for (int i = 0; i < stateMap.size(); i++) {
      double divided = (double) stateMap.get(stateOrder[i]).count / (double) states.size();
      pi.add(divided);
    }

    System.out.println("Initial");
    for (double d : pi) {
      System.out.println(d);
    }

    return pi;
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
        IdxCount curm = stateMap.get(cur);
        IdxCount nextm = stateMap.get(nex);
        m.set(nextm.idx, curm.idx, m.get(nextm.idx, curm.idx) + 1);
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

    System.out.println("Transitions");
    m.print(6,6);
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

        IdxCount xm = observableMap.get(x);
        IdxCount zm = stateMap.get(z);

        m.set(zm.idx, xm.idx, m.get(zm.idx, xm.idx) + 1);
        zm.count++;
      }
    }

    // Divide
    for (int i = 0; i < m.numCols(); i++) {
      for (int j = 0; j < m.numRows(); j++) {
        int count = stateMap.get(stateOrder[j]).count;
        m.set(j, i, m.get(j, i) / (double) count);
      }
    }

    System.out.println("Emmisions");
    m.print(6,6);
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

}
