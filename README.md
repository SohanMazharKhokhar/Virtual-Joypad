# Virtual Joypad 🎮📱

Turn any Android smartphone into a fully functional, highly customizable Xbox 360 controller for your PC over Wi-Fi or USB tethering.

## ✨ Features

* **📡 Auto-Discovery (Zero Setup):** No typing IP addresses! The PC server broadcasts a UDP beacon, and the Android app automatically finds it and connects instantly.
* **🕹️ Drag & Drop Layout Editor:** Don't like the button placement? Tap `LAYOUT: ON` to drag buttons, D-Pads, and joysticks anywhere on your screen. Use your physical volume keys to scale them up or down!
* **👥 Multi-Player Support:** Connect multiple phones simultaneously. The server dynamically assigns them as Player 1, Player 2, Player 3, etc.
* **📳 Haptic Feedback (Rumble):** Feel the game. Gamepad vibration signals from the PC are sent back to the phone in real-time, with an adjustable intensity slider.
* **🏎️ Gyroscope Steering:** Tilt your phone to steer in racing games, complete with an adjustable sensitivity multiplier.
* **🎛️ Hardware Triggers:** Physical Volume Up/Down keys can be hijacked to act as your Left Trigger (LT) and Right Trigger (RT).

## 🛠️ Requirements

**For the PC (Server):**
* Windows OS
* Python 3.x
* The `vgamepad` library (install via `pip install vgamepad`)

**For the Phone (Client):**
* An Android Device
* Connected to the same Wi-Fi network as the PC (or connected via USB Tethering for zero latency).

## 🚀 How to Use

1. **Start the PC Server:** Open your terminal/command prompt and run the Python script:
   ```bash
   python server.py
