/*
 * Copyright 2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.platform.camel.flow.model;

import groovy.lang.Closure;

import org.apache.camel.Expression;
import org.apache.camel.model.ProcessorDefinition;
import org.openehealth.ipf.platform.camel.core.closures.DelegatingExpression;

/**
 * Models IPF DSL extensions that conflict with Camel DSL elements. Usage
 * example:
 * 
 * <pre>
 * from('direct:input').ipf().split()...
 * </pre>
 * 
 * This selects the IPF splitter rather than the Camel splitter.
 * 
 * @author Martin Krasser
 */
public class IpfType extends org.openehealth.ipf.platform.camel.core.model.IpfType {

	public IpfType(ProcessorDefinition<ProcessorDefinition> processorDefinition) {
		super(processorDefinition);
	}
	
	public SplitterType split(Expression splitExpression) {
        SplitterType answer = new SplitterType(splitExpression);        
        getProcessorDefinition().addOutput(answer);
        return answer;
    }
	
	public SplitterType split(Closure splitLogic) {
    	return split(new DelegatingExpression(splitLogic));
    }
	
}
