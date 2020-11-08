package com.meemaw.shared.rest.query;

import lombok.Value;

import java.util.List;

@Value
public class GroupByQuery {

  List<String> fields;
}
