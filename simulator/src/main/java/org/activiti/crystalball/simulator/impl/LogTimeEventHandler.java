package org.activiti.crystalball.simulator.impl;

/*
 * #%L
 * simulator
 * %%
 * Copyright (C) 2012 - 2013 crystalball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

/**
 * Log time information. To see that something is happening during long simulation runs. 
 *
 */
public class LogTimeEventHandler implements SimulationEventHandler {

	private static Logger log = LoggerFactory.getLogger(LogTimeEventHandler.class);
	
	protected long simulationStart;
	protected String type;
	protected int logDelta;
	
	public LogTimeEventHandler(String type, int logDelta) {
		this.type = type;
		this.logDelta = logDelta;
	}
	
	@Override
	public void init() {
		simulationStart = System.currentTimeMillis();
		SimulationRunContext.getEventCalendar().addEvent(new SimulationEvent.Builder(type).
      simulationTime(SimulationRunContext.getClock().getCurrentTime().getTime()).
      build());
	}

	@Override
	public void handle(SimulationEvent event) {
		Date simulationTime = new Date( event.getSimulationTime());
		log.info("SimExec {} sec, Simulation time [{}]", (System.currentTimeMillis() - simulationStart)/1000f, simulationTime);
		
		Calendar c = Calendar.getInstance();
		c.setTime(simulationTime);
		c.add( Calendar.MINUTE, logDelta);
		SimulationRunContext.getEventCalendar().addEvent( new SimulationEvent.Builder( type).
      simulationTime(c.getTimeInMillis()).
      build());
	}

}
