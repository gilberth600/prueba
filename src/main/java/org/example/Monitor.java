package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Monitor {
    private List<String> listaMensajes; // Lista para almacenar mensajes recibidos
    private List<String> listaAgentes;  // Lista para almacenar información de los agentes
    private final String csvPath = "monitor_log.csv"; // Ruta para el archivo de registro CSV
    private final int puertoEscucha = 4300; // Puerto en el que escucha el monitor

    public Monitor() {
        listaMensajes = new ArrayList<>();
        listaAgentes = new ArrayList<>();
        inicializarCSV();
        iniciarEscucha();
    }

    // Método para inicializar el archivo CSV con encabezados
    private void inicializarCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath, true))) {
            writer.println("Fecha,Tipo,Contenido,IP,Puerto");
            System.out.println("Archivo CSV inicializado para registro de mensajes.");
        } catch (IOException e) {
            System.out.println("Error al inicializar el archivo CSV: " + e.getMessage());
        }
    }

    // Método para iniciar la escucha de mensajes
    private void iniciarEscucha() {
        Thread escuchaThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(puertoEscucha)) {
                System.out.println("Monitor escuchando en el puerto: " + puertoEscucha);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String mensaje = in.readLine();
                    procesarMensaje(mensaje, clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error en el servidor de escucha del monitor: " + e.getMessage());
            }
        });
        escuchaThread.start();
    }

    // Procesa el mensaje recibido y lo registra
    private void procesarMensaje(String mensaje, String ip, int puerto) {
        System.out.println("Mensaje recibido del agente " + ip + ":" + puerto + " - " + mensaje);

        // Determina el tipo de mensaje y lo almacena en la lista adecuada
        if (mensaje.contains("heNacido")) {
            registrarAgente(ip, puerto);
        }
        registrarMensaje(mensaje, "General", ip, puerto);
    }

    // Almacena los mensajes en la lista y los guarda en el archivo CSV
    private void registrarMensaje(String mensaje, String tipo, String ip, int puerto) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String registro = timestamp + "," + tipo + "," + mensaje + "," + ip + "," + puerto;
        listaMensajes.add(registro);
        escribirCSV(registro);
    }

    // Registra un agente en la lista de agentes
    private void registrarAgente(String ip, int puerto) {
        String agenteInfo = "Agente en " + ip + ":" + puerto;
        if (!listaAgentes.contains(agenteInfo)) {
            listaAgentes.add(agenteInfo);
            System.out.println("Nuevo agente registrado: " + agenteInfo);
        }
    }

    // Escribe un registro en el archivo CSV
    private void escribirCSV(String registro) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath, true))) {
            writer.println(registro);
            System.out.println("Registro guardado en CSV: " + registro);
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo CSV: " + e.getMessage());
        }
    }

    // Método para mostrar todos los mensajes guardados
    public void mostrarMensajes() {
        System.out.println("Mensajes almacenados:");
        for (String mensaje : listaMensajes) {
            System.out.println(mensaje);
        }
    }

    // Método para mostrar todos los agentes registrados
    public void mostrarAgentes() {
        System.out.println("Agentes registrados:");
        for (String agente : listaAgentes) {
            System.out.println(agente);
        }
    }
}
