package com.peheje.hiddenMarkov;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    //E2();
    E3();
  }

  public static void E3() {
    try {
      String dir = System.getProperty("user.dir");
      MarkovCalculator calculator = new MarkovCalculator();
      final int sets = 10;

      for (int iSet = 0; iSet < sets; iSet++) {
        IObservations observations = new ObservationsFromFile(null);
        int ignoreIdx = 0;

        for (int jSet = 0; jSet < sets; jSet++) {
          if (jSet == ignoreIdx) {
            continue;
          }

          IObservations set = new ObservationsFromFile(
              dir + "/Dataset160" + "/set160." + jSet + ".labels.txt");
          observations.add(set);
        }
        Character[] hiddenOrder = new Character[]{'i', 'M', 'o'};
        Character[] observableOrder = new Character[]{'A', 'C', 'E', 'D', 'G', 'F', 'I', 'H', 'K',
            'M', 'L', 'N', 'Q', 'P', 'S', 'R', 'T', 'W', 'V', 'Y'};
        IMarkovModel countingModel = new MarkovModelFromCounting(observations, hiddenOrder,
            observableOrder);

        countingModel.getTransitions();
        countingModel.getEmissions();
        countingModel.getInitial();

        // Viterbi decoding on ignoreIdx;
        IObservations set = new ObservationsFromFile(
            dir + "/Dataset160" + "/set160." + ignoreIdx + ".labels.txt");

        for (int k = 0; k < set.getSequences().size(); k++) {
          List<Integer> viterbiPathIdx = calculator
              .viterbi(countingModel, set.getSequences().get(k)).getKey();
          StringBuilder sb = new StringBuilder();
          for (Integer vp : viterbiPathIdx) {
            sb.append(countingModel.getHidden().get(vp));
          }

          String viterbiPath = sb.toString();
          System.out.println(viterbiPath);
        }

        ignoreIdx++;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void E2() {
    try {
      String dir = System.getProperty("user.dir");
      IMarkovModel model = new MarkovModelFromFile(dir + "/model.txt");
      IObservations observations = new ObservationsFromFile(dir + "/seq1.txt");

      List<String> sequences = observations.getSequences();
      List<String> states = observations.getStates();
      List<String> names = observations.getNames();

      MarkovCalculator calculator = new MarkovCalculator();

      if (states.size() == sequences.size()) {
        for (int i = 0; i < sequences.size(); i++) {
          double p = calculator.jointLogProbability(model, sequences.get(i), states.get(i));
          System.out.println(p);
        }
      }

      System.out.println("\n\n");

      for (int i = 0; i < sequences.size(); i++) {
        String seq = sequences.get(i);

        String realState = "";
        if (states.size() == sequences.size()) {
          realState = states.get(i);
        }

        System.out.println("Seq: " + (i + 1));
        System.out.println(seq);
        List<Integer> viterbiPathIdx = calculator.viterbi(model, seq).getKey();

        StringBuilder sb = new StringBuilder();
        for (Integer vp : viterbiPathIdx) {
          sb.append(model.getHidden().get(vp));
        }

        String viterbiPath = sb.toString();
        System.out.println("Viterbi: " + viterbiPath);
        System.out.println("Real   : " + realState);
        double p = calculator.jointLogProbability(model, seq, viterbiPath);
        System.out.println("Viterby path joint log propability: " + p);
        System.out.println("\n\n");
      }
/*
      for (int i = 0; i < sequences.size(); i++) {
        String name = names.get(i);
        String seq = sequences.get(i);

        System.out.println(name);
        System.out.println(seq);
        System.out.println("#");

        Pair<List<Integer>, SimpleMatrix> viterbi = calculator.viterbi(model, seq);
        List<Integer> viterbiPathIdx = viterbi.getKey();
        StringBuilder sb = new StringBuilder();
        for (Integer vp : viterbiPathIdx) sb.append(model.getHidden().get(vp));
        String viterbiPath = sb.toString();

        System.out.println(viterbiPath);
        System.out.println("; log P(x,z) (as computed by Viterbi)             = " + calculator.largestValueInLastColumn(viterbi.getValue()));
        System.out.println("; log P(x,z) (as computer by your log-joint-prob) = " + calculator.jointLogProbability(model, seq, viterbiPath));
        System.out.println("\n");
      }
*/
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
