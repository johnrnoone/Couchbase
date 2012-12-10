package couchBasePkg;

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
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.couchbase.client.CouchbaseClient;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;

public class Main {
  public static final int EXP_TIME = 10;
  public static final String KEY = "spoon";
  public static final String VALUE = "Hello World!";

  public static void main(String args[]) {
    // Set the URIs and get a client
    List<URI> uris = new LinkedList<URI>();

    Boolean do_delete = false; // 

    // Connect to localhost or to the appropriate URI
    uris.add(URI.create("http://127.0.0.1:8091/pools"));

    CouchbaseClient client = null;
    try {
      client = new CouchbaseClient(uris, "default", "");
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " 
        + e.getMessage());
      System.exit(0);
    }
    // Do a synchrononous get
    Object getObject = client.get(KEY);
    // Do an asynchronous set
    OperationFuture<Boolean> setOp = client.set(KEY, EXP_TIME, VALUE);
    // Do an asynchronous get
    GetFuture getOp = client.asyncGet(KEY);
    // Do an asynchronous delete
    OperationFuture<Boolean> delOp = null;
    if (do_delete) {
      delOp = client.delete(KEY);
    }
    // Shutdown the client
    client.shutdown(3, TimeUnit.SECONDS);
    // Now we want to see what happened with our data
    // Check to see if our set succeeded
    try {
      if (setOp.get().booleanValue()) {
        System.out.println("Set Succeeded");
      } else {
        System.err.println("Set failed: "
            + setOp.getStatus().getMessage());
      }
    } catch (Exception e) {
      System.err.println("Exception while doing set: "
          + e.getMessage());
    }
    // Print the value from synchronous get
    if (getObject != null) {
      System.out.println("Synchronous Get Suceeded: "
          + (String) getObject);
    } else {
      System.err.println("Synchronous Get failed");
    }
    // Check to see if ayncGet succeeded
    try {
      if ((getObject = getOp.get()) != null) {
        System.out.println("Asynchronous Get Succeeded: "
            + getObject);
      } else {
        System.err.println("Asynchronous Get failed: "
            + getOp.getStatus().getMessage());
      }
    } catch (Exception e) {
      System.err.println("Exception while doing Aynchronous Get: "
          + e.getMessage());
    }
    // Check to see if our delete succeeded
    if (do_delete) {
      try {
        if (delOp.get().booleanValue()) {
          System.out.println("Delete Succeeded");
        } else {
          System.err.println("Delete failed: " + 
              delOp.getStatus().getMessage());
        }
      } catch (Exception e) {
        System.err.println("Exception while doing delete: "
            + e.getMessage());
      }
    }
  }
}

