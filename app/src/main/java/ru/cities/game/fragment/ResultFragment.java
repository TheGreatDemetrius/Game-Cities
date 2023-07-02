package ru.cities.game.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.cities.game.R;

public class ResultFragment extends Fragment {
    private Button btnReplay;
    private TextView tvScore, tvRecord, tvTitle;
    private ImageView ivWinnerIcon;
    private int resId = R.raw.defeat;

    public ResultFragment() {
        super(R.layout.result_fragment);
    }

    private void initViews() {
        View view = getView();
        assert view != null;
        tvScore = view.findViewById(R.id.tv_score);
        tvRecord = view.findViewById(R.id.tv_record);
        tvTitle = view.findViewById(R.id.tv_title);
        ivWinnerIcon = view.findViewById(R.id.iv_winner_icon);
        btnReplay = view.findViewById(R.id.btn_replay);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        SharedPreferences prefs = requireActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        tvScore.setText(getString(R.string.score, prefs.getInt("score", 0)));
        tvRecord.setText(getString(R.string.record, prefs.getInt("record", 0)));
        btnReplay.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MainFragment.class, null)
                .commit());
        if (prefs.getBoolean("isVictory", false)) {
            resId = R.raw.victory;
            tvTitle.setText(getString(R.string.you_won));
            ivWinnerIcon.setImageResource(R.drawable.ic_user_won);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaPlayer.create(requireActivity(), resId).start();
    }
}
