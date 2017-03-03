package com.peheje.hiddenMarkov;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import javafx.util.Pair;
import org.ejml.simple.SimpleMatrix;

public class Main {

  public static void main(String[] args) {
    //E2();
    E3();
  }

  public static void E3() {
    try {
      final String dir = System.getProperty("user.dir");
      final String path = "/Dataset160/set160.";
      final int sets = 10;

      Character[] hiddenOrder = new Character[]{'i', 'M', 'o'};
      Character[] observableOrder = new Character[]{'A', 'C', 'E', 'D', 'G', 'F', 'I', 'H', 'K',
          'M', 'L', 'N', 'Q', 'P', 'S', 'R', 'T', 'W', 'V', 'Y'};
      MarkovCalculator calculator = new MarkovCalculator();

      for (int ignore = 0; ignore < sets; ignore++) {
        Observations observations = new ObservationsFromFile(null);
        for (int j = 0; j < sets; j++) {
          if (j == ignore) {
            // Ignoring
            continue;
          }
          Observations set = new ObservationsFromFile(
              dir + path + j + ".labels.txt");
          observations.add(set);
        }

        MarkovModel model = new MarkovModelFromCounting(observations, hiddenOrder,
            observableOrder);

        // Viterbi decoding on the ignored:
        final String ignoredPath = dir + path + ignore + ".labels.txt";
        Observations ignoredObservations = new ObservationsFromFile(ignoredPath);

        BufferedWriter out = new BufferedWriter(new FileWriter(dir + "/pythonCompareOutput.txt"));

        for (int k = 0; k < ignoredObservations.getSequences().size(); k++) {

          String name = ignoredObservations.getNames().get(k);
          String seq = ignoredObservations.getSequences().get(k);

          out.write(name + "\n");
          out.write(seq + "\n");
          out.write("#\n" );

          Pair<List<Integer>, SimpleMatrix> viterbi = calculator.viterbi(model, seq);
          List<Integer> viterbiPathIdx = viterbi.getKey();

          StringBuilder sb = new StringBuilder();
          for (Integer vp : viterbiPathIdx) {
            sb.append(model.getHidden().get(vp));
          }
          String viterbiPath = sb.toString();

          out.write(viterbiPath);
          out.write("\n");

        }
        out.close();

        String toExec = "python " + dir + "/compare_tm_pred.py " + ignoredPath + " pythonCompareOutput.txt";
        Process p = Runtime.getRuntime().exec(toExec);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringBuilder sb = new StringBuilder();
        for (String l; (l = in.readLine()) != null; sb.append(l + "\n"));
        String res = sb.toString();

        out = new BufferedWriter(new FileWriter(dir + "/resultProject3.txt"));
        if (ignore == 1) {
          out.write("");
        }
        out.append(res);
        out.close();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void E2() {
    try {
      String dir = System.getProperty("user.dir");
      MarkovModel model = new MarkovModelFromFile(dir + "/model.txt");
      Observations observations = new ObservationsFromFile(dir + "/seq1.txt");

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
