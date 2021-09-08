import java.io.*;
import java.util.*;

/**
 * Problem Set 5
 * Terminology for Hidden Markov Model:
 * state = tag = an abbreviation of a part of speech
 * observation = a part (word, punctuation, number, etc.) in a sentence
 *
 * @author Connor Hay, Dartmouth CS 10, Spring 2021
 */

public class POSTagger {
    private Map<String, Map<String, Double>> observationsProbabilities; // holds probability values for each state's observations
    private Map<String, Map<String, Double>> transitionsProbabilities; // holds probability values for moving from one state to another
    final double unseenObservationScore = -15.625; // hard-coded score for unseen observations used when tagging
    private boolean DEBUG = false; // default hard-coded false. can be changed with setter for testing purposes

    /**
     * Constructor does not train. Trained maps should be supplied using this class's setters or fileTraining() method
     */
    public POSTagger() {
        observationsProbabilities = new HashMap<>();
        transitionsProbabilities = new HashMap<>();
    }

    public void setObservationsProbabilities(Map<String, Map<String, Double>> observationsProbabilities) {
        this.observationsProbabilities = observationsProbabilities;
    }

    public void setTransitionsProbabilities(Map<String, Map<String, Double>> transitionsProbabilities) {
        this.transitionsProbabilities = transitionsProbabilities;
    }

    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    /**
     * Handles training from files.
     * Training entails the construction of a Hidden Markov Model with two maps, one for observations and the other for  state transitions
     *
     * @param sentencesFileName name or path of file containing formatted sentences
     * @param tagsFileName name or path of file containing formatted tags
     */
    public void fileTraining(String sentencesFileName, String tagsFileName) {
        try {
            // open files
            BufferedReader sentencesFile = new BufferedReader(new FileReader(sentencesFileName));
            BufferedReader tagsFile = new BufferedReader(new FileReader(tagsFileName));

            // reset maps if they've already been trained
            if (!observationsProbabilities.isEmpty()) observationsProbabilities = new HashMap<>();
            if (!transitionsProbabilities.isEmpty()) transitionsProbabilities = new HashMap<>();
            transitionsProbabilities.put("#", new HashMap<>()); // initialize and put start state
            // read corresponding lines from the files
            String currSentence;
            String currTagLine;
            while (((currSentence = sentencesFile.readLine()) != null) && ((currTagLine = tagsFile.readLine()) != null)) {
                if (DEBUG) {
                    System.out.println("Training...");
                    System.out.println("\tTraining Sentence: " + currSentence);
                    System.out.println("\tCorresponding Tags: " + currTagLine);
                }
                // split the lines into their parts
                currSentence = currSentence.toLowerCase();
                String[] observations = currSentence.split(" ");
                String[] tags = currTagLine.split(" ");
                // updateCounts method works by sentence to avoid IOExceptions
                updateCounts(observations, tags);
            }

            sentencesFile.close();
            tagsFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (DEBUG) {
            System.out.println("Counting Finished!\n");
            System.out.println("Observation Counts Map");
            System.out.println(observationsProbabilities);
            System.out.println("Transitions Counts Map");
            System.out.println(transitionsProbabilities);
        }

        if (DEBUG) System.out.println("Turning raw counts into probabilities with natural log and normalization...");

        for (String tag : transitionsProbabilities.keySet()) { // visit each tag's transition and observation count map
            if (DEBUG) System.out.println("Processing counts to probabilities for tag: " + tag);
            // handle observations
            if (!tag.equals("#")) {
                if (DEBUG) System.out.println("\tObservations:");
                if (countsToProbabilities(observationsProbabilities.get(tag))){
                    System.out.println("Error, observations countMap for " + tag + " was null");
                }
            }
            // handle transitions
            if (DEBUG) System.out.println("\tTransitions:");
            if (countsToProbabilities(transitionsProbabilities.get(tag))) {
                System.out.println("Error, transitions countMap for " + tag + " was null");
            }

        }
        if (DEBUG) {
            System.out.println("Training Finished!");
            System.out.println("\tObservation Probabilities Map");
            System.out.println("\t" + observationsProbabilities + "\n");
            System.out.println("\tTransitions Probabilities Map");
            System.out.println("\t" + transitionsProbabilities + "\n");
        }
    }

    /**
     * Helper method for class Constructor
     * Updates the counts in transitionProbabilities and observationsProbabilities for a given sentence.
     *
     * @param observations a sentence in array form (ends with period)
     * @param tags a sentence of tags in array form (ends with period)
     */
    private void updateCounts(String[] observations, String[] tags) {
        if (observations.length != tags.length) System.out.println("non-matching number of observations and tags"); // potential error handling
        else {
            String currObservation;
            String prevTag = "#"; // default start state
            String currTag;
            // as just proven, tags.length and observations.length are interchangeable
            for (int i = 0; i < tags.length; i++) {
                currObservation = observations[i];
                currTag = tags[i];
                try {
                    incrementCount("observations", currTag, currObservation);
                    incrementCount("transitions", prevTag, currTag);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                prevTag = currTag;
            }
        }
    }

    /**
     * Helper method for Constructor
     * Handles normalization and log-probability calculation during training
     *
     * @param countsMap an inner map from either transitionsProbabilities or observationsProbabilities
     * @return boolean indicating error occurrence or not
     */
    private boolean countsToProbabilities(Map<String, Double> countsMap) {
        if (countsMap == null) return true; // error occurred, no countsTotal found
        else {
            if (DEBUG) {
                System.out.println("\tRaw Counts: " + countsMap);
            }
            double countTotal = countsMap.remove("countsTotal"); // remove and use the total for this tag
            for (String key : countsMap.keySet()) { // visit each count in the map
                double count = countsMap.get(key); // get the count
                double probability = Math.log(count / countTotal); // calculate the probability
                countsMap.put(key, probability); // replace the count with the probability
            }
            if (DEBUG) {
                System.out.println("\tProbabilities: " + countsMap);
            }
            return false; // no error
        }
    }

    /**
     * Helper method for updateCounts()
     * Handles count incrementing during POSTagger training
     *
     * @param chosenMap enter "observations" or "transitions" to increment the corresponding map
     * @param outerKey used to get the inner map of counts
     * @param innerKey used to get and modify the appropriate count from the map
     */
    private void incrementCount(String chosenMap, String outerKey, String innerKey) throws Exception {
        if (DEBUG) System.out.println("Incrementing " + chosenMap);

        // set up the map to increment based on choice
        Map<String, Map<String, Double>> outerMap;
        if (chosenMap.equals("observations")) outerMap = observationsProbabilities;
        else if (chosenMap.equals("transitions")) outerMap = transitionsProbabilities;
        else throw new Exception("invalid map choice for incrementing");
        Map<String, Double> innerMap = outerMap.getOrDefault(outerKey, new HashMap<>());

        if (!innerMap.containsKey(innerKey)) {
            innerMap.put(innerKey, 1.);
        }
        else {
            double currCount = innerMap.get(innerKey);
            innerMap.put(innerKey, currCount+1);
        }
        // track the total of all other slots in-map by incrementing simultaneously
        if (!innerMap.containsKey("countsTotal")) {
            innerMap.put("countsTotal", 1.);
        }
        else {
            double currTotal = innerMap.get("countsTotal");
            innerMap.put("countsTotal", currTotal+1);
        }
        if (DEBUG) {
            System.out.println("\tIncremented key: " + innerKey + " to count: " + innerMap.get(innerKey));
            System.out.println("\tIncremented total: " + innerMap.get("countsTotal"));
        }

        // save the incremented values
        outerMap.put(outerKey, innerMap);
    }

    public String tagViterbi(String sentence) {
        if (observationsProbabilities != null && transitionsProbabilities != null) {
            sentence = sentence.toLowerCase();
            String[] observations = sentence.split(" "); // split sentence into observations

            List<Map<String, String>> backPointerList = new ArrayList<>();

            Set<String> currStates = new HashSet<>();
            Map<String, Double> currScores = new HashMap<>();

            // initialize start case
            currStates.add("#");
            currScores.put("#", 0.);

            // for determining final state from which to back trace
            String finalState = null;
            double bestScore = Double.MIN_VALUE;

            // Viterbi algorithm to produce backPointer list (later used to reconstruct likely tags)
            for (int i = 0; i < observations.length; i++) { // loop over each observation in the sentence
                Set<String> nextStates = new HashSet<>();
                Map<String, Double> nextScores = new HashMap<>();
                Map<String, String> currBackPointers = new HashMap<>();

                String currObservation = observations[i];
                for (String currState : currStates) { // loop over current states (start points)
                    Map<String, Double> possibleTransitions = transitionsProbabilities.get(currState); // get possible transitions (paths)
                    if (possibleTransitions != null) {
                        for (String nextState : possibleTransitions.keySet()) { // loop over possible next states (end points)
                            nextStates.add(nextState);

                            // calculate next score
                            double currScore = currScores.get(currState);
                            double transitionScore = transitionsProbabilities.get(currState).get(nextState);
                            double observationScore = observationsProbabilities.get(nextState).getOrDefault(currObservation, unseenObservationScore);

                            double nextScore = currScore + transitionScore + observationScore;

                            // update nextScores list
                            if (!nextScores.containsKey(nextState) || (nextScore > nextScores.get(nextState))) {
                                // updated if the nextScore is the best so far, regardless of the state that came before
                                nextScores.put(nextState, nextScore);
                                currBackPointers.put(nextState, currState); // save where the best nextScore came from
                            }
                            // record final state on last observation
                            if (i == observations.length - 1 && (finalState == null || nextScore > bestScore)) {
                                bestScore = nextScore;
                                finalState = nextState;
                            }
                        }
                    }
                }
                backPointerList.add(currBackPointers); // update backPointer list

                if (DEBUG) {
                    System.out.println("Processed observation (" + currObservation + ") " + (i + 1) + " of " + (observations.length));
                    System.out.println("\tcurrentStates: " + currStates);
                    System.out.println("\tcurrentScores: " + currScores);
                    System.out.println("\tnextStates: " + nextStates);
                    System.out.println("\tnextScores: " + nextScores);
                }

                currStates = nextStates;
                currScores = nextScores;
            }
            if (DEBUG) System.out.println("Viturbi Algorithm Finished!\n");

            // reconstruct list of tags;
            String currTag = finalState;
            StringBuilder formattedTags = new StringBuilder();
            for (int i = backPointerList.size() - 1; i >= 0; i--) { // trace from back to front
                formattedTags.insert(0, currTag + " "); // add to the output
                currTag = backPointerList.get(i).get(currTag); // trace back
            }
            if (DEBUG) {
                System.out.println("Reconstructed Tags");
                System.out.println("\tfinalState: " + finalState);
                System.out.println("\tbackPointerList: " + backPointerList);
                System.out.println("\ttags: " + formattedTags + "\n");
            }
            // finally return the formatted string of tags in sentence form
            return formattedTags.toString();
        }
        else return "POSTagger not trained!";
    }
}