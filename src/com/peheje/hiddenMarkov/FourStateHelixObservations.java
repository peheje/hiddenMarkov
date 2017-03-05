package com.peheje.hiddenMarkov;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FourStateHelixObservations extends ObservationsFromFastaFile {

  public FourStateHelixObservations(String path) throws IOException {
    super(path);
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
      System.out.println("original: " + orig);
      System.out.println("transformed: " + s);
      transformedStates.add(s);
    }
    return transformedStates;
  }
}
