package com.peheje.hiddenMarkov;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.ejml.simple.SimpleMatrix;

public class MarkovModelFromFile implements IMarkovModel {

  private List<String> hidden;
  private Map<Character, Integer> hiddenMap;
  private List<String> observables;
  private Map<Character, Integer> observablesMap;
  private List<Double> initial;
  private SimpleMatrix transitions;
  private SimpleMatrix emissions;

  public MarkovModelFromFile(String path) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(path));
    for (int i = 0; i < lines.size() - 1; i++) {
      String l = lines.get(i);
      if (l.contains("hidden")) {
        hidden = Arrays.asList(lines.get(i + 1).split(" "));
      } else if (l.contains("observables")) {
        observables = Arrays.asList(lines.get(i + 1).split(" "));
      } else if (l.contains("pi")) {
        initial = Arrays.stream(lines.get(i + 1).split(" ")).map(Double::parseDouble)
            .collect(Collectors.toList());
      } else if (l.contains("transitions")) {
        List<List<Double>> matrix = new ArrayList<>();
        for (int j = i + 1; j < lines.size() && lines.get(j).length() > 0; j++) {
          matrix.add(Arrays.stream(lines.get(j).split(" ")).map(Double::parseDouble)
              .collect(Collectors.toList()));
        }
        double[][] doubles = matrix.stream().map(x -> x.stream().mapToDouble(p -> p).toArray())
            .toArray(double[][]::new);
        transitions = new SimpleMatrix(doubles);
      } else if (l.contains("emissions")) {
        List<List<Double>> matrix = new ArrayList<>();
        for (int j = i + 1; j < lines.size() && lines.get(j).length() > 0; j++) {
          matrix.add(Arrays.stream(lines.get(j).split(" ")).map(Double::parseDouble)
              .collect(Collectors.toList()));
        }
        double[][] doubles = matrix.stream().map(x -> x.stream().mapToDouble(p -> p).toArray())
            .toArray(double[][]::new);
        emissions = new SimpleMatrix(doubles);
      }
    }

    observablesMap = Lookup(observables);
    hiddenMap = Lookup(hidden);

  }

  @Override
  public List<String> getHidden() {
    return hidden;
  }

  @Override
  public Map<Character, Integer> getHiddenMap() {
    return hiddenMap;
  }

  @Override
  public List<String> getObservables() {
    return observables;
  }

  @Override
  public Map<Character, Integer> getObservablesMap() {
    return observablesMap;
  }

  @Override
  public List<Double> getInitial() {
    return initial;
  }

  @Override
  public SimpleMatrix getTransitions() {
    return transitions;
  }

  @Override
  public SimpleMatrix getEmissions() {
    return emissions;
  }
}
