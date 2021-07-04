import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class LibraryClient {
	private SSLSocket sslsocket		 = null;
	private BufferedReader  in = null;
	private PrintStream  out	 = null;
	
	public void startClient(String address, int port) {
		InputStream inStream =getClass().getResourceAsStream("/client.trust");

		 File tmpFile = null;
		try {
			tmpFile = File.createTempFile("clientStream", ".tmp",new File("/home/pi/Desktop/EnrollmentAndAuthentication"));
			tmpFile.deleteOnExit();
	        FileOutputStream outStream = new FileOutputStream(tmpFile);
           IOUtils.copy(inStream, outStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	       
		System.setProperty("javax.net.ssl.trustStore", tmpFile.getPath());
		System.setProperty("javax.net.ssl.trustStorePassword", "password");
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");

		//System.setProperty("javax.net.debug", "all");
		
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			     
	        sslsocket=(SSLSocket) factory.createSocket(address,port);
			out = new PrintStream(sslsocket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public File findFile(String addToPath)
	{
		return new File("/home/pi/Desktop/Resultados/Resultado de la PUF/" + addToPath);
	}
	
	public String readFile(File dataFile) {
		String data = "";
 
		try {
			Scanner scanner = new Scanner(dataFile);
		
			//System.out.println(SNumber.nextLine());
			// keep reading until "Over" is input
			while (scanner.hasNextLine())
			{

					data = data + scanner.nextLine();
					//System.out.println(data);
			}
			scanner.close();
	     }catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	     }
		return data;
	}
	
	public void sendOut(File dataFile) {
	     try {
			Scanner scanner = new Scanner(dataFile);
		
			String data = "";
			//System.out.println(SNumber.nextLine());
			// keep reading until "Over" is input
			while (scanner.hasNextLine())
			{

					data = scanner.nextLine();
					out.println(data);
					//System.out.println(data);
			}
			scanner.close();
			out.println("Over");
	     }catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String readIn() {

		String line = "";
		String wholeData = "";
		  while(!line.equals("Over")) {
			  try
				{
					line = in.readLine();

					if(!line.equals("Over")) {
						wholeData += line;
					}
				}
				catch(IOException i)
				{
					System.out.println(i);
				}
		  }
		  return wholeData;

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
	
	public String sendHexHash(byte[] xor) {
	      String hexHashStr = "";
	      try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashXoR = digest.digest(xor); 
			hexHashStr = bits2Hex(hashXoR);
			
			for(int i = 0; i < hexHashStr.length();i++) {
				out.println(Character.toString(hexHashStr.charAt(i)));
			}
			
			out.println("Over");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return hexHashStr;

	}
	
	public byte[] derivaciónSecretoMaestro(String ownHash) {
		
			String hashBits = Hex2bits(ownHash);
			hashBits = String.format("%256s", hashBits).replace(' ', '0');
			byte [] bList = new byte[32];
			int byteToStore = 0b0;
	
			for (int i = 0; i<hashBits.length(); i = i+8) {
				 byteToStore =  Integer.parseInt(hashBits.substring(i,i+8), 2);	
				 bList[i/8] = (byte) ( byteToStore & 0XFF);
;
		}
		
		return bList;
	}
	
	public void almacenarSecreto( byte[] secreto) {
		try {	
			String path = "/home/pi/Desktop/Resultados/Resultados Inscripcion y Autenticacion/SecretoMaestro.json"; 
       
				JSONObject jsonObj = new JSONObject();
				
				JSONArray secretBytes = new JSONArray();
				for(int i = 0;i<secreto.length;i++) {
					secretBytes.add(secreto[i]);
				}
				
				jsonObj.put("Secreto Maestro",secretBytes);
		
				FileWriter secretFile = new FileWriter(path);
				
				secretFile.write(jsonObj.toJSONString());
				
				secretFile.close();
            
		}catch(IOException i)
		{
			System.out.println(i);
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
