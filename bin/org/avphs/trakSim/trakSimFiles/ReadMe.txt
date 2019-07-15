TrakSim ReadMe (Copyright 2019 July 4 Itty Bitty Computers)...

TrakSim is an emulator program that pretends to be the steering and drive servos of a radio-controlled car with the radio receiver replaced by a LattePanda (LP) computer running Java on Winows 10 and connected to a PointGrey(Flir) Chameleon3 or FireFly camera pointed straight ahead, which TrakSim also simulates, using the same APIs in each case.

TrakSim is designed to work with the hardware complement of the LattePanda (LP) computer where it is used to replace the radio receiver in a standard R/C model car, with user-written software to drive the car. It can also be used in stand-alone mode on any Java-compatible computer to simulate the LP system in the absence of the LP hardware and attached car, which is its reason for existence. Develop on any computer, then deploy on an embedded LP in your car.

This program is released as Java source code which you can run in any standard Java development environment. It is designed to work with FakeFirmata (FF, included) to test your autonomous vehicle software apart from any controlled car, but also can be used in your car while controlling your car's servos. FF in turn works with the JavaSimpleSerialConnector serial port implementation (or any other compatible API) to send serial port commands to the Arduino daughter board included with the LattePanda computer. In a Java development environment other than LP, use package noJSSC (also included) to run the simulator as a stand-alone.

You can design your own tracks and add artifacts like stop signs, traffic lights, moving pedestrians and other vehicles for your simulated car to see and avoid, using the built-in API for that putpose.

For complete documentation, see:

  http://www.IttyBittyComputers.com/APW2/TrackSim/TrackSim.htm

New This Release

I added a tiled floor option to replicate a specific pattern on the track floor. Only the upload with a ReadMe dated today is current.

I added a method Activate() to prevent TrakSim from using CPU time when running on an actual track with a live camera.

I added track boundary walls compatible with the F1/10th race tracks, along with simulating the coefficient of friction and a driveshaft turns counter.

I fixed some bugs that caused exceptions under certain circumstances, and added optional texture to the track to simulate carpets with variegated colors, and to vary the lighting across the park. I also added to the demonstration package code to show how to quickly posterize the pixels and find a few "blobs" of color which might represent traffic lights or stop signs, and timing loops to measure code speed. For a discussion on the additions, see:

  http://www.IttyBittyComputers.com/BG/TechTopix9.htm#PostMortem

License

All of my original source code in this implementation is licensed to the public. Like all software (whether you paid for it or not) there are no warranties, no promises. It worked for our project (so far), but if you have problems, it's not so complicated that you can't poke around and fix it yourself.

If you have questions, you can send me an email and I'll answer the best I can, but I may not have sufficient time or access to the necessary resources to test anything, so I cannot promise any particular improvements.

Tom Pittman
(see IttyBittyComputers.com website for email contact)



