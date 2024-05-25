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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogTesterJUnit5;
import static org.fest.assertions.Assertions.assertThat;


/*
 * TODO DDC : correction of logger pb during test units ... cf 
 * SLF4J(W): No SLF4J providers were found.
 * SLF4J(W): Defaulting to no-operation (NOP) logger implementation
 * SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
 */
class CSharpRulesDefinitionTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void test_rules_missing() {
    Context context = new Context();
    assertThat(context.repositories()).isEmpty();

    Map<String, String> properties = new HashMap<>();
    properties.put("RepositoryKey", "MyRepoKey");
    properties.put("RepositoryLanguage", "MyLangKey");
    properties.put("RepositoryName", "MyRepoName");

    CSharpConfiguration config = new CSharpConfiguration(
        "/configuration.xml",
        Collections.unmodifiableMap(properties),
        Collections.unmodifiableMap(new HashMap<String, String>()));

    CSharpRulesDefinition rulesDefinition = new CSharpRulesDefinition(config,
        SonarRuntimeImpl.forSonarQube(Version.create(9, 9), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY),
        "/missing");
    rulesDefinition.define(context);

    Repository repo = context.repository("MyRepoKey");

    assertThat(repo.rules()).hasSize(0);
  }

  @Test
  void test_rules_defined() {
    Context context = new Context();
    assertThat(context.repositories()).isEmpty();

    Map<String, String> properties = new HashMap<>();
    properties.put("RepositoryKey", "MyRepoKey");
    properties.put("RepositoryLanguage", "MyLangKey");
    properties.put("RepositoryName", "MyRepoName");

    CSharpConfiguration config = new CSharpConfiguration(
        "/configuration.xml",
        Collections.unmodifiableMap(properties),
        Collections.unmodifiableMap(new HashMap<String, String>()));

    CSharpRulesDefinition rulesDefinition = new CSharpRulesDefinition(config,
        SonarRuntimeImpl.forSonarQube(Version.create(9, 9), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    rulesDefinition.define(context);

    assertThat(context.repositories()).hasSize(1);
    Repository repo = context.repository("MyRepoKey");

    assertThat(repo.rules()).hasSize(1);
    Rule rule = repo.rules().get(0);

    assertThat(rule.key()).isEqualTo("EC1000");
    assertThat(rule.name()).isEqualTo("Test rule S1000");
    assertThat(rule.severity()).isEqualTo("MINOR");
    assertThat(rule.template()).isFalse();
    assertThat(rule.htmlDescription()).isEqualTo("<h2>Description test</h2>");
    assertThat(rule.tags()).containsOnly("tag-1", "tag-2", "tag-3");
    assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(rule.params()).isEmpty();

    assertThat(rule.debtRemediationFunction()).isNotNull();
    assertThat(rule.debtRemediationFunction().type()).isEqualTo(DebtRemediationFunction.Type.CONSTANT_ISSUE);
    assertThat(rule.debtRemediationFunction().baseEffort()).isEqualTo("5min");

    assertThat(logTester.logs()).isEmpty();
  }
}
