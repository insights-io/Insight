package com.rebrowse.beacon.resource.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.beacon.model.Beacon;
import com.rebrowse.beacon.model.dto.BeaconDTO;
import com.rebrowse.beacon.service.RecordingService;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.status.MissingStatus;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordingResourceImpl implements RecordingResource {

  @Inject RecordingService recordingService;

  @Inject ObjectMapper objectMapper;

  @Inject Validator validator;

  @Override
  public CompletionStage<Response> beat(
      String organizationId, UUID sessionId, UUID deviceId, UUID pageVisitId, String payload) {
    BeaconDTO beaconDTO;
    try {
      beaconDTO = objectMapper.readValue(payload, BeaconDTO.class);
    } catch (JsonProcessingException ex) {
      log.error("Failed to serialize beacon", ex);
      return CompletableFuture.completedFuture(
          Boom.status(MissingStatus.UNPROCESSABLE_ENTITY)
              .message(ex.getOriginalMessage())
              .response());
    }

    Set<ConstraintViolation<BeaconDTO>> constraintViolations = validator.validate(beaconDTO);
    if (!constraintViolations.isEmpty()) {
      throw new ConstraintViolationException(constraintViolations);
    }

    return beacon(organizationId, sessionId, deviceId, pageVisitId, beaconDTO);
  }

  private CompletionStage<Response> beacon(
      String organizationId, UUID sessionId, UUID deviceId, UUID pageVisitId, BeaconDTO beacon) {
    return recordingService
        .process(organizationId, sessionId, deviceId, pageVisitId, Beacon.from(beacon))
        .thenApply(nothing -> Response.noContent().build());
  }
}
