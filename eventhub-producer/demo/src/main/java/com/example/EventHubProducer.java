package com.example;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class EventHubProducer {

    private static final String connectionString = "Endpoint=sb://evhns-master-feb24.servicebus.windows.net/;SharedAccessKeyName=ehsalesdirective;SharedAccessKey=M7fBh7QUexW8wbfYCQKaDcKuRADOzZyzP+AEhOg3zwQ=;EntityPath=eh-sales";

    private static final int NUM_RECORDS = 10; // Número de registros que deseas crear

    //private static final List<String> products = generateProducts(50); // Genera 50 productos distintos

    private static final int numProducts = 50; // Número total de productos
    private static final int numEmptyProducts = 20; // Número de productos vacíos
    
    private static final List<String> products = generateProducts(numProducts, numEmptyProducts); // Genera la lista de productos    
    private static final List<Integer> clients = generateClientIds(100); // Genera 100 IDs de cliente únicos
    public static void main(String[] args) {
        EventHubProducerClient producer = new EventHubClientBuilder()
                .connectionString(connectionString)
                .buildProducerClient();

        try {
            List<EventData> allEvents = createEventDataList(NUM_RECORDS);
            EventDataBatch eventDataBatch = producer.createBatch();

            for (EventData eventData : allEvents) {
                if (!eventDataBatch.tryAdd(eventData)) {
                    producer.send(eventDataBatch);
                    eventDataBatch = producer.createBatch();

                    // Try to add that event that couldn't fit before.
                    if (!eventDataBatch.tryAdd(eventData)) {
                        throw new IllegalArgumentException("Event is too large for an empty batch. Max size: "
                                + eventDataBatch.getMaxSizeInBytes());
                    }
                }
            }

            // send the last batch of remaining events
            if (eventDataBatch.getCount() > 0) {
                producer.send(eventDataBatch);
            }
        } finally {
            // Dispose of the producer to close any underlying resources when we are finished with it.
            producer.close();
        }
    }

    private static List<EventData> createEventDataList(int numRecords) {
        List<EventData> eventList = new ArrayList<>();

        for (int i = 1; i <= numRecords; i++) {
            String jsonData = generateMockEventData(i); // Generar datos mockeados para cada evento
            eventList.add(createEventData(jsonData));
        }

        return eventList;
    }

    private static String generateMockEventData(int eventId) {
        Random random = new Random();

        int ventaId = eventId;
        String fechaVenta = "2024-02-18";
        String producto = products.get(random.nextInt(products.size())); // Selecciona un producto aleatorio de la lista
        double precio = 10.00 + random.nextDouble() * 10; // Genera un precio aleatorio entre 10 y 20
        int cantidad = random.nextInt(10) + 1; // Genera una cantidad aleatoria entre 1 y 10
        double montoVenta = precio * cantidad;
        int idMoneda = random.nextInt(2) + 1; // Genera un ID de moneda aleatorio entre 1 y 2
        int clienteId = clients.get(random.nextInt(clients.size())); // Selecciona un ID de cliente aleatorio de la lista

        String message = String.format(Locale.US, "{\"id_venta\": %d, \"fecha_venta\": \"%s\", \"id_cliente\": %d, \"producto\": \"%s\", \"precio\": %.2f, \"cantidad\": %d, \"monto_venta\": %.2f, \"id_moneda\": %d}",
        ventaId, fechaVenta, clienteId, producto, precio, cantidad, montoVenta, idMoneda);


        System.out.println(message);

        try {
            // Intenta crear un objeto JSONObject a partir de la cadena JSON
            JSONObject jsonObject = new JSONObject(message);

            // Si se logra crear el objeto JSONObject, significa que la cadena JSON es válida
            System.out.println("La cadena JSON es válida:");
            System.out.println(jsonObject.toString(4)); // Imprime el JSON formateado con indentación de 4 espacios
        } catch (JSONException e) {
            // Si ocurre una JSONException, significa que hay un problema en el formato del JSON
            System.out.println("Error en el formato del JSON:");
            System.out.println(e.getMessage()); // Imprime el mensaje de error
        }                

        return message;

    }    

    private static EventData createEventData(String jsonData) {
        return new EventData(jsonData.getBytes());
    }

    /*private static List<String> generateProducts(int numProducts) {
        List<String> productList = new ArrayList<>();
        for (int i = 1; i <= numProducts; i++) {
            productList.add("Producto" + i);
        }
        return productList;
    }*/

    // Genera una lista de nombres de productos con algunos nombres vacíos
    private static List<String> generateProducts(int numProducts, int numEmptyProducts) {
        List<String> productList = new ArrayList<>();
        
        // Agrega los productos vacíos al principio de la lista
        for (int i = 0; i < numEmptyProducts; i++) {
            productList.add(""); // Agrega un nombre de producto vacío
        }
        
        // Agrega los productos con nombres
        for (int i = 1; i <= numProducts - numEmptyProducts; i++) {
            productList.add("Producto" + i); // Agrega "Producto" seguido del número de producto
        }
        
        return productList; // Devuelve la lista de nombres de productos generada
    }    

    private static List<Integer> generateClientIds(int numClients) {
        List<Integer> clientIds = new ArrayList<>();
        for (int i = 1001; i <= 1000 + numClients; i++) {
            clientIds.add(i);
        }
        return clientIds;
    }    
}
