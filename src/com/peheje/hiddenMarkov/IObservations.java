package com.peheje.hiddenMarkov;

import java.util.List;

public interface IObservations {

  List<String> getSequences();

  List<String> getStates();

  List<String> getNames();
}
