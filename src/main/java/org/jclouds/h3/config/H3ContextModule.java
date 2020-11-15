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
package org.jclouds.h3.config;

import com.google.inject.AbstractModule;
import org.jclouds.blobstore.BlobRequestSigner;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.LocalBlobRequestSigner;
//	import org.jclouds.blobstore.LocalStorageStrategy;
//	import org.jclouds.blobstore.attr.ConsistencyModel;
import org.jclouds.blobstore.LocalStorageStrategy;
import org.jclouds.blobstore.attr.ConsistencyModel;
import org.jclouds.blobstore.config.BlobStoreObjectModule;
import org.jclouds.blobstore.config.LocalBlobStore;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.h3.predicates.validators.H3BlobKeyValidator;
import org.jclouds.h3.predicates.validators.H3ContainerNameValidator;
import org.jclouds.h3.predicates.validators.internal.H3BlobKeyValidatorImpl;
import org.jclouds.h3.predicates.validators.internal.H3ContainerNameValidatorImpl;
import org.jclouds.h3.strategy.internal.H3StorageStrategyImpl;
import org.jclouds.h3.util.internal.H3BlobUtilsImpl;

import static org.jclouds.h3.util.internal.Utils.isWindows;
//	import org.jclouds.blobstore.util.BlobUtils;
//	import com.google.inject.AbstractModule;

public class H3ContextModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(BlobStore.class).to(LocalBlobStore.class);
		install(new BlobStoreObjectModule());
			if (isWindows()) {
				bind(ConsistencyModel.class).toInstance(ConsistencyModel.EVENTUAL);
			} else {
				bind(ConsistencyModel.class).toInstance(ConsistencyModel.STRICT);
			}
			bind(LocalStorageStrategy.class).to(H3StorageStrategyImpl.class);
			bind(BlobUtils.class).to(H3BlobUtilsImpl.class);
			bind(H3BlobKeyValidator.class).to(H3BlobKeyValidatorImpl.class);
			bind(H3ContainerNameValidator.class).to(H3ContainerNameValidatorImpl.class);
		bind(BlobRequestSigner.class).to(LocalBlobRequestSigner.class);
	}


}
