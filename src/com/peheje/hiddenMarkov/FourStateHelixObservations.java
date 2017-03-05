package com.peheje.hiddenMarkov;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FourStateHelixObservations extends ObservationsFromFasta {

  public FourStateHelixObservations(String path) throws IOException {
    super(path);
  }

  public static String fourToThreeState(String viterbiPath) {
    viterbiPath = viterbiPath.replace("E", "M");
    viterbiPath = viterbiPath.replace("L", "M");
    return viterbiPath;
  }

  @Override
  public List<String> getStates() {
    List<String> originalStates = super.getStates();
    List<String> transformedStates = new ArrayList<>(originalStates.size());

    for (String orig : originalStates) {
      StringBuilder sb = new StringBuilder();

      sb.append(orig.charAt(0));  // Assume first is never M
      for (int i = 1; i < orig.length(); i++) {
        char pre = orig.charAt(i - 1);
        char cur = orig.charAt(i);

        if (cur == 'M') {
          if (pre == 'o') {
            sb.append('E');
          } else if (pre == 'i') {
            sb.append('L');
          } else {
            sb.append(sb.charAt(sb.length() - 1));
          }
        } else {
          sb.append(cur);
        }
      }
      String s = sb.toString();
      assert orig.length() == s.length();
      transformedStates.add(s);
    }
    return transformedStates;
  }
}
