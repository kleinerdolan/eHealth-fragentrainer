package com.example.ehafraginator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    //alle fragen einlesen
    //2 textfelder: oben Fragen, unten antwort

    private Boolean answerIsShown = false;
    Button buttonContinue;
    Button buttonMark;
    TextView textAnswer;
    TextView textQuestion;
    ArrayList<String> questions = new ArrayList<>();
    ArrayList<String> answers = new ArrayList<>();
    ArrayList<Integer> usedQuestions = new ArrayList<>();
    int currentQuestion = 0;
    boolean marked = false;

    int defaultQuestions;
    int julianQuestions;
    int floQuestions;
    int zusatzQuestions;


    final boolean[] activeSelection = new boolean[]{
            true,   //Zusatz
            true,   //Julian
            true,   //Flo

    };

    final boolean[] selected = new boolean[]{
            true,   //Zusatz
            true,   //Julian
            true,   //Flo

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initElements();
        importQuestionsAndAnswers();

        //set first question by hand

        Random random = new Random();
            currentQuestion = random.nextInt(questions.size() - 1);
        textQuestion.setText(questions.get(currentQuestion));
        updateHeader();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                break;
            }
            case R.id.settings: {
                showSettingsDialog();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    public void initElements() {
        buttonContinue = findViewById(R.id.buttonNext);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(answerIsShown) {
                    nextQuestion();
                } else {
                    showAnswer();
                }
            }
        });

        buttonMark = findViewById(R.id.buttonMark);

        buttonMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marked = true;
            }
        });

        textAnswer = findViewById(R.id.textViewAnswer);
        textQuestion = findViewById(R.id.textViewQuestion);

        textAnswer.setMovementMethod(new ScrollingMovementMethod());
        textQuestion.setMovementMethod(new ScrollingMovementMethod());

    }


    /**
     * Importiert die Fragen und Antworten aus einer textdatei und speichert diese seperat in den string arrays questions und answers
     * Fragen ohne Zusatz sind unsere Fragen, Z steht für Zusatz aus den Übungen, J steht für Julians Fragen
     */
    public void importQuestionsAndAnswers() {
        //durch gesamtes file laufen
        //anzahl geöffnete { merken
        //auf level 1 -> text-zeile = frage
        //auf level 2 -> antwort, bis } kommt
        //fragen hochzählen, um array index zu haben
        // while(inQuestion) alle zeilen in answers[cur].append
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("questions.txt")));

            String curLine;
            int curQuestion = 0;
            int julianFragen = 0;
            int zusatzFragen = 0;
            int floFragen = 0;
            boolean inQuestion = false;
            while ((curLine = reader.readLine()) != null) {

                //leere Zeilen und #-Zeilen überspringen
                if(curLine.trim().isEmpty() || curLine.contains("#")) {
                    continue;
                }

                if(curLine.contains("{")) {
                    //Overall opening brackets
                    if(curLine.startsWith("{")) {
                        continue;
                    }
                    if(!inQuestion) {
                        inQuestion = true;
                    }

                } else if(curLine.contains("}")) {
                    if(inQuestion) {
                        inQuestion = false;
                    }
                } else {
                    //reine text-zeile
                    if(!inQuestion) {
                        //Frage
//                        System.out.println("FRAGE: " + curLine);


                        Pattern pZusatz = Pattern.compile("\\s+Z");
                        Pattern pJulian = Pattern.compile("\\s+J");
                        Pattern pFlo = Pattern.compile("\\s+F");

                        Matcher mZusatz = pZusatz.matcher(curLine);
                        if (mZusatz.find()) {
                            zusatzFragen++;
                        }

                        Matcher mJulian = pJulian.matcher(curLine);
                        if (mJulian.find()) {
                            julianFragen++;
                        }

                        Matcher mFlo = pFlo.matcher(curLine);
                        if (mFlo.find()) {
                            floFragen++;
                        }

                        questions.add(curLine);
                        curQuestion++;
                    } else {
                        //Antwort
//                        System.out.println("ANTWORT: " + curLine);
                        if(answers.size() == curQuestion) {
                            String old = answers.get(curQuestion-1);
                            answers.set(curQuestion - 1, old + "\n\n" + curLine);
                        } else {
                            answers.add(curLine);
                        }
                    }
                }

            }

            System.out.println("Insgesamt: " + questions.size());
            System.out.println("Julianfragen: " + julianFragen);
            System.out.println("Zusatzfragen: " + zusatzFragen);
            System.out.println("Flofragen: " + floFragen);
            julianQuestions = julianFragen;
            zusatzQuestions = zusatzFragen;
            floQuestions = floFragen;
            defaultQuestions = questions.size() - julianQuestions - zusatzQuestions - floQuestions;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Zeigt die Antwort der aktuellen Frage an
     */
    public void showAnswer() {
        answerIsShown = true;
        textAnswer.setText(answers.get(currentQuestion));
    }

    /**
     * Lädt die nächste Frage
     */
    public void nextQuestion() {
        answerIsShown = false;
        //delete old answer
        textAnswer.setText("");

        //zufallszahl! aber merken, welche frage schon dran kam
        if(marked) {
            marked = false;
        } else {
            usedQuestions.add(currentQuestion);
        }
        Random random = new Random();
        do {
            currentQuestion = random.nextInt(questions.size() - 1);
        }
        while(usedQuestions.contains(currentQuestion) || !checkQuestionConformsSelection(questions.get(currentQuestion)));

        textQuestion.setText(questions.get(currentQuestion));
        updateHeader();
    }

    public boolean checkQuestionConformsSelection(String currentQuestion) {

        if(!activeSelection[0] && !activeSelection[1] && !activeSelection[2]) {
            return !checkQuestionIsZusatz(currentQuestion) && !checkQuestionIsJulian(currentQuestion) && !checkQuestionIsFlo(currentQuestion);
        }

        if(!activeSelection[0] && !activeSelection[2]) {
            return !checkQuestionIsZusatz(currentQuestion) && !checkQuestionIsFlo(currentQuestion);
        }

        if(!activeSelection[0] && !activeSelection[1]) {
            return !checkQuestionIsZusatz(currentQuestion) && !checkQuestionIsJulian(currentQuestion);
        }

        if(!activeSelection[1] && !activeSelection[2]) {
            return !checkQuestionIsJulian(currentQuestion) && !checkQuestionIsFlo(currentQuestion);
        }

        //Zusatz deaktiviert
        if(!activeSelection[0]) {
            return !checkQuestionIsZusatz(currentQuestion);
        }
        //Julian deaktiviert
        if(!activeSelection[1]) {
            return !checkQuestionIsJulian(currentQuestion);
        }
        //Flo deaktiviert
        if(!activeSelection[2]) {
            return !checkQuestionIsFlo(currentQuestion);
        }
        return true;
    }

    public boolean checkQuestionIsZusatz(String currentQuestion) {
        boolean isZusatz = false;

        Pattern pZusatz = Pattern.compile("\\s+Z");


        Matcher mZusatz = pZusatz.matcher(currentQuestion);
        if (mZusatz.find()) {
            isZusatz = true;
        }
        return isZusatz;
    }

    public boolean checkQuestionIsJulian(String currentQuestion) {
        boolean isJulian = false;

        Pattern pJulian = Pattern.compile("\\s+J");


        Matcher mJulian = pJulian.matcher(currentQuestion);
        if (mJulian.find()) {
            isJulian = true;
        }
        return isJulian;
    }

    public boolean checkQuestionIsFlo(String currentQuestion) {
        boolean isFlo = false;

        Pattern pFlo = Pattern.compile("\\s+F");


        Matcher mFlo = pFlo.matcher(currentQuestion);
        if (mFlo.find()) {
            isFlo = true;
        }
        return isFlo;
    }


    public void showSettingsDialog() {
        final String[] items = {"Zusatzfragen", "Julianfragen", "Flofragen"};


        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


        builder.setTitle("Einstellungen");
        builder.setMultiChoiceItems(items, selected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (selected[which] && !activeSelection[which]) {
                    activeSelection[which] = selected[which];
                } else if (!selected[which] && activeSelection[which]) {
                    activeSelection[which] = selected[which];
                }
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                updateHeader();
                dialog.dismiss();
            }
        });

        AlertDialog settingsDialog = builder.create();

        settingsDialog.show();
    }


    public void updateHeader() {
        int unansweredQuestions = defaultQuestions;
        if(activeSelection[0]) {
            unansweredQuestions += zusatzQuestions;
        }
        if(activeSelection[1]) {
            unansweredQuestions += julianQuestions;
        }
        if(activeSelection[2]) {
            unansweredQuestions += floQuestions;
        }

        unansweredQuestions -= usedQuestions.size();

        setTitle(getString(R.string.app_name) + " (noch " + unansweredQuestions + " Fragen)");
    }


}