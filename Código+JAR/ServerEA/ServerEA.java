
public class ServerEA {
	
	public static void main(String args[])
	{
		LibraryServer Server =  new LibraryServer();
		Server.startServer(5000);
		Server.setPath("/home/" + System.getProperty("user.name") + "/Devices");
		String SerialNumber = Server.readIn();
		String status = Server.statusOfDevice(SerialNumber);
		
		if(status.equals("enrollment")) { //ENROLLMENT
			System.out.println("Dispositivo no identificado, inscripcion en curso");
			String puf = Server.readIn();
			Server.createJSON(SerialNumber, puf);
			System.out.println("Inscripcion realizada correctamente");
			Server.closeAll();
		}
		if(status.equals("authentication")) { //Authentication
			System.out.println("Dispositivo identificado, autenticacion en curso");
			String puf = Server.readJSONPuf(SerialNumber);
			String randomBinSeq = Server.generateBinRandom(puf);
			Server.sendBinSeq(randomBinSeq);
			
			String ServerHash = Server.HexHash(Server.xor(randomBinSeq, puf));
			String ClientHash = Server.readIn();

			
			boolean isCorrect = Server.isHashCorrect(ServerHash, ClientHash);

			if(isCorrect == true) {
				byte[] secretoMaestro = Server.derivacionSecretoMaestro(ServerHash);

				Server.almacenarSecreto(SerialNumber, secretoMaestro);
				Server.addSNtoLog(SerialNumber);
				System.out.println("Secreto derivado y almacenado, cierre de conexion");
				Server.closeAll();
			
		}
	}

}
}