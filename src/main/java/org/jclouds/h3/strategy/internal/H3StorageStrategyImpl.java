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
import gr.forth.ics.JH3lib.JH3;
import gr.forth.ics.JH3lib.JH3Exception;
import gr.forth.ics.JH3lib.JH3Object;
import org.jclouds.blobstore.LocalStorageStrategy;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.domain.Location;
import org.jclouds.h3.predicates.validators.H3BlobKeyValidator;
import org.jclouds.h3.predicates.validators.H3ContainerNameValidator;
import org.jclouds.h3.reference.H3Constants;
import org.jclouds.h3.util.internal.Utils;
import org.jclouds.logging.Logger;
//import org.jclouds.h3.reference.H3Constants;

import javax.annotation.Resource;
import javax.inject.Inject;

//import javax.inject.Named;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;


import static com.google.common.base.Preconditions.checkNotNull;


public class H3StorageStrategyImpl implements LocalStorageStrategy {

	private static String storageURI = null;
	private static JH3 H3client;
	private static final boolean debug = true;
	@Resource
	protected Logger logger = Logger.NULL;

	protected final Provider<BlobBuilder> blobBuilders;
	protected final String baseDirectory;
	protected final H3ContainerNameValidator H3ContainerNameValidator;
	protected final H3BlobKeyValidator H3BlobKeyValidator;
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
		System.out.println("[Jclouds-H3] new H3StorageStrategyImpl");
		try {
			H3StorageStrategyImpl.H3client = new JH3(this.baseDirectory, 0);
		} catch (JH3Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean containerExists(String container) {
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
			buckets.trimToSize();
			if (debug) {
				for (String bucket : buckets) {
					System.out.println(bucket);
				}
			}
		} catch (JH3Exception e) {
			e.printStackTrace();
		}

		return buckets;
	}

	@Override
	public boolean createContainerInLocation(String container, Location location, CreateContainerOptions options) {
		System.out.println("[Jclouds-H3] createContainerInLocation");
		try {
			H3StorageStrategyImpl.H3client.createBucket(container);
		} catch (JH3Exception e) {
			e.printStackTrace();
		}
//		if (debug) getAllContainerNames();
		return this.containerExists(container);
	}

	@Override
	public ContainerAccess getContainerAccess(String container) {
		System.out.println("[Jclouds-H3] getContainerAccess");
		return null;
	}

	@Override
	public void setContainerAccess(String container, ContainerAccess access) {
		System.out.println("[Jclouds-H3] setContainerAccess");
	}

	@Override
	public void deleteContainer(String container) {
		System.out.println("[Jclouds-H3] deleteContainer");
		try {
			H3StorageStrategyImpl.H3client.deleteBucket(container);
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

	}

	@Override
	public StorageMetadata getContainerMetadata(String container) {
		System.out.println("[Jclouds-H3] getContainerMetadata");
		try {
			H3StorageStrategyImpl.H3client.infoBucket(container);
			System.out.println("[Jclouds-H3] not yet finished implementing");
			System.exit(-1);
		} catch (JH3Exception e) {
			e.printStackTrace();
		}
		return null;
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

		return null;
	}

	@Override
	public Blob getBlob(String containerName, String blobName) {
		System.out.println("[Jclouds-H3] getBlob");
		try {
			JH3Object jh3Object = H3StorageStrategyImpl.H3client.readObject(containerName, blobName);
			if (jh3Object != null) {
				System.out.println("[Jclouds-H3] not yet implemented / dont know how to convert JH3Object to Blob");
//				return jh3Object;
			}


		} catch (JH3Exception e) {
		}
		System.err.println("[Jclouds-H3] Bucket or object doesn't exist!");
		return null;
	}

	@Override
	public String putBlob(String containerName, Blob blob){
		System.out.println("[Jclouds-H3] putBlob");
		String blobKey = blob.getMetadata().getName();
		if (!this.containerExists(containerName)) {
			System.err.println("containerName doesnt exist");
			System.exit(1);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		byte[] data_bytes = new byte[0];
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(blob);
			out.flush();
			data_bytes = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
		JH3Object object = new JH3Object(data_bytes, data_bytes.length);
		try {
			if (H3StorageStrategyImpl.H3client.createObject(containerName, blob.getMetadata().getName(), object)) {
				logger.debug("Put object with key [%s] to container [%s] successfully", blobKey, containerName);
				return blobKey;
			} else {
				System.err.println("[Jclouds-H3] Error creating Object!");
			}
		} catch (JH3Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void removeBlob(String container, String key) {
		System.out.println("[Jclouds-H3] removeBlob");
	}

	@Override
	public BlobAccess getBlobAccess(String container, String key) {
		System.out.println("[Jclouds-H3] getBlobAccess");

		return null;
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

		return null;
	}

	@Override
	public String getSeparator() {
		System.out.println("[Jclouds-H3] getSeparator");


		return null;
	}

	/**
	 * Check if the file system resource whose name is obtained applying buildPath on the input path
	 * tokens is a directory, otherwise a RuntimeException is thrown
	 *
	 * @param tokens
	 *           the tokens that make up the name of the resource on the file system
	 */
	private boolean buildPathAndChecksIfDirectoryExists(String... tokens) {
		String path = buildPathStartingFromBaseDir(tokens);
		File file = new File(path);
		boolean exists = file.exists() || file.isDirectory();
		return exists;
	}

	/**
	 * Facility method used to concatenate path tokens normalizing separators
	 *
	 * @param pathTokens
	 *           all the string in the proper order that must be concatenated in order to obtain the
	 *           filename
	 * @return the resulting string
	 */
	protected String buildPathStartingFromBaseDir(String... pathTokens) {
		String normalizedToken = removeFileSeparatorFromBorders(normalize(baseDirectory), true);
		StringBuilder completePath = new StringBuilder(normalizedToken);
		if (pathTokens != null && pathTokens.length > 0) {
			for (int i = 0; i < pathTokens.length; i++) {
				if (pathTokens[i] != null) {
					normalizedToken = removeFileSeparatorFromBorders(normalize(pathTokens[i]), false);
					completePath.append(File.separator).append(normalizedToken);
				}
			}
		}
		return completePath.toString();
	}
	/**
	 * Remove leading and trailing separator character from the string.
	 *
	 * @param pathToBeCleaned
	 * @param onlyTrailing
	 *           only trailing separator char from path
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
		public boolean directoryExists(String container, String directory) {
			return buildPathAndChecksIfDirectoryExists(container, directory);
		}

	/**
	 * Convert path to the current OS filesystem standard
	 *
	 * @param path
	 * @return
	 */
	private static String normalize(String path) {
		if (null != path) {
			if (Utils.isWindows()) {
				path = path.replace("\\", File.separator);
			}
			return path.replace("/", File.separator);
		}
		return path;
	}

	/**
	 * Convert path to jclouds standard (/)
	 */
	private static String denormalize(String path) {
		if (null != path && Utils.isWindows() ) {
			return path.replace("\\", "/");
		}
		return path;
	}

}
