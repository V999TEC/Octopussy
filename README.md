# Octopussy

There is a wiki giving context about this project here https://github.com/V999TEC/Octopussy/wiki/The-Home-of-Icarus  

Java CLI uses Octopus API to analyse Agile tariff electricity consumption.  
The recent cost saving is shown compared to the flat rate tariff.  
The day ahead unit pricing is shown, together with the best times to start an activity.

The jar is regularly tested (and works unchanged, such is the beauty of java) on Raspberry Pi OS and Windows 10.

n.b.
For Windows users running the jar in a console, ANSI colour support should be enabled if not already.

One way to do this is to  use the Registry Editor to
set REG_DWORD VirtualTerminalLevel=1 for Computer\HKEY_CURRENT_USER\Console

![EXAMPLE](/assets/Octopussy4.JPG?raw=true "Picture 4")

If for some reason you don't want colour support, ensure *ansi=false* in the properties file, otherwise ansi control characters will ruin the formatting.

After registry editing always relaunch a cmd.exe to pick up any VirtualTerminalLevel change.

The following picure shows a fully configured execution of the program with some history analysis plus export prices enabled with ansi colour support.
OOTB the basic configuation will be much simpler and not show export or history details. All is controlled by the properties file.

In the picture below you will see a couple of zones with colour. These are controlled by colour=GREEN and color=RED to signify optimum import and export respectively.

![EXAMPLE](/assets/Octopussy6.JPG?raw=true "Picture 6")

The zones show the lowest and highest calulated _**average**_ unit prices for the given time period.

For instance, looking at the import average prices (colour=GREEN) while the cheapest 30-min slot appears to be 12:30, as indicated by the green asterisks.  
If you have an electrical load that needs an hour to run, then it is best to start earlier at 12 noon (where the average price for the two 30-min periods is 16.85p).  
Going down the same 1hr column, we note that delaying the start to 12:30 would give an average price of 17.08p and 1:00 pm would be 17.53p. At 5:00 pm the average price is over 40p!

In contrast the export prices (color=RED) show the best times to start export activity.  
A 1hr export is optimal at 6:00pm since the average price is 23.02p (thus 2 x 23.02p per unit exported)  
However if you have enough stored energy to export to the grid over 3 hours then it is much better to start at 4:00 pm when the average price will be 19.98p ( 6 x 19.98 per unit exported)

```
java -jar octopussy.jar  N  My.properties
```
The first parameter passed to the jar indicates the number of columns to create on the right hand side of the display.  
N=7 is good for up to 4-hr.  
The second parameter is the name of a property file containing details of the tariffs and other configuration data plus the api.key
Since the property file needs to hold the api.key do not share this property file.

If no property file is specified, a default built into the jar will be used. Obviously this does not have the correct api.key or meter reference numbers etc so an exception will be generated.
Once you have established your own properties file with the correct details, it is possible to use something like 7-Zip to replace the builtin octopussy.properties in the jar with your preferences.
The only advantage is that the external properties file is no longer needed and need not be specified each time java -jar octopussy.jar N is called
As before, do not share anything containing your api.key and that includes the jar that you might have just customised.

The recommended approach is to keep the personalised property file external and always pass its name as the second parameter.

The builtin octopussy.properties can be used to generate a template so that you have an idea of all the possible settings that drive the program

To display the template do the following:

delete the octopus.import.csv file if it exists

```
java -jar octopussy.jar
```
This will cause the program to just display the properties so that you can copy paste into your own property file (let's say it's called My.properties)

Alternatively you could just pipe the output directly to a file name of your choice

```
java -jar octopussy.jar 1>My.properties
```

After editing the property file just created to fix the apiKey etc., remember to specify the file name each time as the second parameter, i.e.:

```
java -jar octopussy.jar 7 My.properties
```

Setting the properties by hand can be a burden, so it is possible to get many of the required settings by passing another parameter blah  
This need only happen once.

```
java -jar octopussy.jar 5 My.properties blah
```

The program may validate existing property values in My.properties and check the products and tariffs implied
This feature is experimental and subject to change.

```
api.solcast=blah_blah_blah
apiKey=blah_BLAH2pMoreBlahPIXOIO72aIO1blah:
#
base.url=https://api.octopus.energy
electricity.mpan.export=2098765432109
electricity.mpan.import=2012345678901
electricity.sn=21L010101
#
region=H
#
# fixed just used for price comparison (Loyal Octopus 16M Fixed February 2025 v1) in region H
fixed.electricity.unit=23.789
fixed.electricity.standing=59.2617
#
fixed.product.code=OE-LOYAL-FIX-16M-25-02-12
fixed.tariff.code=E-1R-$fixed.product.code$-$region$
fixed.tariff.url=$base.url$/v1/products/$fixed.product.code$/electricity-tariffs/$fixed.tariff.code$
#
# Agile Octopus October 2024 v1 - Ending 15/02/2026 
import.electricity.standing=47.9535
import.product.code=AGILE-24-10-01
import.tariff.code=E-1R-$import.product.code$-$region$
import.tariff.url=$base.url$/v1/products/$import.product.code$/electricity-tariffs/$import.tariff.code$
#
# Outgoing Octopus
export.product.code=OUTGOING-VAR-24-10-26
export.tariff.code=E-1R-$export.product.code$-$region$
export.tariff.url=$base.url$/v1/products/$export.product.code$/electricity-tariffs/$export.tariff.code$
export=true
#
zone.id=Europe/London
history.import=./octopus.import.csv
history.export=./octopus.export.csv
baseline=14.75
#
days=10
plunge=3
target=27
width=46
#
# in Windows console to show ANSI update Registry set REG_DWORD VirtualTerminalLevel=1 for Computer\HKEY_CURRENT_USER\Console
#
ansi=true
colour=GREEN
color=RED
#
yearly=false
monthly=false
weekly=false
daily=false
#day.from=2023-08-11
#day.to=2023-09-08
show.recent=true
show.savings=true
limit=30
#
setting=java -jar icarus.jar ./Icarus.properties inverter setting %1 %2 %3
macro=java -jar icarus.jar ./Icarus.properties inverter macro %1 %2 %3 %4
solar=java -jar icarus.jar ./Icarus.properties inverter meter today solar
temperature=java -jar icarus.jar ./Icarus.properties inverter system inverter temperature
battery=java -jar icarus.jar ./Icarus.properties inverter meter today battery
grid=java -jar icarus.jar ./Icarus.properties inverter meter today grid
consumption=java -jar icarus.jar ./Icarus.properties inverter meter today consumption
percent=java -jar icarus.jar ./Icarus.properties inverter system battery percent
forecast=java -jar icarus.jar ./Icarus.properties forecast solar
sun=java -jar icarus.jar ./Icarus.properties sun
#
file.solar=./solar.csv
max.solar=22000
#
max.rate=6000
#
#dfs1=17:30
#dfs2=18:00
#
part1=00:00
part2=08:00R
part3=12:00N
part4=19:00S
#
slots1=4:Night:2
slots2=3:Day
slots3=2:d
slots4=1
#
power1=5000:100:15
power2=2500:50:25
power3=2500:40:20
power4=3000:30:15
#
```

## History data


When the program starts it will look for octopus.import.csv

If it doesn't exist it will create the file and build up history data each time the program has run

The file is useful if you want to import into a spreadsheet etc.

The columns are 
```
Consumption (kWh), Start, End, Price
```

A corresponding file for export history is on the TODO list.

## Bonus feature

In the properties file the key values yearly, monthly, weekly can be set to true or false to produce an analysis over the respective period, for example:

![EXAMPLE](/assets/Octopussy3.JPG?raw=true "Picture 3")

Note the *Recent daily results* are always shown and the days=number in the properties file will guide the number of days to be included in what is considered recent.
The recent data is used to calculate the running average (A) price which dictates where the 'A' is positioned within the asterisks of the horizontal graph

When the consumption results for a day are incomplete, the day is dropped, so the days=N will not always match the number of days actually shown. 

