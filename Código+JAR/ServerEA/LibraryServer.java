import java.io.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class LibraryServer {

	private SSLSocket sslsocket = null;
	private SSLServerSocket sslserversocket = null;
	private BufferedReader  in	 = null;
	private PrintStream  out	 = null;
	private String path = null;
	
	public void startServer(int port) {
		InputStream inStream =getClass().getResourceAsStream("/server.pkcs12");
        File folderDev = new File("/home/" + System.getProperty("user.name") + "/Devices");  
        boolean folderCreated = folderDev.mkdir(); 
		 File tmpFile = null;
		try {
			tmpFile = File.createTempFile("serverStream", ".tmp",new File("/home/" + System.getProperty("user.name") + "/Devices"));
			tmpFile.deleteOnExit();
	        FileOutputStream outStream = new FileOutputStream(tmpFile);
            IOUtils.copy(inStream, outStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	       

    	System.setProperty("javax.net.ssl.keyStore", tmpFile.getPath());
    	System.setProperty("javax.net.ssl.keyStorePassword", "password");
    	//System.setProperty("javax.net.debug", "all");
    	
        SSLServerSocketFactory factory=(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        try {
			sslserversocket=(SSLServerSocket) factory.createServerSocket(port);
	
		System.out.println("Waiting for a client ...");

        sslsocket=(SSLSocket) sslserversocket.accept();
		System.out.println("Client accepted");
		
		in = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
		out = new PrintStream (sslsocket.getOutputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}

	public String statusOfDevice(String SerialNumber) {
		String path = getPath();
		String status = "enrollment";
		if(path.equals(null)) {
			System.out.println("Path not stated");
		}else {
			path = path + "/" + SerialNumber;  //Cambiar a /

			File f = new File(path);
			if(f.exists()) {
				status = "authentication";
			}
		}
		out.println(status);

		out.println("Over");
		return status;
	}

	public void createJSON(String SerialNumber, String puf) {
		
		try {	
			String path = getPath() + "/" + SerialNumber ; 

			String completePath = path +  "/puf.json";  
			
	        File folder = new File(path);  
	        boolean folderCreated = folder.mkdir();  
	        if(folderCreated){  
	        	System.out.println("Folder for device created at " + path);  
		        
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("puf",puf);

				jsonObj.put("SerialNumber",SerialNumber);
		
				FileWriter pufFile = new FileWriter(completePath);
				
	            pufFile.write(jsonObj.toJSONString());
				
	            pufFile.close();
	        }
	        else{  
 	           System.out.println("Error Found!");  
 	        } 
            
		}catch(IOException i)
		{
			System.out.println(i);
		}


	}

	public String readJSONPuf(String SerialNumber) {
		
		String path = getPath();
		String puf = "";
		try {
			String pufFilePath = path + "/" + SerialNumber + "/" + "puf.json";
				
			JSONParser parser = new JSONParser();
			Object obj;
			obj = parser.parse(new FileReader(pufFilePath));
			JSONObject jsonObj = (JSONObject) obj;

   			puf = (String) jsonObj.get("puf");


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			return puf;

	}
	
	public String readIn() {
		String data = "";
		String completeData = "";
		while(!data.equals("Over")) {
			try {
				data = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(!data.equals("Over")) {
				completeData += data;
			}
			
		}
		return completeData;
	}

	public String generateBinRandom(String puf) {
		String binSeq = "";
		for(int i = 0;i< puf.length();i++) {
				int bit = ((int)(Math.random() * 100)) % 2;
				binSeq = binSeq + String.valueOf(bit);
			}
		
		return binSeq;
	}

	public void sendBinSeq(String randomBinSeq) {
		
		for (int i = 0; i < randomBinSeq.length(); i++) {
			  char bit = randomBinSeq.charAt(i);
			  out.println(Character.toString(bit));
			}
			out.println("Over");
	}
	
	public byte[] xor(String randomBinseq, String completePUFseq) {
	      byte[] xor = new byte[completePUFseq.length()];
	      for (int i = 0; i < completePUFseq.length(); i++) {
	            if (completePUFseq.charAt(i) == randomBinseq.charAt(i))
	            	xor[i] = 0;
	            else
	            	xor[i] = 1;
	      }
	      return xor;
	}

	public String bits2Hex(byte[] bits) {
        StringBuffer hexaNum = new StringBuffer();
        for(int c = 0; c < bits.length; c++) {
        	hexaNum.append(Integer.toString((bits[c] & 0xff) + 0x100, 16).substring(1));
        }
        return hexaNum.toString();
	}
	
	public String Hex2bits(String HexString) {
		String bits = new BigInteger(HexString,16).toString(2);

		return bits;
	}
	
	public String HexHash(byte[] xor) {

	      String hexHashStr = "";
	      try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashXoR = digest.digest(xor); 
			hexHashStr = bits2Hex(hashXoR);
			
			
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return hexHashStr;
	}

	public boolean isHashCorrect(String ownHash, String receivedHash) {
		
		if(ownHash.equals(receivedHash)) {
			System.out.println("Autenticación correcta.");
			out.println("Autenticación correcta");
			out.println("Over");

			return true;
		}
			System.out.println("Autenticación incorrecta.");
			out.println("Autenticación incorrecta");
			out.println("Over");

			return false;
			
	}
	
	public byte[] derivacionSecretoMaestro(String ownHash) {
		String hashBits = Hex2bits(ownHash);
		hashBits = String.format("%256s", hashBits).replace(' ', '0');
		byte [] bList = new byte[32];
		int byteToStore = 0b0;

		for (int i = 0; i<hashBits.length(); i = i+8) {
			 byteToStore =  Integer.parseInt(hashBits.substring(i,i+8), 2);	
			 bList[i/8] = (byte) ( byteToStore & 0XFF);
		}
		
		return bList;
	}
	
	public void almacenarSecreto(String SerialNumber,byte[] secreto) {
		try {	
			String path = getPath() + "/" + SerialNumber +  "/SecretoMaestro.json" ; 
				JSONObject jsonObj = new JSONObject();
				
				JSONArray secretBytes = new JSONArray();
				for(int i = 0;i<secreto.length;i++) {
					secretBytes.add(secreto[i]);
				}
				jsonObj.put("Serial Number", SerialNumber);
				jsonObj.put("Secreto Maestro",secretBytes);
		
				FileWriter secretFile = new FileWriter(path);
				
				secretFile.write(jsonObj.toJSONString());
				
				secretFile.close();
            
		}catch(IOException i)
		{
			System.out.println(i);
		}

	}
	
	public void addSNtoLog(String SerialNumber) {
	    Logger logger = Logger.getLogger("Serial Numbers");  
	    FileHandler fh;  

	    try {  
	        fh = new FileHandler("/home/" + System.getProperty("user.name") + "/Devices/" +SerialNumber +"/SerialNumbers.log");  
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  

	        logger.info(SerialNumber);  

	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	}
	
	////////////
	
	public void closeAll() {
		try {
			
			in.close();
			out.close();
			sslsocket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

}