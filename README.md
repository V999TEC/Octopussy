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

The example above shows that 10am is the cheapest time to start an activity taking 1hr. The price is 15.88p on average.

Another example is that a longer 3.5 hour activity is best started earlier at 09:30 since the average unit price will be 16.47p during the seven 30-minute slots.

Obviously the task requiring electricity generally won't use it evenly across the period, so the program is just a guide, but it's better then a random choice.

By default, the display will show a vertical bar to the right of the asterisks that indicate the unit cost in each 30 minute period
A parameter value larger than value 0 will display a 24 hour time stamp HH:MM to assist when viewing the (potentially many) rows
Values larger than 1 will display increasingly more columns, containing average prices for periods of 1hr, 1.5, 2hr, 2.5 3hr... etc up to 9.5

n.b. Typically daily pricing data does not go beyond 22:30 and will be updated around 4pm by Octopus
This means that sometimes the number of columns is truncated, because it is not possible to determine with certainty the cheapest time to start an activity when the end of the period goes beyond the range of available pricing.  Try playing with differnt values and you'll soon get the picture ;-)

## Setting my API key

Time to get familiar with editing octopussy.properties which is in the root of the jar
One recommended way is simply to use 7-zip

Open the archive, edit the octopussy.properties and save the changes and it will regenerate the jar
You'll need to change at the very least:
```
apiKey=sk_live_BLAH2pPIXOIO72aIO1blah:
electricity.mprn=2000012600000
electricity.sn=21L300071

region=?
```

No doubt you know your region if you have played with the API.
Stick the value in the properties. 
Alternatively put in the first part of the postcode and the API will look up your region (but that wasteful to do every time and it will just return the same answer)

The gas key values are experimental for now and don't do anything useful

Example of octopussy.properties
```
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
```

## Bonus feature

When the program starts it will look for octopus.import.csv

If it doesn't exist it will create the file and build up history data each time the program has run

The file is useful if you want to import into a spreadsheet etc.

The columns are 
```
Consumption (kWh), Start, End, Price
```

