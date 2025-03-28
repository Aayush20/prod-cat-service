//package org.example.prodcatservice.utils;
//
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//public class MultiPostRequest {
//
//    public static void main(String[] args) {
//        String endpoint = "http://localhost:8080/products/"; // Replace with your API endpoint
//
//        // List of product JSON strings
//        List<String> products = List.of(
//                "{ \"title\": \"Glow-in-the-Dark Umbrella\", \"description\": \"Stay dry and glow during rainy nights\", \"price\": 1200, \"categoryName\": \"Outdoor Gear\", \"stock\": 75, \"seller\": \"BrightShades\", \"imageUrl\": \"http://example.com/glow-umbrella.jpg\" }",
//                "{ \"title\": \"Portable Campfire\", \"description\": \"Bring the warmth of a campfire anywhere\", \"price\": 3000, \"categoryName\": \"Camping Equipment\", \"stock\": 50, \"seller\": \"CampBuddy\", \"imageUrl\": \"http://example.com/portable-campfire.jpg\" }",
//                "{ \"title\": \"Wireless Toaster\", \"description\": \"Charge it and toast anywhere you like\", \"price\": 5000, \"categoryName\": \"Kitchen Appliances\", \"stock\": 20, \"seller\": \"GadgetChefs\", \"imageUrl\": \"http://example.com/wireless-toaster.jpg\" }",
//                "{ \"title\": \"Emoji Stress Ball Pack\", \"description\": \"Squeeze away your stress with these fun emoji stress balls\", \"price\": 800, \"categoryName\": \"Toys & Games\", \"stock\": 200, \"seller\": \"StressBusters\", \"imageUrl\": \"http://example.com/emoji-stress-ball.jpg\" }",
//                "{ \"title\": \"Magnetic Plant Pots\", \"description\": \"Decorate your fridge with mini magnetic plants\", \"price\": 1500, \"categoryName\": \"Home Decor\", \"stock\": 100, \"seller\": \"GreenMagnetics\", \"imageUrl\": \"http://example.com/magnetic-plant-pot.jpg\" }",
//                "{ \"title\": \"Inflatable Couch Hammock\", \"description\": \"Relax anywhere with this easy-to-carry couch hammock\", \"price\": 2500, \"categoryName\": \"Furniture\", \"stock\": 60, \"seller\": \"RelaxVibes\", \"imageUrl\": \"http://example.com/inflatable-couch.jpg\" }",
//                "{ \"title\": \"Self-Stirring Mug\", \"description\": \"Perfect for mixing your coffee or tea effortlessly\", \"price\": 900, \"categoryName\": \"Kitchen Accessories\", \"stock\": 150, \"seller\": \"AutoMix\", \"imageUrl\": \"http://example.com/self-stirring-mug.jpg\" }",
//                "{ \"title\": \"Smart Notebook\", \"description\": \"Erase and reuse this high-tech notebook endlessly\", \"price\": 2500, \"categoryName\": \"Stationery\", \"stock\": 70, \"seller\": \"NoteFuture\", \"imageUrl\": \"http://example.com/smart-notebook.jpg\" }",
//                "{ \"title\": \"Space-Themed LED Nightlight\", \"description\": \"Transform your room into a starry galaxy\", \"price\": 2000, \"categoryName\": \"Lighting\", \"stock\": 120, \"seller\": \"StarryNights\", \"imageUrl\": \"http://example.com/galaxy-nightlight.jpg\" }",
//                "{ \"title\": \"Wearable Blanket Hoodie\", \"description\": \"Cozy blanket you can wear like a hoodie\", \"price\": 3500, \"categoryName\": \"Clothing\", \"stock\": 90, \"seller\": \"SnuggleWear\", \"imageUrl\": \"http://example.com/blanket-hoodie.jpg\" }",
//                "{ \"title\": \"Underwater Drone\", \"description\": \"Explore the underwater world with this high-tech drone\", \"price\": 150000, \"categoryName\": \"Drones & Tech\", \"stock\": 10, \"seller\": \"AquaExplorer\", \"imageUrl\": \"http://example.com/underwater-drone.jpg\" }",
//                "{ \"title\": \"Edible Coffee Cup\", \"description\": \"Sip your coffee and eat the cup afterward\", \"price\": 300, \"categoryName\": \"Kitchen Accessories\", \"stock\": 500, \"seller\": \"EcoSip\", \"imageUrl\": \"http://example.com/edible-coffee-cup.jpg\" }"
//        );
//
//
//
//        products.forEach(productJson -> {
//            try {
//                // Create URL object
//                URL url = new URL(endpoint);
//
//                // Open connection
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json; utf-8");
//                connection.setDoOutput(true);
//
//                // Write request body
//                try (OutputStream os = connection.getOutputStream()) {
//                    byte[] input = productJson.getBytes(StandardCharsets.UTF_8);
//                    os.write(input, 0, input.length);
//                }
//
//                // Get response
//                int responseCode = connection.getResponseCode();
//                System.out.println("Response Code: " + responseCode);
//
//                connection.disconnect();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
//}
//
