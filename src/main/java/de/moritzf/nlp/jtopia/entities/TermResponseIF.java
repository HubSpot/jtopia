package de.moritzf.nlp.jtopia.entities;

import java.util.Map;

import org.immutables.value.Value;

import com.hubspot.immutables.style.HubSpotStyle;

@Value.Immutable
@HubSpotStyle
public interface TermResponseIF {
  Map<String, Integer> getTermsAndOccurrences();
}
