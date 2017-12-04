package cz.oz.mavenhoe;

import java.io.File;

class InclExclItem
{
	enum Mode { INCLUDE, EXCLUDE }
	enum Type { JAR, MD5, SHA1, POM }

	public final Mode mode;
	public final File path;
	public final Type type;
   	public final boolean fakePoms;

	public InclExclItem(Mode mode, File path, Type type) {
      this(mode, path, type, false);
	}

	public InclExclItem(Mode mode, File path, Type type, boolean fakePoms) {
		this.mode = mode;
		this.path = path;
		this.type = type;
      	this.fakePoms = fakePoms;
   	}

	
	public File getFileObject() {		return path;	}
	public Mode getMode() {		return mode;	}
	public Type getType() {		return type;	}


	@Override
	public String toString() {
		return "InclExclItem{ " + mode + " " + type + " " + path + " " + fakePoms + " }";
	}

}
