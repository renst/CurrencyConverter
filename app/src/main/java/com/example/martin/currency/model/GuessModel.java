package com.example.martin.currency.model;

import java.util.ArrayList;
import java.util.Random;

/**
 * An instance of this class represents a model for the game "guess my word". A
 * secret word is randomized from a private set of words. If a guess, a
 * character, matches any characters in the secret word the initial "*":s in
 * "guessSoFar" are substituted with that character.
 *
 * @author anderslm@kth.se
 */
public class GuessModel{



    private  static GuessModel model;

    public static GuessModel getInstance(){
        if(model == null)
            model = new GuessModel();
        return model;
    }

    public void addWords(ArrayList<String> words){
        wordArray = words;
    }

    private int noOfGuesses, noOfMatches;
    private char[] theWord, guessSoFar; // Java Strings are immutable
    private ArrayList<String> wordArray = new ArrayList<>();
    private static final Random rand = new Random();
    private static final char STAR = '*';


    private GuessModel() {
    }

    /**
     * Initialize the game for a new round.
     */
    public void reset() {
        noOfGuesses = 0;
        noOfMatches = 0;
        // Pick a random word
        int pos = rand.nextInt(1);
        theWord = wordArray.get(pos).toCharArray();
        // Create a char[] of equal length, filled with '*'
        int n = theWord.length;
        guessSoFar = new char[n];
        for (int i = 0; i < n; i++) {
            guessSoFar[i] = STAR;
        }
    }

    /**
     * Checks whether the guess is correct or not. If so, the guess so far and
     * the number of guesses are updated (repeated guesses on the same character
     * are counted).
     *
     * @param guess the guess
     * @return true if guess is a match, not previously used, false otherwise.
     */
    public boolean handleGuess(char guess) {
        noOfGuesses++;
        guess = Character.toUpperCase(guess);
        // Check whether theWord contains the guess or not (first time)
        int n = theWord.length;
        boolean match = false;
        for (int i = 0; i < n; i++) {
            if (guessSoFar[i] == STAR && theWord[i] == guess) {
                guessSoFar[i] = guess;
                noOfMatches++;
                match = true;
            }
        }
        return match;
    }

    /**
     * Returns whether all characters are correct or not.
     */
    public boolean isSolved() {
        return (noOfMatches == theWord.length);
    }

    /**
     * Returns the number of guesses made, including repeated guesses on the
     * same character.
     */
    public int getNoOfGuesses() {
        return noOfGuesses;
    }

    /**
     * Returns a string representing the guesses so far; unsolved positions are
     * marked with an asterisk (*).
     */
    public String getGuessSoFar() {
        return new String(guessSoFar);
    }

}