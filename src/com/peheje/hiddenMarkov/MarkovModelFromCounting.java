package com.peheje.hiddenMarkov;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ejml.simple.SimpleMatrix;
import sun.java2d.pipe.SpanShapeRenderer.Simple;

public class MarkovModelFromCounting implements IMarkovModel {

  private IObservations observations;

  public MarkovModelFromCounting(IObservations observations) {
    this.observations = observations;
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
    Map<Character, Map<Character, Integer>> outer = new HashMap<>();

    // Count transitions
    for (String state : observations.getStates()) {
      for (int i = 1; i < state.length(); i++) {
        Character pre = state.charAt(i - 1);
        Character cur = state.charAt(i);

        if (!outer.containsKey(pre)) {
          outer.put(pre, new HashMap<>());
        } else {
          Map<Character, Integer> inner = outer.get(pre);
          if (inner.containsKey(cur)) {
            inner.put(cur, inner.get(cur) + 1);
          } else {
            inner.put(cur, 1);
          }
        }
      }
    }

    // Count total transitions for each state, using # in map
    for (Character oKey : outer.keySet()) {
      Map<Character, Integer> iMap = outer.get(oKey);
      int sum = 0;
      for (Character iKey : iMap.keySet()) {
        sum += iMap.get(iKey);
      }
      iMap.put('#', sum);
    }

    // Convert to probability matrix
    int k = outer.size();
    SimpleMatrix m = new SimpleMatrix(k, k);

    int row = 0;
    int col = 0;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    System.out.println(gson.toJson(outer));
    m.print();

    return null;
  }

  @Override
  public SimpleMatrix getEmissions() {
    return null;
  }
}
