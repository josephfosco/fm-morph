from analogio import AnalogIn
import adafruit_dotstar as dotstar
import board
import sys
import time

# Analog input on A1
analog0in = AnalogIn(board.A0)
analog1in = AnalogIn(board.A1)
analog2in = AnalogIn(board.A2)

class BtnCntrlr:
    def __init__(self):
        # Controller Board state vars
        self.bank = 0
        self.bank_btn_released = True

        self.btn = None
        self.btn_released = False
        self.pot_set = False
        self.base_pot_vltg = None
        self.last_pot_val = None

        self.gate_val = 0
        self.gate_btn_released = True

        # One pixel connected internally!
        self.dot = dotstar.DotStar(board.APA102_SCK,
                                   board.APA102_MOSI,
                                   1,
                                   brightness=0.2)
        self.RED = [255, 0, 0]
        self.YELLOW = [255, 255, 0]
        self.GREEN = [0, 255, 0]
        self.BLUE = [0, 0, 255]
        self.colors = [self.RED, self.YELLOW, self.GREEN, self.BLUE]
        self.dot[0] = self.colors[self.bank]

    def next_bank(self):
        if self.bank_btn_released:
            self.bank = (self.bank + 1) % 4
            self.bank_btn_released = False
            self.btn = None
            self.pot_set = False
            self.base_pot_vltg = None
            self.last_pot_val = None
            self.dot[0] = self.colors[self.bank]

    def gate(self):
        if self.gate_btn_released:
            self.gate_btn_released = False
            self.gate_val = (self.gate_val + 1) % 2
            return self.gate_val
        else:
            return None

    def set_board_vars(self, btn_num):
        if self.btn == btn_num:
            if self.btn_released:
                self.base_pot_vltg = getVoltage(analog1in)
                self.last_pot_val = 0
                self.btn_released = False
                self.pot_set = True
            else:
                pass
        else:
            self.btn = btn_num
            self.btn_released = False
            self.pot_set = False
            self.base_pot_vltg = None
            self.last_pot_val = None

    def no_btn(self):
        self.btn_released = True
        self.gate_btn_released = True
        self.bank_btn_released = True

    def check_pot(self, new_pot_vltg):
        # returns an int difference of new - last between 0 and 100
        if self.pot_set:
            new_pot_val = round((new_pot_vltg - self.base_pot_vltg) * 30.303030)
            val_diff = round(new_pot_val - self.last_pot_val)
            if abs(val_diff) == 1:
                val_diff = 0
            else:
                self.last_pot_val = new_pot_val
            return val_diff
        else:
            return 0

    def get_btn(self):
        return self.bank, self.btn

    def print_state(self):
        print("bank: {}".format(self.bank))
        print("bank_btn_released: {}".format(self.bank_btn_released))
        print("btn: {}".format(self.btn))
        print("btn_released: {}".format(self.btn_released))
        print("gate_val: {}".format(self.gate_val))
        print("gate_btn_released: {}".format(self.gate_btn_released))
        print("pot_set: {}".format(self.pot_set))
        print("base_pot_vltg: {}".format(self.base_pot_vltg))
        print("last_pot_val: {}".format(self.last_pot_val))

######################### HELPERS ##############################

# Helper to convert analog input to voltage
def getVoltage(pin):
    return (pin.value * 3.3) / 65536

def send_data(val1, val2, val3):
    # sends one "data packet"
    # a data packet is always 6 bytes
    # GENERALLY the first byte is the bank (0 - 4)
    # the second byte is the button (0 - 9)
    # the third thru sixth bytes are the difference in
    #   pot value (aprox -100 to 100)
    sys.stdout.write("{:01}".format(val1))
    sys.stdout.write("{:01}".format(val2))
    sys.stdout.write("{:04}".format(val3))

def send_gate(gate_val):
    # standard values for a trigger
    send_data(9, 9, gate_val)

def print_board_status():
    return
    # Read analog voltages
    print("*****")
    print("A0: %0.7f" % getVoltage(analog0in))
    print("A1: %0.7f" % getVoltage(analog1in))
    cntrlr.print_state()


######################### MAIN LOOP ##############################

cntrlr = BtnCntrlr()
i = 0

while True:

    btn_vltg = getVoltage(analog0in)

    if btn_vltg < 0.35:    # 0.0  - no button
        cntrlr.no_btn()
    elif btn_vltg < 0.5:   # 0.34
        cntrlr.set_board_vars(7)
    elif btn_vltg < 0.8:   # 0.69
        cntrlr.set_board_vars(6)
    elif btn_vltg < 1.2:   # 1.03
        cntrlr.set_board_vars(5)
    elif btn_vltg < 1.5:   # 1.37
        cntrlr.set_board_vars(4)
    elif btn_vltg < 1.75:   # 1.72
        cntrlr.set_board_vars(3)
    elif btn_vltg < 2.1:   # 2.07
        cntrlr.set_board_vars(2)
    elif btn_vltg < 2.4:   # 2.43
        cntrlr.set_board_vars(1)
    elif btn_vltg < 2.7:   # 2.78
        cntrlr.set_board_vars(0)
    elif btn_vltg < 3.0:   # 3.11 - change bank
        cntrlr.next_bank()
    else:   # 3.43 - gate flip-flop
        new_gate_val = cntrlr.gate()
        if new_gate_val != None:
            send_gate(new_gate_val)

    pot_val = cntrlr.check_pot(getVoltage(analog1in))
    if pot_val:
        bank, btn = cntrlr.get_btn()
        send_data(bank, btn, pot_val)

    i = (i+1) % 512  # run from 0 to x
