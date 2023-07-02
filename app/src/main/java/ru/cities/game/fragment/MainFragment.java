package ru.cities.game.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.cities.game.R;
import ru.cities.game.adapter.City;
import ru.cities.game.adapter.CityAdapter;
import ru.cities.game.db.DatabaseHelper;

public class MainFragment extends Fragment {
    private final boolean TEXT_FOUND = false;
    private final boolean TEXT_NOT_FOUND = true;
    private final List<String> calledNameCities = new ArrayList<>();
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initSpeechRecognizer();
                    startListening();
                } else
                    showMessage(getString(R.string.request_rationale));
            });
    private int level;
    private int score = 0;
    private int booster = 1;
    private int increase = 0;
    private boolean lampOff = true;
    private SharedPreferences prefs;
    private CountDownTimer timer;
    private Context context;
    private TextInputEditText etText;
    private TextInputLayout lText;
    private TextView tvScore, tvTimer;
    private ImageView ivGetHint;
    private RecyclerView rvList;
    private City botCity;
    private CityAdapter adapter;
    private DatabaseHelper databaseHelper;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private AlertDialog alertDialog;

    public MainFragment() {
        super(R.layout.main_fragment);
    }

    private void initViews() {
        View view = getView();
        assert view != null;
        etText = view.findViewById(R.id.et_text_name);
        tvScore = view.findViewById(R.id.tv_score);
        tvTimer = view.findViewById(R.id.tv_timer);
        ivGetHint = view.findViewById(R.id.iv_get_hint);
        lText = view.findViewById(R.id.l_text_name);
        rvList = view.findViewById(R.id.rv_list);
        adapter = new CityAdapter(city -> showMessage(databaseHelper.getCityInfo(city)));
        rvList.setAdapter(adapter);
        rvList.setLayoutManager(new LinearLayoutManager(context));
    }

    private void initClickListeners() {
        ivGetHint.setOnClickListener(showHintUsageDialog);
        lText.setEndIconOnClickListener(sendSpeakText);
        etText.addTextChangedListener(setTextWatcher());
        etText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                lText.findViewById(com.google.android.material.R.id.text_input_end_icon).performClick();
                return true;
            }
            return false;
        });
    }

    private final View.OnClickListener sendSpeakText = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            else if (isNull(speechRecognizer)) {
                initSpeechRecognizer();
                startListening();
            } else startListening();
        }
    };

    private void startListening() {
        lText.setEndIconDrawable(R.drawable.ic_mic_on);
        speechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    private void initSpeechRecognizer() {
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rms) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                lText.setEndIconDrawable(R.drawable.ic_mic_off);
            }

            @Override
            public void onError(int error) {
                lText.setEndIconDrawable(R.drawable.ic_mic_off);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (!isNull(result)) {
                    String text = result.get(0);
                    if (text.length() < 26) {
                        etText.setText(text);
                        etText.setSelection(text.length());
                        checkInput();
                    } else showMessage(getString(R.string.speech_recognition_error));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        final String key = "firstRun";
        if (prefs.getBoolean(key, true)) {
            timer.cancel();
            new MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.rules_title))
                    .setMessage(getString(R.string.rules_message))
                    .setPositiveButton(getString(R.string.rules_ok), (dialog, which) ->
                            new MaterialAlertDialogBuilder(context)
                                    .setTitle(getString(R.string.purpose_title))
                                    .setMessage(getString(R.string.purpose_message))
                                    .setPositiveButton(getString(R.string.purpose_ok), null)
                                    .show())
                    .show();
            prefs.edit().putBoolean(key, false).apply();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS)
                textToSpeech.setLanguage(new Locale("ru"));
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        level = prefs.getInt("level", 2);
        initViews();
        initClickListeners();
        initTimer();
        timer.start();
    }

    @Override
    public void onPause() {
        if (score > prefs.getInt("record", 0))
            prefs.edit().putInt("record", score).apply();
        if (!isNull(speechRecognizer))
            speechRecognizer.cancel();
        if (!isNull(textToSpeech))
            textToSpeech.stop();
        super.onPause();
    }

    private TextWatcher setTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(etText.getText())) {
                    lText.setEndIconOnClickListener(sendSpeakText);
                    lText.setEndIconDrawable(R.drawable.ic_mic_off);
                } else {
                    lText.setEndIconOnClickListener(sendWriteText);
                    lText.setEndIconDrawable(R.drawable.ic_send);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    private final View.OnClickListener sendWriteText = view -> checkInput();

    private void checkInput() {
        String text = getValidText();
        if (isNull(text)) {
            showMessage(getString(R.string.enter_existing_city));
            return;
        }
        char firstChar = text.charAt(0);
        if (botCity != null) {
            char lastChar = botCity.getLastChar();
            if (firstChar != lastChar) {
                showMessage(getString(R.string.city_must_begin_with_letter, lastChar));
                return;
            }
        }
        if (canFindCity(firstChar, text))
            if (canFindCountry(text))
                if (canFindCityWithMistake(firstChar, text))
                    showMessage(getString(R.string.i_do_not_know_such_city));
    }

    @Nullable
    private String getValidText() {
        Editable content = etText.getText();
        if (isNull(content))
            return null;
        String text = content.toString().trim();
        if (text.isEmpty())
            return null;
        char first = text.charAt(0),
                last = text.charAt(text.length() - 1);
        if (first == 'ё' || first == 'ë' ||
                first == 'ь' || first == 'ы' ||
                first == '-' || last == '-')
            return null;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private boolean canFindCity(char firstChar, String text) {
        City userCity = databaseHelper.getCity(firstChar, text);
        if (isNull(userCity))
            return TEXT_NOT_FOUND;
        finishAdding(userCity);
        return TEXT_FOUND;
    }

    private boolean canFindCountry(String text) {
        String description = databaseHelper.getDescription(text);
        if (isNull(description))
            return TEXT_NOT_FOUND;
        showMessage(getString(R.string.is_state, text, description));
        return TEXT_FOUND;
    }

    private boolean canFindCityWithMistake(char firstChar, String text) {
        City userCity = databaseHelper.getCityWithMistake(firstChar, text);
        if (isNull(userCity))
            return TEXT_NOT_FOUND;
        finishAdding(userCity);
        return TEXT_FOUND;
    }

    private boolean canFindCalledCity(City userCity) {
        String cityName = userCity.getName();
        if (calledNameCities.contains(cityName)) {
            showMessage(getString(R.string.city_has_already_been, cityName));
            return TEXT_FOUND;
        }
        return TEXT_NOT_FOUND;
    }

    private void addPoints() {
        if (booster < increase) {
            booster *= 2;
            increase = 0;
        }
        score += booster;
        ++increase;
        tvScore.setText(String.valueOf(score));
    }

    private void finishAdding(City userCity) {
        if (canFindCalledCity(userCity)) {
            etText.setText("");
            showCity(userCity);
            addPoints();
            botSendCity(userCity);
            playSound(R.raw.send_msg);
        }
    }

    private void botSendCity(City userCity) {
        if (booster > level)
            botSurrender();
        int i = 0;
        do {
            botCity = databaseHelper.getRandomCity(userCity.getLastChar());
            if (++i > 10) botSurrender();
        } while (calledNameCities.contains(botCity.getName()));
        botCity.setType(!userCity.getType());
        showCity(botCity);
        if (!lampOff)
            switchLamp();
        if (userCity.getType())
            textToSpeech.speak(botCity.getName(), TextToSpeech.QUEUE_FLUSH, null, null);
        timer.start();
    }

    private void showCity(City city) {
        adapter.insertMessage(city);
        rvList.scrollToPosition(adapter.getItemCount() - 1);
        calledNameCities.add(city.getName());
    }

    private void botSurrender() {
        prefs.edit().putInt("level", level * 2).apply();
        prefs.edit().putBoolean("isVictory", true).apply();
        openResultFragment();
    }

    private final View.OnClickListener showHintUsageDialog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            alertDialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.using_hint))
                    .setMessage(getString(R.string.spend_stars_on_hint, booster))
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        if (score >= booster) {//checking whether the user has enough points
                            score -= booster;
                            tvScore.setText(String.valueOf(score));
                            botCity.setType(false);
                            botSendCity(botCity);
                            botSendCity(botCity);
                        } else
                            showMessage(getString(R.string.missing_stars, booster - score));
                    })
                    .setNegativeButton(getString(R.string.no), null).create();
            alertDialog.show();
        }
    };

    private void playSound(int soundId) {
        MediaPlayer.create(context, soundId).start();
    }

    private void showMessage(String msg) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).setAnchorView(lText).show();
    }

    private boolean isNull(Object o) {
        return o == null;
    }

    private void initTimer() {
        timer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(getString(R.string.seconds, seconds));
                if (seconds < 30 && score >= booster && lampOff)
                    switchLamp();
            }

            public void onFinish() {
                prefs.edit().putBoolean("isVictory", false).apply();
                openResultFragment();
            }
        };
    }

    private void switchLamp() {
        if (lampOff)
            ivGetHint.setImageResource(R.drawable.ic_lamp_on);
        else
            ivGetHint.setImageResource(R.drawable.ic_lamp_off);
        lampOff = !lampOff;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    private void openResultFragment() {
        if (!isNull(alertDialog))
            alertDialog.dismiss();
        prefs.edit().putInt("score", score).apply();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ResultFragment.class, null)
                .commitAllowingStateLoss();
    }
}
