package com.meemaw.shared.rest.query;

import java.util.List;
import lombok.Value;

@Value
public class GroupByQuery {

  List<String> fields;
}
