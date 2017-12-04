package cz.oz.mavenhoe;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Decides whether given path should be "used", based on the list of included and excluded dirs.
 *
 *  This takes the directories hierarchy and list items order into account. Which means,
 *
 *    INCLUDE /a/b
 *    EXCLUDE /a/b/c
 *    INCLUDE /a/b/c/d
 *
 *  would allow /a/b/X.txt and a/b/c/d/Y.txt, but not /a/b/c/Z.txt.
 *
 *  Not finished.
 *
 *  @author Ondrej Zizka
 */
public class InclExclFilter implements FileFilter {
	private static final Logger log = LoggerFactory.getLogger(InclExclFilter.class);

	private final boolean defaultAllowed;
	private final List<InclExclItem> inclExclList;
	private final int startOffset;
  	private final boolean[] parenthoodSequenceFlags;


	/**
	 * @returns  those includes from the list which are not covered by the previous ones.
	 */
	public List<File> getRootIncludes(){
		List<File> list = new ArrayList();
		for (int i = 0; i < inclExclList.size(); i++) {
			InclExclItem inclExclItem = inclExclList.get(i);
			if( parenthoodSequenceFlags[i] )
				list.add( inclExclItem.getFileObject()  );
		}
		return list;
	}

	boolean isRootInclude( InclExclItem item ){
		int index = this.inclExclList.indexOf(item);
		if( index == -1 )
			throw new IllegalArgumentException( "Not a member of this filter: "+item);
		return parenthoodSequenceFlags[ index ];
	}



	/**
	 * Creates a filter based on inclExclList, considering only items at and after index startOffset.
	 * If the given path is not covered by any of includes or exludes, defaultAllowed will be returned.
	 * 
	 * @param inclExclList
	 * @param startOffset
	 * @param defaultAllowed
	 */
	InclExclFilter( List<InclExclItem> inclExclList, int startOffset, boolean defaultAllowed ) {
		this.inclExclList = inclExclList;
		this.startOffset = startOffset;
		this.defaultAllowed = defaultAllowed;

		// TODO: This could be moved to some InclExclList class.
		this.parenthoodSequenceFlags = new boolean[inclExclList.size()];
		
		String prevParentInclude = null;
		for (int i = 0; i < inclExclList.size(); i++) {
			InclExclItem curInclExclItem = inclExclList.get(i);

			// Only Includes
			if( curInclExclItem.getMode() != InclExclItem.Mode.INCLUDE )
				continue;

			// Check the previous highest parent.
			/*if( null != prevParentInclude  &&  curInclExclItem.getPath().startsWith(prevParentInclude) ){
				continue;
			}*/

			// Check all predecessors.
			for (int j = 0; j < i; j++) {
				InclExclItem checkedPredecessor = inclExclList.get(j);
				if( curInclExclItem.getFileObject().getAbsolutePath().startsWith( checkedPredecessor.getFileObject().getAbsolutePath() ) )
					continue;
			}

			// The current include does not start with previous ones,
			// so it's a new root node. 
			this.parenthoodSequenceFlags[i] = true;
			prevParentInclude = curInclExclItem.getFileObject().getAbsoluteFile().getPath();

		}
	}

	/** Alias for isAllowed(). */
	//@Override
    public boolean accept(File path) {
		return this.isAllowed( path );
	}



	/**
	 *  The actual deciding method, see class description.
	 *  @returns true if this path is allowed by this filter.
	 */
	public boolean isAllowed( File path ){
		return isAllowed( path, this.startOffset );
	}

	/**
	 *  The actual deciding method, see class description.
	 *  @returns true if this path is allowed by this filter.
	 */
	public boolean isAllowed( File path, int startOffset ){
		log.debug("  isAllowed( path: "+path+", startOffset: "+startOffset+" )");
		final boolean trace = log.isTraceEnabled();

		boolean allowed = this.defaultAllowed;

		for( int i = startOffset; i < inclExclList.size(); i++ ){
			InclExclItem inclExcl = inclExclList.get(i);

			String traceProlog = (!trace) ? null : "    ["+i+"] "+inclExcl.toString();
			if( this.parenthoodSequenceFlags[i] && trace )  traceProlog += " (a ROOT include)"; ///
			

			/*
			if( inclExcl.getType() == InclExclItem.Type.INCLUDE ){
				if( path.getAbsolutePath().startsWith( inclExcl.getFileObject().getAbsolutePath() )){
					if( trace ) log.trace(traceProlog + " allows."); ///
					allowed = true;
				}
				else { if( trace ) log.trace(traceProlog + " does not affect."); }///
			}
			else if(inclExcl.getType() == InclExclItem.Type.EXCLUDE){
				if( path.getAbsolutePath().startsWith( inclExcl.getFileObject().getAbsolutePath() )){
					if( trace ) log.trace(traceProlog + " forbids."); }///
					allowed = false;
				}
				else { if( trace ) log.trace(traceProlog + " does not affect."); }///
			}
			*/

			if( path.getAbsolutePath().startsWith( inclExcl.getFileObject().getAbsolutePath() )){
				boolean isIncl = inclExcl.getMode() == InclExclItem.Mode.INCLUDE;
				if( trace ) log.trace(traceProlog + (isIncl ? " allows." : " forbids.") ); ///
				allowed = isIncl;
			}
			else { if( trace ) log.trace(traceProlog + " does not affect."); }///

		}// for

		return allowed;
		
	}

}// class
