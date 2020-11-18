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
package org.jclouds.h3.util;

//import static java.nio.file.FileSystems.getDefault;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Utilities for the filesystem blobstore.
 */
public class Utils {
	/**
	 * Private constructor for utility class.
	 */
	private Utils() {
		// Do nothing
	}

	/**
	 * Determine if Java is running on a Mac OS
	 */
	public static boolean isMacOSX() {
		String osName = System.getProperty("os.name");
		return osName.contains("OS X");
	}

	/**
	 * Determine if Java is running on a windows OS
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase().contains("windows");
	}

	/**
	 * @return Localized name for the "Everyone" Windows principal.
	 */
	public static final String getWindowsEveryonePrincipalName() {
		if (isWindows()) {
			try {
				Process process = new ProcessBuilder("whoami", "/groups").start();
				try {
					String line;
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
						while ((line = reader.readLine()) != null) {
							if (line.indexOf("S-1-1-0") != -1) {
								return line.split("\\s{2,}")[0];
							}
						}
					}
				} finally {
					process.destroy();
				}
			} catch (IOException e) {
			}
		}
		// Default/fallback value
		return "Everyone";
	}
}
