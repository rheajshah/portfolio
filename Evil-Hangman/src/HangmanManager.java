/*  Student information for assignment:
 *
 *  On my honor, Rhea Shah, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Name: Rhea Shah
 *  email address: rheajshah@utexas.edu
 *  UTEID: rjs4665
 *  Section 5 digit ID: 52590
 *  Grader name: Emma Simon
 *  Number of slip days used on this assignment: 0
 */

import java.util.*;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible words from a dictionary during
 * rounds of hangman, based on guesses so far.
 */
public class HangmanManager {
    // instance variables / fields
    Set<String> dictionary; //original dictionary
    Set<String> currViableWords; //pruned dict containing words that can still become secret word
    int wordLength;
    int guessesMade;
    int wrongGuessesLeft;
    ArrayList<Character> guesses; //letters guessed so far
    String secretPattern; //constantly updated to contain hardest (or 2nd hardest) pattern
    HangmanDifficulty difficulty;
    boolean deBugOn;

    /**
     * Manages the frequency of a particular word. This class contains
     * methods to compare which word occurs more often and break ties.
     */
    private class WordFrequency implements Comparable<WordFrequency> {
        String word;
        int frequency;

        WordFrequency(String word, int frequency) {
            if (word == null)
                throw new NullPointerException();
            this.word = word;
            this.frequency = frequency;
        }

        /**
         * This method achieves the desired sorting behavior for Evil Hangman game
         */
        @Override
        public int compareTo(WordFrequency o) {
            int iReturn = 1;
            if (this.frequency > o.frequency) {
                iReturn = -1;
            } else if (this.frequency == o.frequency) { //ties
                //Check who reveals less characters
                int myDashes = getNumDashes(this.word);
                int otherDashes = getNumDashes(o.word);
                if (myDashes != otherDashes) {
                    iReturn = -(myDashes - otherDashes);
                } else {
                    iReturn = this.word.compareTo(o.word);
                }
            }
            return iReturn;
        }

        /**
         * Returns number of dash characters in a String
         *
         * @param key a String containing a word pattern. Ex. "c--l"
         *            pre: key != null and key.length() >= 0
         * @return The number of dashes in key
         */
        private int getNumDashes(String key) {
            if (key == null || key.length() <= 0) { //check precons
                throw new IllegalArgumentException("key cannot be null and must have " +
                        "1+ characters.");
            }
            int numDashes = 0;
            char[] chars = key.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '-') {
                    numDashes++;
                }
            }
            return numDashes;
        }
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     *
     * @param words   A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> words, boolean debugOn) {
        if (words == null || words.size() <= 0) { //check precons
            throw new IllegalArgumentException("words cannot be null and " +
                    "must contain 1+ elements");
        }
        dictionary = words;
        guesses = new ArrayList<>();
        this.deBugOn = debugOn;
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * Debugging is off.
     * pre: words != null, words.size() > 0
     *
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        this(words, false); //call super, set debug to false
    }


    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     *
     * @param length The given length to check.
     * @return the number of words in the original Dictionary
     * with the given length
     */
    public int numWords(int length) {
        int numWords = 0;
        for (String word : dictionary) {
            if (word.length() == length) {
                numWords++;
            }
        }
        return numWords;
    }


    /**
     * Get for a new round of Hangman. Think of a round as a
     * complete game of Hangman.
     *
     * @param wordLen    the length of the word to pick this time.
     *                   numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the
     *                   player loses the round. numGuesses >= 1
     * @param diff       The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        if (wordLen <= 0 || numGuesses < 1) { //check precons
            throw new IllegalArgumentException("word length must be at least 1 and number of" +
                    "guesses allowed must be greater than or equal to 1.");
        }
        wordLength = wordLen;
        wrongGuessesLeft = numGuesses; //initialize wrongGuessesLeft to numGuesses @ round's start
        difficulty = diff;
        guessesMade = 0; //curr guesses made are 0 (increment var in makeGuess())
        this.guesses = new ArrayList<>();

        currViableWords = new HashSet<>();
        for (String word : dictionary) {
            if (word.length() == wordLen) {
                //only add word to viable guesses if it is the correct length
                currViableWords.add(word);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordLen; i++) {
            sb.append('-');
        }
        secretPattern = sb.toString(); //sets initial pattern to '-' (times wordLen)
    }


    /**
     * The number of words still possible (live) based on the guesses so far.
     * Guesses will eliminate possible words.
     *
     * @return the number of words that are still possibilities based on the
     * original dictionary and the guesses so far.
     */
    public int numWordsCurrent() {
        return currViableWords.size();
    }


    /**
     * Get the number of wrong guesses the user has left in
     * this round (game) of Hangman.
     *
     * @return the number of wrong guesses the user has left
     * in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return wrongGuessesLeft;
    }


    /**
     * Return a String that contains the letters the user has guessed
     * so far during this round.
     * The characters in the String are in alphabetical order.
     * The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     *
     * @return a String that contains the letters the user
     * has guessed so far during this round.
     */
    public String getGuessesMade() {
        Collections.sort(guesses); //alpha order
        return (guesses.toString());
    }


    /**
     * Check the status of a character.
     *
     * @param guess The character to check.
     * @return true if guess has been used or guessed this round of Hangman,
     * false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        return guesses.contains(guess);
    }


    /**
     * Get the current pattern. The pattern contains '-''s for
     * unrevealed (or guessed) characters and the actual character
     * for "correctly guessed" characters.
     *
     * @return the current pattern.
     */
    public String getPattern() {
        return secretPattern;
    }


    /**
     * Update the game status (pattern, wrong guesses, word list),
     * based on the give guess.
     *
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     * words in each of the new patterns.
     * The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {
        if (alreadyGuessed(guess)) { //check precon
            throw new IllegalArgumentException("This letter has already been guessed.");
        }

        TreeMap<String, Integer> patternAndCount = new TreeMap<>();
        Map<String, ArrayList<String>> patternAndWords = new TreeMap<>();
        guessesMade++; //increment guesses made
        guesses.add(guess);

        //Generate the new pattern maps based on the guess
        generateUpdatedPatternMaps(guess, patternAndCount, patternAndWords);

        String chosenPattern = "";
        if (difficulty == HangmanDifficulty.EASY && guessesMade % 2 == 0) {
            //2nd hardest pattern
            chosenPattern = getNextKeyForPattern(patternAndCount, 2);
        } else if (difficulty == HangmanDifficulty.MEDIUM && guessesMade % 4 == 0) {
            //2nd hardest pattern
            chosenPattern = getNextKeyForPattern(patternAndCount, 2);
        } else { //hardest pattern
            chosenPattern = getNextKeyForPattern(patternAndCount, 1);
        }

        if (secretPattern.equals(chosenPattern)) {
            wrongGuessesLeft--;
        }
        secretPattern = chosenPattern;
        updateCurrViableWords(patternAndWords);

        return patternAndCount;
    }

    /**
     * Helper method to populate the maps of (Pattern, Frequency) and (Pattern, Words)
     * based on the guess
     *
     * @param guess           character guessed by user
     * @param patternAndCount TreeMap with word patterns with frequencies
     * @param patternAndWords TreeMap with word patterns and words that fit the pattern
     */
    private void generateUpdatedPatternMaps(char guess, TreeMap<String, Integer> patternAndCount,
                                            Map<String, ArrayList<String>> patternAndWords) {
        for (String word : currViableWords) {
            //Find the pattern based on this guess.
            String currWordPattern = findPattern(word, guess);

            ArrayList<String> patternWords = patternAndWords.get(currWordPattern);
            if (patternWords == null) {
                patternWords = new ArrayList<>();
            }
            patternWords.add(word);
            patternAndWords.put(currWordPattern, patternWords);

            Integer currPatternFreq = patternAndCount.get(currWordPattern);
            if (currPatternFreq == null) {
                currPatternFreq = 0;
            }
            currPatternFreq++;
            patternAndCount.put(currWordPattern, currPatternFreq);
        }
    }

    /**
     * This method finds the pattern of the current word matching the guess
     *
     * @param word  word to be translated to a pattern
     * @param guess guessed character/letter
     * @return the pattern of the current word accounting for any places where the
     * guessed letter is present. Ex. "l" in "look" translates to "l---"
     */
    private String findPattern(String word, char guess) {
        String strReturn = getPattern();
        if (word.contains("" + guess)) { //only if guessed letter is present in word
            char[] charWord = word.toLowerCase().toCharArray();
            //identify the matching pattern
            StringBuilder sbPattern = new StringBuilder("");
            for (int i = 0; i < word.length(); i++) {
                if (charWord[i] == guess) {
                    sbPattern.append(guess);
                } else {
                    sbPattern.append(strReturn.charAt(i)); //either a dash or pre-guessed letter
                }
            }
            strReturn = sbPattern.toString();
        }
        return strReturn;
    }


    /**
     * Update currViableWords with the words that fit the secret pattern.
     *
     * @param patternAndWords Map of all word families for a given character guess
     *                        and resulting patterns.
     *                        pre: patternAndWords != null
     */
    private void updateCurrViableWords(Map<String, ArrayList<String>> patternAndWords) {
        if (patternAndWords == null) { //check precon
            throw new IllegalArgumentException("patternAndWords cannot be null.");
        }
        HashSet<String> updatedCurrViableWords = new HashSet<>();
        ArrayList<String> words = patternAndWords.get(secretPattern);
        updatedCurrViableWords.addAll(words);
        currViableWords = updatedCurrViableWords;
    }

    /**
     * Returns either the hardest or second hardest word pattern to be made as the secret
     * pattern for the next round. Uses the value of highLevel (1 or 2) to determine which
     * difficulty of a pattern needs to be returned.
     *
     * @param patternMap TreeMap with word patterns and the number of words that satisfy each
     *                   pattern
     * @param highLevel  = 1 for Highest and 2 for 2nd Highest
     * @return next hardest word pattern. If highLevel = 1, then returns the hardest pattern
     * and if highLevel = 2, then returns the second hardest pattern.
     */
    private String getNextKeyForPattern(TreeMap<String, Integer> patternMap, int highLevel) {
        ArrayList<WordFrequency> alWordPatterns = new ArrayList<>();
        for (Map.Entry mapElement : patternMap.entrySet()) {
            alWordPatterns.add(new WordFrequency((String) mapElement.getKey(),
                    (int) mapElement.getValue()));
        }
        Collections.sort(alWordPatterns);
        if (alWordPatterns.size() > 1) {
            return alWordPatterns.get(highLevel - 1).word;
        } else {
            return alWordPatterns.get(0).word;
        }
    }

    /**
     * Return the secret word this HangmanManager finally ended up
     * picking for this round.
     * If there are multiple possible words left one is selected at random.
     * <br> pre: numWordsCurrent() > 0
     *
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        if (numWordsCurrent() <= 0) { //check precons
            throw new IllegalArgumentException("numWordsCurrent must be greater than 0.");
        }
        if (numWordsCurrent() == 1) {
            return (String) (currViableWords.toArray())[0];
        } else { //if multiple possible words are left, select 1 randomly.
            int numWordsLeft = numWordsCurrent();
            return (String) (currViableWords.toArray())[(int) (Math.random() * numWordsLeft)];
        }
    }
}