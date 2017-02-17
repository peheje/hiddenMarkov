package com.peheje.hiddenMarkov;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PHJ-WINDOWS on 12/02/2017.
 */
public class ObservationsFromFile implements IObservations {
    private List<String> sequences = new ArrayList<>();
    private List<String> states = new ArrayList<>();

    public ObservationsFromFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            if (l.startsWith(">")) {
                sequences.add(lines.get(i+1));
            } else if (l.startsWith("#")) {
                states.add(lines.get(i + 1));
            }
        }
    }

    @Override
    public List<String> getSequences() {
        return sequences;
    }

    @Override
    public List<String> getStates() {
        return states;
    }
}
