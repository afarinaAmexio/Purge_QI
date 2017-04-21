package com.amexio;

import java.io.File;
import java.util.ResourceBundle;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;

/**
 *
 * @author zkaiserm
 * @author afarina
 *
 */
public class PurgeQIUtils {
	/**
	 * Read properties and get values for each key
	 *
	 * @param configKey
	 * @param propFileName
	 * @return
	 */
	public static String getConfigVal(final String configKey, final String propFileName) {
		ResourceBundle appConfig = null;
		appConfig = ResourceBundle.getBundle(propFileName);
		if (appConfig.containsKey(configKey)) {
			return appConfig.getString(configKey).trim();
		} else {
			return null;
		}
	}

	/**
	 * Create Session Manager
	 *
	 * @param docbase
	 * @param user
	 * @param pass
	 * @return
	 * @throws DfException
	 */
	public static IDfSessionManager createSessionManager(final String docbase, final String user, final String pass)
			throws DfException {
		debug(null, "Création d'un session manager - docbase : "+docbase+", User : "+user+", pass : ******");
		final IDfClientX clientx = new DfClientX();
		final IDfClient client = clientx.getLocalClient();
		final IDfSessionManager sMgr = client.newSessionManager();
		final IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		loginInfoObj.setUser(user);
		loginInfoObj.setPassword(pass);
		loginInfoObj.setDomain(null);
		sMgr.setIdentity(docbase, loginInfoObj);
		return sMgr;
	}



	/**
	 * Create Directory on the file system
	 *
	 * @param root
	 * @param fldrName
	 * @return
	 * @throws Exception
	 */
	public static String makeDir(final String root, final String fldrName) throws Exception {
		String dirPath = null;
		if (new File(root).exists()) {
			dirPath = root + fldrName;
			if (!new File(dirPath).exists()) {
				final boolean success = (new File(dirPath)).mkdir();
				if (success) {
					return dirPath;
				} else {
					return null;
				}
			} else {
				return dirPath;
			}
		} else {
			throw new Exception("Le chemin "+root+" n'existe pas.");
		}
	}

	/**
	 * Run DQL and return Collection object
	 *
	 * @param dqlQuery
	 * @param dfSession
	 * @return
	 * @throws DfException
	 */
	public static IDfCollection executeQuery(final String dqlQuery, final IDfSession dfSession) throws DfException {
		IDfCollection coll = null;
		final IDfQuery qry = new DfQuery();
		qry.setDQL(dqlQuery);
		coll = qry.execute(dfSession, IDfQuery.DF_READ_QUERY);
		return coll;
	}


		/**
	 * Close DCTM Session
	 *
	 * @param sMgr
	 * @param dfSession
	 */
	public static void closeSess(final IDfSessionManager sMgr, final IDfSession dfSession) {
		if (dfSession != null) {
			sMgr.release(dfSession);
		}
	}
	public static void error(Object cllog,String message,Exception t) {
		System.out.println(message);
		if (cllog==null) {
			DfLogger.error(PurgeQIUtils.class , message, null, t);
		} else {
			DfLogger.error(cllog.getClass(), message, null, t);
		}
	}
	public static void debug(Object cllog,String message,Exception t) {
		if (cllog==null) {
			DfLogger.debug(PurgeQIUtils.class , message, null, t);
		} else {
			DfLogger.debug(cllog.getClass(), message, null, t);
		}
	}
	public static void info(Object cllog,String message,Exception t) {
		System.out.println(message);
		if (cllog==null) {
			DfLogger.info(PurgeQIUtils.class , message, null, t);
		} else {
			DfLogger.info(cllog.getClass(), message, null, t);
		}
	}
	public static void error(Object cllog,String message) {
		error(cllog,message,null);
	}	
	public static void debug(Object cllog,String message) {
		debug(cllog,message,null);
	}
	public static void info(Object cllog,String message) {
		info(cllog,message,null);
	}
}
