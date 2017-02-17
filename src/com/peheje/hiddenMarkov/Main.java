package com.peheje.hiddenMarkov;

import java.util.ArrayList;
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
                double p = calculator.JointLogProbability(model, sequences.get(i), states.get(i));
                System.out.println(p);
            }

            String seq = sequences.get(0);
            System.out.println(seq);
            List<Integer> viterbiPathIdx = calculator.viterbi(model, seq);

            StringBuilder sb = new StringBuilder();
            for (Integer vp : viterbiPathIdx) {
                sb.append(model.getHidden().get(vp));
            }

            String viterbiPath = sb.toString();
            System.out.println(viterbiPath);
            //double p = calculator.JointLogProbability(model, seq, viterbiPath);
            // System.out.println("Viterby path joint log propability: " + p);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
