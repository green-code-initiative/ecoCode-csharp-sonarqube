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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

public class CSharpPluginProperties {

  private CSharpConfiguration config;

  public CSharpPluginProperties(CSharpConfiguration config) {
    this.config = config;
  }

  public List<PropertyDefinition> getProperties() {
    List<PropertyDefinition> properties = new ArrayList<>();
    config.pluginProperties()
        .forEach((key, defaultValue) -> properties.add(newHiddenPropertyDefinition(key, defaultValue)));
    return Collections.unmodifiableList(properties);
  }

  private static PropertyDefinition newHiddenPropertyDefinition(String key, String defaultValue) {
    return PropertyDefinition.builder(key)
        .type(PropertyType.STRING)
        .defaultValue(defaultValue)
        .hidden()
        .build();
  }
}
