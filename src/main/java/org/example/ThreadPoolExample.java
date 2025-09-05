package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolExample {

    public static void main(String[] args) {
        int port = 5050;

        // Crear un pool de 3 hilos para manejar múltiples clientes
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor TCP escuchando en el puerto " + port);

            while (true) {
                // Espera conexiones de clientes
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Asigna la conexión a un hilo del pool
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String mensaje = in.readLine();

                if (mensaje == null || !mensaje.matches("[a-zA-Z]+")) {
                    out.println("ERROR: El mensaje debe contener solo caracteres del alfabeto.");
                    return;
                }

                // Calcular longitud de la palabra
                int longitud = mensaje.length();

                // Verificar si es par o impar
                String tipo = (longitud % 2 == 0) ? "par" : "impar";

                // Armar respuesta
                String respuesta = "La palabra tiene longitud " + longitud + " y es " + tipo + ".";
                System.out.println("Procesado: " + respuesta + " (Hilo: " + Thread.currentThread().getName() + ")");
                out.println(respuesta);

                // Guardar en el archivo correspondiente
                String nombreArchivo = tipo.equals("par") ? "par.txt" : "impar.txt";
                try (FileWriter fw = new FileWriter(nombreArchivo, true);
                     BufferedWriter bw = new BufferedWriter(fw);
                     PrintWriter pw = new PrintWriter(bw)) {
                    pw.println(mensaje); // Guardamos solo la palabra
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
