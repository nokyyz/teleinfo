package org.openhab.binding.teleinfo.reader.plugin.broadcast;

import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.plugin.core.AbstractService;

public interface BroadcastService extends AbstractService {

	void broadcast(final Frame teleinfoFrame);
	
}
