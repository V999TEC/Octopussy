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

Setting the properties by hand can be a burden, so it is possible to get many of the required settings by passing the Account ID as the third parameter.  
This need only happen once.

```
java -jar octopussy.jar 7 A-12345678
```

Substitute the A-12345678 for your Octopus Energy account number.  The program will then generate the actual values taken from the account, for example:

```
base.url=https://api.octopus.energy
electricity.mprn=200001010163
electricity.sn=21L010101
gas.mprn=8870000400
gas.sn=E6S10000061961
flexible.electricity.via.direct.debit=true
flexible.electricity.product.code=VAR-22-11-01
flexible.electricity.unit=30.295124
flexible.electricity.standing=47.9535
agile.electricity.standing=42.7665
import.product.code=AGILE-FLEX-22-11-25
tariff.code=E-1R-AGILE-FLEX-22-11-25-H
tariff.url=https://api.octopus.energy/v1/products/AGILE-FLEX-22-11-25/electricity-tariffs/E-1R-AGILE-FLEX-22-11-25-H
region=H
#postcode=SN5
```

The above values are matched to the specified property file and a warning is given for any discrepencies.

Copy the values into the property file and run the program again.

Once no discrepencies are noted then the Account ID can be dropped from the 3rd parameter.

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


## Other settings in properties file

Most of these should be self explanatory

```
zone.id=Europe/London
history=./octopus.import.csv
#
export.product.code=AGILE-OUTGOING-19-05-13
export.tariff.code=E-1R-AGILE-OUTGOING-19-05-13-H
export.tariff.url=https://api.octopus.energy/v1/products/AGILE-OUTGOING-19-05-13/electricity-tariffs/E-1R-AGILE-OUTGOING-19-05-13-H
export=false
#
days=10
plunge=3
target=30
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
weekly=true
#
extra=false
referral=https://share.octopus.energy/ice-camel-111
```
