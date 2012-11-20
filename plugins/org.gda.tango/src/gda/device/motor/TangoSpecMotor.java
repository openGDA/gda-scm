/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.motor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to control a Tango motor
 */
public class TangoSpecMotor extends MotorBase implements Motor {
	private static final Logger logger = LoggerFactory.getLogger(TangoSpecMotor.class);

	private volatile MotorStatus motorStatus = MotorStatus.UNKNOWN;
	private DevState state;
	private TangoDeviceProxy dev;

	@Override
	public void configure() throws FactoryException {
		try {
			isAvailable();
			motorStatus = MotorStatus.READY;
			state = dev.state();
		} catch (Exception e) {
			logger.error("TangoMotor configure: {}", e);
			logger.error("TangoMotor configure {}", e.getMessage());
			motorStatus = MotorStatus.FAULT;
		}
	}

	public TangoDeviceProxy getTangoDeviceProxy() {
		return dev;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy dev) {
		this.dev = dev;
	}

	@Override
	public double getPosition() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute("Position").extractDouble();
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new MotorException(getStatus(), "failed to get position" + e.errors[0].desc);
		}
	}

	@Override
	public void moveTo(double position) throws MotorException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("Position", position));
			state = DevState.MOVING;
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new MotorException(motorStatus, "failed to initiate start move" + e.errors[0].desc);
		}
	}

	@Override
	public void moveBy(double increment) throws MotorException {
		moveTo(getPosition() + increment);
	}

	@Override
	public void stop() throws MotorException {
		isAvailable();
		try {
			dev.command_inout("Abort");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new MotorException(getStatus(), "failed to stop " + e.errors[0].desc);
		}
	}

	@Override
	public void panicStop() throws MotorException {
		stop();
	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		MotorStatus motorStatus;
		isAvailable();
		try {
//			try {
//				Thread.sleep(100); // Give spec time to change device server state!
//			} catch (InterruptedException e) {
//			}
			state = dev.state();
			switch (state.value()) {
			case DevState._ON:
				motorStatus = MotorStatus.READY;
				break;
			case DevState._MOVING:
				motorStatus = MotorStatus.BUSY;
				break;
			case DevState._FAULT:
				motorStatus = MotorStatus.FAULT;
				break;
			default:
				motorStatus = MotorStatus.UNKNOWN;
				break;
			}
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new MotorException(getStatus(), "failed to get status " + e.errors[0].desc);
		}
		return motorStatus;
	}

	@Override
	public boolean isMoving() throws MotorException {
		return (getStatus() == MotorStatus.BUSY);
	}

	private void isAvailable() throws MotorException {
		try {
			// Is the device still connected or just started
			dev.status();
		} catch (DevFailed e) {
			// device has lost connection
			configured = false;
			throw new MotorException(MotorStatus.UNKNOWN, "Tango device server " + dev.get_name() + " failed");
		} catch (Exception e) {
			throw new MotorException(MotorStatus.UNKNOWN, "Tango device server stuffed");			
		}
	}

// Methods not implemented below here
	
	@Override
	public double getSpeed() throws MotorException {
		logger.error("setPosition() is not implemented");
		return 0.0;
	}

	@Override
	public void setPosition(double position) throws MotorException {
		logger.error("setPosition() is not implemented");
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		logger.error("setSpeed() is not implemented");
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		logger.error("moveContinuosly() is not implemented");
	}

	@Override
	public void home() throws MotorException {
		logger.error("home() is not implemented");
	}
}