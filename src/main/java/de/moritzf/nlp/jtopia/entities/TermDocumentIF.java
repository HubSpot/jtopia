package de.moritzf.nlp.jtopia.entities;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

@Value.Immutable
@HubSpotStyle
public interface TermDocumentIF {

  List<String> getTerms();
  String getNormalizedText();
  List<TaggedTerm> getTaggedTerms();
  Map<String, Integer> getExtractedTerms();
  Map<String, List<Integer>> getFinalFilteredTerms();
}
