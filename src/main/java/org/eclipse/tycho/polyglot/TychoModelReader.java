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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.ModelParseException;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.sonatype.maven.polyglot.PolyglotModelUtil;
import org.sonatype.maven.polyglot.io.ModelReaderSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Tycho model reader. Deduces maven model artifactId and version from OSGi
 * manifest Bundle-SymbolicName and Bundle-Version headers or feature.xml id and version attributes. Assumes parent pom
 * is located in parent directory.
 * 
 * TODO add support for category.xml
 */
@Component(role = ModelReader.class, hint = "tycho")
public class TychoModelReader extends ModelReaderSupport {

	@Requirement(hint = "default")
	ModelReader defaultModelReader;

	public TychoModelReader() {
	}

	public Model read(Reader input, Map<String, ?> options) throws IOException,
			ModelParseException {
		File projectRoot = new File(PolyglotModelUtil.getLocation(options))
				.getParentFile();
		File manifestFile = new File(projectRoot, "META-INF/MANIFEST.MF");
		File featureXml = new File(projectRoot, "feature.xml");
		if (manifestFile.isFile()) {
			return createModelFromManifest(manifestFile);
		} else if (featureXml.isFile()) {
			return createModelFromFeatureXml(featureXml);
		} else {
			throw new IOException(
					"Neither META-INF/MANIFEST.MF nor feature.xml found in "
							+ projectRoot);
		}
	}

	private Model createModelFromFeatureXml(File featureXml) throws IOException {
		DocumentBuilder parser;
		Document doc;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = parser.parse(featureXml);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
		Element root = doc.getDocumentElement();
		Model model = createModel();
		model.setPackaging("eclipse-feature");
		model.setArtifactId(root.getAttribute("id"));
		model.setVersion(getPomVersion(root.getAttribute("version")));
		model.setParent(findParent(featureXml.getParentFile()));
		return model;
	}

	private Model createModelFromManifest(File manifestFile)
			throws IOException, ModelParseException {
		Manifest osgiManifest = readManifest(manifestFile);
		Attributes headers = osgiManifest.getMainAttributes();
		String bundleSymbolicName = getBundleSymbolicName(headers, manifestFile);
		Model model = createModel();
		model.setParent(findParent(manifestFile.getParentFile().getParentFile()));
		// groupId is inherited from parent pom
		model.setArtifactId(bundleSymbolicName);
		model.setVersion(getPomVersion(headers
				.getValue(Constants.BUNDLE_VERSION)));
		model.setPackaging(getPackagingType(bundleSymbolicName));
		return model;
	}

	private Model createModel() {
		Model model = new Model();
		model.setModelVersion("4.0.0");
		return model;
	}

	private String getBundleSymbolicName(Attributes headers, File manifestFile)
			throws ModelParseException {
		String symbolicName;
		try {
			String rawValue = headers.getValue(Constants.BUNDLE_SYMBOLICNAME);
			if (rawValue == null) {
				throw new ModelParseException("Required header "
						+ Constants.BUNDLE_SYMBOLICNAME + " missing in "
						+ manifestFile, -1, -1);
			}
			// strip off any directives/attributes
			symbolicName = ManifestElement.parseHeader(
					Constants.BUNDLE_SYMBOLICNAME, rawValue)[0].getValue();
		} catch (BundleException e) {
			throw new ModelParseException(e.getMessage(), -1, -1);
		}
		return symbolicName;
	}

	private Manifest readManifest(File manifestFile) throws IOException {
		Manifest osgiManifest = new Manifest();
		FileInputStream stream = new FileInputStream(manifestFile);
		try {
			osgiManifest.read(stream);
		} finally {
			stream.close();
		}
		return osgiManifest;
	}

	private static String getPomVersion(String pdeVersion) {
		// suffix ".qualifier" translates to "-SNAPSHOT"
		String qualifierSuffix = ".qualifier";
		String pomVersion = pdeVersion;
		if (pdeVersion.endsWith(qualifierSuffix)) {
			pomVersion = pdeVersion.substring(0, pdeVersion.length()
					- qualifierSuffix.length())
					+ "-SNAPSHOT";
		}
		return pomVersion;
	}

	private String getPackagingType(String symbolicName) {
		// assume test bundles end with ".tests"
		if (symbolicName.endsWith(".tests")) {
			return "eclipse-test-plugin";
		} else {
			return "eclipse-plugin";
		}
	}

	private Parent findParent(File projectRoot) throws ModelParseException,
			IOException {
		// assumption/limitation: parent pom must be physically located in
		// parent directory
		// TODO this should be probably be polyglot-enabled too, not hardcoded
		// to pom.xml
		File parentDirPomFile = new File(projectRoot.getParentFile(), "pom.xml");
		if (!parentDirPomFile.isFile()) {
			throw new FileNotFoundException("parent pom " + parentDirPomFile
					+ " not found");
		}
		Map<String, File> options = new HashMap<String, File>();
		options.put(ModelProcessor.SOURCE, parentDirPomFile);
		Model parentModel;
		FileInputStream stream = new FileInputStream(parentDirPomFile);
		try {
			parentModel = defaultModelReader.read(stream, options);
		} finally {
			stream.close();
		}
		Parent parent = new Parent();
		String groupId = parentModel.getGroupId();
		if (groupId == null) {
			// must be inherited from grandparent
			groupId = parentModel.getParent().getGroupId();
		}
		parent.setGroupId(groupId);
		parent.setArtifactId(parentModel.getArtifactId());
		String version = parentModel.getVersion();
		if (version == null) {
			// must be inherited from grandparent
			version = parentModel.getParent().getVersion();
		}
		parent.setVersion(version);
		return parent;
	}
}
