package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.net.util.SubnetUtils;

public class AgenteCambiaCromos {
    private String ip;
    private int puerto;
    private List<String> listaIPs; // Lista de IPs conocidas
    private List<String> cromosDeseados;
    private List<String> cromosPoseidos;
    private int rupias;
    private final String monitorIP = "192.168.1.100"; // IP del monitor (se establece manualmente)
    private final int monitorPuerto = 4300; // Puerto del monitor
    private final String xsdPath = "path/to/schema.xsd"; // Ruta del archivo XSD para validación

    public AgenteCambiaCromos(List<String> cromosDeseados, List<String> cromosPoseidos, int rupias) {
        this.cromosDeseados = cromosDeseados;
        this.cromosPoseidos = cromosPoseidos;
        this.rupias = rupias;
        inicializarAgente();
    }

    // Inicializa el agente asignándole un puerto, IPs posibles, y carga el esquema XSD
    private void inicializarAgente() {
        buscarNido();
        establecerIPs();
        cargarXSD();
        enviarMensajeNacimiento();
        crearThreadEscucha();
        buscarAgentes();
    }

    // Busca un puerto libre entre 4000 y 4100 para establecer el agente
    private void buscarNido() {
        for (int i = 4000; i <= 4100; i += 2) { // Usando puertos pares para TCP
            try {
                ServerSocket serverSocket = new ServerSocket(i);
                this.puerto = i;
                this.ip = InetAddress.getLocalHost().getHostAddress();
                serverSocket.close();
                System.out.println("Agente asignado al puerto: " + this.puerto);
                break;
            } catch (IOException e) {
                System.out.println("Puerto " + i + " ocupado, intentando siguiente...");
            }
        }
    }

    // Asigna todas las IPs de la subred a la lista de IPs posibles
    private void establecerIPs() {
        listaIPs = new ArrayList<>();
        SubnetUtils utils = new SubnetUtils("192.168.1.0/24");
        listaIPs = Arrays.asList(utils.getInfo().getAllAddresses());
        System.out.println("IPs posibles cargadas: " + listaIPs.size());
    }

    // Carga el archivo XSD para validación de mensajes XML
    private void cargarXSD() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            System.out.println("XSD cargado correctamente para validación.");
        } catch (Exception e) {
            System.out.println("Error al cargar XSD: " + e.getMessage());
        }
    }

    // Enviar mensaje de nacimiento al monitor
    private void enviarMensajeNacimiento() {
        try {
            Socket socket = new Socket(monitorIP, monitorPuerto);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String mensaje = crearMensaje("heNacido");
            out.println(mensaje);
            socket.close();
            System.out.println("Mensaje de nacimiento enviado al monitor.");
        } catch (IOException e) {
            System.out.println("Error al enviar mensaje al monitor: " + e.getMessage());
        }
    }

    // Crea una plantilla de mensaje XML según el tipo
    private String crearMensaje(String tipo) {
        // Usar DOM para construir el XML (simplificado aquí)
        return "<mensaje tipo='" + tipo + "'><contenido>Información</contenido></mensaje>";
    }

    // Crea un hilo de escucha para recibir mensajes de otros agentes
    private void crearThreadEscucha() {
        Thread escuchaThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(puerto)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String mensaje = in.readLine();
                    procesarMensaje(mensaje);
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error en el servidor de escucha: " + e.getMessage());
            }
        });
        escuchaThread.start();
    }

    // Procesa los mensajes recibidos
    private void procesarMensaje(String mensaje) {
        System.out.println("Mensaje recibido: " + mensaje);
        // Procesar el mensaje según tipo (a implementar)
    }

    // Busca otros agentes en la red
    private void buscarAgentes() {
        Thread buscarAgentesThread = new Thread(() -> {
            while (true) {
                for (String ip : listaIPs) {
                    for (int port = 4000; port <= 4100; port += 2) {
                        try {
                            Socket socket = new Socket(ip, port);
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(crearMensaje("hola"));
                            socket.close();
                            System.out.println("Mensaje de 'hola' enviado a " + ip + ":" + port);
                        } catch (IOException e) {
                            // Si no se conecta, pasa al siguiente
                        }
                    }
                }
                try {
                    Thread.sleep(2000); // Evita sobrecargar la búsqueda
                } catch (InterruptedException e) {
                    System.out.println("Error en búsqueda de agentes: " + e.getMessage());
                }
            }
        });
        buscarAgentesThread.start();
    }

    // Funciones básicas como "reproducirse", "pararse", "matarse" (simplificadas aquí)
    public void reproducirse() {
        System.out.println("El agente se está reproduciendo...");
        // Lógica de reproducción
    }

    public void pararse() {
        System.out.println("El agente se ha detenido.");
        // Lógica de parada
    }

    public void matarse() {
        System.out.println("El agente se ha autodestruido.");
        // Lógica de autodestrucción
    }
}

