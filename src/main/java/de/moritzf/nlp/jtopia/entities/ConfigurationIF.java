package de.moritzf.nlp.jtopia.entities;

import java.util.Set;

import org.immutables.value.Value;

import com.hubspot.immutables.style.HubSpotStyle;

@Value.Immutable
@HubSpotStyle
public interface ConfigurationIF {

  int getSingleWordMinOccurrence();
  int getMultiWordMinStrength();
  String getModelFileLocation();
  Set<String> getStopWords();
}
