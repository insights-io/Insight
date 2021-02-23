package com.rebrowse.shared.rest.query;

import com.rebrowse.api.query.SortDirection;
import java.util.List;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;

@Value
public class SortQuery {

  List<Pair<String, SortDirection>> orders;
}
