package org.jboss.qa.mavenhoe;

import java.io.File;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Ondrej Zizka
 *
 * TODO: Rename to ArtifactInfo.
 */
public class JarInfo implements Comparable {

	public static final JarInfo DUMMY_INSTANCE = new JarInfo("Dummy", null, null, null, null, null, null);

   public static final String NULL_CLASSIFIER_STR = "-";

	// TODO: private
	private String name;
	private String version;
	private String group;
	private String path;
	private String fileName;
	protected File baseDir;
	protected String packaging;
	protected String classifier;
	private boolean virtual;


	public JarInfo(String name, String version, String group, String classifier, String path, String fileName, File baseDir, String packaging) {
		this(name, version, group, path, fileName, baseDir, packaging);
		this.classifier = classifier;
	}

	public JarInfo(String name, String version, String group, String path, String fileName, File baseDir, String packaging) {
		this.name = name;
		this.version = version;
		this.group = group;
		this.path = path;
		this.fileName = fileName;
		this.baseDir = baseDir;
		this.packaging = packaging;
	}

	
	@Override
	public String toString() {

   		if( DUMMY_INSTANCE == this )
			return JarInfo.class.getSimpleName()+"{ dummy instance }";
		
		String path = StringUtils.replaceOnce(this.path, this.baseDir.getAbsolutePath(), "@");

		StringBuilder sb = new StringBuilder(JarInfo.class.getSimpleName());
      	if( this.virtual )
      		sb.append( "?" );
      
		sb.append("{ ")
		.append( StringUtils.defaultString(this.group, "-"))
		.append(" : ")
		.append(this.name);

		if( this.classifier != null && this.classifier.length() != 0 )
			 sb.append(" @").append(this.classifier);

		sb.append(" : ")
			.append(this.version)
			.append(" : ")
			.append(this.packaging)
			.append(", fn: ")
			.append(this.fileName)
			.append(", pth: ")
			.append(path)
			.append(", base: ")
			.append( baseDir == null ? "" : StringUtils.right( baseDir.getPath(), 30) )
			.append(" }");

		return sb.toString();
	}

   
	public String toStringLong() {

		String path = StringUtils.replaceOnce(this.path, this.baseDir.getAbsolutePath(), "@");

		StringBuilder sb = new StringBuilder(JarInfo.class.getSimpleName());
      	if( this.virtual ) sb.append( "?" );

		sb.append("{ grp: ")
			.append( StringUtils.defaultString(this.group, "-"))
			.append(", name: ")
			.append(this.name);

		if( this.classifier != null && this.classifier.length() != 0 )
			sb.append(" @").append(this.classifier);

		sb.append(", ver: ")
			.append(this.version)
			.append(", fileName: ")
			.append(this.fileName)
			.append(", pack: ")
			.append(this.packaging)
			.append(", path: ")
			.append(path)
			.append(", base: ")
			.append( baseDir == null ? "-" : baseDir.getPath() )
			.append(" }");

      	return sb.toString();
	}


  	// <editor-fold defaultstate="collapsed" desc="get / set">
	public String getGroup() {		return group;	}
	public void setGroup(String group) {		this.group = group;	}
	public String getName() {		return name;	}
	public void setName(String name) {		this.name = name;	}
	public String getFileName() {		return fileName;	}
	public void setFileName(String fileName) {		this.fileName = fileName;	}
	public String getPath() {		return path;	}
	public void setPath(String path) {		this.path = path;	}
	public String getVersion() {		return version;	}
	public void setVersion(String version) {		this.version = version;	}
	public File getBaseDir() {		return baseDir;	}
	public void setBaseDir(File baseDir) {		this.baseDir = baseDir;	}
	public String getPackaging() {		return packaging;	}
	public void setPackaging(String packaging) {		this.packaging = packaging;	}
	public boolean isVirtual() {      return virtual;   }
	public void setVirtual(boolean virtual) {      this.virtual = virtual;   }
	public String getClassifier() {      return classifier;   }
	public void setClassifier(String classifier) {      this.classifier = classifier;   }
	// </editor-fold>

	
	//@Override
	public int compareTo(Object other) {
		if( ! (other instanceof JarInfo) )
			throw new IllegalArgumentException("  Not comparable - not a JarInfo: " + other);
		return this.getPath().compareTo( ((JarInfo) other).getPath() );
	}
	
}
