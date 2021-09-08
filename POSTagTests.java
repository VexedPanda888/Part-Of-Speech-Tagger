import java.io.*;
import java.util.*;

/**
 * Problem Set 5
 *
 *
 * @author Connor Hay, Dartmouth CS 10, Spring 2021
 */

public class POSTagTests {
    /**
     * When reviewing, comment out the methods you don't want to occur
     * You can run all of them, but only the last method's training will apply.
     */
    public static void main(String[] args) {
        POSTagger tagger = new POSTagger();
        //manualTrain(tagger); // manual training
        //manualTaggingTest(tagger); // tagging testing of Viterbi Algorithm
        //exampleTrainTest(tagger); // training testing with example files
        formattedFilesTrainAndTagTest(tagger, "simple"); // training and tagging testing with simple dataset
        //formattedFilesTrainAndTagTest(tagger, "brown"); // training and tagging testing with brown dataset
        //consoleTest(tagger); // console-based testing (REQUIRES TRAINED TAGGER)
    }

    /**
     * Compare two sets of tags to determine accuracy by tag.
     *
     * @param computedTags from trained POSTagger tagging a sentence
     * @param correctTags 100% accurate tags for the same sentence
     * @return map with right and wrong answer counts
     */
    private static Map<String, Integer> taggingAccuracy(String computedTags, String correctTags) {
        // prepare sentences
        String[] computed = computedTags.split(" ");
        String[] correct = correctTags.split(" ");
        // prepare counts
        int right = 0;
        int wrong = 0;
        // iterate to fill counts
        for (int i = 0; i < computed.length ; i++) {
            if (computed[i].equals(correct[i])) right++;
            else wrong++;
        }
        // construct and return map of results
        Map<String, Integer> result = new HashMap<>();
        result.put("right", right);
        result.put("wrong", wrong);
        return result;
    }

    public static void fileTest(POSTagger tagger, String sentencesFileName, String tagsFileName) {
        System.out.println("Beginning fileTest...");
        int correctLines = 0;
        int incorrectLines = 0;
        int correctTags = 0;
        int incorrectTags = 0;

        try {
            // open files
            BufferedReader sentencesFile = new BufferedReader(new FileReader(sentencesFileName));
            BufferedReader tagsFile = new BufferedReader(new FileReader(tagsFileName));

            // read corresponding lines from the files
            String currSentence;
            String expectedTags;
            System.out.println("Beginning Tagging...");
            while (((currSentence = sentencesFile.readLine()) != null) && ((expectedTags = tagsFile.readLine()) != null)) {
                String computedTags = tagger.tagViterbi(currSentence); // tag line

                Map<String, Integer> lineAccuracy = taggingAccuracy(computedTags, expectedTags); // calculate line accuracy
                correctTags += lineAccuracy.get("right");
                incorrectTags += lineAccuracy.get("wrong");

                // assess and count line correctness
                if (incorrectTags == 0) correctLines++;
                else incorrectLines++;
            }

            sentencesFile.close();
            tagsFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Tagging Complete!");
        System.out.println("Out of " + (correctLines + incorrectLines) + " lines, " + correctLines + " were tagged correctly and " + incorrectLines + " incorrectly.");
        System.out.println("Out of " + (correctTags + incorrectTags) + " tags, " + correctTags + " were correct and " + incorrectTags + " were incorrect.");
        System.out.println("Completed fileTest...\n");
    }

    /**
     * Assess the efficacy of the POSTagger on a set of files.
     *
     * @param setName set of 4 files, one pair with training data, the other with testing data
     */
    public static void formattedFilesTrainAndTagTest(POSTagger tagger, String setName) {
        String path = "PS5/"; // hard-code path for text files
        String trainingSentencesFileName = path + setName + "-train-sentences.txt";
        String trainingTagsFileName = path + setName + "-train-tags.txt";
        String testingSentencesFileName = path + setName + "-test-sentences.txt";
        String testingTagsFileName = path + setName + "-test-tags.txt";

        tagger.fileTraining(trainingSentencesFileName, trainingTagsFileName);
        fileTest(tagger, testingSentencesFileName, testingTagsFileName);
    }

    /**
     * Run a trained tagger on sentences given in the console
     */
    public static void consoleTest(POSTagger tagger) {
        Scanner input = new Scanner(System.in);
        System.out.print(
                """ 
                Console Test Formatting Guide:
                1. Enter a space between every element of the sentence (words, punctuation, etc.)
                2. Capitalization does not matter
                
                Enter a sentence >> 
                """
        );
        String sentence = input.nextLine();
        String tags = tagger.tagViterbi(sentence);
        System.out.println("Tagged Sentence: " + tags + "\n");
        System.out.print("Try another sentence? Enter y for yes or n for no >> ");
        String choice = input.nextLine();
        if (choice.charAt(0) == 'y') consoleTest(tagger);
    }

    /**
     * Trains with example files and checks for any errors in training process.
     */
    private static void exampleTrainTest(POSTagger tagger) {
        // relies on built-in processing information supplied in POSTagger methods
        tagger.setDEBUG(true);
        tagger.fileTraining("PS5/example-sentences.txt", "PS5/example-tags.txt");
        tagger.setDEBUG(false);

        // TODO: do these if time
        // hard-code the expected map
        // compare the method-made maps to hard-coded maps
    }

    /**
     * Test a trained tagger to confirm that the algorithm for tagging is fully functioning.
     * Hard-coded
     */
    public static void manualTaggingTest(POSTagger tagger) {
        tagger.setDEBUG(true); // relies on built-in processing information supplied in POSTagger methods

        // properly formatted sentence testing
        String firstResult = tagger.tagViterbi("I chase the dog ."); // simple sentence
        String secondResult = tagger.tagViterbi("While we watch , you chase the dog and cat ."); // complicated sentence

        System.out.println("I chase the dog .");
        System.out.println("Computed: " + firstResult);
        System.out.println("Expected: N V CNJ N .");
        System.out.println("Correct: PRO V DET N .");
        System.out.println("Accuracy: " + taggingAccuracy(firstResult, "PRO V DET N .") + "\n");
        System.out.println("While we watch , you chase the dog and cat .");
        System.out.println("Computed: " + secondResult);
        System.out.println("Expected: N CNJ V NP V NP V N CNJ N .");
        System.out.println("Correct: WH PRO V , PRO V DET N CNJ N .");
        System.out.println("Accuracy: " + taggingAccuracy(secondResult, "WH PRO V , PRO V DET N CNJ N .") + "\n");

        tagger.setDEBUG(false); // turn off DEBUG information in case of other testing on this tagger
    }

    /**
     * Trains a POSTagger with hard-coded maps (from programming drill)
     */
    public static void manualTrain(POSTagger tagger) {
        // create trained observation probabilities map
        Map<String, Map<String, Double>> observations = new HashMap<>();
        Map<String, Double> stateObservations;
        // create a map of a state's observations
        stateObservations = new HashMap<>(); // NP
        stateObservations.put("chase", 10.);
        // create that state with its map
        observations.put("NP", stateObservations);
        // repeat for N
        stateObservations = new HashMap<>();
        stateObservations.put("cat", 4.);
        stateObservations.put("dog", 4.);
        stateObservations.put("watch", 2.);
        observations.put("N", stateObservations);
        // CNJ
        stateObservations = new HashMap<>();
        stateObservations.put("and", 10.);
        observations.put("CNJ", stateObservations);
        // V
        stateObservations = new HashMap<>();
        stateObservations.put("get", 1.);
        stateObservations.put("chase", 3.);
        stateObservations.put("watch", 6.);
        observations.put("V", stateObservations);

        // create trained transitions probabilities map
        Map<String, Map<String, Double>> transitions = new HashMap<>();
        Map<String, Double> stateTransitions;
        // create a map of state's transitions
        stateTransitions = new HashMap<>();
        stateTransitions.put("NP", 3.);
        stateTransitions.put("N", 7.);
        // create that state with its map
        transitions.put("#", stateTransitions);
        // repeat for NP
        stateTransitions = new HashMap<>();
        stateTransitions.put("V", 8.);
        stateTransitions.put("CNJ", 2.);
        transitions.put("NP", stateTransitions);
        // N
        stateTransitions = new HashMap<>();
        stateTransitions.put("V", 8.);
        stateTransitions.put("CNJ", 2.);
        transitions.put("N", stateTransitions);
        // CNJ
        stateTransitions = new HashMap<>();
        stateTransitions.put("V", 4.);
        stateTransitions.put("NP", 2.);
        stateTransitions.put("N", 4.);
        transitions.put("CNJ", stateTransitions);
        // V
        stateTransitions = new HashMap<>();
        stateTransitions.put("CNJ", 2.);
        stateTransitions.put("NP", 4.);
        stateTransitions.put("N", 4.);
        transitions.put("V", stateTransitions);

        // manually set trained maps to "train" the tagger
        tagger.setObservationsProbabilities(observations);
        tagger.setTransitionsProbabilities(transitions);
    }
}
