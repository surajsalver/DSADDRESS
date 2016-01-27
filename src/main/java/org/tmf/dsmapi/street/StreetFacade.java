package org.tmf.dsmapi.street;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tmf.dsmapi.commons.facade.AbstractFacade;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.commons.exceptions.ExceptionType;
import org.tmf.dsmapi.commons.exceptions.UnknownResourceException;
import org.tmf.dsmapi.commons.utils.BeanUtils;
import org.tmf.dsmapi.address.model.Street;
import org.tmf.dsmapi.street.event.StreetEventPublisherLocal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.tmf.dsmapi.address.model.StreetSegment;
import org.tmf.dsmapi.streetSegment.StreetSegmentFacade;

@Stateless
public class StreetFacade extends AbstractFacade<Street> {

    @PersistenceContext(unitName = "DSAddressPU")
    private EntityManager em;
    
    @EJB
    StreetEventPublisherLocal publisher;
//    StateModelImpl stateModel = new StateModelImpl();

    public StreetFacade() {
        super(Street.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public void checkCreation(Street entity) throws BadUsageException, UnknownResourceException {

        Street sc = null;
        if (entity.getId() == null
                || entity.getId().isEmpty()) {
//            throw new BadUsageException(ExceptionType.BAD_USAGE_GENERIC, "While creating Street, id must be not null");
                //Do nothing create ok
                Logger.getLogger(StreetFacade.class.getName()).log(Level.INFO, "Street with autogenerated id is being posted");
        } else {
            try {
                sc = this.find(entity.getId());
                if (null != sc) {
                    throw new BadUsageException(ExceptionType.BAD_USAGE_GENERIC,
                            "Duplicate Exception, Street with same id :" + entity.getId() + " alreay exists");
                }
            } catch (UnknownResourceException ex) {
                //Do nothing create ok
                Logger.getLogger(StreetFacade.class.getName()).log(Level.INFO, "Street with id = " + entity.getId() + " is being posted", ex);
            }
        }

        //verify first status
        /**
        if (null == entity.getLifecycleState()) {
            entity.setLifecycleState(LifecycleStateValues.PENDING);
            throw new BadUsageException(ExceptionType.BAD_USAGE_MANDATORY_FIELDS, "LifecycleState is mandatory");
        } else {
            if (!entity.getLifecycleState().name().equalsIgnoreCase(LifecycleStateValues.PENDING.name())) {
                throw new BadUsageException(ExceptionType.BAD_USAGE_FLOW_TRANSITION, "lifecycleState " + entity.getLifecycleState().value() + " is not the first state, attempt : " + LifecycleStateValues.PENDING.value());
            }
        }
        */

    }

    public Street patchAttributs(String id, Street partialEntity) throws UnknownResourceException, BadUsageException {
        Street currentEntity = this.find(id);

        if (currentEntity == null) {
            throw new UnknownResourceException(ExceptionType.UNKNOWN_RESOURCE);
        }

        verifyStatus(currentEntity, partialEntity);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.convertValue(partialEntity, JsonNode.class);
        partialEntity.setId(id);
        if (BeanUtils.patch(currentEntity, partialEntity, node)) {
            publisher.valueChangedNotification(currentEntity, new Date());
        }

        return currentEntity;
    }

    public void verifyStatus(Street currentEntity, Street partialEntity) throws BadUsageException {
        /**
        if (null != partialEntity.getLifecycleState()) {
            stateModel.checkTransition(currentEntity.getLifecycleState(), partialEntity.getLifecycleState());
            publisher.statusChangedNotification(currentEntity, new Date());
        }
        */
    }

}
