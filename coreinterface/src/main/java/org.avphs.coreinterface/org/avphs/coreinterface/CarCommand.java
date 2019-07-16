package org.avphs.coreinterface;

public class CarCommand {
    public CarCommandType command;
    public Object[] parameters;

    public CarCommand(CarCommandType command, Object[] parameters) {
        this.command = command;
        this.parameters = parameters;
    }

    public static CarCommand accelerate(boolean absolute, int angle) {
        return new CarCommand(CarCommandType.ACCELERATE_COMMAND, new Object[] { absolute, angle });
    }
    public static CarCommand steer(boolean absolute, int angle) {
        return new CarCommand(CarCommandType.STEER_COMMAND, new Object[] { absolute, angle });
    }
    public static CarCommand stop() {
        return new CarCommand(CarCommandType.STOP_COMMAND, new Object[] { });
    }

}
