package com.example.project4_sasanka;

import static com.example.project4_sasanka.Constants.ATTEMPTS_MAXED;
import static com.example.project4_sasanka.Constants.FIRST_STEP;
import static com.example.project4_sasanka.Constants.GENERATE_NUM;
import static com.example.project4_sasanka.Constants.NUMBER_OF_ROUNDS;
import static com.example.project4_sasanka.Constants.P1_RESULT;
import static com.example.project4_sasanka.Constants.P2_RESULT;
import static com.example.project4_sasanka.Constants.RECEIVE_RESPONSE;
import static com.example.project4_sasanka.Constants.SETPLAYER1_UI;
import static com.example.project4_sasanka.Constants.SETPLAYER2_UI;
import static com.example.project4_sasanka.Constants.SLEEP_TIME;
import static com.example.project4_sasanka.Constants.START_GUESS;
import static com.example.project4_sasanka.Constants.VALIDATE_GUESS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Handler player1Handler, player2Handler, mainHandler; /* Thread Handlers for 2 players and UI Thread*/
    Thread p1, p2; /* Threads for players*/
    Button startGame; /* Button to intiate the game*/
    TextView player1Val, player2Val; /* Text View to display the Secret Sequence of Player 1 and Player2 */
    TextView resultText; /*Text View to Display the result of the game*/
    ScrollView player1, player2; /*Scroll View to display all the guesses and responses*/
    String validateResponse1, validateResponse2;/* Variables to store the responses*/
    int p1SecSeq = 0;/* Player 1 secret sequence*/
    int p2SecSeq = 0;/* Player 2 secret sequence*/
    LinearLayout p1LinearLayout;/* Linear layout to hold all guess and response text views of player 1*/
    LinearLayout p2LinearLayout;/* Linear layout to hold all guess and response text views of player 2*/
    ArrayList<Integer> trackPlayer1Guesses = new ArrayList<>();/* List to track all the guesses of player1*/
    ArrayList<Integer> trackPlayer2Guesses = new ArrayList<>();/* List to track all the guesses of player*/
    ArrayList<Integer> p1Val = new ArrayList<>();/* List to store feedback values*/
    ArrayList<Integer> p2Val = new ArrayList<>();/* List to store feedback values*/
    Boolean p1GuessCorrect = false;/* Bool to identify if the guess is correct or not*/
    Boolean p2GuessCorrect = false;/* Bool to identify if the guess is correct or not*/
    int counter = NUMBER_OF_ROUNDS;/* Defines number of rounds the game has to continue*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* Intialising all the views to corresponding class members*/
        player1Val = findViewById(R.id.editText1);
        player2Val = findViewById(R.id.editText2);
        resultText = findViewById(R.id.resultText);
        startGame = findViewById(R.id.startGame);
        player1 = findViewById(R.id.scrollView1);
        player2 = findViewById(R.id.scrollView2);
        p1LinearLayout = findViewById(R.id.player1GuessContainer);
        p2LinearLayout = findViewById(R.id.player2GuessContainer);
        startGame.setOnClickListener(v -> initialiseGuessFourGame()); /* On Click of start game intialise the game*/
        /* Intialise the lists to all numbers*/
        for (int i = 0; i < 10; i++) {
            p1Val.add(i);
            p2Val.add(i);
        }
/*        if (savedInstanceState != null) {
            if (savedInstanceState.getInt("Clear") == 1) {
                *//*resetVals();*//*
            }
        }*/
        /* Define the main handler and its looper*/
        mainHandler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        /* First step would ask the Player 1 thread to generate a random number*/
                        case FIRST_STEP: {
                            Message message = player1Handler.obtainMessage(GENERATE_NUM);
                            player1Handler.sendMessage(message);
                            Log.i(TAG, "handleMessage: enter FIRST_STEP");
                            break;
                        }
                        /* The Major task of SETPLAYER1_UI is to maintain and update the Player 1 UI*/
                        case SETPLAYER1_UI: {
                            counter--; /* Decrement counter everytime we update the UI*/
                            if (counter > 0) {
                                /* If the player two yet to choose a number send message to player 2 handler to choose a number*/
                                if (p2SecSeq == 0) {
                                    if (player1Val.getText().length() == 0) {
                                        p1SecSeq = msg.arg1;
                                        player1Val.setText(String.valueOf(msg.arg1)); /* Set the player 1 choosen number on screen*/
                                    }
                                    Message message = player2Handler.obtainMessage(GENERATE_NUM);
                                    player2Handler.sendMessage(message);
                                } else {
                                    addViewToLayout(p1LinearLayout, validateResponse1); /* Add the player 1 guess to UI*/
                                    player1.fullScroll(View.FOCUS_DOWN); /* Scroll down to the last guess*/
                                    /* Check if the guess is correct or not*/
                                    if (p1GuessCorrect) {
                                        stopGame(); /* Stop the game if the guess is correct*/
                                        player2.fullScroll(View.FOCUS_DOWN);
                                        resultText.setText(P1_RESULT); /* Display the winner*/
                                    } else {
                                        /* Inform player 2 to start guessing*/
                                        Message message = player2Handler.obtainMessage(START_GUESS);
                                        player2Handler.sendMessage(message);
                                    }
                                }
                            } else {
                                stopGame();/* Stop the game if the counter exceeds*/
                                resultText.setText(ATTEMPTS_MAXED);
                                Log.i(TAG, "handleMessage: Exited with counter 0");
                            }
                            Log.i(TAG, "handleMessage: enter SETPLAYER1_UI");
                            break;
                        }
                        /* The Major task of SETPLAYER2_UI is to maintain and update the Player 2 UI*/
                        case SETPLAYER2_UI: {
                            counter--;/* Decrement counter everytime we update the UI*/
                            if (counter > 0) {
                                if (player2Val.getText().length() == 0) {
                                    p2SecSeq = msg.arg1;
                                    player2Val.setText(String.valueOf(msg.arg1)); /* Set value choosen by the player 2 on screen*/
                                } else {
                                    addViewToLayout(p2LinearLayout, validateResponse2);/* Add the player 2 guess to UI*/
                                    player2.fullScroll(View.FOCUS_DOWN);
                                }
                                /* Check if the guess is correct or not*/
                                if (p2GuessCorrect) {
                                    stopGame();/* Stop the game if the guess is correct*/
                                    player2.fullScroll(View.FOCUS_DOWN);
                                    resultText.setText(P2_RESULT);/* Display the winner*/
                                } else {
                                    /* Inform player 1 to start guessing*/
                                    Message message = player1Handler.obtainMessage(START_GUESS);
                                    player1Handler.sendMessage(message);
                                }
                            } else {
                                stopGame();/* Stop the game if the counter exceeds*/
                                resultText.setText(ATTEMPTS_MAXED);
                                Log.i(TAG, "handleMessage: enter exit counter in second ui thread");
                            }
                            Log.i(TAG, "handleMessage: enter SETPLAYER2_UI");
                        }
                        break;
                        default: {
                            Log.i(TAG, "handleMessage: Entered the default case");
                        }
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "handleMessage counterVal: " + counter);
            }
        };
    }

    /* Generate random numbers for Secret Sequences*/
    public int generateRandnumber() {
        int length = 4;
        ArrayList<Integer> val = new ArrayList<>();
        while (length > 0) {
            int digit = (int) (Math.random() * 10);
            if (digit == 0 && length == 4) continue;
            if (val.contains(digit)) {
                continue;
            }
            val.add(digit);
            length--;
        }
        String s = "";
        for (Integer integer : val) {
            s += integer.toString();
        }
        return Integer.parseInt(s);
    }

    /* Clear all the intialised values on Game end or restart game*/
    public void clearVals() {
        p1 = new Player1();
        p2 = new Player2();
        p1SecSeq = 0;
        p2SecSeq = 0;
        counter = NUMBER_OF_ROUNDS;
        p1GuessCorrect = false;
        p2GuessCorrect = false;
        p1LinearLayout.removeAllViews();
        p2LinearLayout.removeAllViews();
        player1Val.setText("");
        player2Val.setText("");
        resultText.setText("");
        p1Val.clear();
        p2Val.clear();
        trackPlayer1Guesses.clear();
        trackPlayer2Guesses.clear();
        for (int i = 0; i < 10; i++) {
            p1Val.add(i);
            p2Val.add(i);
        }
    }

    /* Method to stop game by destroying the threads and its handlers and loopers*/
    public void stopGame() {
        try {
            p1.interrupt();
            p2.interrupt();
            mainHandler.removeCallbacksAndMessages(null);/*Remove any pending posts of callbacks and sent messages */
            player1Handler.removeCallbacksAndMessages(null);/*Remove any pending posts of callbacks and sent messages */
            player2Handler.removeCallbacksAndMessages(null);/*Remove any pending posts of callbacks and sent messages */
            player1Handler.getLooper().quitSafely();/* Exit the player1 handler looper safely*/
            player2Handler.getLooper().quitSafely();/* Exit the player2 handler looper safely*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /* Method to stop game and reintialise all values*/
    public void resetVals() {
        stopGame();
        clearVals();
    }

    /*Intialising the game by defining the threads and starting them*/
    public void initialiseGuessFourGame() {
        if (p1 != null && p2 != null) {
            resetVals();
            Log.i(TAG, "initialiseGame: Hi I am here");
        }
        p1 = new Player1();
        p2 = new Player2();
        p1.start();
        p2.start();
    }

    /* Helper method to put a thread to sleep*/
    public void sleepHelper(Thread t) {
        try {
            t.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Method to make a guess of player2 sec seq*/
    public int guessPlayer2Val() {
        /* If the guess is not made, for the first time generate a random number*/
        if (trackPlayer1Guesses.isEmpty()) {
            int guess = generateRandnumber();
            trackPlayer1Guesses.add(guess); /* Add the randomly generated guess in Arraylist*/
            return guess;
        } else {
            /* Logic to guess the player2 value based on the feedback given*/
            Log.i(TAG, "guessPlayer2Val: use the feedback");
            String guess = "";
            if (p2Val.size() == 4) {
                while (true) {
                    Collections.shuffle(p2Val);
                    if (p2Val.get(0) == 0) {
                        continue;
                    }
                    for (int i = 0; i < p2Val.size(); i++) {
                        guess += String.valueOf(p2Val.get(i));
                    }
                    if (!trackPlayer1Guesses.contains(Integer.parseInt(guess)))
                        trackPlayer1Guesses.add(Integer.parseInt(guess));
                    if (Integer.parseInt(guess) == p2SecSeq) p1GuessCorrect = true;
                    return Integer.parseInt(guess);
                }
            } else {
                Collections.shuffle(p2Val);
                Log.i(TAG, "guessPlayer2Val: The size of p2Val" + String.valueOf(p2Val.size()));
                while (p2Val.get(0) == 0) {
                    Collections.shuffle(p2Val);
                }
                for (int i = 0; i < 4; i++) {
                    guess += String.valueOf(p2Val.get(i));
                }
                Log.i(TAG, "guessPlayer2Val: The size of p2Val" + guess);
                if (Integer.parseInt(guess) == p2SecSeq) p1GuessCorrect = true;
                return Integer.parseInt(guess);
            }
        }
    }

    /* Method to make a guess of player1 sec seq*/
    public int guessPlayer1Val() {
        /* If the guess is not made, for the first time generate a random number*/
        if (trackPlayer2Guesses.isEmpty()) {
            int guess = generateRandnumber();
            trackPlayer2Guesses.add(guess);/* Add the randomly generated guess in Arraylist*/
            return guess;
        } else {
            /* Logic to guess the player1 value based on the feedback given*/
            int digit1, digit2, digit3, digit4;
            while (true) {
                int rand = (int) (Math.random() * 10);
                if (p1Val.contains(rand)) {
                    digit1 = rand;
                    if (digit1 == 0) {
                        continue;
                    }
                    rand = (int) (Math.random() * 10);
                    if (p1Val.contains(rand)) {
                        digit2 = rand;
                        if (digit2 == digit1) continue;
                        rand = (int) (Math.random() * 10);
                        if (p1Val.contains(rand)) {
                            digit3 = rand;
                            if (digit3 == digit2 || digit3 == digit1) continue;
                            rand = (int) (Math.random() * 10);
                            if (p1Val.contains(rand)) {
                                digit4 = rand;
                                if (digit3 == digit4 || digit4 == digit2 || digit4 == digit1)
                                    continue;
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            Log.i(TAG, "guessPlayer1Val: use the feedback");
            int val = digit1 * 1000 + digit2 * 100 + digit3 * 10 + digit4;
            if (val == p1SecSeq) p2GuessCorrect = true;
            return val;
            /*Use the feedback*/
        }
    }
    /* Method to validate the guess made by player 1 with p2 secret sequence*/
    public String validatePlayer1Guess(int guessVal) {
        /*Have to validate with p2seqsec*/
        int numCorrectPos = 0;
        int numCorrectGuessWrongPos = 0;
        int numNotInSecSeq = -1;
        String seqSec = String.valueOf(p2SecSeq);
        String guess = String.valueOf(guessVal);
        Log.i(TAG, "validatePlayer1Guess:" + guess);
        Log.i(TAG, "validatePlayer1Guess: " + seqSec);
        HashMap<String, Integer> storeGuessVals = new HashMap<>();
        boolean doneFlag = false;
        for (int i = 0; i < seqSec.length(); i++) {
            if (seqSec.charAt(i) == guess.charAt(i)) {
                numCorrectPos++;
                storeGuessVals.put(String.valueOf(seqSec.charAt(i)), 1);
            }
            if (seqSec.indexOf(guess.charAt(i)) != -1) {
                numCorrectGuessWrongPos++;
            } else {
                if (!doneFlag) {
                    int i1 = Integer.parseInt(String.valueOf(guess.charAt(i)));
                    if (p2Val.contains(i1)) {
                        numNotInSecSeq = i1;
                        doneFlag = true;
                        p2Val.remove(Integer.valueOf(i1));
                    }
                }
            }
        }
        return "Guess Val: " + String.valueOf(guessVal) + "\nNumberOfGuessinCorrPos: " + String.valueOf(numCorrectPos)
                + "\nNumCorrGuessinWrongPos: " + String.valueOf(numCorrectGuessWrongPos - numCorrectPos) + "\nNumberNotInSeq: " + String.valueOf(numNotInSecSeq) + "\n";
    }
    /* Method to validate the guess made by player 2 with p1 secret sequence*/
    public String validatePlayer2Guess(int guessVal) {
        /*Have to validate with p1seqsec*/
        Log.i(TAG, "validatePlayer2Guess: hello");
        int numCorrectPos = 0;
        int numCorrectGuessWrongPos = 0;
        int numNotInSecSeq = -1;
        String seqSec = String.valueOf(p1SecSeq);
        String guess = String.valueOf(guessVal);
        Log.i(TAG, "validatePlayer2Guess:" + guess);
        Log.i(TAG, "validatePlayer2Guess: " + seqSec);
        boolean doneFlag = false;
        for (int i = 0; i < seqSec.length(); i++) {
            if (seqSec.charAt(i) == guess.charAt(i)) {
                numCorrectPos++;
            }
            if (seqSec.indexOf(guess.charAt(i)) != -1) {
                numCorrectGuessWrongPos++;
            } else {
                if (!doneFlag) {
                    int i1 = Integer.parseInt(String.valueOf(guess.charAt(i)));
                    if (p1Val.contains(Integer.valueOf(i1))) {
                        numNotInSecSeq = i1;
                        doneFlag = true;
                        p1Val.remove(Integer.valueOf(i1));
                    }
                }
            }
        }
        return "Guess Val: " + String.valueOf(guessVal) + "\nNumberOfGuessinCorrPos: " + String.valueOf(numCorrectPos)
                + "\nNumCorrGuessinWrongPos: " + String.valueOf(numCorrectGuessWrongPos - numCorrectPos) + "\nNumberNotInSeq: " + String.valueOf(numNotInSecSeq) + "\n";
    }

    /*Method to add Text View to Linear Layout*/
    @SuppressLint("SetTextI18n")
    public void addViewToLayout(LinearLayout layout, String responseVal) {
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView response = new TextView(this);
        response.setTextColor(ContextCompat.getColor(this, R.color.black));
        response.setLayoutParams(lparams);
        if (counter % 2 == 0) {
            response.setText("Round No:" + String.valueOf((int) Math.floor((NUMBER_OF_ROUNDS - counter - 2) / 2) + 1) + "\n" + responseVal);
        } else {
            response.setText("Round No:" + String.valueOf((int) Math.floor((NUMBER_OF_ROUNDS - counter - 3) / 2) + 1) + "\n" + responseVal);
        }
        layout.addView(response);
    }

    /* Player 1 class which defines the looper and handler of thread 1*/
    public class Player1 extends Thread {
        private static final String TAG = "Player1";

        @Override
        public void run() {
            super.run();
            /*sleepHelper(currentThread());*/
            Looper.prepare();
            player1Handler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    Log.i(TAG, "handleMessage: Enter here");
                    Message msgTobeSent;
                    this.post(() -> {
                        try {
                            Player1.sleep(SLEEP_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    switch (msg.what) {
                        /* Generate a secret sequence for player1 and send it to UI handler*/
                        case GENERATE_NUM: {
                            if (p1SecSeq == 0) {
                                msgTobeSent = mainHandler.obtainMessage(SETPLAYER1_UI);
                                msgTobeSent.arg1 = generateRandnumber();
                                mainHandler.sendMessage(msgTobeSent);
                            }
                            Log.i(TAG, "handleMessage: Entered GENERATE_NUM");
                        }
                        break;
                        /* Guess the player 2 secret sequence*/
                        case START_GUESS: {
                            msgTobeSent = player2Handler.obtainMessage(VALIDATE_GUESS);
                            msgTobeSent.arg1 = guessPlayer2Val();
                            player2Handler.sendMessage(msgTobeSent);
                            Log.i(TAG, "handleMessage: Entered START_GUESS");
                        }
                        break;
                        /* Validate the player2 guess val and send the reponse*/
                        case VALIDATE_GUESS: {
                            int guessVal = msg.arg1;
                            validateResponse2 = validatePlayer2Guess(guessVal);
                            msgTobeSent = player2Handler.obtainMessage(RECEIVE_RESPONSE);
                            player2Handler.sendMessage(msgTobeSent);
                            Log.i(TAG, "handleMessage: Entered VALIDATE_GUESS");
                        }
                        break;
                        /* Receive the validation response from player 2 and send it to UI to update*/
                        case RECEIVE_RESPONSE: {
                            msgTobeSent = mainHandler.obtainMessage(SETPLAYER1_UI);
                            mainHandler.sendMessage(msgTobeSent);
                            Log.i(TAG, "handleMessage: Entered RECEIVE_RESPONSE");
                        }
                        break;
                        default: {
                            Log.i(TAG, "handleMessage: Please Check the msg.what");
                        }
                    }
                }
            };
            /* Message the UI thread to start on the first instance*/
            Message message = mainHandler.obtainMessage(FIRST_STEP);
            mainHandler.sendMessage(message);
            Looper.loop();
        }
    }

    /* Player 2 class which defines the looper and handler of thread 2*/
    public class Player2 extends Thread {
        private static final String TAG = "Player2";

        @Override
        public void run() {
            super.run();
            /*sleepHelper(currentThread());*/
            Looper.prepare();
            player2Handler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    Log.i(TAG, "handleMessage: Enter here");
                    this.post(() -> {
                        try {
                            Player2.sleep(SLEEP_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    Message msgTobeSent;
                    switch (msg.what) {
                        /* Generate a secret sequence for player2 and send it to UI handler*/
                        case GENERATE_NUM: {
                            msgTobeSent = mainHandler.obtainMessage(SETPLAYER2_UI);
                            msgTobeSent.arg1 = generateRandnumber();
                            mainHandler.sendMessage(msgTobeSent);
                            Log.i(TAG, "handleMessage: Entered GENERATE_NUM");
                        }
                        break;
                        /* Guess the player 1 secret sequence*/
                        case START_GUESS: {
                            msgTobeSent = player1Handler.obtainMessage(VALIDATE_GUESS);
                            msgTobeSent.arg1 = guessPlayer1Val();
                            player1Handler.sendMessage(msgTobeSent);
                            Log.i(TAG, "handleMessage: Entered START_GUESS");
                        }
                        break;
                        /* Validate the player2 guess val and send the reponse*/
                        case VALIDATE_GUESS: {
                            int guessVal = msg.arg1;
                            Log.i(TAG, "handleMessage:" + String.valueOf(guessVal));
                            validateResponse1 = validatePlayer1Guess(guessVal);
                            msgTobeSent = player1Handler.obtainMessage(RECEIVE_RESPONSE);
                            player1Handler.sendMessage(msgTobeSent);
                            Log.i(TAG, "handleMessage: Entered VALIDATE_GUESS");
                        }
                        break;
                        /* Receive the validation response from player 1 and send it to UI to update*/
                        case RECEIVE_RESPONSE: {
                            msgTobeSent = mainHandler.obtainMessage(SETPLAYER2_UI);
                            mainHandler.sendMessage(msgTobeSent);
                            Log.i(TAG, "handleMessage: Entered RECEIVE_RESPONSE");
                        }
                        break;
                        default: {
                            Log.i(TAG, "handleMessage: Please Check the msg.what");
                        }
                    }
                }
            };
            Looper.loop();
        }
    }
    /* The following Interface explains the flow of execution*/
    /* FIRST_STEP ->
    P1-GENERATE_NUM ->
    SETPLAYER1_UI ->
    P2-GENERATE_NUM ->
    SETPLAYER2_UI ->
    P1-START_GUESS -> <----------------|
    P2-VALIDATE_GUESS ->               |
    P1-RECEIVE_RESPONSE ->             |
    SETPLAYER1_UI ->                   |
    P2-START_GUESS ->                  |
    P1-VALIDATE_GUESS ->               |
    P2-RECEIVE_RESPONSE ->             |
    SETPLAYER2_UI -> -------------------
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        /*resetVals();*/
    }
}