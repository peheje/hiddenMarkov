package com.peheje.hiddenMarkov;

import java.util.List;

public interface Observations {

  List<String> getSequences();

  List<String> getStates();

  List<String> getNames();

  void add(Observations other);
}
