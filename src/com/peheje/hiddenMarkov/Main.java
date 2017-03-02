package com.peheje.hiddenMarkov;

import java.util.List;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

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
      MarkovCalculator calculator = new MarkovCalculator();

      for (int ignore = 0; ignore < sets; ignore++) {
        IObservations observations = new ObservationsFromFile(null);
        for (int j = 0; j < sets; j++) {
          if (j == ignore) {
            // Ignoring
            continue;
          }

          IObservations set = new ObservationsFromFile(
              dir + path + j + ".labels.txt");
          observations.add(set);
        }
        Character[] hiddenOrder = new Character[]{'i', 'M', 'o'};
        Character[] observableOrder = new Character[]{'A', 'C', 'E', 'D', 'G', 'F', 'I', 'H', 'K',
            'M', 'L', 'N', 'Q', 'P', 'S', 'R', 'T', 'W', 'V', 'Y'};
        IMarkovModel model = new MarkovModelFromCounting(observations, hiddenOrder,
            observableOrder);

        /*
        countingModel.getTransitions();
        countingModel.getEmissions();
        countingModel.getInitial();
        */

        // Viterbi decoding on the ignored;
        IObservations set = new ObservationsFromFile(
            dir + path + ignore + ".labels.txt");

        for (int k = 0; k < set.getSequences().size(); k++) {
          List<Integer> viterbiIdx = calculator
              .viterbi(model, set.getSequences().get(k)).getKey();
          StringBuilder sb = new StringBuilder();
          for (Integer vp : viterbiIdx) {
            sb.append(model.getHidden().get(vp));
          }

          String viterbiPath = sb.toString();
          System.out.println(viterbiPath);



          PythonInterpreter interpreter = new PythonInterpreter();
          interpreter.exec("import sys");
          interpreter.exec("sys.path.append('/usr/lib/python2.7')");

          interpreter.execfile(dir + "/compare_tm_pred.py");
          PyObject someFunc = interpreter.get("funcName");
          PyObject result = someFunc.__call__(new PyString("Test!"));
          String realResult = (String) result.__tojava__(String.class);

        }
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
