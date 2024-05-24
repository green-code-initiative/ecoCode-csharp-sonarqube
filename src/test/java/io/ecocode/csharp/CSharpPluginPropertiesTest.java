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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.server.rule.RulesDefinition.Context;

import static org.fest.assertions.Assertions.assertThat;

class CSharpPluginPropertiesTest {

  @Test
  void test() {
    Context context = new Context();
    assertThat(context.repositories()).isEmpty();

    Map<String, String> pluginProperties = new LinkedHashMap<>();
    pluginProperties.put("foo", "fooValue");
    pluginProperties.put("bar", "barValue");

    CSharpConfiguration config = new CSharpConfiguration(
        "/configuration.xml",
        Collections.unmodifiableMap(new HashMap<String, String>()),
        Collections.unmodifiableMap(pluginProperties));

    List<PropertyDefinition> properties = new CSharpPluginProperties(config).getProperties();
    assertThat(properties).hasSize(2);

    PropertyDefinition foo = properties.get(0);
    assertThat(foo.key()).isEqualTo("foo");
    assertThat(foo.defaultValue()).isEqualTo("fooValue");
    assertThat(foo.type()).isEqualTo(PropertyType.STRING);
    assertThat(foo.global()).isFalse();

    PropertyDefinition bar = properties.get(1);
    assertThat(bar.key()).isEqualTo("bar");
    assertThat(bar.defaultValue()).isEqualTo("barValue");
    assertThat(bar.type()).isEqualTo(PropertyType.STRING);
    assertThat(bar.global()).isFalse();
  }

}
