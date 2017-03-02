package com.peheje.hiddenMarkov;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.ejml.simple.SimpleMatrix;

public class MarkovModelFromCounting implements IMarkovModel {

  private IObservations observations;
  private Comparator<Character> hiddenComparator;
  private Comparator<Character> observableComparator;


  public MarkovModelFromCounting(IObservations observations, List<Character> stateOrder, List<Character> observableOrder) {
    this.observations = observations;

    this.hiddenComparator = (o1, o2) -> {
      if (stateOrder != null) {
        int ai = stateOrder.indexOf(o1);
        int bi = stateOrder.indexOf(o2);
        if (ai != -1 && bi != -1) {
          return Integer.compare(ai, bi);
        }
      }
      return o1 - o2;
    };

    this.observableComparator = (o1, o2) -> {
      if (observableOrder != null) {
        int ai = observableOrder.indexOf(o1);
        int bi = observableOrder.indexOf(o2);
        if (ai != -1 && bi != -1) {
          return Integer.compare(ai, bi);
        }
      }
      return o1 - o2;
    };
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

    // E.g. { "o": { "i": 3, "M": 1 }, "i": { "o": 5, "M": 8 } }
    SortedMap<Character, SortedMap<Character, Integer>> oMap = new TreeMap<>(hiddenComparator);
    List<Character> uniqueStates = uniques(observations.getStates());

    // Count transitions
    for (String state : observations.getStates()) {
      for (int i = 1; i < state.length(); i++) {
        Character pre = state.charAt(i - 1);
        Character cur = state.charAt(i);
        if (!oMap.containsKey(pre)) {
          SortedMap<Character, Integer> iMap = new TreeMap<>(hiddenComparator);
          for (Character c : uniqueStates) {
            iMap.put(c, 0); // Pseudo count?
          }
          oMap.put(pre, iMap);
        } else {
          SortedMap<Character, Integer> iMap = oMap.get(pre);
          iMap.put(cur, iMap.get(cur) + 1);
        }
      }
    }

    // Count total transitions for each state, using # in map
    for (Character oKey : oMap.keySet()) {
      SortedMap<Character, Integer> iMap = oMap.get(oKey);
      int sum = 0;
      for (Character iKey : iMap.keySet()) {
        sum += iMap.get(iKey);
      }
      iMap.put('#', sum);
    }

    // Convert to probability matrix
    final int k = oMap.size();
    SimpleMatrix m = new SimpleMatrix(k, k);

    int row = 0;
    int col = 0;
    for (Character oKey : oMap.keySet()) {
      SortedMap<Character, Integer> iMap = oMap.get(oKey);
      for (Character iKey : iMap.keySet()) {
        if (!iKey.equals('#')) {
          m.set(row, col, (double)iMap.get(iKey) / (double)iMap.get('#'));
          col++;
        }
      }
      row++;
      col = 0;
    }

    for (int i = 0; i < k; i++) {
      SimpleMatrix rowVector = m.extractVector(true, i);
      double v = rowVector.elementSum();
      assert v == 1;
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    System.out.println(gson.toJson(oMap));
    m.print();

    return m;
  }

  @Override
  public SimpleMatrix getEmissions() {

    // E.g. { "M": {"A": 4, "C": 3, "D": 1 }, "i": { "A": 5, "C": 8, "D": 9 } }
    Map<Character, Map<Character, Integer>> oMap = new TreeMap<>(hiddenComparator);

    // Count emissions
    List<String> sequences = observations.getSequences();
    List<String> states = observations.getStates();
    assert sequences.size() == states.size();
    int N = sequences.size();

    for (int i = 0; i < N; i++) {
      String sta = states.get(i);
      String seq = sequences.get(i);
      assert sta.length() == seq.length();

      for (int j = 0; j < sta.length(); j++) {
        Character obs_j = sta.charAt(j);
        Character cur_j = seq.charAt(j);

        if (!oMap.containsKey(obs_j)) {
          oMap.put(obs_j, new TreeMap<>(observableComparator));
        } else {
          Map<Character, Integer> iMap = oMap.get(obs_j);
          if (iMap.containsKey(cur_j)) {
            iMap.put(cur_j, iMap.get(cur_j) + 1);
          } else {
            iMap.put(cur_j, 1);
          }
        }
      }
    }

    // Count total emissions for each state, using # in map
    for (Character oKey : oMap.keySet()) {
      Map<Character, Integer> iMap = oMap.get(oKey);
      int sum = 0;
      for (Character iKey : iMap.keySet()) {
        sum += iMap.get(iKey);
      }
      iMap.put('#', sum);
    }

    // To emission matrix
    final int k = oMap.size();
    final int d = uniques(sequences).size();

    SimpleMatrix m = new SimpleMatrix(k, d);
    int row = 0;
    int col = 0;
    for (Character oKey : oMap.keySet()) {
      Map<Character, Integer> iMap = oMap.get(oKey);
      for (Character iKey : iMap.keySet()) {
        if (!iKey.equals('#')) {
          m.set(row, col, (double)iMap.get(iKey) / (double)iMap.get('#'));
          col++;
        }
      }
      row++;
      col = 0;
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    System.out.println(gson.toJson(oMap));
    m.print();

    for (int i = 0; i < k; i++) {
      SimpleMatrix rowVector = m.extractVector(true, i);
      double v = rowVector.elementSum();
      assert v == 1;
    }

    return m;
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
