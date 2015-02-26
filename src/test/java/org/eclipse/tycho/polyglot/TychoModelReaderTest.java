/*******************************************************************************
 * Copyright (c) 2015 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.polyglot;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.PlexusTestCase;

public class TychoModelReaderTest extends PlexusTestCase {

	private ModelReader tychoModelReader;

	@Override
	protected void setUp() throws Exception {
		tychoModelReader = lookup(ModelReader.class, "tycho");
	}

	public void testReadBundle() throws Exception {
		File buildProperties = new File(getPolyglotTestDir(),
				"bundle1/build.properties");
		Model model = tychoModelReader.read((Reader) null,
				createReaderOptions(buildProperties));
		assertEquals("4.0.0", model.getModelVersion());
		assertEquals("pomless.bundle", model.getArtifactId());
		assertEquals("0.1.0-SNAPSHOT", model.getVersion());
		assertEquals("eclipse-plugin", model.getPackaging());
		assertParent(model.getParent());
	}

	public void testReadTestBundle() throws Exception {
		File buildProperties = new File(getPolyglotTestDir(),
				"bundle1.tests/build.properties");
		Model model = tychoModelReader.read((Reader) null,
				createReaderOptions(buildProperties));
		assertEquals("pomless.bundle.tests", model.getArtifactId());
		assertEquals("1.0.1", model.getVersion());
		assertEquals("eclipse-test-plugin", model.getPackaging());
		assertParent(model.getParent());
	}

	public void testReadFeature() throws Exception {
		File buildProperties = new File(getPolyglotTestDir(),
				"feature/build.properties");
		Model model = tychoModelReader.read((Reader) null,
				createReaderOptions(buildProperties));
		assertEquals("pomless.feature", model.getArtifactId());
		assertEquals("1.0.0-SNAPSHOT", model.getVersion());
		assertEquals("eclipse-feature", model.getPackaging());
		assertParent(model.getParent());
	}

	private void assertParent(Parent parent) {
		assertNotNull(parent);
		assertEquals("testParent.groupId", parent.getGroupId());
		assertEquals("testparent", parent.getArtifactId());
		assertEquals("0.0.1-SNAPSHOT", parent.getVersion());
	}

	private Map<String, String> createReaderOptions(File buildProperties) {
		Map<String, String> options = new HashMap<String, String>();
		options.put(ModelProcessor.SOURCE, buildProperties.getAbsolutePath());
		return options;
	}

	private File getPolyglotTestDir() {
		return new File(getBasedir(), "src/test/resources/testpomless/");
	}

}
