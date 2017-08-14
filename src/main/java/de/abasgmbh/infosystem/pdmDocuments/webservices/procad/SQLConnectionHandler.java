package de.abasgmbh.infosystem.pdmDocuments.webservices.procad;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

import de.abasgmbh.infosystem.pdmDocuments.PdmDocumentsException;
import de.abasgmbh.infosystem.pdmDocuments.config.Configuration;
import de.abasgmbh.infosystem.pdmDocuments.utils.Util;


public class SQLConnectionHandler {

	private Connection connection = null;
	Logger log = Logger.getLogger(SQLConnectionHandler.class);
		
		public Connection getConnection()
		{
			return connection;
		}
		
		public SQLConnectionHandler(Configuration config) throws PdmDocumentsException {
			// Declare the JDBC objects.
			connection = null;


			try {
				Class.forName(config.getSqlDriver());
				connection = DriverManager.getConnection("jdbc:sqlserver://" + config.getSqlServer() +":"+config.getSqlPortString()+";databasename=" + config.getSqldatabase(), config.getSqlUser(),config.getSqlPassword());
				log.info(Util.getMessage("pdmDocument.info.sqlconnection.connect"));
			} catch (Exception e) {
				log.error(Util.getMessage("pdmDocument.error.sqlconnection.connect"), e);
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.error.sqlconnection.connect"), e);
			} finally {
				if (connection != null)
					try {
						connection.close();
					} catch (Exception e) {
					}
			}
		}
	}
	
