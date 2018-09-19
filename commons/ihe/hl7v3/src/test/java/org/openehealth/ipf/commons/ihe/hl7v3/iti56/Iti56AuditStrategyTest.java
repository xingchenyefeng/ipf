/*
 * Copyright 2018 the original author or authors.
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

package org.openehealth.ipf.commons.ihe.hl7v3.iti56;

import org.junit.Test;
import org.openehealth.ipf.commons.audit.codes.EventActionCode;
import org.openehealth.ipf.commons.audit.codes.EventIdCode;
import org.openehealth.ipf.commons.audit.codes.EventOutcomeIndicator;
import org.openehealth.ipf.commons.audit.model.AuditMessage;
import org.openehealth.ipf.commons.ihe.core.atna.AuditDataset.HumanUser;
import org.openehealth.ipf.commons.ihe.hl7v3.atna.HL7v3AuditorTestBase;
import org.openehealth.ipf.commons.ihe.hl7v3.audit.Hl7v3AuditDataset;

import static org.junit.Assert.assertNotNull;

/**
 * @author Christian Ohr
 */
public class Iti56AuditStrategyTest extends HL7v3AuditorTestBase<Iti56AuditStrategy> {

    @Test
    public void testServerSide() {
        testRequest(true);
    }

    @Test
    public void testClientSide() {
        testRequest(false);
    }

    private void testRequest(boolean serverSide) {
        Iti56AuditStrategy strategy = new Iti56AuditStrategy(serverSide);
        Hl7v3AuditDataset auditDataset = getHl7v3AuditDataset(strategy);
        AuditMessage auditMessage = makeAuditMessage(strategy, auditContext, auditDataset);

        assertNotNull(auditMessage);
        auditMessage.validate();
        assertCommonV3AuditAttributes(auditMessage,
                EventOutcomeIndicator.Success,
                EventIdCode.Query,
                EventActionCode.Execute,
                serverSide,
                serverSide);
    }

    @Override
    protected Hl7v3AuditDataset getHl7v3AuditDataset(Iti56AuditStrategy strategy) {
        Hl7v3AuditDataset auditDataset = super.getHl7v3AuditDataset(strategy);
        auditDataset.setRequestPayload(QUERY_PAYLOAD);
        auditDataset.setHomeCommunityId(HOME_COMMUNITY_ID);
        return auditDataset;
    }
}