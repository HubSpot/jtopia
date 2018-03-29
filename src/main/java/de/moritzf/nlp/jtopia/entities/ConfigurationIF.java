package de.moritzf.nlp.jtopia.entities;

import java.util.Set;

import org.immutables.value.Value;

import com.hubspot.immutables.style.HubSpotStyle;

@Value.Immutable
@HubSpotStyle
public interface ConfigurationIF {

  int getSingleStrengthMinOccur();
  int getNoLimitStrength();
  String getModelFileLocation();
  Set<String> getStopWords();
}
