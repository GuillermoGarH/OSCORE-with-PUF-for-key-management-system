/*******************************************************************************
 * Copyright (c) 2018 RISE SICS and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Tobias Andersson (RISE SICS)
 *    
 ******************************************************************************/
package org.eclipse.californium.oscore;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Scanner;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.cose.AlgorithmID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * 
 * HelloWorldServer to display basic OSCORE mechanics
 *
 */
public class ServidorOSCORE {

	private final static HashMapCtxDB db = new HashMapCtxDB();
	private final static String uriLocal = "coap://localhost";
	private final static AlgorithmID alg = AlgorithmID.AES_CCM_16_64_128;
	private final static AlgorithmID kdf = AlgorithmID.HKDF_HMAC_SHA_256;

	// test vector OSCORE draft Appendix C.1.2
	private static byte[] master_secret = {};	
	
	
	
	private final static byte[] master_salt = { (byte) 0x9e, (byte) 0x7c, (byte) 0xa9, (byte) 0x22, (byte) 0x23,(byte) 0x78, (byte) 0x63, (byte) 0x40 };
	private final static byte[] sid = new byte[] { 0x01 };
	private final static byte[] rid = new byte[0];

	public static void main(String[] args) throws OSException {
		
		String SerialNumber = deviceSNtoComs();
		master_secret = readJSONMS(SerialNumber);
		
		
		OSCoreCtx ctx = new OSCoreCtx(master_secret, false, alg, sid, rid, kdf, 32, master_salt, null);
		db.addContext(uriLocal, ctx);
		OSCoreCoapStackFactory.useAsDefault(db);

		final CoapServer server = new CoapServer(5683);


		OSCoreResource ComsResource = new OSCoreResource("ComsResource", true) {
			Scanner sc = new Scanner(System.in);
			@Override
			public void handleGET(CoapExchange exchange) {
				System.out.println("Accessing ComsResource resource");
				String data = sc.nextLine();
				Response r = new Response(ResponseCode.CONTENT);

				r.setPayload(data);
				exchange.respond(r);
				if(data.equals("Over")) {
					server.destroy();
				}
				

			}
		};
		
		server.add(ComsResource);
		server.start();
	}
	
	public static String deviceSNtoComs() {
	    StringBuilder builder = new StringBuilder();
	    RandomAccessFile randomAccessFile = null;
	    String SerialNumber = "ERROR";
		try {
			File logSN = new File("/home/" + System.getProperty("user.name") + "/Devices/SerialNumbers.log");
		    randomAccessFile = new RandomAccessFile(logSN, "r");
		    
		    long startLastSN = logSN.length() - 17;
		    long endLastSN = logSN.length() - 1;
		    
		    randomAccessFile.seek(startLastSN);
		    char SNchar;
			for (long i = startLastSN; i<endLastSN;i++ ) {
			    randomAccessFile.seek(i);
			    SNchar = (char)randomAccessFile.read();
		        builder.append(SNchar);
			}
		 SerialNumber = builder.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  return SerialNumber;
	}
	
	public static byte[] readJSONMS(String SerialNumber) {
		
		String path = "/home/" + System.getProperty("user.name") +  "/Devices";
		byte[] secretInfo= new byte[32];
		try {
			String fullPath = path + "/" + SerialNumber + "/" + "SecretoMaestro.json";
			JSONParser parser = new JSONParser();
			Object obj;
			obj = parser.parse(new FileReader(fullPath));
			JSONObject jsonObj = (JSONObject) obj;
			JSONArray secretBytes = (JSONArray)jsonObj.get("Secreto Maestro");
			String seq;
			for(int i = 0; i<secretBytes.size();i++) {
				seq = secretBytes.get(i).toString();
				secretInfo[i] = (byte) Integer.parseInt(seq);
			}
			
  

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	
		}
		
			return secretInfo;

	}
}
