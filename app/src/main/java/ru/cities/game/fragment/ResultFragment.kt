package ru.cities.game.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.cities.game.R
import ru.cities.game.databinding.ResultFragmentBinding
import ru.cities.game.util.*

class ResultFragment : Fragment(R.layout.result_fragment) {

    private var _binding: ResultFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setStatusBar(requireActivity(), 8, R.color.white, true)
        _binding = ResultFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val score = args.get(SCORE_ID)
        val isVictory = args.get(IS_VICTORY_ID) as Boolean
        binding.tvScore.text = getString(R.string.score, score)
        val prefs = requireActivity().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        binding.tvRecord.text = getString(R.string.record, prefs.getInt(KEY_SCORE, 0))
        if (isVictory) {
            binding.tvTitle.text = getString(R.string.you_won)
            binding.ivWinnerIcon.setImageResource(R.drawable.ic_user_won)
        }
        binding.btnReplay.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment()).commit()
        }
    }
}
