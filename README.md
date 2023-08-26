# Octopussy
Java CLI uses Octopus API to analyse Agile tariff electricity consumption.
The recent cost saving is shown compared to the flat rate tariff.
The day ahead unit pricing is shown, together with the best times to start an activity.

The jar has been tested (unchanged) on Raspberry Pi OS and Windows 10.

n.b.
For Windows users running the jar in a console, ANSI colour support should be enabled if not already.

One way to do this is to  use the Registry Editor to
set REG_DWORD VirtualTerminalLevel=1 for Computer\HKEY_CURRENT_USER\Console

Then relaunch a cmd.exe

![EXAMPLE](/assets/Octopussy.JPG?raw=true "Title")

The parameter passed to the jar indicates the number of columns to create on the right hand side of the display.

By default, the display will show a vertical bar to the right of the asterisks that indicate the unit cost in each 30 minute period
A parameter value larger than value 0 will display a 24 hour time stamp HH:MM to assist when viewing the (potentially many) rows
Values larger than 1 will display increasingly more columns, containing average prices for periods of 1hr, 1.5, 2hr, 2.5 3hr... etc up to 9.5

n.b. Typically daily pricing data does not go beyond 22:30 and will be updated around 4pm by Octopus
This means that sometimes the number of columns is truncated, because it is not possible to determine with certainty the cheapest time to start an activity when the end of the period goes beyond the range of available pricing.  Try playing with differnt values and you'll soon get the picture ;-)

Setting my API key

Time to get familiar with editing octopussy.properties which is in the root of the jar
One receomneded way is to use 7-zip

Open the archive, edit the octopussy.properties and save the changes and it will regenerate the jar
You'll need to change at the very least

apiKey=sk_live_BLAH2pPIXOIO72aIO1blah:

electricity.mprn=2000012600000
electricity.sn=21L300071

gas.mprn=887000000
gas.sn=E6S100000061961

flexible.gas.unit=7.61
flexible.gas.standing=27.47

flexible.electricity.unit=30.03
flexible.electricity.standing=47.95

agile.electricity.standing=42.77

zone.id=Europe/London

history=./octopus.import.csv
#
# n.b. Southern England is region H - see https://mysmartenergy.uk/Electricity-Region
#
# if postcode is uncommented it will override region=H based on Octopus API
#
#postcode=SN5

region=H
base.url=https://api.octopus.energy

import.product.code=AGILE-FLEX-22-11-25
tariff.code=E-1R-$import.product.code$-$region$
tariff.url=$base.url$/v1/products/$import.product.code$/electricity-tariffs/$tariff.code$

export.product.code=AGILE-OUTGOING-19-05-13
export.tariff.code=E-1R-$export.product.code$-$region$
export.tariff.url=$base.url$/v1/products/$export.product.code$/electricity-tariffs/$export.tariff.code$

export=false
days=14
plunge=3
target=30
width=63

# in Windows console to show ANSI update Registry set REG_DWORD VirtualTerminalLevel=1 for Computer\HKEY_CURRENT_USER\Console
ansi=true
extra=false



