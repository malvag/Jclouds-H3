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


import com.google.common.base.Supplier;
import com.google.common.io.ByteStreams;
import gr.forth.ics.JH3lib.JH3;
import gr.forth.ics.JH3lib.JH3BucketInfo;
import gr.forth.ics.JH3lib.JH3Exception;
import gr.forth.ics.JH3lib.JH3Object;
import gr.forth.ics.JH3lib.JH3ObjectInfo;
import gr.forth.ics.JH3lib.JH3Status;
import org.jclouds.blobstore.LocalStorageStrategy;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.MutableStorageMetadata;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.Tier;
import org.jclouds.blobstore.domain.internal.MutableStorageMetadataImpl;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.domain.Location;
import org.jclouds.h3.predicates.validators.H3BlobKeyValidator;
import org.jclouds.h3.predicates.validators.H3ContainerNameValidator;
import org.jclouds.h3.reference.H3Constants;
import org.jclouds.h3.util.Utils;
import org.jclouds.io.Payload;
import org.jclouds.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;

import javax.inject.Named;
import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
//import java.util.UUID;


import static com.google.common.base.Preconditions.checkNotNull;


public class H3StorageStrategyImpl implements LocalStorageStrategy {

	private static String storageURI = null;
	private static JH3 H3client;
	private static final boolean debug = true;
	@Resource
	protected Logger logger = Logger.NULL;

	protected Provider<BlobBuilder> blobBuilders;
	protected String baseDirectory;
	protected H3ContainerNameValidator H3ContainerNameValidator;
	protected H3BlobKeyValidator H3BlobKeyValidator;
	private final Supplier<Location> defaultLocation;

	static {
		storageURI = System.getenv("H3LIB_STORAGE_URI");
		if (storageURI == null) {
			storageURI = "file:///tmp/h3";
		}
	}

	@Inject
	protected H3StorageStrategyImpl(Provider<BlobBuilder> blobBuilders,
									@Named(H3Constants.PROPERTY_BASEDIR) String baseDir,
									H3ContainerNameValidator h3ContainerNameValidator,
									H3BlobKeyValidator h3BlobKeyValidator,
									Supplier<Location> defaultLocation) {
		this.blobBuilders = checkNotNull(blobBuilders, "h3 storage strategy blobBuilders");
		this.baseDirectory = checkNotNull(baseDir, "h3 storage strategy base directory");
		this.H3ContainerNameValidator = checkNotNull(h3ContainerNameValidator,
				"h3 container name validator");

		this.H3BlobKeyValidator = checkNotNull(h3BlobKeyValidator, "h3 blob key validator");

		this.defaultLocation = defaultLocation;
		System.out.println("[Jclouds-H3] new H3StorageStrategyImpl with " + baseDir);

		try {
			H3StorageStrategyImpl.H3client = new JH3(this.baseDirectory, 0);
		} catch (JH3Exception e) {
			System.out.println(H3StorageStrategyImpl.H3client.getStatus());
			e.printStackTrace();
		}
	}

	@Override
	public boolean containerExists(String container) {
		if (debug)
			System.out.println("[Jclouds-H3] containerExists " + container);
		if (debug && container.equals("_all_")) {
			getAllContainerNames();
			return true;
		}
		boolean exists = true;
		try {
			if (H3StorageStrategyImpl.H3client.infoBucket(container) == null)
				exists = false;
		} catch (JH3Exception e) {
			System.err.println("[Jclouds-H3] Bucket '" + container + "' doesn't exist!");
			exists = false;
		}
		return exists;
	}

	@Override
	public Collection<String> getAllContainerNames() {
		System.out.println("[Jclouds-H3] getAllContainerNames");
		ArrayList<String> buckets = null;
		try {
			buckets = H3StorageStrategyImpl.H3client.listBuckets();
			if (buckets == null)
				return null;
			buckets.trimToSize();
			if (debug) {
				for (String bucket : buckets) {
					System.out.println(bucket);
				}
			}
		} catch (JH3Exception e) {
			System.out.println(H3StorageStrategyImpl.H3client.getStatus());
			e.printStackTrace();
		}

		return buckets;
	}

	@Override
	public boolean createContainerInLocation(String container, Location location, CreateContainerOptions options) {
		System.out.println("[Jclouds-H3] createContainerInLocation");
		try {
			if (H3StorageStrategyImpl.H3client.createBucket(container)) {
				return true;
			} else {
				System.err.println("[Jclouds-H3] Error creating Bucket " + container + " " + H3StorageStrategyImpl.H3client.getStatus());
			}
		} catch (JH3Exception e) {
			System.out.println(H3StorageStrategyImpl.H3client.getStatus());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ContainerAccess getContainerAccess(String container) {
		System.out.println("[Jclouds-H3] getContainerAccess");
		return ContainerAccess.PUBLIC_READ;
	}

	@Override
	public void setContainerAccess(String container, ContainerAccess access) {
		System.out.println("[Jclouds-H3] setContainerAccess");
	}

	@Override
	public void deleteContainer(String container) {
		System.out.println("[Jclouds-H3] deleteContainer");
		try {
			for (String listObject : H3StorageStrategyImpl.H3client.listObjects(container, 0)) {
				H3StorageStrategyImpl.H3client.deleteObject(container, listObject);
			}
			if (!H3StorageStrategyImpl.H3client.deleteBucket(container))
				System.err.println("[Jclouds-H3] Error deleting Bucket " + container + " " + H3StorageStrategyImpl.H3client.getStatus());

		} catch (JH3Exception e) {
			System.err.println("[Jclouds-H3] Bucket '" + container + "' doesn't exist!");
		}
	}

	@Override
	public void clearContainer(String container) {
		System.out.println("[Jclouds-H3] clearContainer");
		try {
			H3StorageStrategyImpl.H3client.deleteBucket(container);
			H3StorageStrategyImpl.H3client.createBucket(container);
		} catch (JH3Exception e) {
			System.err.println("[Jclouds-H3] Bucket '" + container + "' doesn't exist!");
		}

	}

	@Override
	public void clearContainer(String container, ListContainerOptions options) {
		System.out.println("[Jclouds-H3] clearContainer2");
		this.clearContainer(container);
	}

	@Override
	public StorageMetadata getContainerMetadata(String container) {
		System.out.println("[Jclouds-H3] getContainerMetadata");
		MutableStorageMetadata metadata = new MutableStorageMetadataImpl();
		try {
			JH3BucketInfo info = H3StorageStrategyImpl.H3client.infoBucket(container);
			if (info == null) {
				throw new IllegalStateException();
			}
			metadata.setName(container);
			metadata.setType(StorageType.CONTAINER);
			metadata.setLocation(getLocation(container));
			if (info.getStats() != null) {
				metadata.setLastModified(new Date(info.getStats().getLastModification().getSeconds() * 1000L));
				metadata.setSize(info.getStats().getSize());
			}
			metadata.setCreationDate(this.getJH3Date(info.getCreation().getSeconds()));
			System.out.println("[Jclouds-H3] not yet finished implementing " + new Date(info.getCreation().getSeconds()));
		} catch (JH3Exception | IllegalStateException e) {
			e.printStackTrace();
		}
		return metadata;
	}

	@Override
	public boolean blobExists(String container, String key) {
		System.out.println("[Jclouds-H3] blobExists");
		boolean exists = true;
		try {
			if (H3StorageStrategyImpl.H3client.infoObject(container, key) == null)
				exists = false;
		} catch (JH3Exception e) {
			System.err.println("[Jclouds-H3] Bucket or object doesn't exist!");
			exists = false;
		}
		return exists;
	}

	@Override
	public Iterable<String> getBlobKeysInsideContainer(String container, String prefix) throws IOException {
		System.out.println("[Jclouds-H3] getBlobKeysInsideContainer");
		try {
			ArrayList<String> arrayList = H3StorageStrategyImpl.H3client.listObjects(container, 0);
			if (arrayList != null) {
				for (String s : arrayList) {
					System.out.println(s);
				}
				return arrayList;
			}
		} catch (JH3Exception e) {
			e.printStackTrace();
		}
		System.out.println(H3StorageStrategyImpl.H3client.getStatus());
		return null;
	}

	@Override
	public Blob getBlob(String containerName, String blobName) {
		System.out.println("[Jclouds-H3] getBlob");
		BlobBuilder builder = blobBuilders.get();
		builder.name(blobName);
		Tier tier = Tier.STANDARD;
		try {
			JH3ObjectInfo objectInfo = H3client.infoObject(containerName, blobName);
			System.out.println(objectInfo);
			JH3Object jh3Object = H3StorageStrategyImpl.H3client.readObject(containerName, blobName);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			if (jh3Object != null) {
				long size_offset = jh3Object.getData().length;
				long remaining_size = objectInfo.getSize();
				int counter = 0;
				/**
				 * Get every chunk of data as long as we get JH3_CONTINUE as Status
				 */
				while (H3StorageStrategyImpl.H3client.getStatus() == JH3Status.JH3_CONTINUE) {
					JH3Object tmp_obj = null;
					if (size_offset <= remaining_size)
						tmp_obj = H3StorageStrategyImpl.H3client.readObject(containerName, blobName, size_offset * counter, size_offset);
					else
						tmp_obj = H3StorageStrategyImpl.H3client.readObject(containerName, blobName, size_offset * counter, remaining_size);
					remaining_size -= size_offset;
					counter += 1;
					outputStream.write(tmp_obj.getData());
					jh3Object.setData(outputStream.toByteArray());
				}

				//where do i write bytes in simple downloads

				jh3Object.setSize(objectInfo.getSize());

				System.out.println(H3StorageStrategyImpl.H3client.getStatus());
//				System.out.println(jh3Object.getSize());
				builder.payload(jh3Object.getData())
						.contentLength(jh3Object.getSize())
						.tier(tier);
				Blob blob = builder.build();
				blob.getMetadata().setContainer(containerName);
				blob.getMetadata().setCreationDate(this.getJH3Date(objectInfo.getCreation().getSeconds()));
				blob.getMetadata().setLastModified(this.getJH3Date(objectInfo.getLastModification().getSeconds()));
				blob.getMetadata().setSize(jh3Object.getSize());
				return blob;
			}


		} catch (JH3Exception | IOException e) {
			System.out.println(H3StorageStrategyImpl.H3client.getStatus());
			e.printStackTrace();
		}
		System.err.println("[Jclouds-H3] Bucket or object doesn't exist!");
		return null;
	}

	@Override
	public String putBlob(String containerName, Blob blob) throws IOException {
		System.out.println("[Jclouds-H3] putBlob" + blob);
		String blobKey = blob.getMetadata().getName();
		Payload payload = blob.getPayload();
		H3ContainerNameValidator.validate(containerName);
		H3BlobKeyValidator.validate(blobKey);
		InputStream inputStream = null;
		byte[] data_bytes = null;
		try {
			if (!this.containerExists(containerName)) {
				System.err.println("containerName doesnt exist");
				System.exit(1);
			}

			inputStream = payload.openStream();
			data_bytes = ByteStreams.toByteArray(inputStream);
			long expectedSize = blob.getMetadata().getContentMetadata().getContentLength() == null ?
					0 : blob.getMetadata().getContentMetadata().getContentLength();

			JH3Object object = new JH3Object(data_bytes, expectedSize > 0 ? expectedSize : 1);

			try {
				if (H3StorageStrategyImpl.H3client.writeObject(containerName, blob.getMetadata().getName(), object)) {
					logger.debug("Put object with key [%s] to container [%s] successfully", blobKey, containerName);
					/**
					 *  TBD: E-Tag to be implemented from H3 ?
					 */
					return blobKey;
				} else {

					System.err.println("[Jclouds-H3] Error creating Object! " + H3StorageStrategyImpl.H3client.getStatus());
				}
			} catch (JH3Exception e) {
				e.printStackTrace();
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ignored) {
				}
			}
			if (payload != null) {
				payload.release();
			}
		}

		return null;
	}

	@Override
	public void removeBlob(String container, String key) {
		System.out.println("[Jclouds-H3] removeBlob");
		try {
			if (!H3client.deleteObject(container, key)) {
				System.err.println("[Jclouds-H3] Error deleting Object! " + H3StorageStrategyImpl.H3client.getStatus());
			}
		} catch (JH3Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public BlobAccess getBlobAccess(String container, String key) {
		System.out.println("[Jclouds-H3] getBlobAccess");
		return BlobAccess.PUBLIC_READ;
	}

	@Override
	public void setBlobAccess(String container, String key, BlobAccess access) {
		System.out.println("[Jclouds-H3] setBlobAccess");
		try {
			H3client.setObjectOwner(container, key, 0, 0);
		} catch (JH3Exception e) {
			e.printStackTrace();
		}
//		H3client.setObjectPermissions(container,key,); // need to dive into source code


	}

	@Override
	public Location getLocation(String containerName) {
		System.out.println("[Jclouds-H3] getLocation");
		return defaultLocation.get();
	}

	@Override
	public String getSeparator() {
		System.out.println("[Jclouds-H3] getSeparator");


		return "/";
	}


	/**
	 * Remove leading and trailing separator character from the string.
	 *
	 * @param pathToBeCleaned
	 * @param onlyTrailing    only trailing separator char from path
	 * @return
	 */
	private String removeFileSeparatorFromBorders(String pathToBeCleaned, boolean onlyTrailing) {
		if (null == pathToBeCleaned || pathToBeCleaned.equals(""))
			return pathToBeCleaned;

		int beginIndex = 0;
		int endIndex = pathToBeCleaned.length();

		// search for separator chars
		if (!onlyTrailing) {
			if (pathToBeCleaned.charAt(0) == '/' || (pathToBeCleaned.charAt(0) == '\\' && Utils.isWindows()))
				beginIndex = 1;
		}
		if (pathToBeCleaned.charAt(pathToBeCleaned.length() - 1) == '/' ||
				(pathToBeCleaned.charAt(pathToBeCleaned.length() - 1) == '\\' && Utils.isWindows()))
			endIndex--;

		return pathToBeCleaned.substring(beginIndex, endIndex);
	}


	private Date getJH3Date(long epoch_seconds) {
		return new Date(epoch_seconds * 1000L);
	}

	public static JH3 getH3client() {
		return H3client;
	}
}
