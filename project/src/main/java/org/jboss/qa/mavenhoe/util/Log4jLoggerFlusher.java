
package org.jboss.qa.mavenhoe.util;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ondrej Zizka
 */
public class Log4jLoggerFlusher {


	public static void flushLoggerByLoggingSpaces() {
		flushLoggerByLoggingSpaces( LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME), 4, 512 );
	}

	public static void flushLoggerByLoggingSpaces( org.slf4j.Logger log, int lines, int spaces ) {
		for( int i = 0; i < lines; i++ ){
			log.info( StringUtils.repeat(" ", spaces) );
		}
	}


	public static void flushLoggers() {

		Set<FileAppender> flushedFileAppenders = new HashSet<FileAppender>();

		Enumeration currentLoggers = LogManager.getCurrentLoggers();
		while( currentLoggers.hasMoreElements() ) {
			Object nextLogger = currentLoggers.nextElement();
			if( ! ( nextLogger instanceof Logger )) continue;

			Logger currentLogger = (Logger) nextLogger;
			Enumeration allAppenders = currentLogger.getAllAppenders();
			while( allAppenders.hasMoreElements() )	{
				Object nextElement = allAppenders.nextElement();
				if( ! ( nextElement instanceof FileAppender )) continue;

				FileAppender fileAppender = (FileAppender) nextElement;
				if( flushedFileAppenders.contains(fileAppender) )
					continue;
			    else
			    	flushedFileAppenders.add(fileAppender);

				if( ! fileAppender.getImmediateFlush() )	{
                    //log.info("Appender "+fileAppender.getName()+" is not doing immediateFlush ");
                    fileAppender.setImmediateFlush(true);
                    currentLogger.info(".");
                    fileAppender.setImmediateFlush(false);
				}
				else{
                    //log.info("fileAppender"+fileAppender.getName()+" is doing immediateFlush");
				}
			}
		}

	}// flushLoggers()

}// class
