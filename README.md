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

![EXAMPLE](/assets/example_a.JPG?raw=true "Picture 6")

The zones show the lowest and highest calulated _**average**_ unit prices for the given time period.

For instance, looking at the import average prices (colour=GREEN) while the cheapest 1-hour period appears to be 10:00pm but the cheapest 4-hour period starts at 11:00am . 
If you have an electrical load that needs several hours to run, then use the vertical bands of green to guide you to the start of the cheapest contiguous periods.  

We can see from the red banding that 4:00pm to 7:00pm are best avoided (which is usually the case each day).

For example, if we neded to find the best 3-hour period (perhaps to run a domestic apppliance that required a long cycle - such as washing machine or dish washer) then we can easily tell from looking at the 3hr column that the cheapest time to run the appliance will start at noon (average 7.59p / unit) but if we delayed to 4pm it would average 30.68p /unit.  If the program cycle neded a couple of kWhrs then the pricing would be 15p verses 61p and so on.

The "A" that appears in the rows of asterisks indicates the running average unit price that has been achieved over the last week to 10-days (configurable).
In the eaxmple the (A)verage unit price: 18p

The display is designed to update every half-hour, so the "Today's import cost" figure is just an estimate based on the number of units imported so far that day, allowing for standing charge.

The "Plunge price is set to: 3p" is a configurable parameter which highlights when there is very cheap import electricity available.

From 10:30pm onwards we do not know the pricing of the following half-hour slots. That is why some of the averge price calculations are not filled in.
However the pricing usually updates aound 4pm, giving figures up to 10:30pm on the following day.

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

```
java -jar octopussy.jar
```
This will cause the program to just display a minimal set of properties so that you can copy paste into your own property file (let's say it's called My.properties)

```
apiKey=blah_BLAH2pMoreBlahPIXOIO72aIO1blah:
#
base.url=https://api.octopus.energy
#
electricity.mpan.import=2012345678901
electricity.mpan.export=2098765432109
electricity.sn=21L010101
#
export=true
fixed.product.code=OE-LOYAL-FIX-16M-25-02-12
import.product.code=AGILE-24-10-01
export.product.code=OUTGOING-VAR-24-10-26
```

Alternatively you could just pipe the output directly to a file name of your choice

```
java -jar octopussy.jar 1>My.properties
```

Edit the property file just created to fix the apiKey, mpans and serial number etc., 
Override the product.code values with the actual Octopus product names you intend to use

To verify the product.codes and generate a whole lot more property values (which allows a lot of fine tuning subsequently),  do this:

```
java -jar octopussy.jar 7 My.properties  verify
```

This will display an expanded set of property values which should be inserted into My.properies 
For example, here is a subset of the expanded properties. 
Notice a region value has been generated and some comments expanded.

```
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
# Agile Octopus October 2024 v1 
import.electricity.standing=47.9535
import.product.code=AGILE-24-10-01
import.tariff.code=E-1R-$import.product.code$-$region$
import.tariff.url=$base.url$/v1/products/$import.product.code$/electricity-tariffs/$import.tariff.code$
#
# Outgoing Octopus
export.product.code=OUTGOING-VAR-24-10-26
export.tariff.code=E-1R-$export.product.code$-$region$
export.tariff.url=$base.url$/v1/products/$export.product.code$/electricity-tariffs/$export.tariff.code$
```

Once there are no errors and you are satisfied the verify has worked, drop the verify parameter from the command line

```
java -jar octopussy.jar 7 My.properties
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

