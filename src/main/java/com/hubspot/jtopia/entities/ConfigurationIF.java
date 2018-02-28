package com.hubspot.jtopia.entities;

import java.util.Set;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@Value.Style(
    get = {"is*", "get*"},
    init = "set*",
    typeAbstract = {"Abstract*", "*IF"},
    typeImmutable = "*",
    jdkOnly = true)
@JsonSerialize(as = Configuration.class)
@JsonDeserialize(as = Configuration.class)
public interface ConfigurationIF {

  int getSingleStrengthMinOccur();
  int getNoLimitStrength();
  String getModelFileLocation();
  Set<String> getStopWords();
}
