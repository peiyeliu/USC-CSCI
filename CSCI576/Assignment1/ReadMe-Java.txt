CSCI576 Assignment 1, Spring 2022
Peiye Liu
peiyeliu@usc.edu

A sample program to read and display an image in JavaFX panels.
By default, this program will read in the first frame of a given .rgb video file.

To compile the program, type:
    javac ImageDisplay.java

Put rgb input files in the same folder

To run the program, type:
    java ImageDisplay [rgb file name] S Q M

Parameter formats:
    S (Scale): double, 0.0 < S <= 1.0
    Q (Quantization number): int, 1 <= Q <= 8
    M (Mode for quantization): int, -1 for uniform quantization
        0 <= M <= 255 for logarithmic quantization


