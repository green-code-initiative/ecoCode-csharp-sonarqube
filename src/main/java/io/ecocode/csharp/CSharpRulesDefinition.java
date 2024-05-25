/*
 * ecoCode - C# language - Provides rules to reduce the environmental footprint of your C# programs
 * Copyright © 2024 Green Code Initiative (https://www.ecocode.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.ecocode.csharp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.net.JarURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;
import org.slf4j.Logger;

public class CSharpRulesDefinition implements RulesDefinition {

  private CSharpConfiguration config;
  private static final Logger LOG = LoggerFactory.getLogger(CSharpRulesDefinition.class);

  private final String rulesResourceBasePath;

  private final SonarRuntime sonarRuntime;

  public CSharpRulesDefinition(CSharpConfiguration config, SonarRuntime sonarRuntime) {
    this(config, sonarRuntime, "io/ecocode/rules/csharp");
  }

  public CSharpRulesDefinition(CSharpConfiguration config, SonarRuntime sonarRuntime, String rulesResourceBasePath) {
    this.config = config;
    this.sonarRuntime = sonarRuntime;
    this.rulesResourceBasePath = rulesResourceBasePath;
  }

  @Override
  public void define(Context context) {
    try {
      NewRepository repository = context
          .createRepository(config.mandatoryProperty("RepositoryKey"), config.mandatoryProperty("RepositoryLanguage"))
          .setName(config.mandatoryProperty("RepositoryName"));

      List<String> jsonRules = this.listJsonRules();

      LOG.debug("C# Rules found : {}", jsonRules.size());
      LOG.debug("Rule names: {}", jsonRules);

      RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader(rulesResourceBasePath, sonarRuntime);
      ruleMetadataLoader.addRulesByRuleKey(repository, jsonRules);

      repository.done();
    } catch (IOException e) {
      LOG.error("Unable to locate rules files", e);
    }
  }

  // SONAR : Disable rules S5042, considered as safe
  @SuppressWarnings("java:S5042")
  private List<String> listJsonRules() throws IOException {
    List<String> jsonFiles = new ArrayList<>();

    // Obtain the ClassLoader
    ClassLoader classLoader = CSharpRulesDefinition.class.getClassLoader();

    // Get the URL of the 'rules' directory
    URL rulesDir = classLoader.getResource(rulesResourceBasePath);

    if (rulesDir != null) {
      String protocol = rulesDir.getProtocol();

      if ("jar".equals(protocol)) {
        LOG.debug("Scan JAR to find rules");
        // Handle JAR resources
        JarURLConnection jarConnection = (JarURLConnection) rulesDir.openConnection();
        JarFile jarFile = jarConnection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          String name = entry.getName();
          if (name.startsWith(rulesResourceBasePath) && name.endsWith(".json")) {
            String fileName = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
            LOG.debug("Rule {} found in JAR", fileName);
            jsonFiles.add(fileName);
          }
        }
      } else if ("file".equals(protocol)) {
        // Handle directory resources (useful during development outside of JAR)
        LOG.debug("Scan directory to find rules");
        try (Stream<Path> files = Files.walk(Paths.get(rulesDir.toURI()))) {
          files.filter(Files::isRegularFile)
              .map(path -> path.getFileName().toString())
              .filter(name -> name.endsWith(".json"))
              .map(name -> name.substring(0, name.lastIndexOf('.')))
              .forEach(jsonFiles::add);
        } catch (Exception e) {
          throw new IOException("Failed to list files in the rules directory", e);
        }
      }
    }

    return jsonFiles;
  }
}
