package de.moritzf.nlp.jtopia.entities;

import org.immutables.value.Value;

@Value.Immutable
@HubSpotStyle
public interface TaggedTermIF {

  String getTerm();
  String getTag();
  String getNorm();

  static TaggedTerm of(String term, String tag, String norm) {
    return TaggedTerm.builder()
        .setTerm(term)
        .setTag(tag)
        .setNorm(norm)
        .build();
  }
}
