
package cz.oz.mavenhoe.mappers;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cz.oz.mavenhoe.JarIndex;
import cz.oz.mavenhoe.JarInfo;
import cz.oz.mavenhoe.ex.MavenhoeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the info from a database filled during the EAP build process.
 * Named after Fernando Nasser :)
 *
 * TODO: Keep the connection open?
 * TODO: Read all the data in advance?
 *
 * @author Ondrej Zizka
 */
public class FernandoDatabaseMapper implements IMavenAxisToJarInfoMapper {
	private static final Logger log = LoggerFactory.getLogger(FernandoDatabaseMapper.class);

	private static final String JDBC_URL = "jdbc:postgresql://tooth.usersys.redhat.com/rhaps?user=mavenhoe&password=mavenhoe";
	private static final String JDBC_DRIVER_CLASS = "org.postgresql.Driver";



	JarIndex jarIndex;

	public FernandoDatabaseMapper( JarIndex jarIndex ){
		this.jarIndex = jarIndex;
	}

	

	//@Override
	public JarInfo find(String group, String artifact, String classifier, String version, String fileName, String packaging ) throws MavenhoeException
	{

		//org.postgresql.Driver.class.newInstance()
    try{ Class.forName( JDBC_DRIVER_CLASS ).newInstance(); }
		catch( ClassNotFoundException ex ) {
			throw new MavenhoeException( "Unable to find JDBC driver '"+JDBC_DRIVER_CLASS+"'", ex);
		}
		catch( InstantiationException ex ) {
			throw new MavenhoeException( "Unable to instantiate JDBC driver '"+JDBC_DRIVER_CLASS+"'", ex);
		}
		catch( IllegalAccessException ex ) {
			throw new MavenhoeException( "Unable to access JDBC driver '"+JDBC_DRIVER_CLASS+"'", ex);
		}

		// Do the SQL query.
		java.sql.Connection conn;
		String dbFileName = null;
		try{
			conn = DriverManager.getConnection(JDBC_URL);
			final String SQL = "SELECT filename FROM eap_jars WHERE group = ? AND artifact = ?";
			PreparedStatement ps = conn.prepareStatement(SQL);
			ps.setString( 1, group );
			ps.setString( 2, artifact );
			log.debug("SQL: {}; # {}, {}", new Object[]{SQL, group, artifact} );
			ResultSet rs = ps.executeQuery();
			rs.last();

			if( 0 == rs.getRow() )
				return null;

			if( 1 < rs.getRow() )
				throw new MavenhoeException("Multiple results for ["+group+":"+artifact+"].");

			dbFileName = rs.getString("filename");

		}
		catch( SQLException ex ){
			throw new MavenhoeException( "SQL Error when querying for ["+group+":"+artifact+"].", ex);
		}

		if( dbFileName == null ){
			return null;
		}


		// Once we have the supposed distr. filename, ask where in the zip it is, and return it's JarInfo.

		return jarIndex.getByNameMap().get( dbFileName ); //findJarInfoByName( dbFileName );
		

		
	}// find()

}// class
