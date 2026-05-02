# import socket
# import vgamepad as vg

# # Keep track of the active connection to send vibration data back
# active_connection = None

# # --- VIBRATION LISTENER ---
# def rumble_callback(client, target, large_motor, small_motor, led_number, user_data):
#     """Catches vibration signals from the game and sends them to the phone."""
#     global active_connection
#     if active_connection:
#         try:
#             # large_motor and small_motor are values from 0 to 255
#             msg = f"VIB:{large_motor}:{small_motor}\n"
#             active_connection.sendall(msg.encode('utf-8'))
#         except Exception:
#             pass # Socket might be closed, ignore

# # 1. Create the gamepad and register the vibration listener
# gamepad = vg.VX360Gamepad()
# gamepad.register_notification(callback_function=rumble_callback)
# print("Dual-Sense Virtual Xbox Controller Created!")

# BUTTON_MAP = {
#     "A": vg.XUSB_BUTTON.XUSB_GAMEPAD_A, "B": vg.XUSB_BUTTON.XUSB_GAMEPAD_B,
#     "X": vg.XUSB_BUTTON.XUSB_GAMEPAD_X, "Y": vg.XUSB_BUTTON.XUSB_GAMEPAD_Y,
#     "UP": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP, "DOWN": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN,
#     "LEFT": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT, "RIGHT": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT,
#     "START": vg.XUSB_BUTTON.XUSB_GAMEPAD_START, "SELECT": vg.XUSB_BUTTON.XUSB_GAMEPAD_BACK,
#     "LB": vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_SHOULDER, "RB": vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_SHOULDER
# }

# HOST = '0.0.0.0' 
# PORT = 5000
# print(f"\nWaiting for mobile app connection on port {PORT}...")

# with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
#     s.bind((HOST, PORT))
#     s.listen()
    
#     while True: 
#         conn, addr = s.accept()
#         active_connection = conn # Save connection for the rumble callback
#         with conn:
#             print(f"Phone connected! 2-Way Link Established.")
#             buffer = ""
#             while True:
#                 try:
#                     data = conn.recv(1024)
#                     if not data:
#                         break
                    
#                     buffer += data.decode('utf-8')
#                     commands = buffer.split('\n')
#                     buffer = commands.pop() 
                    
#                     for cmd in commands:
#                         cmd = cmd.strip()
#                         if not cmd: continue
                        
#                         try:
#                             action_type, value = cmd.split(':')
                            
#                             # Buttons & Bumpers
#                             if action_type in BUTTON_MAP:
#                                 if value == "1": gamepad.press_button(button=BUTTON_MAP[action_type])
#                                 elif value == "0": gamepad.release_button(button=BUTTON_MAP[action_type])
                                
#                             # Triggers (LT / RT)
#                             elif action_type == "LT": gamepad.left_trigger_float(value_float=float(value))
#                             elif action_type == "RT": gamepad.right_trigger_float(value_float=float(value))
                                
#                             # Joysticks
#                             elif action_type == "JOY_L":
#                                 x, y = map(float, value.split(','))
#                                 gamepad.left_joystick_float(x_value_float=x, y_value_float=-y)
#                             elif action_type == "JOY_R":
#                                 x, y = map(float, value.split(','))
#                                 gamepad.right_joystick_float(x_value_float=x, y_value_float=-y)
                                
#                         except Exception:
#                             pass
                            
#                     gamepad.update()
#                 except ConnectionResetError:
#                     break
#             active_connection = None
import socket
import threading
import time
import vgamepad as vg

# Global counter to assign Player 1, Player 2, etc.
player_counter = 0

def udp_broadcaster():
    """Broadcasts the server's presence to the local network so phones can auto-connect."""
    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    
    while True:
        try:
            # Shout "JOYPAD_SERVER" to the entire network on port 5005
            udp_socket.sendto(b"JOYPAD_SERVER", ('<broadcast>', 5005))
        except Exception:
            pass
        time.sleep(1) # Broadcast every 1 second

def handle_client(conn, addr, player_num):
    """Handles the TCP connection and gamepad inputs for a single player."""
    print(f"\n[+] Player {player_num} Connected from {addr[0]}")
    gamepad = vg.VX360Gamepad()
    print(f"[*] Virtual Xbox Controller {player_num} Created!")

    # Vibration callback tied to THIS specific phone's socket
    def rumble_callback(client, target, large_motor, small_motor, led_number, user_data):
        try:
            msg = f"VIB:{large_motor}:{small_motor}\n"
            conn.sendall(msg.encode('utf-8'))
        except Exception:
            pass

    gamepad.register_notification(callback_function=rumble_callback)

    BUTTON_MAP = {
        "A": vg.XUSB_BUTTON.XUSB_GAMEPAD_A, "B": vg.XUSB_BUTTON.XUSB_GAMEPAD_B,
        "X": vg.XUSB_BUTTON.XUSB_GAMEPAD_X, "Y": vg.XUSB_BUTTON.XUSB_GAMEPAD_Y,
        "UP": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP, "DOWN": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN,
        "LEFT": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT, "RIGHT": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT,
        "START": vg.XUSB_BUTTON.XUSB_GAMEPAD_START, "SELECT": vg.XUSB_BUTTON.XUSB_GAMEPAD_BACK,
        "LB": vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_SHOULDER, "RB": vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_SHOULDER
    }

    buffer = ""
    try:
        while True:
            data = conn.recv(1024)
            if not data:
                break # Phone disconnected
            
            buffer += data.decode('utf-8')
            commands = buffer.split('\n')
            buffer = commands.pop() 
            
            for cmd in commands:
                cmd = cmd.strip()
                if not cmd: continue
                
                try:
                    action_type, value = cmd.split(':')
                    
                    if action_type in BUTTON_MAP:
                        if value == "1": gamepad.press_button(button=BUTTON_MAP[action_type])
                        elif value == "0": gamepad.release_button(button=BUTTON_MAP[action_type])
                    elif action_type == "LT": gamepad.left_trigger_float(value_float=float(value))
                    elif action_type == "RT": gamepad.right_trigger_float(value_float=float(value))
                    elif action_type == "JOY_L":
                        x, y = map(float, value.split(','))
                        gamepad.left_joystick_float(x_value_float=x, y_value_float=-y)
                    elif action_type == "JOY_R":
                        x, y = map(float, value.split(','))
                        gamepad.right_joystick_float(x_value_float=x, y_value_float=-y)
                except Exception:
                    pass
            
            gamepad.update()
    except ConnectionResetError:
        pass
    finally:
        print(f"[-] Player {player_num} Disconnected.")
        conn.close()

# Start the UDP Broadcaster in the background
threading.Thread(target=udp_broadcaster, daemon=True).start()

# Main TCP Server Loop
HOST = '0.0.0.0' 
PORT = 5000

print(f"Starting Multi-Device Server...")
print(f"Broadcasting Auto-Discovery Signal on UDP 5005...")
print(f"Waiting for players to connect on TCP {PORT}...\n")

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen(4) # Support up to 4 players simultaneously
    
    while True: 
        conn, addr = s.accept()
        player_counter += 1
        # Spawn a new thread for every new phone that connects
        threading.Thread(target=handle_client, args=(conn, addr, player_counter), daemon=True).start()