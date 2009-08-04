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
package org.openehealth.ipf.platform.camel.core.model;

import org.apache.camel.Processor;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateProcessor;
import org.apache.camel.spi.RouteContext;

/**
 * @author Martin Krasser
 */
public class InterceptType extends OutputDefinition<ProcessorDefinition> {

    private DelegateProcessor delegateProcessor;
    
    public InterceptType(DelegateProcessor delegateProcessor) {
        this.delegateProcessor = delegateProcessor;
    }
    
    @Override
    public Processor createProcessor(RouteContext routeContext) throws Exception {
    	delegateProcessor.setProcessor(routeContext.createProcessor(this));
    	return delegateProcessor;
    }
    
}
