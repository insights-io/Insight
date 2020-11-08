package com.meemaw.shared.rest.query;

import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Value
public class SortQuery {

  List<Pair<String, SortDirection>> orders;
}
