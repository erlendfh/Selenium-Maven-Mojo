/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License") you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.codehaus.mojo.selenium

import org.codehaus.groovy.maven.mojo.GroovyMojo

/**
 * Abstract base class for creating Mojos that require a selenium server
 *
 * @since 1.1
 *
 * @version $Id$
 * @author <a href="mailto:erlend.halvorsen@bekk.no">Erlend Halvorsen</a>
 */
abstract class AbstractServerMojo extends GroovyMojo {
	/**
     * The file or resource to use for default user-extensions.js.
     *
     * @parameter default-value="org/codehaus/mojo/selenium/default-user-extensions.js"
     */
    String defaultUserExtensions

    /**
     * Enable or disable default user-extensions.js
     *
     * @parameter default-value="true"
     */
    boolean defaultUserExtensionsEnabled

    /**
     * Location of the user-extensions.js to load into the server.
     * If defaultUserExtensionsEnabled is true, then this file will be appended to the defaults.
     *
     * @parameter expression="${userExtensions}"
     */
    String userExtensions

    /**
     * Working directory where Selenium server will be started from.
     *
     * @parameter expression="${project.build.directory}/selenium"
     * @required
     */
    File workingDirectory
	
	/**
	 * Create the user-extensions.js file to use, or null if it should not be installed.
	 */
	protected File createUserExtensionsFile() {
	    if (!defaultUserExtensionsEnabled && userExtensions == null) {
	        return null
	    }

	    def resolveResource = { name ->
	        if (name == null) return null

	        def url
	        def file = new File(name)
	        if (file.exists()) {
	            url = file.toURI().toURL()
	        }
	        else {
	            try {
	                url = new URL(name)
	            }
	            catch (MalformedURLException e) {
	                url = Thread.currentThread().contextClassLoader.getResource(name)
	            }
	        }

	        if (!url) {
	            fail("Could not resolve resource: $name")
	        }

	        log.debug("Resolved resource '$name' as: $url")

	        return url
	    }

	    // File needs to be named 'user-extensions.js' or Selenium server will puke
	    def file = new File(workingDirectory, 'user-extensions.js')
	    if (file.exists()) {
	        log.debug("Reusing previously generated file: $file")
	        return file
	    }

	    def writer = file.newPrintWriter()

	    if (defaultUserExtensionsEnabled) {
	        def url = resolveResource(defaultUserExtensions)
	        log.debug("Using defaults: $url")

	        writer.println('//')
	        writer.println("// Default user extensions from: $url")
	        writer.println('//')
	        writer << url.openStream()
	    }

	    if (userExtensions) {
	        def url = resolveResource(userExtensions)
	        log.debug("Using user extensions: $url")

	        writer.println('//')
	        writer.println("// User extensions from: $url")
	        writer.println('//')
	        writer << url.openStream()
	    }

	    writer.flush()
	    writer.close()

	    return file
	}
}