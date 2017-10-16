module org.openhab.binding.teleinfo.reader.core {
	exports org.openhab.binding.teleinfo.reader.context;
	exports org.openhab.binding.teleinfo.reader.context.conf;
	exports org.openhab.binding.teleinfo.reader.context.defaultt;

	requires transitive org.openhab.binding.teleinfo.reader.io.serialport;
	requires org.openhab.binding.teleinfo.reader.io;
	requires transitive org.openhab.binding.teleinfo.reader.plugin.persistence;
	requires transitive org.openhab.binding.teleinfo.reader.plugin.broadcast;

	requires guava;
	requires jpf;
	requires org.slf4j;
}