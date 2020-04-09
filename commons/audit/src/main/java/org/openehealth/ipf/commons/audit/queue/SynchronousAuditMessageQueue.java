/*
 * Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openehealth.ipf.commons.audit.queue;


import org.openehealth.ipf.commons.audit.AuditContext;

/**
 *
 * Synchronously pass the message to the {@link org.openehealth.ipf.commons.audit.protocol.AuditTransmissionProtocol}
 *
 * @author Christian Ohr
 * @since 3.5
 */
public class SynchronousAuditMessageQueue extends AbstractAuditMessageQueue {

    @Override
    protected void handle(AuditContext auditContext, String auditRecord) {
        try {
            auditContext.getAuditTransmissionProtocol().send(auditContext, auditRecord);
        } catch (Exception e) {
            auditContext.getAuditExceptionHandler().handleException(auditContext, e, auditRecord);
        }
    }
}
   