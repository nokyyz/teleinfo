module org.openhab.binding.teleinfo.reader.io {
	exports org.openhab.binding.teleinfo.reader.io;
	exports org.openhab.binding.teleinfo.reader.io.stream;
	
	requires transitive org.openhab.binding.teleinfo.reader.dsl;
	
	requires validation.api;
	requires org.slf4j;
	//requires junit;
}