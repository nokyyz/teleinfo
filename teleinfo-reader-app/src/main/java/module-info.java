module org.openhab.binding.teleinfo.reader.app {
	exports org.openhab.binding.teleinfo.reader.app;

	requires org.openhab.binding.teleinfo.reader.core;
	requires org.openhab.binding.teleinfo.reader.plugin.core;
	requires java.logging;
	requires args4j;
	requires org.slf4j;
}