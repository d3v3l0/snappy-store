/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.management.internal.cli.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.EvictionAction;
import com.gemstone.gemfire.cache.EvictionAlgorithm;
import com.gemstone.gemfire.cache.EvictionAttributes;

public class EvictionAttributesInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private String evictionAction = "";
	private String evictionAlgorithm = "";
	private int  evictionMaxValue = 0;
	
	public EvictionAttributesInfo(EvictionAttributes ea) {
		EvictionAction evictAction = ea.getAction();
		
		if (evictAction != null) {
				evictionAction = evictAction.toString();
		}
		EvictionAlgorithm evictionAlgo = ea.getAlgorithm();
		if (evictionAlgo != null){
			evictionAlgorithm = evictionAlgo.toString();
		}
			
		evictionMaxValue = ea.getMaximum();
	}

	public String getEvictionAction() {
		return evictionAction;
	}

	public String getEvictionAlgorithm() {
		return evictionAlgorithm;
	}

	public int getEvictionMaxValue() {
		return evictionMaxValue;
	}

}
