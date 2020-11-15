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
package org.jclouds.h3.strategy.internal;

import gr.forth.ics.JH3lib.JH3;
import gr.forth.ics.JH3lib.JH3Exception;
import org.jclouds.blobstore.LocalStorageStrategy;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.domain.Location;

import java.io.IOException;
import java.util.Collection;


public class H3StorageStrategyImpl implements LocalStorageStrategy {

	private static String storageURI = null;
	private static JH3 H3client;

	static {
		storageURI = System.getenv("H3LIB_STORAGE_URI");
		if (storageURI == null) {
			storageURI = "file:///tmp/h3";
		}
	}


	public H3StorageStrategyImpl(String storageURI, int uid) throws JH3Exception {
		System.out.println("[H3] new H3StorageStrategyImpl");
		H3StorageStrategyImpl.H3client = new JH3(storageURI, uid);

	}

	@Override
	public boolean containerExists(String container) {
		System.out.println("[H3] containerExists " + container);
		return false;
	}

	@Override
	public Collection<String> getAllContainerNames() {
		System.out.println("[H3] getAllContainerNames");
		return null;
	}

	@Override
	public boolean createContainerInLocation(String container, Location location, CreateContainerOptions options) {
		System.out.println("[H3] createContainerInLocation");
		try {
			H3StorageStrategyImpl.H3client.createBucket(container);
		} catch (JH3Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ContainerAccess getContainerAccess(String container) {
		System.out.println("[H3] getContainerAccess");
		return null;
	}

	@Override
	public void setContainerAccess(String container, ContainerAccess access) {
		System.out.println("[H3] setContainerAccess");
	}

	@Override
	public void deleteContainer(String container) {
		System.out.println("[H3] deleteContainer");

	}

	@Override
	public void clearContainer(String container) {
		System.out.println("[H3] clearContainer");

	}

	@Override
	public void clearContainer(String container, ListContainerOptions options) {
		System.out.println("[H3] clearContainer2");

	}

	@Override
	public StorageMetadata getContainerMetadata(String container) {
		System.out.println("[H3] getContainerMetadata");
		return null;
	}

	@Override
	public boolean blobExists(String container, String key) {
		System.out.println("[H3] blobExists");

		return false;
	}

	@Override
	public Iterable<String> getBlobKeysInsideContainer(String container, String prefix) throws IOException {
		System.out.println("[H3] getBlobKeysInsideContainer");

		return null;
	}

	@Override
	public Blob getBlob(String containerName, String blobName) {
		System.out.println("[H3] getBlob");

		return null;
	}

	@Override
	public String putBlob(String containerName, Blob blob) throws IOException {
		System.out.println("[H3] putBlob");

		return null;
	}

	@Override
	public void removeBlob(String container, String key) {
		System.out.println("[H3] removeBlob");
	}

	@Override
	public BlobAccess getBlobAccess(String container, String key) {
		System.out.println("[H3] getBlobAccess");

		return null;
	}

	@Override
	public void setBlobAccess(String container, String key, BlobAccess access) {
		System.out.println("[H3] setBlobAccess");

	}

	@Override
	public Location getLocation(String containerName) {
		System.out.println("[H3] getLocation");

		return null;
	}

	@Override
	public String getSeparator() {
		System.out.println("[H3] getSeparator");


		return null;
	}
}
