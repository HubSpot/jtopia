package de.moritzf.nlp.jtopia.entities;

import java.util.Set;

import org.immutables.value.Value;

@Value.Immutable
@HubSpotStyle
public interface ConfigurationIF {

  int getSingleStrengthMinOccur();
  int getNoLimitStrength();
  String getModelFileLocation();
  Set<String> getStopWords();
}
