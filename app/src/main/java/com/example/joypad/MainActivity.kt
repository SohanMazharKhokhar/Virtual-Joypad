////package com.example.joypad
////
////import android.annotation.SuppressLint
////import android.content.Context
////import android.graphics.Color
////import android.hardware.Sensor
////import android.hardware.SensorEvent
////import android.hardware.SensorEventListener
////import android.hardware.SensorManager
////import android.os.Build
////import android.os.Bundle
////import android.os.VibrationEffect
////import android.os.Vibrator
////import android.os.VibratorManager
////import android.view.KeyEvent
////import android.view.MotionEvent
////import android.view.View
////import android.widget.Button
////import android.widget.EditText
////import android.widget.Toast
////import androidx.appcompat.app.AppCompatActivity
////import java.io.BufferedReader
////import java.io.InputStreamReader
////import java.io.PrintWriter
////import java.net.Socket
////import java.util.concurrent.Executors
////import kotlin.concurrent.thread
////import kotlin.math.hypot
////
////class MainActivity : AppCompatActivity(), SensorEventListener {
////
////    private var outStream: PrintWriter? = null
////    private var socket: Socket? = null
////    private var isConnected = false
////
////    // Create ONE highly efficient background queue for network data
////    private val networkExecutor = Executors.newSingleThreadExecutor()
////
////    private lateinit var sensorManager: SensorManager
////    private var accelerometer: Sensor? = null
////
////    private var isGyroEnabled = false
////    private var isVibrationEnabled = true
////    private var isVolKeysEnabled = false
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////
////        supportActionBar?.hide()
////        @Suppress("DEPRECATION")
////        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
////                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
////                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
////
////        setContentView(R.layout.activity_main)
////
////        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
////        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
////
////        findViewById<Button>(R.id.btnConnect).setOnClickListener { view ->
////            animateButton(view, MotionEvent.ACTION_DOWN)
////            val ip = findViewById<EditText>(R.id.etIpAddress).text.toString()
////
////            // Connect using the executor
////            networkExecutor.execute {
////                try {
////                    socket?.close()
////                    socket = Socket(ip, 5000)
////                    outStream = PrintWriter(socket!!.getOutputStream(), true)
////                    isConnected = true
////                    runOnUiThread { Toast.makeText(this, "Link Connected!", Toast.LENGTH_SHORT).show() }
////                    startRumbleListener()
////                } catch (e: Exception) {
////                    isConnected = false
////                    runOnUiThread { Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show() }
////                }
////            }
////            animateButton(view, MotionEvent.ACTION_UP)
////        }
////
////        val btnGyro = findViewById<Button>(R.id.btnGyro)
////        btnGyro.setOnClickListener { view ->
////            animateButton(view, MotionEvent.ACTION_DOWN)
////            isGyroEnabled = !isGyroEnabled
////            if (isGyroEnabled) {
////                btnGyro.text = "GYRO: ON"
////                btnGyro.setBackgroundColor(Color.parseColor("#00AA00"))
////                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
////            } else {
////                btnGyro.text = "GYRO: OFF"
////                btnGyro.setBackgroundColor(Color.parseColor("#AA0000"))
////                sensorManager.unregisterListener(this)
////                networkExecutor.execute { outStream?.println("JOY_L:0.0,0.0") }
////            }
////            animateButton(view, MotionEvent.ACTION_UP)
////        }
////
////        val btnVibration = findViewById<Button>(R.id.btnVibration)
////        btnVibration.setOnClickListener { view ->
////            animateButton(view, MotionEvent.ACTION_DOWN)
////            isVibrationEnabled = !isVibrationEnabled
////            if (isVibrationEnabled) {
////                btnVibration.text = "VIB: ON"
////                btnVibration.setBackgroundColor(Color.parseColor("#00AA00"))
////            } else {
////                btnVibration.text = "VIB: OFF"
////                btnVibration.setBackgroundColor(Color.parseColor("#AA0000"))
////                getVibratorService().cancel()
////            }
////            animateButton(view, MotionEvent.ACTION_UP)
////        }
////
////        val btnVol = findViewById<Button>(R.id.btnVol)
////        btnVol.setOnClickListener { view ->
////            animateButton(view, MotionEvent.ACTION_DOWN)
////            isVolKeysEnabled = !isVolKeysEnabled
////            if (isVolKeysEnabled) {
////                btnVol.text = "VOL: ON"
////                btnVol.setBackgroundColor(Color.parseColor("#00AA00"))
////                Toast.makeText(this, "Vol Up = RT | Vol Down = LT", Toast.LENGTH_SHORT).show()
////            } else {
////                btnVol.text = "VOL: OFF"
////                btnVol.setBackgroundColor(Color.parseColor("#AA0000"))
////            }
////            animateButton(view, MotionEvent.ACTION_UP)
////        }
////
////        setupButton(findViewById(R.id.btnA), "A")
////        setupButton(findViewById(R.id.btnB), "B")
////        setupButton(findViewById(R.id.btnX), "X")
////        setupButton(findViewById(R.id.btnY), "Y")
////        setupButton(findViewById(R.id.btnUp), "UP")
////        setupButton(findViewById(R.id.btnDown), "DOWN")
////        setupButton(findViewById(R.id.btnLeft), "LEFT")
////        setupButton(findViewById(R.id.btnRight), "RIGHT")
////        setupButton(findViewById(R.id.btnStart), "START")
////        setupButton(findViewById(R.id.btnSelect), "SELECT")
////
////        setupButton(findViewById(R.id.btnLB), "LB")
////        setupButton(findViewById(R.id.btnRB), "RB")
////        setupTrigger(findViewById(R.id.btnLT), "LT")
////        setupTrigger(findViewById(R.id.btnRT), "RT")
////
////        setupJoystick(findViewById(R.id.joyBaseLeft), findViewById(R.id.joyStickLeft), "JOY_L")
////        setupJoystick(findViewById(R.id.joyBaseRight), findViewById(R.id.joyStickRight), "JOY_R")
////    }
////
////    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
////        if (isVolKeysEnabled) {
////            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
////                networkExecutor.execute { outStream?.println("RT:1.0") }
////                return true
////            }
////            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
////                networkExecutor.execute { outStream?.println("LT:1.0") }
////                return true
////            }
////        }
////        return super.onKeyDown(keyCode, event)
////    }
////
////    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
////        if (isVolKeysEnabled) {
////            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
////                networkExecutor.execute { outStream?.println("RT:0.0") }
////                return true
////            }
////            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
////                networkExecutor.execute { outStream?.println("LT:0.0") }
////                return true
////            }
////        }
////        return super.onKeyUp(keyCode, event)
////    }
////
////    override fun onSensorChanged(event: SensorEvent?) {
////        if (!isGyroEnabled || event == null) return
////        val yTilt = (event.values[1] / 5.0f).coerceIn(-1.0f, 1.0f)
////        val xTilt = (event.values[0] / 5.0f).coerceIn(-1.0f, 1.0f)
////        networkExecutor.execute { outStream?.println("JOY_L:$yTilt,$xTilt") }
////    }
////
////    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
////
////    private fun animateButton(view: View, action: Int) {
////        if (action == MotionEvent.ACTION_DOWN) {
////            view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(50).start()
////        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
////            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
////        }
////    }
////
////    @SuppressLint("ClickableViewAccessibility")
////    private fun setupButton(button: Button, commandName: String) {
////        button.setOnTouchListener { view, event ->
////            animateButton(view, event.action)
////            when (event.action) {
////                MotionEvent.ACTION_DOWN -> {
////                    networkExecutor.execute { outStream?.println("$commandName:1") }
////                    true
////                }
////                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
////                    networkExecutor.execute { outStream?.println("$commandName:0") }
////                    true
////                }
////                else -> false
////            }
////        }
////    }
////
////    @SuppressLint("ClickableViewAccessibility")
////    private fun setupTrigger(button: Button, commandName: String) {
////        button.setOnTouchListener { view, event ->
////            animateButton(view, event.action)
////            when (event.action) {
////                MotionEvent.ACTION_DOWN -> {
////                    networkExecutor.execute { outStream?.println("$commandName:1.0") }
////                    true
////                }
////                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
////                    networkExecutor.execute { outStream?.println("$commandName:0.0") }
////                    true
////                }
////                else -> false
////            }
////        }
////    }
////
////    @SuppressLint("ClickableViewAccessibility")
////    private fun setupJoystick(base: View, stick: View, commandName: String) {
////        base.setOnTouchListener { _, event ->
////            val maxTravelRadius = (base.width / 2f) - (stick.width / 2f)
////            var dx = event.x - (base.width / 2f)
////            var dy = event.y - (base.height / 2f)
////            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
////
////            when (event.action) {
////                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
////                    if (distance > maxTravelRadius) {
////                        dx = dx * maxTravelRadius / distance
////                        dy = dy * maxTravelRadius / distance
////                    }
////                    stick.translationX = dx
////                    stick.translationY = dy
////
////                    val nx = (dx / maxTravelRadius).coerceIn(-1f, 1f)
////                    val ny = (dy / maxTravelRadius).coerceIn(-1f, 1f)
////
////                    if (!(isGyroEnabled && commandName == "JOY_L")) {
////                        networkExecutor.execute { outStream?.println("$commandName:$nx,$ny") }
////                    }
////                }
////                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
////                    stick.animate().translationX(0f).translationY(0f).setDuration(100).start()
////                    if (!(isGyroEnabled && commandName == "JOY_L")) {
////                        networkExecutor.execute { outStream?.println("$commandName:0.0,0.0") }
////                    }
////                }
////            }
////            true
////        }
////    }
////
////    private fun startRumbleListener() {
////        thread {
////            try {
////                val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
////                while (isConnected) {
////                    val line = reader.readLine() ?: break
////                    if (line.startsWith("VIB:")) {
////                        val parts = line.split(":")
////                        if (parts.size == 3) {
////                            handleGameVibration(parts[1].toInt(), parts[2].toInt())
////                        }
////                    }
////                }
////            } catch (e: Exception) { isConnected = false }
////        }
////    }
////
////    private fun handleGameVibration(large: Int, small: Int) {
////        if (!isVibrationEnabled) return
////        val maxIntensity = maxOf(large, small)
////        val vibrator = getVibratorService()
////        if (!vibrator.hasVibrator()) return
////
////        if (maxIntensity == 0) vibrator.cancel()
////        else {
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                val amplitude = if (maxIntensity < 1) 1 else maxIntensity
////                vibrator.vibrate(VibrationEffect.createOneShot(300, amplitude))
////            } else {
////                @Suppress("DEPRECATION")
////                vibrator.vibrate(300)
////            }
////        }
////    }
////
////    private fun getVibratorService(): Vibrator {
////        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
////            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
////            vibratorManager.defaultVibrator
////        } else {
////            @Suppress("DEPRECATION")
////            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
////        }
////    }
////
////    override fun onDestroy() {
////        super.onDestroy()
////        sensorManager.unregisterListener(this)
////        networkExecutor.shutdown() // Clean up the queue when the app closes
////    }
////}
//package com.example.joypad
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Color
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.os.Build
//import android.os.Bundle
//import android.os.VibrationEffect
//import android.os.Vibrator
//import android.os.VibratorManager
//import android.view.KeyEvent
//import android.view.MotionEvent
//import android.view.View
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.io.PrintWriter
//import java.net.Socket
//import java.util.concurrent.Executors
//import kotlin.concurrent.thread
//import kotlin.math.hypot
//
//class MainActivity : AppCompatActivity(), SensorEventListener {
//
//    private var outStream: PrintWriter? = null
//    private var socket: Socket? = null
//    private var isConnected = false
//
//    private val networkExecutor = Executors.newSingleThreadExecutor()
//
//    private lateinit var sensorManager: SensorManager
//    private var accelerometer: Sensor? = null
//
//    private var isGyroEnabled = false
//    private var isVibrationEnabled = true
//    private var isVolKeysEnabled = false
//
//    // --- LAYOUT EDIT MODE VARIABLES ---
//    private var isLayoutEditMode = false
//    private var selectedEditGroup: View? = null
//    private var dX = 0f
//    private var dY = 0f
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        supportActionBar?.hide()
//        @Suppress("DEPRECATION")
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
//
//        setContentView(R.layout.activity_main)
//
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//
//        // CONNECTION
//        findViewById<Button>(R.id.btnConnect).setOnClickListener { view ->
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            val ip = findViewById<EditText>(R.id.etIpAddress).text.toString()
//            networkExecutor.execute {
//                try {
//                    socket?.close()
//                    socket = Socket(ip, 5000)
//                    outStream = PrintWriter(socket!!.getOutputStream(), true)
//                    isConnected = true
//                    runOnUiThread { Toast.makeText(this, "Link Connected!", Toast.LENGTH_SHORT).show() }
//                    startRumbleListener()
//                } catch (e: Exception) {
//                    isConnected = false
//                    runOnUiThread { Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show() }
//                }
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        // TOGGLES
//        val btnGyro = findViewById<Button>(R.id.btnGyro)
//        btnGyro.setOnClickListener { view ->
//            if (isLayoutEditMode) return@setOnClickListener
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            isGyroEnabled = !isGyroEnabled
//            if (isGyroEnabled) {
//                btnGyro.text = "GYRO: ON"
//                btnGyro.setBackgroundColor(Color.parseColor("#00AA00"))
//                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
//            } else {
//                btnGyro.text = "GYRO: OFF"
//                btnGyro.setBackgroundColor(Color.parseColor("#AA0000"))
//                sensorManager.unregisterListener(this)
//                networkExecutor.execute { outStream?.println("JOY_L:0.0,0.0") }
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        val btnVibration = findViewById<Button>(R.id.btnVibration)
//        btnVibration.setOnClickListener { view ->
//            if (isLayoutEditMode) return@setOnClickListener
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            isVibrationEnabled = !isVibrationEnabled
//            if (isVibrationEnabled) {
//                btnVibration.text = "VIB: ON"
//                btnVibration.setBackgroundColor(Color.parseColor("#00AA00"))
//            } else {
//                btnVibration.text = "VIB: OFF"
//                btnVibration.setBackgroundColor(Color.parseColor("#AA0000"))
//                getVibratorService().cancel()
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        val btnVol = findViewById<Button>(R.id.btnVol)
//        btnVol.setOnClickListener { view ->
//            if (isLayoutEditMode) return@setOnClickListener
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            isVolKeysEnabled = !isVolKeysEnabled
//            if (isVolKeysEnabled) {
//                btnVol.text = "VOL: ON"
//                btnVol.setBackgroundColor(Color.parseColor("#00AA00"))
//                Toast.makeText(this, "Vol Up = RT | Vol Down = LT", Toast.LENGTH_SHORT).show()
//            } else {
//                btnVol.text = "VOL: OFF"
//                btnVol.setBackgroundColor(Color.parseColor("#AA0000"))
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        // --- LAYOUT EDIT BUTTON ---
//        val btnLayout = findViewById<Button>(R.id.btnLayout)
//        btnLayout.setOnClickListener { view ->
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            isLayoutEditMode = !isLayoutEditMode
//            if (isLayoutEditMode) {
//                btnLayout.text = "LAYOUT: ON"
//                btnLayout.setBackgroundColor(Color.parseColor("#00AA00"))
//                Toast.makeText(this, "EDIT MODE: Tap a button to drag its group. Use Vol +/- to resize.", Toast.LENGTH_LONG).show()
//            } else {
//                btnLayout.text = "LAYOUT: OFF"
//                btnLayout.setBackgroundColor(Color.parseColor("#333333"))
//                selectedEditGroup?.alpha = 1.0f // Remove transparency highlight
//                selectedEditGroup = null
//                Toast.makeText(this, "Layout Locked!", Toast.LENGTH_SHORT).show()
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        // MAPPING
//        setupButton(findViewById(R.id.btnA), "A")
//        setupButton(findViewById(R.id.btnB), "B")
//        setupButton(findViewById(R.id.btnX), "X")
//        setupButton(findViewById(R.id.btnY), "Y")
//        setupButton(findViewById(R.id.btnUp), "UP")
//        setupButton(findViewById(R.id.btnDown), "DOWN")
//        setupButton(findViewById(R.id.btnLeft), "LEFT")
//        setupButton(findViewById(R.id.btnRight), "RIGHT")
//        setupButton(findViewById(R.id.btnStart), "START")
//        setupButton(findViewById(R.id.btnSelect), "SELECT")
//
//        setupButton(findViewById(R.id.btnLB), "LB")
//        setupButton(findViewById(R.id.btnRB), "RB")
//        setupTrigger(findViewById(R.id.btnLT), "LT")
//        setupTrigger(findViewById(R.id.btnRT), "RT")
//
//        setupJoystick(findViewById(R.id.joyBaseLeft), findViewById(R.id.joyStickLeft), "JOY_L")
//        setupJoystick(findViewById(R.id.joyBaseRight), findViewById(R.id.joyStickRight), "JOY_R")
//    }
//
//    // --- VOLUME ENGINE: SCALES VIEWS WHEN EDITING, TRIGGERS BUTTONS WHEN PLAYING ---
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        // 1. Resize Layout Feature
//        if (isLayoutEditMode && selectedEditGroup != null) {
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//                selectedEditGroup!!.scaleX += 0.05f
//                selectedEditGroup!!.scaleY += 0.05f
//                return true
//            }
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//                if (selectedEditGroup!!.scaleX > 0.4f) { // Prevent making it invisible
//                    selectedEditGroup!!.scaleX -= 0.05f
//                    selectedEditGroup!!.scaleY -= 0.05f
//                }
//                return true
//            }
//        }
//
//        // 2. Normal Game Trigger Feature
//        if (isVolKeysEnabled && !isLayoutEditMode) {
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//                networkExecutor.execute { outStream?.println("RT:1.0") }
//                return true
//            }
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//                networkExecutor.execute { outStream?.println("LT:1.0") }
//                return true
//            }
//        }
//        return super.onKeyDown(keyCode, event)
//    }
//
//    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//        if (isLayoutEditMode && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
//            return true
//        }
//        if (isVolKeysEnabled && !isLayoutEditMode) {
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//                networkExecutor.execute { outStream?.println("RT:0.0") }
//                return true
//            }
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//                networkExecutor.execute { outStream?.println("LT:0.0") }
//                return true
//            }
//        }
//        return super.onKeyUp(keyCode, event)
//    }
//
//    // --- NEW BULLETPROOF DRAG ENGINE ---
//    private fun handleDrag(group: View, event: MotionEvent): Boolean {
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                selectedEditGroup?.alpha = 1.0f // Reset old highlight
//                selectedEditGroup = group
//                group.alpha = 0.6f // Highlight current item
//
//                dX = group.x - event.rawX
//                dY = group.y - event.rawY
//            }
//            MotionEvent.ACTION_MOVE -> {
//                // Instantly update position (No animation lag)
//                group.x = event.rawX + dX
//                group.y = event.rawY + dY
//            }
//        }
//        return true
//    }
//
//    // --- CORE GAME CONTROLS ---
//    override fun onSensorChanged(event: SensorEvent?) {
//        if (!isGyroEnabled || event == null || isLayoutEditMode) return
//        val yTilt = (event.values[1] / 5.0f).coerceIn(-1.0f, 1.0f)
//        val xTilt = (event.values[0] / 5.0f).coerceIn(-1.0f, 1.0f)
//        networkExecutor.execute { outStream?.println("JOY_L:$yTilt,$xTilt") }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//    private fun animateButton(view: View, action: Int) {
//        if (action == MotionEvent.ACTION_DOWN) {
//            view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(50).start()
//        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
//            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupButton(button: Button, commandName: String) {
//        button.setOnTouchListener { view, event ->
//            // IF IN EDIT MODE: Bypass the button entirely and drag the parent group
//            if (isLayoutEditMode) {
//                return@setOnTouchListener handleDrag(view.parent as View, event)
//            }
//
//            // Normal Game Mode
//            animateButton(view, event.action)
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    networkExecutor.execute { outStream?.println("$commandName:1") }
//                    true
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    networkExecutor.execute { outStream?.println("$commandName:0") }
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupTrigger(button: Button, commandName: String) {
//        button.setOnTouchListener { view, event ->
//            if (isLayoutEditMode) {
//                return@setOnTouchListener handleDrag(view.parent as View, event)
//            }
//
//            animateButton(view, event.action)
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    networkExecutor.execute { outStream?.println("$commandName:1.0") }
//                    true
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    networkExecutor.execute { outStream?.println("$commandName:0.0") }
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupJoystick(base: View, stick: View, commandName: String) {
//        base.setOnTouchListener { _, event ->
//            if (isLayoutEditMode) {
//                return@setOnTouchListener handleDrag(base, event)
//            }
//
//            val maxTravelRadius = (base.width / 2f) - (stick.width / 2f)
//            var dx = event.x - (base.width / 2f)
//            var dy = event.y - (base.height / 2f)
//            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
//
//            when (event.action) {
//                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
//                    if (distance > maxTravelRadius) {
//                        dx = dx * maxTravelRadius / distance
//                        dy = dy * maxTravelRadius / distance
//                    }
//                    stick.translationX = dx
//                    stick.translationY = dy
//
//                    val nx = (dx / maxTravelRadius).coerceIn(-1f, 1f)
//                    val ny = (dy / maxTravelRadius).coerceIn(-1f, 1f)
//
//                    if (!(isGyroEnabled && commandName == "JOY_L")) {
//                        networkExecutor.execute { outStream?.println("$commandName:$nx,$ny") }
//                    }
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    stick.animate().translationX(0f).translationY(0f).setDuration(100).start()
//                    if (!(isGyroEnabled && commandName == "JOY_L")) {
//                        networkExecutor.execute { outStream?.println("$commandName:0.0,0.0") }
//                    }
//                }
//            }
//            true
//        }
//    }
//
//    private fun startRumbleListener() {
//        thread {
//            try {
//                val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
//                while (isConnected) {
//                    val line = reader.readLine() ?: break
//                    if (line.startsWith("VIB:")) {
//                        val parts = line.split(":")
//                        if (parts.size == 3) {
//                            handleGameVibration(parts[1].toInt(), parts[2].toInt())
//                        }
//                    }
//                }
//            } catch (e: Exception) { isConnected = false }
//        }
//    }
//
//    private fun handleGameVibration(large: Int, small: Int) {
//        if (!isVibrationEnabled) return
//        val maxIntensity = maxOf(large, small)
//        val vibrator = getVibratorService()
//        if (!vibrator.hasVibrator()) return
//
//        if (maxIntensity == 0) vibrator.cancel()
//        else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val amplitude = if (maxIntensity < 1) 1 else maxIntensity
//                vibrator.vibrate(VibrationEffect.createOneShot(300, amplitude))
//            } else {
//                @Suppress("DEPRECATION")
//                vibrator.vibrate(300)
//            }
//        }
//    }
//
//    private fun getVibratorService(): Vibrator {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
//            vibratorManager.defaultVibrator
//        } else {
//            @Suppress("DEPRECATION")
//            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        sensorManager.unregisterListener(this)
//        networkExecutor.shutdown()
//    }
//}
//package com.example.joypad
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Color
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.os.Build
//import android.os.Bundle
//import android.os.VibrationEffect
//import android.os.Vibrator
//import android.os.VibratorManager
//import android.view.KeyEvent
//import android.view.MotionEvent
//import android.view.View
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.io.PrintWriter
//import java.net.DatagramPacket
//import java.net.DatagramSocket
//import java.net.InetSocketAddress
//import java.net.Socket
//import java.util.concurrent.Executors
//import kotlin.concurrent.thread
//import kotlin.math.hypot
//
//class MainActivity : AppCompatActivity(), SensorEventListener {
//
//    private var outStream: PrintWriter? = null
//    private var socket: Socket? = null
//    private var isConnected = false
//
//    private val networkExecutor = Executors.newSingleThreadExecutor()
//
//    private lateinit var sensorManager: SensorManager
//    private var accelerometer: Sensor? = null
//
//    private var isGyroEnabled = false
//    private var isVibrationEnabled = true
//    private var isVolKeysEnabled = false
//
//    private var isLayoutEditMode = false
//    private var selectedEditGroup: View? = null
//    private var dX = 0f
//    private var dY = 0f
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        supportActionBar?.hide()
//        @Suppress("DEPRECATION")
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
//
//        setContentView(R.layout.activity_main)
//
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//
//        // START AUTO-DISCOVERY
//        val tvStatus = findViewById<TextView>(R.id.tvStatus)
//        startAutoDiscovery(tvStatus)
//
//        // TOGGLES
//        val btnGyro = findViewById<Button>(R.id.btnGyro)
//        btnGyro.setOnClickListener { view ->
//            if (isLayoutEditMode) return@setOnClickListener
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            isGyroEnabled = !isGyroEnabled
//            if (isGyroEnabled) {
//                btnGyro.text = "GYRO: ON"
//                btnGyro.setBackgroundColor(Color.parseColor("#00AA00"))
//                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
//            } else {
//                btnGyro.text = "GYRO: OFF"
//                btnGyro.setBackgroundColor(Color.parseColor("#AA0000"))
//                sensorManager.unregisterListener(this)
//                networkExecutor.execute { outStream?.println("JOY_L:0.0,0.0") }
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        val btnVibration = findViewById<Button>(R.id.btnVibration)
//        btnVibration.setOnClickListener { view ->
//            if (isLayoutEditMode) return@setOnClickListener
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            isVibrationEnabled = !isVibrationEnabled
//            if (isVibrationEnabled) {
//                btnVibration.text = "VIB: ON"
//                btnVibration.setBackgroundColor(Color.parseColor("#00AA00"))
//            } else {
//                btnVibration.text = "VIB: OFF"
//                btnVibration.setBackgroundColor(Color.parseColor("#AA0000"))
//                getVibratorService().cancel()
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        val btnVol = findViewById<Button>(R.id.btnVol)
//        btnVol.setOnClickListener { view ->
//            if (isLayoutEditMode) return@setOnClickListener
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            isVolKeysEnabled = !isVolKeysEnabled
//            if (isVolKeysEnabled) {
//                btnVol.text = "VOL: ON"
//                btnVol.setBackgroundColor(Color.parseColor("#00AA00"))
//                Toast.makeText(this, "Vol Up = RT | Vol Down = LT", Toast.LENGTH_SHORT).show()
//            } else {
//                btnVol.text = "VOL: OFF"
//                btnVol.setBackgroundColor(Color.parseColor("#AA0000"))
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        val btnLayout = findViewById<Button>(R.id.btnLayout)
//        btnLayout.setOnClickListener { view ->
//            animateButton(view, MotionEvent.ACTION_DOWN)
//            isLayoutEditMode = !isLayoutEditMode
//            if (isLayoutEditMode) {
//                btnLayout.text = "LAYOUT: ON"
//                btnLayout.setBackgroundColor(Color.parseColor("#00AA00"))
//                Toast.makeText(this, "EDIT MODE: Tap a button to drag its group. Use Vol +/- to resize.", Toast.LENGTH_LONG).show()
//            } else {
//                btnLayout.text = "LAYOUT: OFF"
//                btnLayout.setBackgroundColor(Color.parseColor("#333333"))
//                selectedEditGroup?.alpha = 1.0f
//                selectedEditGroup = null
//            }
//            animateButton(view, MotionEvent.ACTION_UP)
//        }
//
//        setupButton(findViewById(R.id.btnA), "A")
//        setupButton(findViewById(R.id.btnB), "B")
//        setupButton(findViewById(R.id.btnX), "X")
//        setupButton(findViewById(R.id.btnY), "Y")
//        setupButton(findViewById(R.id.btnUp), "UP")
//        setupButton(findViewById(R.id.btnDown), "DOWN")
//        setupButton(findViewById(R.id.btnLeft), "LEFT")
//        setupButton(findViewById(R.id.btnRight), "RIGHT")
//        setupButton(findViewById(R.id.btnStart), "START")
//        setupButton(findViewById(R.id.btnSelect), "SELECT")
//
//        setupButton(findViewById(R.id.btnLB), "LB")
//        setupButton(findViewById(R.id.btnRB), "RB")
//        setupTrigger(findViewById(R.id.btnLT), "LT")
//        setupTrigger(findViewById(R.id.btnRT), "RT")
//
//        setupJoystick(findViewById(R.id.joyBaseLeft), findViewById(R.id.joyStickLeft), "JOY_L")
//        setupJoystick(findViewById(R.id.joyBaseRight), findViewById(R.id.joyStickRight), "JOY_R")
//    }
//
//    // --- AUTO-DISCOVERY ENGINE ---
//    private fun startAutoDiscovery(tvStatus: TextView) {
//        thread {
//            var udpSocket: DatagramSocket? = null
//            try {
//                udpSocket = DatagramSocket(null)
//                udpSocket.reuseAddress = true
//                udpSocket.bind(InetSocketAddress(5005))
//
//                val buffer = ByteArray(256)
//                val packet = DatagramPacket(buffer, buffer.size)
//
//                while (!isConnected) {
//                    udpSocket.receive(packet)
//                    val msg = String(packet.data, 0, packet.length).trim()
//
//                    if (msg == "JOYPAD_SERVER") {
//                        val serverIp = packet.address.hostAddress
//                        udpSocket.close()
//                        connectToPC(serverIp, tvStatus)
//                        break
//                    }
//                }
//            } catch (e: Exception) {
//                udpSocket?.close()
//            }
//        }
//    }
//
//    private fun connectToPC(ip: String?, tvStatus: TextView) {
//        if (ip == null) return
//        networkExecutor.execute {
//            try {
//                socket?.close()
//                socket = Socket(ip, 5000)
//                outStream = PrintWriter(socket!!.getOutputStream(), true)
//                isConnected = true
//                runOnUiThread {
//                    tvStatus.text = "🎮 CONNECTED: $ip"
//                    tvStatus.setTextColor(Color.parseColor("#00AA00"))
//                }
//                startRumbleListener()
//            } catch (e: Exception) {
//                isConnected = false
//                runOnUiThread {
//                    tvStatus.text = "⚠️ CONNECTION FAILED. RETRYING..."
//                    tvStatus.setTextColor(Color.parseColor("#AA0000"))
//                }
//                startAutoDiscovery(tvStatus)
//            }
//        }
//    }
//
//    // --- VOLUME KEY OVERRIDE ---
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (isLayoutEditMode && selectedEditGroup != null) {
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//                selectedEditGroup!!.scaleX += 0.05f
//                selectedEditGroup!!.scaleY += 0.05f
//                return true
//            }
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//                if (selectedEditGroup!!.scaleX > 0.4f) {
//                    selectedEditGroup!!.scaleX -= 0.05f
//                    selectedEditGroup!!.scaleY -= 0.05f
//                }
//                return true
//            }
//        }
//        if (isVolKeysEnabled && !isLayoutEditMode) {
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//                networkExecutor.execute { outStream?.println("RT:1.0") }
//                return true
//            }
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//                networkExecutor.execute { outStream?.println("LT:1.0") }
//                return true
//            }
//        }
//        return super.onKeyDown(keyCode, event)
//    }
//
//    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//        if (isLayoutEditMode && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
//            return true
//        }
//        if (isVolKeysEnabled && !isLayoutEditMode) {
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//                networkExecutor.execute { outStream?.println("RT:0.0") }
//                return true
//            }
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//                networkExecutor.execute { outStream?.println("LT:0.0") }
//                return true
//            }
//        }
//        return super.onKeyUp(keyCode, event)
//    }
//
//    // --- DRAG ENGINE ---
//    private fun handleDrag(group: View, event: MotionEvent): Boolean {
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                selectedEditGroup?.alpha = 1.0f
//                selectedEditGroup = group
//                group.alpha = 0.6f
//
//                dX = group.x - event.rawX
//                dY = group.y - event.rawY
//            }
//            MotionEvent.ACTION_MOVE -> {
//                group.x = event.rawX + dX
//                group.y = event.rawY + dY
//            }
//        }
//        return true
//    }
//
//    // --- SENSORS & BUTTONS ---
//    override fun onSensorChanged(event: SensorEvent?) {
//        if (!isGyroEnabled || event == null || isLayoutEditMode) return
//        val yTilt = (event.values[1] / 5.0f).coerceIn(-1.0f, 1.0f)
//        val xTilt = (event.values[0] / 5.0f).coerceIn(-1.0f, 1.0f)
//        networkExecutor.execute { outStream?.println("JOY_L:$yTilt,$xTilt") }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//    private fun animateButton(view: View, action: Int) {
//        if (action == MotionEvent.ACTION_DOWN) {
//            view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(50).start()
//        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
//            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupButton(button: Button, commandName: String) {
//        button.setOnTouchListener { view, event ->
//            if (isLayoutEditMode) {
//                return@setOnTouchListener handleDrag(view.parent as View, event)
//            }
//            animateButton(view, event.action)
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    networkExecutor.execute { outStream?.println("$commandName:1") }
//                    true
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    networkExecutor.execute { outStream?.println("$commandName:0") }
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupTrigger(button: Button, commandName: String) {
//        button.setOnTouchListener { view, event ->
//            if (isLayoutEditMode) {
//                return@setOnTouchListener handleDrag(view.parent as View, event)
//            }
//            animateButton(view, event.action)
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    networkExecutor.execute { outStream?.println("$commandName:1.0") }
//                    true
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    networkExecutor.execute { outStream?.println("$commandName:0.0") }
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupJoystick(base: View, stick: View, commandName: String) {
//        base.setOnTouchListener { _, event ->
//            if (isLayoutEditMode) {
//                return@setOnTouchListener handleDrag(base, event)
//            }
//
//            val maxTravelRadius = (base.width / 2f) - (stick.width / 2f)
//            var dx = event.x - (base.width / 2f)
//            var dy = event.y - (base.height / 2f)
//            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
//
//            when (event.action) {
//                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
//                    if (distance > maxTravelRadius) {
//                        dx = dx * maxTravelRadius / distance
//                        dy = dy * maxTravelRadius / distance
//                    }
//                    stick.translationX = dx
//                    stick.translationY = dy
//
//                    val nx = (dx / maxTravelRadius).coerceIn(-1f, 1f)
//                    val ny = (dy / maxTravelRadius).coerceIn(-1f, 1f)
//
//                    if (!(isGyroEnabled && commandName == "JOY_L")) {
//                        networkExecutor.execute { outStream?.println("$commandName:$nx,$ny") }
//                    }
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    stick.animate().translationX(0f).translationY(0f).setDuration(100).start()
//                    if (!(isGyroEnabled && commandName == "JOY_L")) {
//                        networkExecutor.execute { outStream?.println("$commandName:0.0,0.0") }
//                    }
//                }
//            }
//            true
//        }
//    }
//
//    private fun startRumbleListener() {
//        thread {
//            try {
//                val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
//                while (isConnected) {
//                    val line = reader.readLine() ?: break
//                    if (line.startsWith("VIB:")) {
//                        val parts = line.split(":")
//                        if (parts.size == 3) {
//                            handleGameVibration(parts[1].toInt(), parts[2].toInt())
//                        }
//                    }
//                }
//            } catch (e: Exception) { isConnected = false }
//        }
//    }
//
//    private fun handleGameVibration(large: Int, small: Int) {
//        if (!isVibrationEnabled) return
//        val maxIntensity = maxOf(large, small)
//        val vibrator = getVibratorService()
//        if (!vibrator.hasVibrator()) return
//
//        if (maxIntensity == 0) vibrator.cancel()
//        else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val amplitude = if (maxIntensity < 1) 1 else maxIntensity
//                vibrator.vibrate(VibrationEffect.createOneShot(300, amplitude))
//            } else {
//                @Suppress("DEPRECATION")
//                vibrator.vibrate(300)
//            }
//        }
//    }
//
//    private fun getVibratorService(): Vibrator {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
//            vibratorManager.defaultVibrator
//        } else {
//            @Suppress("DEPRECATION")
//            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        sensorManager.unregisterListener(this)
//        networkExecutor.shutdown()
//    }
//}
package com.example.joypad

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.math.hypot

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var outStream: PrintWriter? = null
    private var socket: Socket? = null
    private var isConnected = false

    private val networkExecutor = Executors.newSingleThreadExecutor()

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var isGyroEnabled = false
    private var isVibrationEnabled = true
    private var isVolKeysEnabled = false

    private var isLayoutEditMode = false
    private var selectedEditGroup: View? = null
    private var dX = 0f
    private var dY = 0f

    // --- SENSITIVITY MULTIPLIERS ---
    private var gyroSensitivity = 1.0f  // 1.0 is default (100%)
    private var vibMultiplier = 1.0f    // 1.0 is default (100%)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // START AUTO-DISCOVERY
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        startAutoDiscovery(tvStatus)

        // --- SENSITIVITY SLIDERS SETUP ---
        val seekGyro = findViewById<SeekBar>(R.id.seekGyro)
        seekGyro.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Max is 200. Progress 100 = 1.0f, Progress 200 = 2.0f
                gyroSensitivity = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val seekVib = findViewById<SeekBar>(R.id.seekVib)
        seekVib.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Max is 100. Progress 100 = 1.0f, Progress 50 = 0.5f
                vibMultiplier = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // TOGGLES
        val btnGyro = findViewById<Button>(R.id.btnGyro)
        btnGyro.setOnClickListener { view ->
            if (isLayoutEditMode) return@setOnClickListener
            animateButton(view, MotionEvent.ACTION_DOWN)
            isGyroEnabled = !isGyroEnabled
            if (isGyroEnabled) {
                btnGyro.text = "GYRO: ON"
                btnGyro.setBackgroundColor(Color.parseColor("#00AA00"))
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            } else {
                btnGyro.text = "GYRO: OFF"
                btnGyro.setBackgroundColor(Color.parseColor("#AA0000"))
                sensorManager.unregisterListener(this)
                networkExecutor.execute { outStream?.println("JOY_L:0.0,0.0") }
            }
            animateButton(view, MotionEvent.ACTION_UP)
        }

        val btnVibration = findViewById<Button>(R.id.btnVibration)
        btnVibration.setOnClickListener { view ->
            if (isLayoutEditMode) return@setOnClickListener
            animateButton(view, MotionEvent.ACTION_DOWN)
            isVibrationEnabled = !isVibrationEnabled
            if (isVibrationEnabled) {
                btnVibration.text = "VIB: ON"
                btnVibration.setBackgroundColor(Color.parseColor("#00AA00"))
            } else {
                btnVibration.text = "VIB: OFF"
                btnVibration.setBackgroundColor(Color.parseColor("#AA0000"))
                getVibratorService().cancel()
            }
            animateButton(view, MotionEvent.ACTION_UP)
        }

        val btnVol = findViewById<Button>(R.id.btnVol)
        btnVol.setOnClickListener { view ->
            if (isLayoutEditMode) return@setOnClickListener
            animateButton(view, MotionEvent.ACTION_DOWN)
            isVolKeysEnabled = !isVolKeysEnabled
            if (isVolKeysEnabled) {
                btnVol.text = "VOL: ON"
                btnVol.setBackgroundColor(Color.parseColor("#00AA00"))
                Toast.makeText(this, "Vol Up = RT | Vol Down = LT", Toast.LENGTH_SHORT).show()
            } else {
                btnVol.text = "VOL: OFF"
                btnVol.setBackgroundColor(Color.parseColor("#AA0000"))
            }
            animateButton(view, MotionEvent.ACTION_UP)
        }

        val btnLayout = findViewById<Button>(R.id.btnLayout)
        btnLayout.setOnClickListener { view ->
            animateButton(view, MotionEvent.ACTION_DOWN)
            isLayoutEditMode = !isLayoutEditMode
            if (isLayoutEditMode) {
                btnLayout.text = "LAYOUT: ON"
                btnLayout.setBackgroundColor(Color.parseColor("#00AA00"))
                Toast.makeText(this, "EDIT MODE: Tap a button to drag its group. Use Vol +/- to resize.", Toast.LENGTH_LONG).show()
            } else {
                btnLayout.text = "LAYOUT: OFF"
                btnLayout.setBackgroundColor(Color.parseColor("#333333"))
                selectedEditGroup?.alpha = 1.0f
                selectedEditGroup = null
            }
            animateButton(view, MotionEvent.ACTION_UP)
        }

        val draggableGroups = listOf(
            R.id.actionGroup, R.id.dpadGroup, R.id.joyBaseLeft, R.id.joyBaseRight,
            R.id.leftBumpers, R.id.rightBumpers, R.id.centerControls
        )
        for (id in draggableGroups) {
            findViewById<View>(id)?.let { makeDraggable(it) }
        }

        setupButton(findViewById(R.id.btnA), "A")
        setupButton(findViewById(R.id.btnB), "B")
        setupButton(findViewById(R.id.btnX), "X")
        setupButton(findViewById(R.id.btnY), "Y")
        setupButton(findViewById(R.id.btnUp), "UP")
        setupButton(findViewById(R.id.btnDown), "DOWN")
        setupButton(findViewById(R.id.btnLeft), "LEFT")
        setupButton(findViewById(R.id.btnRight), "RIGHT")
        setupButton(findViewById(R.id.btnStart), "START")
        setupButton(findViewById(R.id.btnSelect), "SELECT")

        setupButton(findViewById(R.id.btnLB), "LB")
        setupButton(findViewById(R.id.btnRB), "RB")
        setupTrigger(findViewById(R.id.btnLT), "LT")
        setupTrigger(findViewById(R.id.btnRT), "RT")

        setupJoystick(findViewById(R.id.joyBaseLeft), findViewById(R.id.joyStickLeft), "JOY_L")
        setupJoystick(findViewById(R.id.joyBaseRight), findViewById(R.id.joyStickRight), "JOY_R")
    }

    // --- AUTO-DISCOVERY ENGINE ---
    private fun startAutoDiscovery(tvStatus: TextView) {
        thread {
            var udpSocket: DatagramSocket? = null
            try {
                udpSocket = DatagramSocket(null)
                udpSocket.reuseAddress = true
                udpSocket.bind(InetSocketAddress(5005))

                val buffer = ByteArray(256)
                val packet = DatagramPacket(buffer, buffer.size)

                while (!isConnected) {
                    udpSocket.receive(packet)
                    val msg = String(packet.data, 0, packet.length).trim()

                    if (msg == "JOYPAD_SERVER") {
                        val serverIp = packet.address.hostAddress
                        udpSocket.close()
                        connectToPC(serverIp, tvStatus)
                        break
                    }
                }
            } catch (e: Exception) {
                udpSocket?.close()
            }
        }
    }

    private fun connectToPC(ip: String?, tvStatus: TextView) {
        if (ip == null) return
        networkExecutor.execute {
            try {
                socket?.close()
                socket = Socket(ip, 5000)
                outStream = PrintWriter(socket!!.getOutputStream(), true)
                isConnected = true
                runOnUiThread {
                    tvStatus.text = "🎮 CONNECTED: $ip"
                    tvStatus.setTextColor(Color.parseColor("#00AA00"))
                }
                startRumbleListener()
            } catch (e: Exception) {
                isConnected = false
                runOnUiThread {
                    tvStatus.text = "⚠️ CONNECTION FAILED. RETRYING..."
                    tvStatus.setTextColor(Color.parseColor("#AA0000"))
                }
                startAutoDiscovery(tvStatus)
            }
        }
    }

    // --- VOLUME KEY OVERRIDE ---
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isLayoutEditMode && selectedEditGroup != null) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                selectedEditGroup!!.scaleX += 0.05f
                selectedEditGroup!!.scaleY += 0.05f
                return true
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (selectedEditGroup!!.scaleX > 0.4f) {
                    selectedEditGroup!!.scaleX -= 0.05f
                    selectedEditGroup!!.scaleY -= 0.05f
                }
                return true
            }
        }
        if (isVolKeysEnabled && !isLayoutEditMode) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                networkExecutor.execute { outStream?.println("RT:1.0") }
                return true
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                networkExecutor.execute { outStream?.println("LT:1.0") }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (isLayoutEditMode && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return true
        }
        if (isVolKeysEnabled && !isLayoutEditMode) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                networkExecutor.execute { outStream?.println("RT:0.0") }
                return true
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                networkExecutor.execute { outStream?.println("LT:0.0") }
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    // --- DRAG ENGINE ---
    @SuppressLint("ClickableViewAccessibility")
    private fun makeDraggable(viewGroup: View) {
        viewGroup.setOnTouchListener { v, event ->
            if (!isLayoutEditMode) return@setOnTouchListener false

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    selectedEditGroup?.alpha = 1.0f
                    selectedEditGroup = v
                    v.alpha = 0.6f

                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate().x(event.rawX + dX).y(event.rawY + dY).setDuration(0).start()
                }
            }
            true
        }
    }

    private fun handleDrag(group: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                selectedEditGroup?.alpha = 1.0f
                selectedEditGroup = group
                group.alpha = 0.6f

                dX = group.x - event.rawX
                dY = group.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                group.x = event.rawX + dX
                group.y = event.rawY + dY
            }
        }
        return true
    }

    // --- SENSORS & BUTTONS ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isGyroEnabled || event == null || isLayoutEditMode) return

        // Multiply by the custom slider value
        val yTilt = ((event.values[1] / 5.0f) * gyroSensitivity).coerceIn(-1.0f, 1.0f)
        val xTilt = ((event.values[0] / 5.0f) * gyroSensitivity).coerceIn(-1.0f, 1.0f)

        networkExecutor.execute { outStream?.println("JOY_L:$yTilt,$xTilt") }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun animateButton(view: View, action: Int) {
        if (action == MotionEvent.ACTION_DOWN) {
            view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(50).start()
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupButton(button: Button, commandName: String) {
        button.setOnTouchListener { view, event ->
            if (isLayoutEditMode) {
                return@setOnTouchListener handleDrag(view.parent as View, event)
            }
            animateButton(view, event.action)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    networkExecutor.execute { outStream?.println("$commandName:1") }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    networkExecutor.execute { outStream?.println("$commandName:0") }
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTrigger(button: Button, commandName: String) {
        button.setOnTouchListener { view, event ->
            if (isLayoutEditMode) {
                return@setOnTouchListener handleDrag(view.parent as View, event)
            }
            animateButton(view, event.action)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    networkExecutor.execute { outStream?.println("$commandName:1.0") }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    networkExecutor.execute { outStream?.println("$commandName:0.0") }
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupJoystick(base: View, stick: View, commandName: String) {
        base.setOnTouchListener { _, event ->
            if (isLayoutEditMode) {
                return@setOnTouchListener handleDrag(base, event)
            }

            val maxTravelRadius = (base.width / 2f) - (stick.width / 2f)
            var dx = event.x - (base.width / 2f)
            var dy = event.y - (base.height / 2f)
            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()

            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (distance > maxTravelRadius) {
                        dx = dx * maxTravelRadius / distance
                        dy = dy * maxTravelRadius / distance
                    }
                    stick.translationX = dx
                    stick.translationY = dy

                    val nx = (dx / maxTravelRadius).coerceIn(-1f, 1f)
                    val ny = (dy / maxTravelRadius).coerceIn(-1f, 1f)

                    if (!(isGyroEnabled && commandName == "JOY_L")) {
                        networkExecutor.execute { outStream?.println("$commandName:$nx,$ny") }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stick.animate().translationX(0f).translationY(0f).setDuration(100).start()
                    if (!(isGyroEnabled && commandName == "JOY_L")) {
                        networkExecutor.execute { outStream?.println("$commandName:0.0,0.0") }
                    }
                }
            }
            true
        }
    }

    private fun startRumbleListener() {
        thread {
            try {
                val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                while (isConnected) {
                    val line = reader.readLine() ?: break
                    if (line.startsWith("VIB:")) {
                        val parts = line.split(":")
                        if (parts.size == 3) {
                            handleGameVibration(parts[1].toInt(), parts[2].toInt())
                        }
                    }
                }
            } catch (e: Exception) { isConnected = false }
        }
    }

    private fun handleGameVibration(large: Int, small: Int) {
        // Apply slider multiplier and kill it immediately if set to 0
        if (!isVibrationEnabled || vibMultiplier <= 0.0f) {
            getVibratorService().cancel()
            return
        }

        val maxRaw = maxOf(large, small)
        val finalIntensity = (maxRaw * vibMultiplier).toInt().coerceIn(0, 255)

        val vibrator = getVibratorService()
        if (!vibrator.hasVibrator()) return

        if (finalIntensity == 0) vibrator.cancel()
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = if (finalIntensity < 1) 1 else finalIntensity
                vibrator.vibrate(VibrationEffect.createOneShot(300, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(300)
            }
        }
    }

    private fun getVibratorService(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        networkExecutor.shutdown()
    }
}