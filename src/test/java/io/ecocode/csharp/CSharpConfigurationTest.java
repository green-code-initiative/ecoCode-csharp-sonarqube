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

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CSharpConfigurationTest {

  @Test
  void invalid_xml() {
    assertThat(assertThrows(IllegalStateException.class,
        () -> new CSharpConfiguration("/RoslynSdkConfigurationTest/invalid_xml.xml")).getMessage())
        .isEqualTo("Invalid Roslyn SDK XML configuration file: /RoslynSdkConfigurationTest/invalid_xml.xml");
  }

  @Test
  void invalid_config() {
    assertThat(assertThrows(IllegalStateException.class,
        () -> new CSharpConfiguration("/RoslynSdkConfigurationTest/invalid_config.xml")).getMessage())
        .isEqualTo("<PluginProperties> can be present at most once");
  }

  @Test
  void invalid_root() {
    assertThat(assertThrows(IllegalStateException.class,
        () -> new CSharpConfiguration("/RoslynSdkConfigurationTest/invalid_root.xml")).getMessage())
        .isEqualTo(
            "Expected <RoslynSdkConfiguration> as root element in configuration file: /RoslynSdkConfigurationTest/invalid_root.xml but got: <foo>");
  }

  @Test
  void valid() {
    CSharpConfiguration config = new CSharpConfiguration("/RoslynSdkConfigurationTest/valid.xml");
    assertThat(config.mandatoryProperty("Foo")).isEqualTo("FooValue");
    assertThat(config.property("Foo").get()).isEqualTo("FooValue");
    assertThat(config.mandatoryProperty("Bar")).isEqualTo("BarValue");
    assertThat(config.property("Bar").get()).isEqualTo("BarValue");

    assertThat(config.property("NonExisting").isPresent()).isFalse();

    Map<String, String> expectedProperties = new HashMap<>();
    expectedProperties.put("Foo", "FooValue");
    expectedProperties.put("Bar", "BarValue");
    assertThat(config.properties()).isEqualTo(expectedProperties);

    Map<String, String> expectedPluginProperties = new HashMap<>();
    expectedPluginProperties.put("PluginFoo", "PluginFooValue");
    expectedPluginProperties.put("PluginBar", "PluginBarValue");
    assertThat(config.pluginProperties()).isEqualTo(expectedPluginProperties);
  }

  @Test
  void missing_config() {
    assertThat(assertThrows(IllegalArgumentException.class,
        () -> new CSharpConfiguration("/missing-file.xml")).getMessage())
        .isEqualTo(
            "Could not read /missing-file.xml");
  }

  @Test
  void missing_mandatory() {
    CSharpConfiguration config = new CSharpConfiguration("/RoslynSdkConfigurationTest/valid.xml");
    assertThat(
        assertThrows(IllegalStateException.class, () -> config.mandatoryProperty("MissingPropertyKey")).getMessage())
        .isEqualTo(
            "Mandatory <MissingPropertyKey> element not found in the Roslyn SDK XML configuration file: /RoslynSdkConfigurationTest/valid.xml");
  }

  @Test
  void empty() {
    CSharpConfiguration config = new CSharpConfiguration("/RoslynSdkConfigurationTest/empty.xml");
    assertThat(config.properties()).isEmpty();
    assertThat(config.pluginProperties()).isEmpty();
  }

  @Test
  void default_path() {
    assertThat(new CSharpConfiguration().mandatoryProperty("PluginKeyDifferentiator")).isEqualTo("ecocodecsharp");
  }

}
