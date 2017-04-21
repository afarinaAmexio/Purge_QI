package com.amexio;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;

public class PurgeQIMain {
	private static int batch_size;

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		info("Début de traitement");
		batch_size=10000;
		if (args !=null)
		{
			for (int j = 0; j < args.length; j++) {
				debug("Argument "+j+" : "+args[j]);				
			}
			if (args.length!=2) {
				error("Nombre de paramètres incorrect.");
				usage();
				System.exit(1);			
			} 
			if (args[0].equalsIgnoreCase("-n")) {
				batch_size=Integer.parseInt(args[1]);
			}
			else {
				error("Paramètre incorrect");
				usage();
				System.exit(1);						
			}
		}
		new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String Rep = PurgeQIUtils.getConfigVal(PurgeQIConstants.REPO, PurgeQIConstants.PROPFILE);
		String UName = PurgeQIUtils.getConfigVal(PurgeQIConstants.USERNAME, PurgeQIConstants.PROPFILE);
		String UPass = PurgeQIUtils.getConfigVal(PurgeQIConstants.USERPASS, PurgeQIConstants.PROPFILE);

		info("Ouverture de session Documentum...");
		IDfSessionManager sMgr=null;
		IDfSession dfSession =null;
		try {
			sMgr = PurgeQIUtils.createSessionManager(Rep, UName, UPass);
			dfSession = sMgr.getSession(Rep);
		} catch (DfException e) {
			error("Erreur lors de la prise de session Documentum",e);
			System.exit(1);
		}
		info("\tOK");
		try {
			traiteQI(dfSession);
		} catch (NumberFormatException e) {
			error("L'argument -n doit être suivi d'un nombre.");
			usage();
			System.exit(1);
		} catch (DfException e) {
			System.exit(1);
		} catch (IOException e) {
			System.exit(1);
		} finally {
			if (dfSession!=null) {
				PurgeQIUtils.closeSess(sMgr, dfSession);
			}
		}
	}

	/**
	 * Correction des erreurs CC-0007
	 * @param dfSession Session Dctm
	 * @throws DfException
	 * @throws IOException
	 */
	private static void traiteQI(IDfSession dfSession) throws DfException, IOException {
		info("Génération du script de purge des Queue Item");
		String dqlCount="select count(r_object_id) as total from dmi_queue_item where name ='dm_fulltext_index_user'";
		info("Comptage du nombre de QI à purger...");
		debug("\t\t"+dqlCount);
		IDfCollection coll=null;
		IDfCollection coll2=null;
		IDfCollection coll3=null;
		int resultat=0;
		try {
			coll=PurgeQIUtils.executeQuery(dqlCount, dfSession);
			int total=0;
			while(coll.next()) {
				total = coll.getInt("total");
				info("Calcul du nombre d'itérations pour une taille de batch de "+batch_size);
				int nbIterations = (int) Math.ceil(total/batch_size);
				for (int i = 0; i < nbIterations; i++) {
					info("\tTraitement itération "+i+"/"+nbIterations);
					String dql="select r_object_id from dmi_queue_item where name ='dm_fulltext_index_user' order by r_object_id asc enable (return_top "+batch_size+")";
					coll2=PurgeQIUtils.executeQuery(dql, dfSession);
					String max_roid="0000000000000000";
					while(coll2.next()) {
						max_roid=coll2.getString("r_object_id");
					}
					String dqlDelete="delete dmi_queue_item objects where name ='dm_fulltext_index_user' and r_object_id <='"+max_roid+"'";
					debug("\t\t"+dqlDelete);
					coll3=PurgeQIUtils.executeQuery(dqlDelete, dfSession);
					while(coll3.next()){
						int resultatIter = coll3.getInt(coll3.getAttr(0).getName());
						debug("\t\tObjets supprimés="+resultatIter);
						resultat+=resultatIter;
					}
				}
			}
		} catch (DfException e) {
			error("Erreur lors de la purge des QI" ,e);
			throw e;
		} finally {
			if (coll!=null) {
				coll.close();
			}
			if (coll2!=null) {
				coll2.close();
			}
			if (coll3!=null) {
				coll3.close();
			}
		}
		info("\tFin de suppression - Nombre d'objets supprimés : "+resultat);
	}
	private static void error(String message,Exception t) {
		PurgeQIUtils.error(null, message, t);
	}

	private static void info(String message,Exception t) {
		PurgeQIUtils.info(null, message, t);
	}
	private static void debug(String message,Exception t) {
		PurgeQIUtils.debug(null, message, t);
	}
	private static void debug(String message) {
		debug(message,null);
	}	
	private static void error(String message) {
		error(message,null);
	}	
	private static void info(String message) {
		info(message,null);
	}
	private static void usage() {
		System.out.println("Utilisation : ");
		System.out.println("\t purge_qi.bat [-n NNN]");
		System.out.println("\t\t -n NNN : défini la taille du batch à NNN objets");
	}
}
