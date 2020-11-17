/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.h3.H3;
import org.jclouds.h3.reference.H3Constants;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class H3viaJcloudsTest {
	@Test
	static void SimpleTest( )
	{
		System.out.println( "Hello World!" );
		H3.main(null);
		// setup where the provider must store the files
		Properties properties = new Properties();
		properties.setProperty(H3Constants.PROPERTY_BASEDIR, "/tmp/s3proxy");
		// setup the container name used by the provider (like bucket in S3)
		String containerName = "test-container";

		// get a context with filesystem that offers the portable BlobStore api
		BlobStoreContext context = ContextBuilder.newBuilder("h3").overrides(properties).build();

		// create a container in the default location
		BlobStore blobStore = context.getBlobStore();
		blobStore.createContainerInLocation(null, containerName);
//
//        // add blob
//        Blob blob = blobStore.blobBuilder("test").build();
//        blob.setPayload("test data");
//        blobStore.putBlob(containerName, blob);
//
//        // retrieve blob
//        Blob blobRetrieved = blobStore.getBlob(containerName, "test");
//
//        // delete blob
//        blobStore.removeBlob(containerName, "test");

		//close context
		context.close();
		Assert.assertNotNull(blobStore);
	}
}
