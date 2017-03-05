package com.peheje.hiddenMarkov;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ObservationsFromFasta implements Observations {

  private List<String> names = new ArrayList<>();
  private List<String> sequences = new ArrayList<>();
  private List<String> states = new ArrayList<>();

  public ObservationsFromFasta(String path) throws IOException {
    if (path == null) {
      return;
    }

    List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.ISO_8859_1);
    for (int i = 0; i < lines.size(); i++) {
      String l = lines.get(i);
      if (l.startsWith(">")) {
        names.add(l);
        int o = 1;
        while (lines.get(i + o).trim().equals("")) {
          o++;
        }
        sequences.add(lines.get(i + o).trim());
      } else if (l.startsWith("#")) {
        int o = 0;
        while (lines.get(i + o).replace("#", "").trim().equals("")) {
          o++;
        }
        states.add(lines.get(i + o).replace("#", "").trim());
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

  @Override
  public List<String> getNames() {
    return names;
  }

  @Override
  public void add(Observations other) {
    sequences.addAll(other.getSequences());
    states.addAll(other.getStates());
    names.addAll(other.getNames());
  }
}
