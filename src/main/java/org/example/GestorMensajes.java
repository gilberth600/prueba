package org.example;

import java.io.*;
import java.net.*;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class GestorMensajes {
    private String xsdPath = "path/to/schema.xsd"; // Ruta del archivo XSD para validación de XML

    // Constructor de GestorMensajes
    public GestorMensajes(String xsdPath) {
        this.xsdPath = xsdPath;
    }

    // Método para validar un mensaje XML contra el esquema XSD
    public boolean validarMensaje(String xmlMensaje) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlMensaje)));
            System.out.println("Mensaje XML válido según el XSD.");
            return true;
        } catch (Exception e) {
            System.out.println("Mensaje XML no válido: " + e.getMessage());
            return false;
        }
    }

    // Enviar mensaje TCP
    public void enviarMensajeTCP(String ip, int puerto, String mensaje) {
        if (!validarMensaje(mensaje)) {
            System.out.println("No se puede enviar el mensaje TCP: XML no válido.");
            return;
        }
        try (Socket socket = new Socket(ip, puerto);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(mensaje);
            System.out.println("Mensaje TCP enviado a " + ip + ":" + puerto);
        } catch (IOException e) {
            System.out.println("Error al enviar mensaje TCP: " + e.getMessage());
        }
    }

    // Enviar mensaje UDP
    public void enviarMensajeUDP(String ip, int puerto, String mensaje) {
        if (!validarMensaje(mensaje)) {
            System.out.println("No se puede enviar el mensaje UDP: XML no válido.");
            return;
        }
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = mensaje.getBytes();
            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, puerto);
            socket.send(packet);
            System.out.println("Mensaje UDP enviado a " + ip + ":" + puerto);
            socket.close();
        } catch (IOException e) {
            System.out.println("Error al enviar mensaje UDP: " + e.getMessage());
        }
    }

    // Recibir mensaje TCP
    public String recibirMensajeTCP(int puerto) {
        try (ServerSocket serverSocket = new ServerSocket(puerto);
             Socket clientSocket = serverSocket.accept();
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String mensaje = in.readLine();
            if (validarMensaje(mensaje)) {
                System.out.println("Mensaje TCP recibido y válido.");
                return mensaje;
            } else {
                System.out.println("Mensaje TCP recibido pero no válido.");
                return null;
            }
        } catch (IOException e) {
            System.out.println("Error al recibir mensaje TCP: " + e.getMessage());
            return null;
        }
    }

    // Recibir mensaje UDP
    public String recibirMensajeUDP(int puerto) {
        try (DatagramSocket socket = new DatagramSocket(puerto)) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String mensaje = new String(packet.getData(), 0, packet.getLength());
            if (validarMensaje(mensaje)) {
                System.out.println("Mensaje UDP recibido y válido.");
                return mensaje;
            } else {
                System.out.println("Mensaje UDP recibido pero no válido.");
                return null;
            }
        } catch (IOException e) {
            System.out.println("Error al recibir mensaje UDP: " + e.getMessage());
            return null;
        }
    }

    // Método para construir un mensaje XML
    public String construirMensajeXML(String tipo, String contenido) {
        // Ejemplo básico de construcción de un mensaje XML
        return "<mensaje tipo='" + tipo + "'><contenido>" + contenido + "</contenido></mensaje>";
    }
}
