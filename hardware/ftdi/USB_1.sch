v 20030223
C 7200 81100 1 0 0 FT232BM.sym
{
T 7500 87000 5 10 1 1 0 0
refdes=U1
}
C 2000 84600 1 0 0 connector4-1.sym
{
T 2000 86000 5 10 1 1 0 0
refdes=CONN1
T 2000 84400 5 10 0 1 0 0
footprint=USB-B
T 2000 84400 5 10 1 1 0 0
device=USB-B
}
C 4000 85300 1 0 0 resistor-2.sym
{
T 4200 85600 5 10 1 1 0 0
refdes=R1
T 4600 85600 5 10 1 1 0 0
value=27R
T 4000 85300 5 10 0 1 0 0
footprint=CHIP 805
}
C 4900 85000 1 0 0 resistor-2.sym
{
T 5100 85300 5 10 1 1 0 0
refdes=R2
T 5500 85300 5 10 1 1 0 0
value=27R
T 4900 85000 5 10 0 1 0 0
footprint=CHIP 805
}
C 4000 83100 1 90 0 resistor-2.sym
{
T 3700 83300 5 10 1 1 90 0
refdes=R3
T 4000 83100 5 10 1 1 0 0
value=10k
T 4000 83100 5 10 0 1 0 0
footprint=CHIP 805
}
C 4000 86400 1 90 0 resistor-2.sym
{
T 3700 86600 5 10 1 1 90 0
refdes=R4
T 4000 86400 5 10 1 1 0 0
value=100k
T 4000 86400 5 10 0 1 0 0
footprint=CHIP 805
}
C 6000 83700 1 90 0 resistor-2.sym
{
T 5700 83900 5 10 1 1 90 0
refdes=R5
T 6000 83700 5 10 1 1 0 0
value=1k5
T 6000 83700 5 10 0 1 0 0
footprint=CHIP 805
}
C 8800 87800 1 0 0 resistor-2.sym
{
T 9000 88100 5 10 1 1 0 0
refdes=R6
T 9400 88100 5 10 1 1 0 0
value=470R
T 8800 87800 5 10 0 1 0 0
footprint=CHIP 805
}
N 3700 85400 4000 85400 4
N 4900 85400 5000 85400 4
N 5000 85400 5000 85600 4
N 5000 85600 7200 85600 4
N 3700 85100 4900 85100 4
N 5800 85100 5900 85100 4
N 5900 85000 7200 85000 4
C 10800 87000 1 90 0 capacitor-1.sym
{
T 10300 87200 5 10 1 1 90 0
refdes=C1
T 10700 87100 5 10 1 1 0 0
value=100n
T 10800 87000 5 10 0 1 0 0
footprint=CHIP 805
}
C 6200 86100 1 90 0 capacitor-1.sym
{
T 5700 86300 5 10 1 1 90 0
refdes=C2
T 6100 86200 5 10 1 1 0 0
value=33n
T 6200 86100 5 10 0 1 0 0
footprint=CHIP 805
}
C 1800 81800 1 90 0 capacitor-1.sym
{
T 1300 82000 5 10 1 1 90 0
refdes=C3
T 1100 82400 5 10 1 1 0 0
value=100n
T 1800 81800 5 10 0 1 0 0
footprint=CHIP 805
}
C 2500 81800 1 90 0 capacitor-1.sym
{
T 2000 82000 5 10 1 1 90 0
refdes=C4
T 1800 82400 5 10 1 1 0 0
value=100n
T 2500 81800 5 10 0 1 0 0
footprint=CHIP 805
}
C 4500 83300 1 180 1 2N3904-1.sym
{
T 5400 82800 5 10 1 1 180 6
refdes=Q1
T 4500 83300 5 10 0 1 0 0
footprint=SOT23
}
C 2700 82700 1 270 0 capacitor-2.sym
{
T 3200 82500 5 10 1 1 270 0
refdes=C5
T 3000 82000 5 10 1 1 0 0
value=10u
T 2700 82700 5 10 0 1 0 0
footprint=CAP
}
C 8200 88200 1 0 0 vcc-1.sym
{
T 8200 88200 5 10 0 1 0 0
net=VCC:1
}
C 2100 82900 1 0 0 vcc-1.sym
{
T 2100 82900 5 10 0 1 0 0
net=VCC:1
}
C 2200 81300 1 0 0 gnd-1.sym
C 10500 86600 1 0 0 gnd-1.sym
C 5900 85700 1 0 0 gnd-1.sym
N 6000 86000 6000 86100 4
C 3700 87600 1 0 0 vcc-1.sym
{
T 3700 87600 5 10 0 1 0 0
net=VCC:1
}
N 5900 84600 5900 85100 4
N 5100 83300 5100 83500 4
N 5100 83500 5900 83500 4
N 5900 83500 5900 83700 4
N 3900 83100 3900 82800 4
N 3900 82800 4500 82800 4
N 3900 84000 3900 86400 4
N 3900 85700 3700 85700 4
N 3900 87600 3900 87300 4
N 6000 87000 6500 87000 4
N 6500 87000 6500 86200 4
N 6500 86200 7200 86200 4
N 8400 88200 8400 86800 4
N 8400 87900 8800 87900 4
N 9700 87900 10600 87900 4
N 10600 87000 10600 86900 4
N 7200 84400 6400 84400 4
N 6400 84400 6400 82300 4
N 6400 82300 5100 82300 4
N 2300 82900 2300 82700 4
N 1600 82700 2900 82700 4
N 2300 81600 2300 81800 4
N 1600 81800 2900 81800 4
N 7200 84100 7100 84100 4
N 7100 84100 7100 87900 4
C 3600 84100 1 0 0 gnd-1.sym
N 3700 84400 3700 84800 4
N 7100 87900 8400 87900 4
N 8400 87500 10000 87500 4
N 10000 87500 10000 82300 4
N 10000 82300 9600 82300 4
C 5700 81100 1 90 0 crystal-1.sym
{
T 5400 81300 5 10 1 1 90 0
refdes=U2
T 5800 81300 5 10 1 1 0 0
value=6MHz
T 5700 81100 5 10 0 1 0 0
footprint=SMDQ
}
C 4100 80800 1 0 0 gnd-1.sym
C 4100 81500 1 0 0 gnd-1.sym
C 4400 81600 1 0 0 capacitor-1.sym
{
T 4600 82100 5 10 1 1 0 0
refdes=C6
T 4400 81600 5 10 1 1 0 0
value=27p
T 4400 81600 5 10 0 1 0 0
footprint=CHIP 603
}
C 4400 80900 1 0 0 capacitor-1.sym
{
T 4600 81400 5 10 1 1 0 0
refdes=C7
T 4400 80900 5 10 1 1 0 0
value=27p
T 4400 80900 5 10 0 1 0 0
footprint=CHIP 603
}
N 4200 81800 4400 81800 4
N 4200 81100 4400 81100 4
N 5300 81800 6600 81800 4
N 6600 81800 6600 83500 4
N 6600 83500 7200 83500 4
N 7200 82900 6700 82900 4
N 6700 82900 6700 81100 4
N 5300 81100 6700 81100 4
C 6800 79100 1 0 0 resistor-2.sym
{
T 7000 79400 5 10 1 1 0 0
refdes=R7
T 7300 79400 5 10 1 1 0 0
value=10k
T 6800 79100 5 10 0 1 0 0
footprint=CHIP 805
}
C 6900 79900 1 90 0 resistor-2.sym
{
T 6600 80100 5 10 1 1 90 0
refdes=R8
T 6600 80500 5 10 1 1 90 0
value=2k2
T 6900 79900 5 10 0 1 0 0
footprint=CHIP 805
}
N 6800 79900 7200 79900 4
N 6800 79900 6800 79200 4
C 7600 79300 1 0 0 vcc-1.sym
{
T 7600 79300 5 10 0 1 0 0
net=VCC:1
}
N 7800 79300 7800 79200 4
N 7800 79200 7700 79200 4
N 7200 80100 7000 80100 4
N 6800 80800 7000 80800 4
N 7000 80100 7000 82000 4
N 7000 82000 7200 82000 4
N 7200 82300 7100 82300 4
N 7100 82300 7100 80300 4
N 7100 80300 7200 80300 4
N 7200 82600 6900 82600 4
N 6900 82600 6900 81100 4
N 6900 81100 7200 81100 4
N 7200 81100 7200 80500 4
C 12100 83400 1 0 0 vcc-1.sym
{
T 12100 83400 5 10 0 1 0 0
net=VCC:1
}
C 11300 83100 1 0 0 resistor-2.sym
{
T 11500 83400 5 10 1 1 0 0
refdes=R9
T 11900 83400 5 10 1 1 0 0
value=220
T 11300 83100 5 10 0 1 0 0
footprint=CHIP 805
}
C 11300 82800 1 0 0 resistor-2.sym
{
T 11500 82600 5 10 1 1 0 0
refdes=R10
T 11900 82600 5 10 1 1 0 0
value=220
T 11300 82800 5 10 0 1 0 0
footprint=CHIP 805
}
C 11200 83300 1 180 0 led-2.sym
{
T 10500 83500 5 10 1 1 180 0
refdes=D1
T 11200 83300 5 10 0 1 0 0
footprint=CHIP_POL 805
}
C 11200 83000 1 180 0 led-2.sym
{
T 10500 82700 5 10 1 1 180 0
refdes=D2
T 11200 83000 5 10 0 1 0 0
footprint=CHIP_POL 805
}
N 9600 83200 10300 83200 4
N 9600 82900 10300 82900 4
N 11200 83200 11300 83200 4
N 11200 82900 11300 82900 4
N 12200 82900 12300 82900 4
N 12300 82900 12300 83400 4
N 12200 83200 12300 83200 4
C 13500 86700 1 0 0 vcc-1.sym
{
T 13500 86700 5 10 0 1 0 0
net=VCC:1
}
C 13600 83300 1 0 0 gnd-1.sym
N 13700 86700 13700 86500 4
N 13700 83800 13700 83600 4
N 9600 86200 11400 86200 4
N 10600 88200 10600 87900 4
C 10400 88200 1 0 0 generic-power.sym
{
T 10600 88450 5 10 1 1 0 3
net=AVCC:1
}
C 13100 83300 1 0 0 gnd-1.sym
{
T 12900 83100 5 10 1 1 0 0
net=AGND:1
}
N 13200 83800 13200 83600 4
N 9600 85600 11600 85600 4
N 9600 85000 11800 85000 4
N 9600 84400 12000 84400 4
C 16100 86800 1 90 0 header10-2.sym
{
T 14000 87400 5 10 1 1 90 0
refdes=J2
}
N 13700 86500 14300 86500 4
N 14300 86500 14300 86800 4
N 11400 86200 11400 88400 4
N 11400 88400 14300 88400 4
N 14300 88400 14300 88200 4
N 9600 85900 14700 85900 4
N 14700 85900 14700 86800 4
N 11600 85600 11600 88600 4
N 11600 88600 14700 88600 4
N 14700 88600 14700 88200 4
N 9600 85300 15100 85300 4
N 15100 85300 15100 86800 4
N 11800 85000 11800 88800 4
N 11800 88800 15100 88800 4
N 15100 88800 15100 88200 4
N 9600 84700 15500 84700 4
N 15500 84700 15500 86800 4
N 12000 84400 12000 89000 4
N 12000 89000 15500 89000 4
N 15500 89000 15500 88200 4
N 9600 84100 15900 84100 4
N 15900 84100 15900 86800 4
N 13200 83800 16400 83800 4
N 16400 83800 16400 88400 4
N 16400 88400 15900 88400 4
N 15900 88400 15900 88200 4
C 7200 79800 1 0 0 93Cx6-1.sym
{
T 8200 80800 5 10 1 1 0 0
refdes=U3
T 7200 79800 5 10 0 1 0 0
footprint=SO 8
T 8100 79700 5 10 0 1 0 0
device=93C56
}
