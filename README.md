KPix EventDisplay
=================

Usage
-----

The compiled, executable JAR should be run either directly (prohibiting the use of runtime arguments) or from the command line using:

  `java -jar EventDisplay.jar`
  
There are three optional flags which may be specified as arguments in the command line.

"-b" : Denotes that the relative path+name for a binary data file will follow any flags
"-c" : Attempt to calibrate scaling with the specified .bin file at runtime
"--usage" : Print this information, and exit.

Typical usage would be:
  
  `java -jar EventDisplay.jar -bc path/name_of_data_file.bin`
  
...which loads the file at ./path/name_of_data_file.bin and immediately attempts to calibrate.

Running from the command line without specifying flags is equivalent to running the executable JAR by double-clicking, and in the latter case any error output will be saved as a log.
Both loading .bin files and calibration can be accomplished from within the GUI of the display at any point after execution.