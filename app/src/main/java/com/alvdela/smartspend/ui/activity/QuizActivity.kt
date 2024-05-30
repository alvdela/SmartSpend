package com.alvdela.smartspend.ui.activity

import android.app.Dialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.alvdela.smartspend.R
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.alvdela.smartspend.model.Pregunta
import com.alvdela.smartspend.model.Quiz
import com.alvdela.smartspend.ui.Animations
import com.echo.holographlibrary.PieGraph
import com.echo.holographlibrary.PieSlice
import com.google.gson.Gson
import java.io.IOException

class QuizActivity : AppCompatActivity() {

    private var mpMusic: MediaPlayer? = null
    private var mpCorrect: MediaPlayer? = null
    private var mpWrong: MediaPlayer? = null
    private var mpWinning: MediaPlayer? = null


    private lateinit var dialog: Dialog

    private var mHandler: Handler? = null
    private var mIntervar = 1000

    private lateinit var sbMusic: SeekBar

    private lateinit var gameTitle: ImageView
    private lateinit var lyStartButtons: LinearLayout
    private lateinit var lyLevels: LinearLayout
    private lateinit var lyCorrectAnswers: LinearLayout
    private lateinit var cyTimeLeft: ConstraintLayout

    private lateinit var tvQuestion: TextView
    private lateinit var clAnswers: ConstraintLayout
    private lateinit var tvCorrectAnswers: TextView
    private lateinit var pbTime: ProgressBar

    private lateinit var answers : MutableList<TextView>

    private lateinit var quiz: Quiz
    private lateinit var questions: MutableList<Pregunta>
    private var correctAnswers = 0
    private var totalQuestions = 0
    private var currentQuestion = 0
    private var timeInSeconds = 90

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        initObjects()
        initMusic()
        parseJson(this)
    }

    private fun initObjects() {
        gameTitle = findViewById(R.id.gameTitle)
        lyStartButtons = findViewById(R.id.lyStartButtons)

        lyLevels = findViewById(R.id.lyLevels)
        lyLevels.visibility = View.GONE

        tvQuestion = findViewById(R.id.tvQuestion)
        clAnswers = findViewById(R.id.clAnswers)
        tvQuestion.translationY = -2000f
        clAnswers.translationY = 2000f

        lyCorrectAnswers = findViewById(R.id.lyCorrectAnswers)
        lyCorrectAnswers.visibility = View.GONE
        cyTimeLeft = findViewById(R.id.cyTimeLeft)
        cyTimeLeft.visibility = View.GONE

        tvCorrectAnswers = findViewById(R.id.tvCorrectAnswers)
        pbTime = findViewById(R.id.pbTime)
        pbTime.max = timeInSeconds
        pbTime.progress = timeInSeconds

        initAnswers()
        initButtons()
    }

    private fun initAnswers(){
        val answer1 = findViewById<TextView>(R.id.answer1)
        val answer2 = findViewById<TextView>(R.id.answer2)
        val answer3 = findViewById<TextView>(R.id.answer3)
        val answer4 = findViewById<TextView>(R.id.answer4)
        answers = mutableListOf(answer1,answer2,answer3,answer4)
    }

    private fun initButtons() {
        val startGame = findViewById<Button>(R.id.startGame)
        startGame.setOnClickListener {
            showLevels()
        }

        val endGame = findViewById<Button>(R.id.endGame)
        endGame.setOnClickListener {
            finish()
        }

        val lv10 = findViewById<Button>(R.id.lv10)
        lv10.setOnClickListener {
            initQuiz(10)
        }
        val lv15 = findViewById<Button>(R.id.lv15)
        lv15.setOnClickListener {
            initQuiz(15)
        }
        val lv20 = findViewById<Button>(R.id.lv20)
        lv20.setOnClickListener {
            initQuiz(20)
        }
        val contrareloj = findViewById<Button>(R.id.contrareloj)
        contrareloj.setOnClickListener {
            initQuiz(0)
        }
    }

    private fun showLevels() {
        Animations.animateViewOfFloat(gameTitle,"translationY", -2000f, 200)
        Animations.animateViewOfFloat(lyStartButtons,"translationY", 2000f, 200)
        lyLevels.visibility = View.VISIBLE
        lyLevels.translationX = 1000f
        Animations.animateViewOfFloat(lyLevels,"translationX", 0f, 200)
    }

    private fun initQuiz(questionNumber: Int){
        Animations.animateViewOfFloat(lyLevels,"translationX", -2000f, 200)
        lyCorrectAnswers.visibility = View.VISIBLE

        if (questionNumber == 0){
            mHandler = Handler(Looper.getMainLooper())
            cyTimeLeft.visibility = View.VISIBLE
            totalQuestions = quiz.preguntas.size
            questions = quiz.preguntas.toMutableList()
            questions.shuffle()
            chronometer.run()
            nextQuestion()
        }else{
            var index = 0
            questions = mutableListOf()
            totalQuestions = questionNumber
            currentQuestion = 0
            while (index < questionNumber){
                val pregunta = quiz.preguntas.random()
                if (!questions.contains(pregunta)){
                    questions.add(pregunta)
                    index++
                }
            }
            nextQuestion()
        }
    }

    private fun nextQuestion() {

        tvQuestion.text = "Pregunta: ${currentQuestion + 1}\n${questions[currentQuestion].pregunta}"
        makeAnswersInvisible()
        for ((index, answer) in questions[currentQuestion].opciones.withIndex()){
            answers[index].visibility = View.VISIBLE
            answers[index].text = answer
            answers[index].setOnClickListener {
                checkAnswer(answer, questions[currentQuestion].respuesta_correcta)
            }
        }
        animateQuestion()
    }

    private fun animateQuestion(){
        tvQuestion.translationX = 0f
        clAnswers.translationX = 0f

        tvQuestion.translationY = -2000f
        clAnswers.translationY = 2000f
        Animations.animateViewOfFloat(tvQuestion,"translationY", 0f, 500)
        Animations.animateViewOfFloat(clAnswers,"translationY", 0f, 500)
    }

    private fun makeAnswersInvisible() {
        for (tv in answers){
            tv.visibility = View.GONE
        }
    }

    private fun checkAnswer(answer: String, respuestaCorrecta: String) {
        if (answer == respuestaCorrecta){
            correctAnswers++
            correctAnswer()
        }else{
            wrongAnswer()
        }
        currentQuestion++
        //Salida de la pregunta
        Animations.animateViewOfFloat(tvQuestion,"translationX", -2000f, 500)
        Animations.animateViewOfFloat(clAnswers,"translationX", -2000f, 500)

        Handler().postDelayed({
            if (currentQuestion < totalQuestions && timeInSeconds > 0){
                nextQuestion()
            }else{
                endGame()
            }
        }, 600)

    }

    private fun endGame() {
        showPopUp(R.layout.pop_up_quiz_result)

        mpWinning?.start()

        val tvCorrectNumber = dialog.findViewById<TextView>(R.id.tvCorrectNumber)
        tvCorrectNumber.text = "Correctas: $correctAnswers"

        val tvWrongAnswer = dialog.findViewById<TextView>(R.id.tvWrongAnswer)
        tvWrongAnswer.text = "Erroneas: ${currentQuestion - correctAnswers}"

        val pieGraphQuiz = dialog.findViewById<PieGraph>(R.id.pieGraphQuiz)
        val correctSlice = PieSlice()
        correctSlice.color = resources.getColor(R.color.green)
        correctSlice.value = correctAnswers.toFloat()
        pieGraphQuiz.addSlice(correctSlice)
        val wrongSlice = PieSlice()
        wrongSlice.color = resources.getColor(R.color.red)
        wrongSlice.value = (currentQuestion - correctAnswers).toFloat()
        pieGraphQuiz.addSlice(wrongSlice)

        val restartButton = dialog.findViewById<Button>(R.id.restartButton)
        restartButton.setOnClickListener {
            dialog.dismiss()
            restartGame()
        }
    }

    private fun restartGame() {
        correctAnswers = 0
        totalQuestions = 0
        currentQuestion = 0
        timeInSeconds = 90
        tvCorrectAnswers.text = correctAnswers.toString()
        pbTime.progress = timeInSeconds

        tvQuestion.translationY = -2000f
        clAnswers.translationY = 2000f
        lyCorrectAnswers.visibility = View.GONE
        cyTimeLeft.visibility = View.GONE

        Animations.animateViewOfFloat(gameTitle,"translationY", 0f, 200)
        Animations.animateViewOfFloat(lyStartButtons,"translationY", 0f, 200)
    }

    private fun initMusic() {
        mpMusic = MediaPlayer.create(this, R.raw.quiz_music)
        mpCorrect = MediaPlayer.create(this, R.raw.correct)
        mpWrong = MediaPlayer.create(this, R.raw.wrong)
        mpWinning = MediaPlayer.create(this,R.raw.winning)

        sbMusic = findViewById(R.id.sbVolume)

        mpMusic?.isLooping = true
        mpMusic?.start()
        setVolumes()
    }

    private fun setVolumes() {
        val icVolume = findViewById<ImageView>(R.id.icVolume)
        sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpMusic?.setVolume(i / 100.0f, i / 100.0f)
                if (sbMusic.progress == 0){
                    icVolume.setImageResource(R.drawable.ic_volume_off)
                }else{
                    icVolume.setImageResource(R.drawable.ic_volume_up)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun correctAnswer(){
        mpCorrect?.start()
        tvCorrectAnswers.text = correctAnswers.toString()
    }

    private fun wrongAnswer(){
        mpWrong?.start()

    }

    private fun loadJsonFromAsset(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    private fun parseJson(context: Context) {
        val jsonString = loadJsonFromAsset(context, "quiz.json")
        if (jsonString != null) {
            val gson = Gson()
            quiz = gson.fromJson(jsonString, Quiz::class.java)
            println(quiz)
        } else {
            println("Error reading JSON file")
        }
    }

    private var chronometer: Runnable = object : Runnable {
        override fun run() {
            try {
                timeInSeconds--
                updateStopWatchView()
            } finally {
                mHandler!!.postDelayed(this, mIntervar.toLong())
            }
        }
    }

    private fun updateStopWatchView() {
        pbTime.progress = timeInSeconds
    }

    override fun onDestroy() {
        super.onDestroy()
        mpMusic?.stop()
    }

    private fun showPopUp(layout: Int) {
        dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.setCancelable(false)
        dialog.show()
    }
}