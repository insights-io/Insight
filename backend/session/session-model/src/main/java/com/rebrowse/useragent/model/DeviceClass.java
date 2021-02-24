package com.rebrowse.useragent.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DeviceClass {
  DESKTOP("Desktop"),
  ANONYMIZED("Anonymized"),
  MOBILE("Mobile"),
  TABLET("Tablet"),
  PHONE("Phone"),
  WATCH("Watch"),
  VIRTUAL_REALITY("Virtual Reality"),
  E_READER("eReader"),
  SET_TOP_BOX("Set-top box"),
  TV("TV"),
  GAME_CONSOLE("Game Console"),
  HANDHELD_GAME_CONSOLE("Handheld Game Console"),
  ROBOT("Robot"),
  ROBOT_MOBILE("Robot Mobile"),
  ROBOT_IMITATOR("Robot Imitator"),
  HACKER("Hacker"),
  UNKNOWN("Unknown"),
  UNCLASSIFIED("Unclassified");

  private static final Map<String, String> REVERSE_LOOKUP =
      Arrays.stream(DeviceClass.values())
          .collect(Collectors.toMap(DeviceClass::getValue, DeviceClass::name));

  private final String value;

  DeviceClass(String value) {
    this.value = value;
  }

  @JsonCreator
  public static DeviceClass fromString(String value) {
    return DeviceClass.valueOf(REVERSE_LOOKUP.get(value));
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
