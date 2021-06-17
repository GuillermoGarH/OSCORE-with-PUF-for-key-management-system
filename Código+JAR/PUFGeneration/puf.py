import spidev
import RPi.GPIO as GPIO
import os

spi = spidev.SpiDev()
spi.open(0,0)
spi.max_speed_hz = 100000
spi.mode = 0b00
spi.no_cs = True

read = 0x03
GPIO.setmode(GPIO.BCM)
GPIO.setup(17,GPIO.OUT)
GPIO.output(17,GPIO.HIGH)

GPIO.output(17,GPIO.LOW)

spi.writebytes([read,0x00,0x00,0x00])
data = spi.readbytes(5000)
stableBits = []

if (os.path.isfile("/home/pi/Documents/ReadV2/ReadSRAM/pufUNF.txt") == False):
    f = open("pufUNF.txt", "w")
    for byteRead in data:
        f.write('{0:08b}'.format(byteRead) + "\n")

    f.close()

else:
    f = open("pufUNF.txt","r")

    for i in range(len(data)):
        readBytes = '{0:08b}'.format(data[i])
        stableBytes = f.readline()

        stableSeq = ""

        for j in range(8):
           # readBit =  (readBytes & (1<< j)) >> j
           # stableBit =  (stableBytes & (1<< j)) >> j
            readBit = readBytes[j]
            stableBit = stableBytes[j]

            if (readBit == stableBit):
                stableSeq = stableSeq + stableBit
            else:
                stableSeq = stableSeq + "X"

        stableBits.append(stableSeq)

    f.close()

    f = open("pufUNF.txt","w")

    for seq in stableBits:
        f.write(seq + "\n")


f1 = open("pufUNF.txt","r")
f2 = open("puf.txt","w")

completeSeq = ""


for byte_to_read in f1:
    for bit in range(0,8):
     #   bit_to_read =  (byte_to_read & (1<< bit)) >> bit
        bit_to_read = byte_to_read[bit]
        if (bit_to_read != "X"):
            completeSeq = completeSeq + bit_to_read
f2.write(completeSeq)


f1.close()
f2.close()
