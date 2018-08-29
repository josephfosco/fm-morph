import board, digitalio, time, sys

led = digitalio.DigitalInOut(board.D13)
led.direction = digitalio.Direction.OUTPUT

cnt = 0

while True:
      led.value = not led.value
      cnt = (cnt + 1) % 10
      print(cnt, new="")
      time.sleep(2.0)
