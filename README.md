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

Configuration Files
-------------------

The state of the detector can be read or generated as a series of properties and options, in the format:

    property1 : option
    property2 : option
    ...
    
Configuration files are saved by convention with a `.config` suffix, but file names are not required to follow that pattern.
Necessary syntax is limited to separating properties by line breaks, separating properties from options by a single colon (:) character, and (when required) separating multiple options with a single comma (,).
Properties and their corresponding options are evaluated as follows (in the order specified by the file):

    browse : [path+file]                Load the .bin data file at the specified (relative or absolute) path.
    calibrate : [true/false]            If true, this is equivalent to pressing the 'calibrate' button in the GUI.
    scale : [float]                     Set the color scaling coefficient to the provided floating-point value.
    update-live : [true/false]          If true, GUI data tables will update each frame while the data file is playing.
    adjust : [true/false]               If true, automatically subtract minima from ADC and mean values in GUI.
    labels : [true/false]               Toggle detector labels on/off.
    label-type : [ADC/ADC - min/delta/% delta/indices]    Set detector labels to provided mode.
    display : [calib/abs]               Set detector display mode to calibrated or absolute scaling.
    speed : [0-100]                     Set data playback speed to the provided value (percentage).
    zoom : [float]                      Set distance from the detector - negative values are "between" detector and user.
    axis : [float], [float]             Set the x, y position of the viewing frame relative to the center of the detector.
    index : [integer]                   Attempts to seek the provided index in a previously loaded data file.
    
If a file `def.config` is present in the `./res/` directory, it will be loaded and parsed on startup.