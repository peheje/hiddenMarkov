package com.peheje.hiddenMarkov;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.ejml.simple.SimpleMatrix;

public class MarkovFromCounting implements MarkovModel {

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private final Character[] observableOrder;
  private final Character[] stateOrder;
  private final List<String> hidden = new ArrayList<>();
  private final Map<Character, Integer> hiddenLookup;
  private final List<String> observables = new ArrayList<>();
  private final Map<Character, Integer> observablesLookup;
  private Observations observations;


  public MarkovFromCounting(Observations observations, Character[] stateOrder, Character[] observableOrder) {
    this.observations = observations;
    this.stateOrder = stateOrder;
    this.observableOrder = observableOrder;

    for (Character c : stateOrder) {
      this.hidden.add(c.toString());
    }
    this.hiddenLookup = lookup(this.hidden);

    for (Character c : observableOrder) {
      this.observables.add(c.toString());
    }
    this.observablesLookup = lookup(this.observables);
  }

  @Override
  public List<String> getHidden() {
    return hidden;
  }

  @Override
  public Map<Character, Integer> getHiddenMap() {
    return hiddenLookup;
  }

  @Override
  public List<String> getObservables() {
    return observables;
  }

  @Override
  public Map<Character, Integer> getObservablesMap() {
    return observablesLookup;
  }

  @Override
  public List<Double> getInitial() {

    // Each dataset contains 16 observation/state pairs. So 9*16 = 144.
    List<String> states = observations.getStates();

    Double[] pi = new Double[stateOrder.length];
    Arrays.fill(pi, 0.0);

    // Count
    for (int i = 0; i < states.size(); i++) {
      String z = states.get(i);
      char c = z.charAt(0);
      int ci = hiddenLookup.get(c);
      pi[ci]++;
    }

    for (int i = 0; i < pi.length; i++) {
      pi[i] /= states.size();
    }

    /*System.out.println("Initial");
    for (double d : pi) {
      System.out.println(d);
    }*/

    return Arrays.asList(pi);
  }

  @Override
  public SimpleMatrix getTransitions() {

    final int K = stateOrder.length;
    SimpleMatrix m = new SimpleMatrix(K, K);

    // Count
    for (String z : observations.getStates()) {
      for (int i = 0; i < z.length() - 1; i++) {
        char cur = z.charAt(i);
        char nex = z.charAt(i + 1);
        int curi = hiddenLookup.get(cur);
        int nexi = hiddenLookup.get(nex);
        m.set(nexi, curi, m.get(nexi, curi) + 1);
      }
    }

    for (int r = 0; r < m.numRows(); r++) {
      // Normalize this row
      int rowSum = (int) m.extractVector(true, r).elementSum();
      for (int c = 0; c < m.numCols(); c++) {
        m.set(r, c, m.get(r, c) / rowSum);
      }
    }

    for (int r = 0; r < m.numRows(); r++) {
      double s = m.extractVector(true, r).elementSum();
      assert Math.round(s) == 1;
    }

    //System.out.println("Transitions");
    //m.print(6,6);
    return m;
  }

  @Override
  public SimpleMatrix getEmissions() {

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
        int xi = observablesLookup.get(x);
        int zi = hiddenLookup.get(z);
        m.set(zi, xi, m.get(zi, xi) + 1);
      }
    }

    for (int r = 0; r < m.numRows(); r++) {
      // Normalize this row
      int rowSum = (int) m.extractVector(true, r).elementSum();
      for (int c = 0; c < m.numCols(); c++) {
        m.set(r, c, m.get(r, c) / rowSum);
      }
    }

    for (int r = 0; r < m.numRows(); r++) {
      double s = m.extractVector(true, r).elementSum();
      assert Math.round(s) == 1;
    }

    //System.out.println("Emmisions");
    //m.print(6,6);
    return m;
  }
}
