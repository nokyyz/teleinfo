package org.openhab.binding.teleinfo.reader.persistence.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.plugin.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaPersistenceService implements PersistenceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JpaPersistenceService.class);
	
	private static EntityManager em;
	private Map<String, String> parameters;
	
	@Override
	public void insert(Frame teleinfoFrame) {
		LOGGER.debug("insert(Frame) [start]");
		em.getTransaction().begin();
        em.persist(teleinfoFrame);
        em.getTransaction().commit();
		LOGGER.debug("insert(Frame) [end]");
	}

	@Override
	public Frame findById(UUID id) {
		LOGGER.debug("findById(UUID) [start]");
		em.getTransaction().begin();
		Frame frame = em.find(Frame.class, id);
		em.getTransaction().commit();
		LOGGER.debug("findById(UUID) [end]");
		return frame;
	}

	@Override
	public void init() {
		LOGGER.debug("init() [start]");
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.driver", getRequiredStringParameterValue("javax.persistence.jdbc.driver"));
	    properties.put("javax.persistence.jdbc.url", getRequiredStringParameterValue("javax.persistence.jdbc.url"));
	    properties.put("javax.persistence.jdbc.user", getRequiredStringParameterValue("javax.persistence.jdbc.user"));
	    properties.put("javax.persistence.jdbc.password", getRequiredStringParameterValue("javax.persistence.jdbc.password"));
	    
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("TeleinfoPU", properties);
        em = emf.createEntityManager();
		LOGGER.debug("init() [end]");
	}

	@Override
	public void destroy() {
		LOGGER.debug("destroy() [start]");
		em.close();
		em = null;
		LOGGER.debug("destroy() [end]");
	}
	
	private String getRequiredStringParameterValue(String parameterId) {
		if (!parameters.containsKey(parameterId)) {
			throw new IllegalStateException("The required '" + parameterId + "' parameter is missing");
		}
		
		return parameters.get(parameterId);
	}

	private int getIntegerParameterValue(String parameterId, int defaultValue) {
		if (!parameters.containsKey(parameterId)) {
			return defaultValue;
		}
		
		return Integer.parseInt(parameters.get(parameterId));
	}	
}
