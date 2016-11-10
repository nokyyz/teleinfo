package org.openhab.binding.teleinfo.reader.plugin.persistence;

import java.util.UUID;

import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.plugin.core.AbstractService;

public interface PersistenceService extends AbstractService {

	void insert(final Frame teleinfoFrame);
	
	Frame findById(UUID id);
	
}
