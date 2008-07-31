package org.opennms.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("nodes")
public class NodeRestService extends OnmsRestService {
    
	private static final int LIMIT=10;
	
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private EventProxy m_eventProxy;
    
    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsNodeList getNodes() {
        OnmsCriteria criteria = getQueryFilters();
        return new OnmsNodeList(m_nodeDao.findMatching(criteria));
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{nodeId}")
    public OnmsNode getNode(@PathParam("nodeId") int nodeId) {
        return m_nodeDao.get(nodeId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional(readOnly=false)
    public Response addNode(OnmsNode node) {
        log().debug("addNode: Adding node " + node);
        m_nodeDao.save(node);
        Event e = new Event();
        e.setUei(EventConstants.NODE_ADDED_EVENT_UEI);
        e.setNodeid(node.getId());
        e.setSource(getClass().getName());
        e.setTime(EventConstants.formatToString(new java.util.Date()));
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok(node).build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{nodeId}")
    @Transactional(readOnly=false)
    public Response updateNode(OnmsNode node, @PathParam("nodeId") int nodeId) {
        if (nodeId != node.getId())
            throwException(Status.CONFLICT, "updateNode: invalid nodeId for node " + node);
        log().debug("updateNode: updating node " + nodeId);
        m_nodeDao.saveOrUpdate(node);
        return Response.ok().build();
    }
    
    @DELETE
    @Path("{nodeId}")
    @Transactional(readOnly=false)
    public Response deleteNode(@PathParam("nodeId") int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null)
            throwException(Status.BAD_REQUEST, "deleteNode: Can't find node with id " + nodeId);
        log().debug("deleteNode: deleting node " + nodeId);
        m_nodeDao.delete(node);
        Event e = new Event();
        e.setUei(EventConstants.NODE_DELETED_EVENT_UEI);
        e.setNodeid(nodeId);
        e.setSource(getClass().getName());
        e.setTime(EventConstants.formatToString(new java.util.Date()));
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }

    @Path("{nodeId}/ipinterfaces")
    public OnmsIpInterfaceResource getIpInterfaceResource() {
        return m_context.getResource(OnmsIpInterfaceResource.class);
    }

    @Path("{nodeId}/snmpinterfaces")
    public OnmsSnmpInterfaceResource getSnmpInterfaceResource() {
        return m_context.getResource(OnmsSnmpInterfaceResource.class);
    }
    
    private OnmsCriteria getQueryFilters() {
        MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);

    	setLimitOffset(params, criteria, LIMIT);
    	addFiltersToCriteria(params, criteria);
        
        return criteria;
    }
    
    private void throwException(Status status, String msg) {
        log().error(msg);
        throw new WebApplicationException(Response.status(status).tag(msg).build());
    }
    
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
