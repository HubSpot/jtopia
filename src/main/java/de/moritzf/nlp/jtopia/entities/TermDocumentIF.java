package de.moritzf.nlp.jtopia.entities;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@Value.Style(
    get = {"is*", "get*"},
    init = "set*",
    typeAbstract = {"Abstract*", "*IF"},
    typeImmutable = "*",
    optionalAcceptNullable = true,
    visibility = ImplementationVisibility.SAME,
    jdkOnly = true)
@JsonSerialize(as = TermDocument.class)
@JsonDeserialize(as = TermDocument.class)
public interface TermDocumentIF {

  List<String> getTerms();
  String getNormalizedText();
  List<TaggedTerm> getTaggedTerms();
  Map<String, Integer> getExtractedTerms();
  Map<String, List<Integer>> getFinalFilteredTerms();
}
