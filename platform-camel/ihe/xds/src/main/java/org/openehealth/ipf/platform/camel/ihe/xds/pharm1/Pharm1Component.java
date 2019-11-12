package org.openehealth.ipf.platform.camel.ihe.xds.pharm1;

import org.apache.camel.Endpoint;
import org.openehealth.ipf.commons.ihe.ws.JaxWsClientFactory;
import org.openehealth.ipf.commons.ihe.ws.WsTransactionConfiguration;
import org.openehealth.ipf.commons.ihe.xds.core.audit.XdsQueryAuditDataset;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryRequest;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryResponse;
import org.openehealth.ipf.platform.camel.ihe.ws.AbstractWsEndpoint;
import org.openehealth.ipf.platform.camel.ihe.ws.AbstractWsProducer;
import org.openehealth.ipf.platform.camel.ihe.ws.SimpleWsProducer;
import org.openehealth.ipf.platform.camel.ihe.xds.XdsComponent;
import org.openehealth.ipf.platform.camel.ihe.xds.XdsEndpoint;

import java.util.Map;

import static org.openehealth.ipf.commons.ihe.xds.XDS.Interactions.PHARM_1;

/**
 * The Camel component for the IHE PHARM-1 transaction.
 */
public class Pharm1Component extends XdsComponent<XdsQueryAuditDataset> {

    public Pharm1Component() {
        super(PHARM_1);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) {
        return new XdsEndpoint<XdsQueryAuditDataset>(uri, remaining, this, parameters, Pharm1Service.class) {
            @Override
            public AbstractWsProducer<XdsQueryAuditDataset, WsTransactionConfiguration<XdsQueryAuditDataset>, ?, ?> getProducer(AbstractWsEndpoint<XdsQueryAuditDataset, WsTransactionConfiguration<XdsQueryAuditDataset>> endpoint, JaxWsClientFactory<XdsQueryAuditDataset> clientFactory) {
                return new SimpleWsProducer(endpoint, clientFactory, AdhocQueryRequest.class, AdhocQueryResponse.class);
            }
        };
    }
}
