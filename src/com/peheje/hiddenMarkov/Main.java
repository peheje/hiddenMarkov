package com.peheje.hiddenMarkov;

import java.util.List;

public class Main {
  public static void main(String[] args) {
    try {
      String dir = System.getProperty("user.dir");
      IMarkovModel model = new MarkovModelFromFile(dir + "/data.txt");
      IObservations observations = new ObservationsFromFile(dir + "/seq1.txt");

      List<String> sequences = observations.getSequences();
      List<String> states = observations.getStates();

      MarkovCalculator calculator = new MarkovCalculator();
      for (int i = 0; i < sequences.size(); i++) {
        double p = calculator.jointLogProbability(model, sequences.get(i), states.get(i));
        System.out.println(p);
      }

      System.out.println("\n\n");

      for (int i = 0; i < sequences.size(); i++) {
        String seq = sequences.get(i);
        String state = states.get(i);
        System.out.println("Seq: " + (i + 1));
        System.out.println(seq);
        List<Integer> viterbiPathIdx = calculator.viterbi(model, seq);

        StringBuilder sb = new StringBuilder();
        for (Integer vp : viterbiPathIdx) {
          sb.append(model.getHidden().get(vp));
        }

        String viterbiPath = sb.toString();
        System.out.println("Viterbi: " + viterbiPath);
        System.out.println("Real   : " + state);
        double p = calculator.jointLogProbability(model, seq, viterbiPath);
        System.out.println("Viterby path joint log propability: " + p);
        System.out.println("\n\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
