TrackSim Distribution ReadMe (2018 May 9)...

TrackSim is an emulator program that pretends to be the steering and drive servos of a radio-controlled car with the radio receiver replaced by a LattePanda (LP) computer running Java on Winows 10 and connected to a PointGrey(Flir) Chameleon3 or FireFly camera pointed straight ahead, which TrackSim also simulates, using the same APIs in each case.

TrackSim is designed to work with and simulate the hardware complement of the LattePanda (LP) computer, or in stand-alone mode on any Java-compatible computer in the absence of the LP hardware and attached car, which is its reason for existence. Develop on any computer, then deploy on an embedded LP in your car.

This program is released as Java source code for five packages, which you can run in any standard Java development environment. It is designed to work with the JavaSimpleSerialConnector (JSSC) serial port implementation available elsewhere, and with the PointGrey FLIR FlyCapture C-based DLLs (likewise), both of which are needed only in the LP when controlling the R/C car. Otherwise the five packages included in this distribution are sufficient. Each package is in its own containing folder:

Fly2cam

This is a minor revision of the Java interface to the JNI (C-coded) DLL that accesses the Pt.Grey Chameleon3 or FireFly camera driver DLLs. FlyCamera.dll is included here, and its source code is in the subfolder FlyCamera. Additional supporting files and code are available in the original release (see link below).

noJSSC

This is a non-functional (stub) plug-compatible substitute for the JSSC API, which may be used in its place when running TrackSim in stand-alone mode on any computer, specifically other than the LP.

FakeFirm

This is a Java clone of the C# API released by LP for driving digitial outputs and servos. It is a slight upgrade of my original released on the LP website, so that it diverts a copy of the servo commands to TrackSim, so that TrackSim can properly respond to your self-driving code without you code knowing it. There is additional information in its own web page, accessible from the link below.

APW3

This package contains the source code for TrackSim and its supporting classes, mostly documented in the link below.

Also included in this folder are three data files named "TrackImg.xxx" with one each of the suffix/file types ".txt" and ".tiff" and ".indx". The text and TIFF image files are optional, but the index file is a required default track definition resembling the PatsAcres go-kart track near Portland, but which you could replace with your own default as explained in the documentation. These three files must be in the Java project folder for TrackSim to see them.

DriveDemo

This is a JFrame window program designed to demonstrate how to use both TrackSim and the servo & camera interfaces simulated by TrackSim. You can plug your self-driving code into DriveDemo, or else replace it entirely with your own code. The goal is to release your own code with APW3 and DriveDemo completely removed, and your code supported only by the drivers in FakeFirm and Fly2cam (or else your own code replacements for them too).

For complete documentation, see:

  http://www.IttyBittyComputers.com/APW2/TrackSim/TrackSim.htm
  
License
  
All of my original source code in this implementation is released to the public domain. Like all software (whether you paid for it or not) there are no warranties, no promises. It worked for our project (so far), but if you have problems, it's not so complicated that you can't poke around and fix it yourself.
  
If you have questions, you can send me an email and I'll answer the best I can, but I may not have sufficient time or access to the necessary resources to test anything, so I cannot promise any particular improvements.
  
Tom Pittman
TPittman@IttyBittyComputers.com
