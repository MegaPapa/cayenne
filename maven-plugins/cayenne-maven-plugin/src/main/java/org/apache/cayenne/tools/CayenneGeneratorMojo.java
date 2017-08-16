/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
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
 ****************************************************************/

package org.apache.cayenne.tools;

import org.apache.cayenne.configuration.xml.DataChannelMetaData;
import org.apache.cayenne.dbsync.filter.NamePatternMatcher;
import org.apache.cayenne.dbsync.reverse.configuration.ToolsModule;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.gen.CgenModule;
import org.apache.cayenne.gen.ClassGenerationAction;
import org.apache.cayenne.gen.ClientClassGenerationAction;
import org.apache.cayenne.gen.xml.CgenConfiguration;
import org.apache.cayenne.map.DataMap;
import org.slf4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Maven mojo to perform class generation from data map. This class is an Maven
 * adapter to DefaultClassGenerator class.
 *
 * @since 3.0
 */
@Mojo(name = "cgen", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class CayenneGeneratorMojo extends AbstractMojo {

    public static final File[] NO_FILES = new File[0];

    /**
	 * Path to additional DataMap XML files to use for class generation.
	 */
    @Parameter
	private File additionalMaps;

	/**
	 * Whether we are generating classes for the client tier in a Remote Object
	 * Persistence application. Default is <code>false</code>.
	 */
	@Parameter
	private boolean client = false;

	/**
	 * Destination directory for Java classes (ignoring their package names).
	 */
	@Parameter(defaultValue = "${project.build.sourceDirectory}")
	private File destDir;

	/**
	 * Specify generated file encoding if different from the default on current
	 * platform. Target encoding must be supported by the JVM running Maven
	 * build. Standard encodings supported by Java on all platforms are
	 * US-ASCII, ISO-8859-1, UTF-8, UTF-16BE, UTF-16LE, UTF-16. See Sun Java
	 * Docs for java.nio.charset.Charset for more information.
	 */
	@Parameter
	private String encoding;

	/**
	 * Entities (expressed as a perl5 regex) to exclude from template
	 * generation. (Default is to include all entities in the DataMap).
	 */
	@Parameter
	private String excludeEntities;

	/**
	 * Entities (expressed as a perl5 regex) to include in template generation.
	 * (Default is to include all entities in the DataMap).
	 */
	@Parameter
	private String includeEntities;

	/**
	 * If set to <code>true</code>, will generate subclass/superclass pairs,
	 * with all generated code included in superclass (default is
	 * <code>true</code>).
	 */
	@Parameter
	private boolean makePairs = true;

	/**
	 * DataMap XML file to use as a base for class generation.
	 */
	@Parameter(required = true)
	private File map;

	/**
	 * Specifies generator iteration target. &quot;entity&quot; performs one
	 * iteration for each selected entity. &quot;datamap&quot; performs one
	 * iteration per datamap (This is always one iteration since cgen currently
	 * supports specifying one-and-only-one datamap). (Default is &quot;entity&quot;)
	 */
	@Parameter
	private String mode = "entity";

	/**
	 * Name of file for generated output. (Default is &quot;*.java&quot;)
	 */
	@Parameter
	private String outputPattern = "*.java";

	/**
	 * If set to <code>true</code>, will overwrite older versions of generated
	 * classes. Ignored unless makepairs is set to <code>false</code>.
	 */
	@Parameter
	private boolean overwrite = false;

	/**
	 * Java package name of generated superclasses. Ignored unless
	 * <code>makepairs</code> set to <code>true</code>. If omitted, each
	 * superclass will be assigned the same package as subclass. Note that
	 * having superclass in a different package would only make sense when
	 * <code>usepkgpath</code> is set to <code>true</code>. Otherwise classes
	 * from different packages will end up in the same directory.
	 */
	@Parameter
	private String superPkg;

	/**
	 * Location of Velocity template file for Entity superclass generation.
	 * Ignored unless <code>makepairs</code> set to <code>true</code>. If
	 * omitted, default template is used.
	 */
	@Parameter
	private String superTemplate;

	/**
	 * Location of Velocity template file for Entity class generation. If
	 * omitted, default template is used.
	 */
	@Parameter
	private String template;

	/**
	 * Location of Velocity template file for Embeddable superclass generation.
	 * Ignored unless <code>makepairs</code> set to <code>true</code>. If
	 * omitted, default template is used.
	 */
	@Parameter
	private String embeddableSuperTemplate;

	/**
	 * Location of Velocity template file for Embeddable class generation. If
	 * omitted, default template is used.
	 */
	@Parameter
	private String embeddableTemplate;

	/**
	 * If set to <code>true</code> (default), a directory tree will be generated
	 * in "destDir" corresponding to the class package structure, if set to
	 * <code>false</code>, classes will be generated in &quot;destDir&quot;
	 * ignoring their package.
	 */
	@Parameter
	private boolean usePkgPath = true;

    /**
     * If set to <code>true</code>, will generate String Property names.
     * Default is <code>false</code>.
     */
    @Parameter
    private boolean createPropertyNames = false;

    private transient Injector injector;
    private boolean useMavenConfig;
    private boolean isDefaultDestDirExist;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// Create the destination directory if necessary.
		// TODO: (KJM 11/2/06) The destDir really should be added as a
		// compilation resource for maven.
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		injector = DIBootstrap.createInjector(new ToolsModule(LoggerFactory.getLogger(CayenneGeneratorMojo.class)),
												new CgenModule());


		Logger logger = new MavenLogger(this);
		CayenneGeneratorMapLoaderAction loaderAction = new CayenneGeneratorMapLoaderAction(injector);
		loaderAction.setMainDataMapFile(map);

		CayenneGeneratorEntityFilterAction filterAction = new CayenneGeneratorEntityFilterAction();
		filterAction.setClient(client);
		filterAction.setNameFilter(NamePatternMatcher.build(logger, includeEntities, excludeEntities));

		try {
			loaderAction.setAdditionalDataMapFiles(convertAdditionalDataMaps(additionalMaps));

			DataMap dataMap = loaderAction.getMainDataMap();
			DataChannelMetaData metaData = injector.getInstance(DataChannelMetaData.class);
			CgenConfiguration dataMapConfiguration = metaData.get(dataMap, CgenConfiguration.class);
			if (dataMapConfiguration != null) {
				File metaAdditionalDataMaps = dataMapConfiguration.getAdditionalMaps();
				loaderAction.setAdditionalDataMapFiles(convertAdditionalDataMaps(metaAdditionalDataMaps));
				dataMap = loaderAction.getMainDataMap();
			}
			ClassGenerationAction generator;


			if ((useMavenConfig) && (dataMapConfiguration != null)) {
				LoggerFactory.getLogger(CayenneGeneratorMojo.class).warn("Found several cgen configurations. " +
						"Configuration selected from 'pom.xml' file.");
			}

			if ((dataMapConfiguration != null) && (!useMavenConfig)) {
				generator = createGenerator(dataMapConfiguration);
			} else {
				generator = createGenerator();
			}
			generator.setLogger(logger);
			generator.setTimestamp(map.lastModified());
			generator.setDataMap(dataMap);
			generator.addEntities(filterAction.getFilteredEntities(dataMap));
			// ksenia khailenko 15.10.2010
			// TODO add the "includeEmbeddables" and "excludeEmbeddables"
			// attributes
			generator.addEmbeddables(dataMap.getEmbeddables());
			// TODO add the "includeQueries" and "excludeQueries" attributes
			generator.addQueries(dataMap.getQueryDescriptors());
			generator.execute();
		} catch (Exception e) {
			throw new MojoExecutionException("Error generating classes: ", e);
		}
	}

	/**
	 * Loads and returns DataMap based on <code>map</code> attribute.
	 */
	protected File[] convertAdditionalDataMaps(File additionalMaps) throws Exception {

		if (additionalMaps == null) {
			return NO_FILES;
		}

		if (!additionalMaps.isDirectory()) {
			throw new MojoFailureException(
					"'additionalMaps' must be a directory.");
		}

        FilenameFilter mapFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null &&
                       name.toLowerCase().endsWith(".map.xml");
            }
        };
        return additionalMaps.listFiles(mapFilter);
	}

	ClassGenerationAction newGeneratorInstance(boolean client) {
		return client ? new ClientClassGenerationAction() : new ClassGenerationAction();
	}

	/**
	 * Factory method to create internal class generator. Called from
	 * constructor.
	 */
	protected ClassGenerationAction createGenerator() {
		ClassGenerationAction action = newGeneratorInstance(client);

		injector.injectMembers(action);

		action.setDestDir(destDir);
		action.setEncoding(encoding);
		action.setMakePairs(makePairs);
		action.setArtifactsGenerationMode(mode);
		action.setOutputPattern(outputPattern);
		action.setOverwrite(overwrite);
		action.setSuperPkg(superPkg);
		action.setSuperTemplate(superTemplate);
		action.setTemplate(template);
		action.setEmbeddableSuperTemplate(embeddableSuperTemplate);
		action.setEmbeddableTemplate(embeddableTemplate);
		action.setUsePkgPath(usePkgPath);
        action.setCreatePropertyNames(createPropertyNames);

		return action;
	}

	private ClassGenerationAction createGenerator(CgenConfiguration configuration) {
		ClassGenerationAction action = newGeneratorInstance(configuration.isClient());
		injector.injectMembers(action);

		action.setDestDir(configuration.getDestDir());
		action.setEncoding(configuration.getEncoding());
		action.setMakePairs(configuration.isMakePairs());
		action.setArtifactsGenerationMode(configuration.getArtifactsGenerationMode().getLabel());
		action.setOutputPattern(configuration.getOutputPattern());
		action.setOverwrite(configuration.isOverwrite());
		action.setSuperPkg(configuration.getSuperPkg());
		action.setSuperTemplate(configuration.getSuperTemplate());
		action.setTemplate(configuration.getTemplate());
		action.setEmbeddableSuperTemplate(configuration.getEmbeddableSuperTemplate());
		action.setEmbeddableTemplate(configuration.getEmbeddableTemplate());
		action.setUsePkgPath(configuration.isUsePkgPath());
		action.setCreatePropertyNames(configuration.isCreatePropertyNames());

		return action;
	}

	public void setDestDir(File destDir) {
		this.destDir = destDir;
		if (isDefaultDestDirExist) {
			useMavenConfig = true;
		} else {
			isDefaultDestDirExist = true;
		}
	}

	public void setAdditionalMaps(File additionalMaps) {
		this.additionalMaps = additionalMaps;
		useMavenConfig = true;
	}

	public void setClient(boolean client) {
		this.client = client;
		useMavenConfig = true;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
		useMavenConfig = true;
	}

	public void setExcludeEntities(String excludeEntities) {
		this.excludeEntities = excludeEntities;
		useMavenConfig = true;
	}

	public void setIncludeEntities(String includeEntities) {
		this.includeEntities = includeEntities;
		useMavenConfig = true;
	}

	public void setMakePairs(boolean makePairs) {
		this.makePairs = makePairs;
		useMavenConfig = true;
	}

	public void setMode(String mode) {
		this.mode = mode;
		useMavenConfig = true;
	}

	public void setOutputPattern(String outputPattern) {
		this.outputPattern = outputPattern;
		useMavenConfig = true;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
		useMavenConfig = true;
	}

	public void setSuperPkg(String superPkg) {
		this.superPkg = superPkg;
		useMavenConfig = true;
	}

	public void setSuperTemplate(String superTemplate) {
		this.superTemplate = superTemplate;
		useMavenConfig = true;
	}

	public void setTemplate(String template) {
		this.template = template;
		useMavenConfig = true;
	}

	public void setEmbeddableSuperTemplate(String embeddableSuperTemplate) {
		this.embeddableSuperTemplate = embeddableSuperTemplate;
		useMavenConfig = true;
	}

	public void setEmbeddableTemplate(String embeddableTemplate) {
		this.embeddableTemplate = embeddableTemplate;
		useMavenConfig = true;
	}

	public void setUsePkgPath(boolean usePkgPath) {
		this.usePkgPath = usePkgPath;
		useMavenConfig = true;
	}

	public void setCreatePropertyNames(boolean createPropertyNames) {
		this.createPropertyNames = createPropertyNames;
		useMavenConfig = true;
	}
}
