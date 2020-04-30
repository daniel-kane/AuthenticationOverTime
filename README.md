# AuthenticationOverTime

The beginning steps for a prototype for a behavior-based continous authentication system. 

## Usage

Clone this repository into an Android studio project, and run from either emulator or phone. 

The UI doesn't really provide much since the nature of this application is to run in the background. 

NOTE: on the first install of the program, the application should prompt the user for permission to use the location. This
operates as normal, but might not begin showing / getting the location data immediately. A rerun (NOT reinstall) is necessary
to begin getting location data.

SIMILAR NOTE: the data in this application is stored on a csv file. It is set to append mode, so a complete reinstall will erase
this csv file. Subsequent reruns will just append the data. 

### To See Location and App Data

The UI will show the most recent location. However, to see the program properly writing to and reading from the csv file
along with the timestamp, check the system output in the terminal or logcat. See note above if location data is not being displayed

The same is true for App data. 

## References and Resources

The code for the location data was adapted from a tutorial by Ferdousur Rahman Sarker, which can be found at the link below:
https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html

The code for the app usage statistics was adapted from Cole Murray from the following github:
https://github.com/ColeMurray/UsageStatsSample/tree/master/app
