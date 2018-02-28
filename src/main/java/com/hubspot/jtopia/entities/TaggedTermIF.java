package com.hubspot.jtopia.entities;

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
@JsonSerialize(as = TaggedTerm.class)
@JsonDeserialize(as = TaggedTerm.class)
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
