package com.jtransc;

public class JTranscVersion {
	static private final String version = "0.6.4";

	static public String getVersion() {
		return version;
	}

	static public String getRuntime() {
		return JTranscSystem.getRuntimeKind();
	}
}
