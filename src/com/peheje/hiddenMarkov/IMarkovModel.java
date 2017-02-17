package com.peheje.hiddenMarkov;

import org.ejml.simple.SimpleMatrix;

import java.util.List;
import java.util.Map;

/**
 * Created by PHJ-WINDOWS on 12/02/2017.
 */
public interface IMarkovModel {
    List<String> getHidden();
    Map<Character, Integer> getHiddenMap();

    List<String> getObservables();
    Map<Character, Integer> getObservablesMap();

    List<Double> getInitial();

    SimpleMatrix getTransitions();

    SimpleMatrix getEmissions();
}
