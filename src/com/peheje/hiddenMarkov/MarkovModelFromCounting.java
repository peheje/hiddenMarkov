package com.peheje.hiddenMarkov;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ejml.simple.SimpleMatrix;

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
    Map<Character, Map<Character, Integer>> counts = new HashMap<>();

    for (String state : observations.getStates()) {
      for (int i = 0; i < state.length(); i++) {
        Character c = state.charAt(i);
        int x = 0;
      }
    }



    return null;
  }

  @Override
  public SimpleMatrix getEmissions() {
    return null;
  }
}
