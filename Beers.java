package couchBasePkg;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.couchbase.client.CouchbaseClient;

import java.io.FileReader;

import com.google.gson.Gson;
import java.util.ArrayList;

public class Beers {

   public static void main(String[] args) throws Exception {

      // Read the Data

      Gson gson = new Gson();
      Beer[] Beers = gson.fromJson(new 
            FileReader("Beers.json"),
            Beer[].class);
      
      System.out.println("Read old beers from Beers.json ");
      for (Beer entry : Beers) {
         String JSONentry = gson.toJson(entry);
         System.out.println(JSONentry);
      }

      List<String> keys = new ArrayList<String>();

      try {

         URI local = new URI("http://localhost:8091/pools");
         List<URI> baseURIs = new ArrayList<URI>();
         baseURIs.add(local);     

         CouchbaseClient c = new CouchbaseClient(baseURIs, "beer-sample", "");
         
         System.out.println("Save old beers in beers-sample bucket, calling c.set() ");
         for (Beer entry : Beers) {
//            entry.name = String.format("%s_MAX", entry.name);
            String beerEntry = gson.toJson(entry);

            // Create a new key
            UUID idOne = UUID.randomUUID();
            String beerId = new StringBuffer(idOne.toString()).substring(24, 36);
            //c.add(beerId, 0, gson.toJson(beerEntry));
            c.set(beerId, 0, gson.toJson(beerEntry)).get();
            keys.add(beerId);
         }
         
         // Create a new beer
         String newBeer = new String (" {'name':" + "'Old Yankee Ale'," +       
               "'abv':5.00,'ibu':0,'srm':0,'upc':0,'type':'beer'," +
               "'brewery_id':110a45622a,'updated':'2012-08-30 20:00:20'," +
               "'description':'A medium-bodied Amber Ale'," +
               "'style': 'American-Style Amber'," +
               "'category': 'North American Ale'} ");
         System.out.println("Create newBeer = ");
         System.out.println(newBeer);
         
         // Create a new beer
         String noStyleBeer = new String (" {'name':" + "'Old Yankee Ale'," +       
               "'abv':5.00,'ibu':0,'srm':0,'upc':0,'type':'beer'," +
               "'brewery_id':110a45622a,'updated':'2012-08-30 20:00:20'," +
               "'description':'A medium-bodied Amber Ale'," +
//               "'style': 'American-Style Amber'," +
               "'category': 'North American Ale'} ");
         System.out.println("Create noStyleBeer = ");
         System.out.println(noStyleBeer);

         Beer newBeerEntry = gson.fromJson(newBeer, Beer.class);
         UUID idOne = UUID.randomUUID();
         String beerId = new StringBuffer(idOne.toString()).substring(24, 36);
         System.out.println("\nAdd the new beer using c.add()");
         c.add(beerId, 0, gson.toJson(newBeerEntry));
//         c.set(beerId, 0, gson.toJson(newBeerEntry)).get();
         keys.add(beerId);
         
         newBeerEntry = gson.fromJson(noStyleBeer, Beer.class);
         UUID idTwo = UUID.randomUUID();
         beerId = new StringBuffer(idTwo.toString()).substring(24, 36);
         System.out.println("\nAdd the new noStyleBeer using c.add()");
         c.add(beerId, 0, gson.toJson(newBeerEntry));
//         c.set(beerId, 0, gson.toJson(newBeerEntry)).get();
         keys.add(beerId);

         System.out.println("\nRead back all entries of beer-sample using c.get()");
         for (String key : keys) {
            System.out.println("Perform c.get("+key+")");
            System.out.println(key + " " + c.get(key));
         } 
         
      } catch (IllegalStateException ise) {
         System.err.println("IllegalStateException: " + ise.getMessage());
         ise.printStackTrace();   
      } catch (Exception e) {
         System.err.println("Error connecting to Couchbase: " + e.getMessage());
         e.printStackTrace(); 
      }
   }
}

