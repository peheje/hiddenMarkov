package com.peheje.hiddenMarkov;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ObservationsFromFile implements IObservations {

  private List<String> sequences = new ArrayList<>();
  private List<String> states = new ArrayList<>();

  public ObservationsFromFile(String path) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(path));
    for (int i = 0; i < lines.size(); i++) {
      String l = lines.get(i);
      if (l.startsWith(">")) {
        int o = 1;
        while (lines.get(i + o).trim().equals("")) o++;
        sequences.add(lines.get(i + o).trim());
      } else if (l.startsWith("#")) {
        int o = 1;
        while (lines.get(i + o).trim().equals("")) o++;
        states.add(lines.get(i + o).trim());
      }
    }
  }

  @Override
  public List<String> getSequences() {
    return sequences;
  }

  @Override
  public List<String> getStates() {
    return states;
  }
}
