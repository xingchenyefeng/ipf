package org.openehealth.ipf.commons.ihe.xds.core.requests.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Identifiable;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.TimeRange;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Represents a stored query for FindMedicationListQuery (PHARM-1).
 * @author Quentin Ligier
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindMedicationListQuery", propOrder = {"formatCodes", "typeCodes"})
@XmlRootElement(name = "findMedicationListQuery")
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public class FindMedicationListQuery extends PharmacyDocumentsQuery {
    private static final long serialVersionUID = 7810851265303915098L;

    @XmlElement(name = "formatCode")
    @Getter @Setter private List<Code> formatCodes;
    @XmlElement(name = "typeCode")
    @Getter @Setter private List<Code> typeCodes;

    /**
     * Constructs the query.
     */
    public FindMedicationListQuery() {
        super(QueryType.FIND_MEDICATION_LIST);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
