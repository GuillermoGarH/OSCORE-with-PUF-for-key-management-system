
public class ClientEA {
	
	public static void main(String args[])
	{
		LibraryClient Client =  new LibraryClient();
		Client.startClient("192.168.1.110", 5000); //CAMBIAR. SOLO PARA TESTING
		System.out.println("Client connected");
		Client.sendOut(Client.findFile("SerialNumber.txt"));
		
		String status = Client.readIn();
		if(status.equals("enrollment")) { //ENROLLMENT
			System.out.println("Dispositivo no identificado, inscripci�n en curso");
			Client.sendOut(Client.findFile("puf.txt"));
			System.out.println("Bits estables enviados al servidor, finalizando inscripci�n");
		}
		if(status.equals("authentication")) { //Authentication
			System.out.println("Dispositivo identificado, autenticaci�n en curso");
			String randomBinSeq = Client.readIn();
			String puf = Client.readFile(Client.findFile("puf.txt"));
			
			String ClientHash = Client.sendHexHash(Client.xor(randomBinSeq, puf));
			String estadodeAutenticacion = Client.readIn();
			
			byte[] secretoMaestro;
			
			if (estadodeAutenticacion.equals("Autenticaci�n correcta")) {
				System.out.println("Autenticaci�n correcta");
				secretoMaestro = Client.derivaci�nSecretoMaestro(ClientHash);
				Client.almacenarSecreto(secretoMaestro);
				System.out.println("Secreto derivado y almacenado, cierre de conexi�n");

			}else {
				System.out.println("Autenticaci�n incorrecta");

			}
		
		}
		///iff path not stated
	}

}
