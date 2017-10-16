module org.openhab.binding.teleinfo.reader.io.serialport {
	exports org.openhab.binding.teleinfo.reader.io.serialport;

	requires transitive org.openhab.binding.teleinfo.reader.io;
	requires org.openhab.binding.teleinfo.reader.dsl;
	
	requires nrjavaserial;
	requires org.slf4j;
}