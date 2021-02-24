package com.rebrowse.shared.ip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rebrowse.shared.context.RequestContextUtils;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ResourceInfo;
import org.junit.jupiter.api.Test;

public class RequestContextUtilsTest {

  @Test
  public void should_returnEmpty_on_noResourceInfo() {
    assertEquals(
        Optional.empty(),
        RequestContextUtils.getResourcePath(
            new ResourceInfo() {
              @Override
              public Method getResourceMethod() {
                return null;
              }

              @Override
              public Class<?> getResourceClass() {
                return null;
              }
            }));
  }

  @Test
  public void should_returnClassPath_on_noResourceMethod() {
    assertEquals(
        Optional.of("hello/world/"),
        RequestContextUtils.getResourcePath(
            new ResourceInfo() {
              @Override
              public Method getResourceMethod() {
                return null;
              }

              @Override
              public Class<?> getResourceClass() {
                return ExampleResource.class;
              }
            }));
  }

  @Test
  public void should_returnFullClassPath_onFullResourceInfo() {
    assertEquals(
        Optional.of("hello/world/hi"),
        RequestContextUtils.getResourcePath(
            new ResourceInfo() {
              @Override
              public Method getResourceMethod() {
                try {
                  return ExampleResource.class.getMethod("greet");
                } catch (NoSuchMethodException e) {
                  throw new RuntimeException(e);
                }
              }

              @Override
              public Class<?> getResourceClass() {
                return ExampleResource.class;
              }
            }));
  }

  @Test
  public void should_returnFullClassPath_onFullResourceInfo_with_path_param() {
    assertEquals(
        Optional.of("hello/world/hi/{name}"),
        RequestContextUtils.getResourcePath(
            new ResourceInfo() {
              @Override
              public Method getResourceMethod() {
                try {
                  return ExampleResource.class.getMethod("greetUser", String.class);
                } catch (NoSuchMethodException e) {
                  throw new RuntimeException(e);
                }
              }

              @Override
              public Class<?> getResourceClass() {
                return ExampleResource.class;
              }
            }));
  }

  @Path("hello/world")
  interface ExampleResource {

    @GET
    @Path("hi")
    String greet();

    @GET
    @Path("hi/{name}")
    String greetUser(@PathParam("name") String name);
  }
}
