package com.peheje.hiddenMarkov;

import java.util.List;

/**
 * Created by PHJ-WINDOWS on 12/02/2017.
 */
public interface IObservations {
    List<String> getSequences();
    List<String> getStates();
}
