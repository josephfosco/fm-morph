import board, digitalio, time, sys

led = digitalio.DigitalInOut(board.D13)
led.direction = digitalio.Direction.OUTPUT

cnt = 0

while True:
      led.value = not led.value
      cnt = (cnt + 1) % 10
      # print(cnt, end="")
      # always send 3 chars with leading zeros
      sys.stdout.write("{:03}".format(cnt))
      time.sleep(2.0)
