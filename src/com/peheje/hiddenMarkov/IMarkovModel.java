package com.peheje.hiddenMarkov;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ejml.simple.SimpleMatrix;

public interface IMarkovModel {

  List<String> getHidden();

  Map<Character, Integer> getHiddenMap();

  List<String> getObservables();

  Map<Character, Integer> getObservablesMap();

  List<Double> getInitial();

  SimpleMatrix getTransitions();

  SimpleMatrix getEmissions();

  default Map<Character, Integer> Lookup(List<String> list) {
    Map<Character, Integer> map = new HashMap<>();
    for (int i = 0; i < list.size(); i++) {
      map.put(list.get(i).charAt(0), i);
    }
    return map;
  }
}
