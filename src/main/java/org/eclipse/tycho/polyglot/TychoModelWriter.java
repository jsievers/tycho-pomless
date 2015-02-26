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

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.ModelWriter;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.maven.polyglot.io.ModelWriterSupport;

@Component(role = ModelWriter.class, hint = "tycho")
public class TychoModelWriter extends ModelWriterSupport {
    public void write(Writer output, Map<String, Object> o, Model model) throws IOException {
        throw new UnsupportedOperationException(
                "serializing the maven model to MANIFEST.MF does not make sense (mapping MANIFEST.MF -> pom.xml is not bijective)");
    }

}
