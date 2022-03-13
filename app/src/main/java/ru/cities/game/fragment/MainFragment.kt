package ru.cities.game.fragment

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.TooltipCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.internal.CheckableImageButton
import com.google.android.material.snackbar.Snackbar
import ru.cities.game.R
import ru.cities.game.adapter.*
import ru.cities.game.databinding.MainFragmentBinding
import ru.cities.game.db.DatabaseHelper
import ru.cities.game.util.*
import ru.cities.game.tts.Speech

class MainFragment : Fragment(R.layout.main_fragment) {

    private lateinit var prefs: SharedPreferences
    private lateinit var database: SQLiteDatabase
    private lateinit var adapter: MessageAdapter
    private lateinit var fragmentContext: Context
    private lateinit var fragmentActivity: FragmentActivity
    private lateinit var textToSpeech: Speech
    private var botCity: String = EMPTY_STRING
    private var level: Int = 1
    private var score: Int = 0
    private var booster: Int = 1
    private var increase: Int = 0
    private var _binding: MainFragmentBinding? = null
    private val binding get() : MainFragmentBinding = _binding!!
    private val calledCities: MutableList<String> = mutableListOf()

    /** Initialization context, activity, TTS, DB for the fast run */
    override fun onAttach(context: Context) {
        fragmentContext = requireContext()
        fragmentActivity = requireActivity()
        textToSpeech = Speech(fragmentContext)
        database = DatabaseHelper(fragmentContext).readableDatabase
        prefs = fragmentActivity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setStatusBar(fragmentActivity, 0, R.color.blue, false)
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Initialization RV, CL and start timer */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        level = prefs.getInt(KEY_LEVEL, 2)
        initRecyclerView()
        initClickListeners()
        timer.start()
    }

    /** Destroying the TTS */
    override fun onPause() {
        if (score > prefs.getInt(KEY_SCORE, 0))
            prefs.edit().putInt(KEY_SCORE, score).apply()
        textToSpeech.destroy()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //database.close()
        //timer.cancel()
        //textToSpeech.destroy()
        _binding = null
    }

    /** Set custom adapter */
    private fun initRecyclerView() {
        adapter = MessageAdapter()
        binding.rvList.adapter = adapter
        binding.rvList.layoutManager = LinearLayoutManager(fragmentContext)
    }

    private fun initClickListeners() {
        binding.ivGetHint.setOnClickListener(showHintUsageDialog)
        binding.lCityName.setEndIconOnClickListener(userSendCity)
        TooltipCompat.setTooltipText(
            binding.ivGetHint,
            getString(R.string.get_hint)
        )//notification that the user may receive a hint

        binding.etCityName.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_SEND) {
                binding.lCityName.findViewById<CheckableImageButton>(com.google.android.material.R.id.text_input_end_icon)
                    .performClick()
                true
            } else false
        }
    }

    private var timer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val seconds = millisUntilFinished / 1000//converting milliseconds to
            binding.tvTimer.text = getString(R.string.seconds, seconds.toString())
            if (seconds == TIME_USE_HINT && score >= booster)
                binding.ivGetHint.performLongClick()//call notification
        }

        override fun onFinish() {
            openResultFragment(false)//at the end of the countdown, a fragment of defeat opens
        }
    }

    /** The user wants to get a hint */
    private val showHintUsageDialog = View.OnClickListener {
        MaterialAlertDialogBuilder(fragmentContext)
            .setTitle(getString(R.string.using_hint))
            .setMessage(getString(R.string.spend_stars_on_hint, booster))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (score >= booster) {//checking whether the user has enough points
                    score -= booster
                    binding.tvScore.text = score.toString()
                    botSendCity(USER_ID, botCity)//the bot responds for the user
                    botSendCity(BOT_ID, botCity)//the bot responds for itself
                    timer.start()//restarting the countdown
                } else
                    showMessage(getString(R.string.missing_stars, booster - score))
            }
            .setNegativeButton(getString(R.string.no), null).show()
    }

    /** Displaying a message */
    private fun showMessage(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).setAnchorView(binding.lCityName)
            .show()
    }

    /** Processing of the city entered by the user */
    private val userSendCity = View.OnClickListener {
        val userCity: String =
            normalizeUserCity(binding.etCityName.text.toString())//checking the correctness of the entered text
        val firstChar: Char = userCity.first()
        if (userCity.isBlank() || firstChar == 'Ь' || firstChar == 'Ы') {
            showMessage(getString(R.string.enter_existing_city))
            return@OnClickListener//return CITY_FOUND
        }
        if (botCity.isNotEmpty()) {//checking the city when launching the app
            val lastChar: Char = getLastChar(botCity)
            if (firstChar != lastChar) {//checking that the city should start with the last letter
                showMessage(getString(R.string.the_city_must_begin_with_letter, lastChar))
                return@OnClickListener//return CITY_FOUND
            }
        }
        if (canFoundCity(firstChar, userCity))
            if (canFoundModifiedCity(firstChar, userCity))
                if (canFoundOldCity(userCity))
                    if (canFoundCountry(userCity))
                        if (canFoundCityWithMistake(firstChar, userCity))
                            showMessage(getString(R.string.i_do_not_know_such_city))
    }

    /** Search for cities in the database with one error */
    private fun canFoundCityWithMistake(firstChar: Char, userCity: String): Boolean {
        var queryString = "SELECT * FROM $firstChar WHERE name = '$firstChar'"
        val userCityLength = userCity.length
        for (i in 1 until userCityLength) {
            val chars = userCity.toMutableList()
            chars[i] = '_'
            queryString += " OR name LIKE '${chars.joinToString(separator = "")}'"
        }
        return canFoundCity(firstChar, userCity, queryString)
    }

    /** Search cities in the database */
    private fun canFoundCity(
        firstChar: Char,
        userCity: String,
        queryString: String = "SELECT * FROM $firstChar WHERE name = '$userCity'"
    ): Boolean {
        val cursor: Cursor =
            database.rawQuery(
                queryString,
                null
            )
        if (cursor.moveToFirst()) {//checking the receipt of data
            //there was an error with resending a city containing the letter "ё"
            val city: String = cursor.getString(0)
            if (calledCities.firstOrNull { it == city } != null) {//checking whether this city wasn't there before
                showMessage(getString(R.string.city_has_already_been, city))
                return CITY_FOUND
            }
            binding.etCityName.setText(EMPTY_STRING)
            timer.start()
            val flag: Int = cursor.getInt(1)
            cursor.close()
            playSoundMessage()
            addCity(USER_ID, city, flag)
            addPoints()
            botSendCity(BOT_ID, city)
            return CITY_FOUND
        }
        cursor.close()
        return CITY_NOT_FOUND
    }

    private fun addPoints() {
        if (booster < increase) {
            booster = booster++ * 2
            increase = 0//the counter for increasing the booster
        }
        score += booster
        increase++
        binding.tvScore.text = score.toString()
    }

    /** Search for the only letter 'ё' in the word */
    private fun canFoundModifiedCity(firstChar: Char, userCity: String): Boolean {
        for (index in userCity.indices)
            if (userCity[index] == 'е')
                if (!canFoundCity(//or canFoundCityWithMistake?
                        firstChar,
                        userCity.substring(0, index) +
                                'ё' + userCity.substring(index + 1)
                    )
                )
                    return CITY_FOUND
        return CITY_NOT_FOUND
    }

    /** Playback of the message sound */
    private fun playSoundMessage() {
        MediaPlayer.create(fragmentActivity, R.raw.send_msg).start()
    }

    /** Search for a city in the table of old city names */
    private fun canFoundOldCity(userCity: String): Boolean {
        val cursor: Cursor =
            database.rawQuery("SELECT * FROM old_city WHERE old_name = '$userCity'", null)
        if (cursor.moveToFirst()) {
            val oldCity: String = cursor.getString(0)
            showMessage(getString(R.string.former_name, userCity, oldCity))
            cursor.close()
            return CITY_FOUND
        }
        cursor.close()
        return CITY_NOT_FOUND
    }

    /** Search for a city in the country table */
    private fun canFoundCountry(userCity: String): Boolean {
        val cursor: Cursor =
            database.rawQuery("SELECT * FROM country WHERE name = '$userCity'", null)
        if (cursor.moveToFirst()) {
            showMessage(getString(R.string.is_country, userCity))
            cursor.close()
            return CITY_FOUND
        }
        cursor.close()
        return CITY_NOT_FOUND
    }

    /** Adding a city to the RV and to the list of called cities */
    private fun addCity(id: Boolean, city: String, flag: Int) {
        val cityModel = MessageModel(id, city, flag)
        adapter.insertMessage(cityModel)
        binding.rvList.scrollToPosition(adapter.itemCount - 1)//scroll down the RV
        calledCities.add(city)
    }

    /** The bot responds to the user */
    private fun botSendCity(id: Boolean, userCity: String) {
        if (booster > level)
            botSurrender()
        var flag: Int
        var numberSamples = 0
        var cursor: Cursor
        do {
            if (numberSamples++ > MAX_NUMBER_SAMPLES)//checking for the maximum number of repeated samples
                botSurrender()
            cursor =
                database.rawQuery(
                    "SELECT * FROM ${getLastChar(userCity)} ORDER BY RANDOM() LIMIT 1",
                    null
                )
            cursor.moveToFirst()
            botCity = cursor.getString(0)
            flag = cursor.getInt(1)
        } while (calledCities.firstOrNull { it == botCity } != null)//checking whether there has already been such a city
        cursor.close()
        if (id == BOT_ID)//checking that the bot speaks for itself
            textToSpeech = Speech(fragmentContext, botCity)
        addCity(id, botCity, flag)
    }

    /** The bot capitulates */
    private fun botSurrender() {
        prefs.edit().putInt(KEY_LEVEL, level * 2).apply()
        openResultFragment(true)
    }

    /** Opening the fragment of victory or defeat */
    fun openResultFragment(isVictory: Boolean) {
        timer.cancel()
        val fragment = ResultFragment()
        fragment.arguments = bundleOf(
            SCORE_ID to score,
            IS_VICTORY_ID to isVictory
        )
        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment).commit()
    }
}
