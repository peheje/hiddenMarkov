package com.peheje.hiddenMarkov;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.Pair;
import org.ejml.simple.SimpleMatrix;

public class Main {

  public static void main(String[] args) {
    //E2();
    //E3(new Character[]{'i', 'M', 'o'}, "3state");
    E3(new Character[]{'i', 'L', 'o', 'E'}, "4state");
  }

  public static void E3(Character[] hiddenOrder, String prefix) {
    try {
      final String dir = System.getProperty("user.dir");
      final String datasetPath = "/Dataset160/set160.";
      final int sets = 10;
      final int stateModel = hiddenOrder.length;

      // Respect this order when creating the matrices.
      Character[] observableOrder = new Character[]{'A', 'C', 'E', 'D', 'G', 'F', 'I', 'H', 'K', 'M', 'L', 'N', 'Q', 'P', 'S', 'R', 'T', 'W', 'V', 'Y'};
      MarkovCalculator calculator = new MarkovCalculator();

      // Run 10 fold test where we ignore 1 dataset and train by the others.
      // Then use the ignored to do viterbi and compare with real result.

      // Write down the accuracies of the comparing tool.
      double[] acArr = new double[sets];

      for (int ignoreIdx = 0; ignoreIdx < sets; ignoreIdx++) {
        Observations observations = stateModel == 4 ? new FourStateHelixObservations(null) : new ObservationsFromFasta(null);
        for (int j = 0; j < sets; j++) {
          if (j == ignoreIdx) {
            continue;
          }
          // Read observations from file and add it to the observations.
          String obsPath = dir + datasetPath + j + ".labels.txt";
          Observations set = stateModel == 4 ? new FourStateHelixObservations(obsPath) : new ObservationsFromFasta(obsPath);
          observations.add(set);
        }

        // Create the markov model by counting.
        MarkovModel model = new MarkovFromCounting(observations, hiddenOrder, observableOrder);

        // Viterbi decoding on the ignored.
        final String ignorePath = dir + datasetPath + ignoreIdx + ".labels.txt";
        Observations ignoredObservations = stateModel == 4 ? new FourStateHelixObservations(ignorePath) : new ObservationsFromFasta(ignorePath);

        // Write the result in the fasta format for comparing tool to read.
        BufferedWriter out = new BufferedWriter(new FileWriter(dir + "/tmp_kfold.txt"));
        for (int k = 0; k < ignoredObservations.getSequences().size(); k++) {

          String name = ignoredObservations.getNames().get(k);
          String seq = ignoredObservations.getSequences().get(k);

          out.write(name + "\n");
          out.write(seq + "\n");
          out.write("#\n");

          Pair<List<Integer>, SimpleMatrix> viterbi = calculator.viterbi(model, seq);

          StringBuilder sb = new StringBuilder();
          for (Integer vp : viterbi.getKey()) {
            sb.append(model.getHidden().get(vp));
          }
          String viterbiPath = sb.toString();

          out.write(viterbiPath);
          out.write("\n");
        }
        out.close();

        // Run the python comparing tool proveded by teacher
        List<String> listResult = new ArrayList<>();
        {
          String toExec = "python " + dir + "/compare_tm_pred.py " + ignorePath + " tmp_kfold.txt";
          Process p = Runtime.getRuntime().exec(toExec);
          BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String l;
          while ((l = in.readLine()) != null) {
            listResult.add(l);
          }
        }

        // Collect Sensitivity (SN), cross-correlation (CC), specificity (SP) and accuracy (AC)
        List<String> sn_cc_sp_ac = new ArrayList<>();
        {
          String lastLine = listResult.get(listResult.size() - 1);
          Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");  //http://stackoverflow.com/questions/25707779/regex-to-find-integer-or-decimal-from-a-string-in-java-in-a-single-group
          Matcher matcher = regex.matcher(lastLine);
          while (matcher.find()) {
            sn_cc_sp_ac.add(matcher.group(1));
          }
        }
        acArr[ignoreIdx] = Double.parseDouble(sn_cc_sp_ac.get(3));

        // Write the output of the comparing tool to file.
        String res = String.join("\n", listResult);
        out = new BufferedWriter(new FileWriter(dir + "/" + prefix + "_result_project_3_" + ignoreIdx + ".txt"));
        if (ignoreIdx == 0) {
          out.write("");  // Delete content
        }
        out.append(res);
        out.close();
      }

      // Calculate mean and variance
      Statistics st = new Statistics(acArr);
      Path file = Paths.get(prefix + "_result_project_3_AC.txt");
      Files.write(file, Arrays.asList("AC mean: " + st.getMean(), "AC variance: " + st.getVariance()), StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void E2() {
    try {
      String dir = System.getProperty("user.dir");
      MarkovModel model = new MarkovFromFasta(dir + "/model.txt");
      Observations observations = new ObservationsFromFasta(dir + "/seq1.txt");

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
