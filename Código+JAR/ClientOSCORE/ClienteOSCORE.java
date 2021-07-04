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

import java.io.FileReader;
import java.io.IOException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.cose.AlgorithmID;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * 
 * HelloWorldClient to display the basic OSCORE mechanics
 *
 */
public class ClienteOSCORE {

	private final static HashMapCtxDB db = new HashMapCtxDB();
	private final static String uriLocal = "coap://192.168.1.110";
	private final static String ComsResource = "/ComsResource";
	private final static AlgorithmID alg = AlgorithmID.AES_CCM_16_64_128;
	private final static AlgorithmID kdf = AlgorithmID.HKDF_HMAC_SHA_256;

	// test vector OSCORE draft Appendix C.1.1
	private static byte[] master_secret = {};
	private final static byte[] master_salt = { (byte) 0x9e, (byte) 0x7c, (byte) 0xa9, (byte) 0x22, (byte) 0x23,
			(byte) 0x78, (byte) 0x63, (byte) 0x40 };
	private final static byte[] sid = new byte[0];
	private final static byte[] rid = new byte[] { 0x01 };

	public static void main(String[] args) throws OSException, ConnectorException, IOException {

		master_secret = readJSONMS();

		OSCoreCtx ctx = new OSCoreCtx(master_secret, true, alg, sid, rid, kdf, 32, master_salt, null);
		db.addContext(uriLocal, ctx);

		OSCoreCoapStackFactory.useAsDefault(db);
		CoapClient c = new CoapClient(uriLocal + ComsResource);

		Request r = new Request(Code.GET);
		CoapResponse resp = c.advanced(r);
		while(resp == null) {}

		printResponse(resp);

		String data = "";
		
		r = new Request(Code.GET);
		r.getOptions().setOscore(new byte[0]);
		while(resp == null) {}
		while (!resp.getResponseText().equals("Over")) {
			if(!resp.getResponseText().equals("Over")) {
				resp = c.advanced(r);
				printResponse(resp);
			}
		}
		c.shutdown();
	}

	private static void printResponse(CoapResponse resp) {
		if (resp != null) {
			System.out.println("RESPONSE CODE: " + resp.getCode().name() + " " + resp.getCode());
			if (resp.getPayload() != null) {
				System.out.print("RESPONSE PAYLOAD: ");
				for (byte b : resp.getPayload()) {
					System.out.print(Integer.toHexString(b & 0xff) + " ");
				}
				System.out.println();
			}
			System.out.println("RESPONSE TEXT: " + resp.getResponseText());
			
		} else {
			System.out.println("RESPONSE IS NULL");
		}
	}
	
	public static byte[] readJSONMS() {
		
		String path = "/home/pi/Desktop/Resultados/Resultados Inscripcion y Autenticacion";
		byte[] secretInfo= new byte[32];
		try {
			String fullPath = path  + "/SecretoMaestro.json";
				
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
