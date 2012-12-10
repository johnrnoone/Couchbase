package jsonDemo;

/**

 * Copyright (C) 2009-2012 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import java.net.URI;

import com.couchbase.client.CouchbaseClient;

import net.spy.memcached.CASValue;
import net.spy.memcached.transcoders.IntegerTranscoder;

/**
 * Sets up a number of threads each cooperating to generate a set of random
 * numbers and illustrates the time savings that can be achieved by using
 * Couchbase.
 */
public class GettingStarted {

    static final int numIntegers = 100;
    static String addresses;
    static CountDownLatch countdown;

    /**
     * @param args
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("usage: addresses numthreads");
            System.exit(1);
        }

        addresses = args[0];

        int numThreads = Integer.parseInt(args[1]);

        countdown = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
           System.out.println(
           "Calling new Thread(new ClientThread("+String.format("Client-%d", i)+")");
            Thread t = new Thread(new ClientThread(String.format(
                    "Client-%d", i)));
            t.setName("Client Thread " + i);
            t.start();
        }

        try {
            countdown.await();
        } catch (InterruptedException e) {
        }

        System.exit(0);
    }

    private static class ClientThread implements Runnable {

        private String name;

        public ClientThread(String name) {
            this.name = name;
        }

        @Override
        public void run() {

            try {
                URI server = new URI(addresses);

                ArrayList<URI> serverList = new ArrayList<URI>();

                serverList.add(server);

                CouchbaseClient client = new CouchbaseClient(
                        serverList, "default", "");
                IntegerTranscoder intTranscoder = new IntegerTranscoder();

                // Not really random, all threads
                // will have the same seed and sequence of
                // numbers.
                Random rand = new Random(1);

                long startTime = System.currentTimeMillis();

                int created = 0;
                int cached = 0;

                for (int i = 0; i < numIntegers; i++) {
                    String key = String.format("Value-%d", i);

                    CASValue<Integer> value = client.gets(key,
                            intTranscoder);

                    if (value == null) {
                        // The value doesn't exist in Membase
                        client.set(key, 15, rand.nextInt(), intTranscoder);

                        // Simulate the value taking time to create.
                        Thread.sleep(100);

                        created++;

                    } else {

                        // The value does exist, another thread
                        // created it already so this thread doesn't
                        // have to.
                        int v = value.getValue();

                        // Check that the value is what we
                        // expect it to be.
                        if (v != rand.nextInt()) {
                            System.err.println("No match.");
                        }
                        cached++;
                    }

                    client.waitForQueues(1, TimeUnit.MINUTES);
                }
                
                System.out.println(String.format(        // added by jrn
                      "%s took %.4f ms per key. Created %d. Retrieved %d from cache.", 
                      name,
                      (System.currentTimeMillis() - startTime)/ (double)numIntegers, 
                      created, cached)
                );

/*                System.err.println(String.format(
                        "%s took %.4f ms per key. Created %d."
                                + " Retrieved %d from cache.", name,
                        (System.currentTimeMillis() - startTime)
                                / (double)numIntegers, created, cached));                                
                                 */

            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            countdown.countDown();
        }
    }
}
