package com.peheje.hiddenMarkov;

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
}
