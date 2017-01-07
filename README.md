# Motion Profiling
Creates file with data for graphing motion profiles (trapezoidal or s-curve velocity)

Input desired distance, max acceleration, mode (pure s-curve, trapezoidal, more to be added later), max velocity, and clock speed.
Output data for velocity, distance, and acceleration over time

Includes example data file with parameters: 
```
  distance = 100;
  max acceleration = 5;
  mode = 1;
  max velocity = 10;
  clock speed = 0.1;
```

Also compatible with FRC Talon Control Mode "MotionProfile" by including option to write data in Java code as a 2-dimensional array. 
