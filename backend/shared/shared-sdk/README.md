# Rebrowse Java client library

The official [Rebrowse](https://rebrowse.dev/) Java client library.

## Installation

### Requirements

- Java 11 or later

## Usage

RebrowseExample.java

```java
import java.util.HashMap;
import java.util.Map;

import com.rebrowse.Rebrowse;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.exception.RebrowseException;


public class RebrowseExample {

    public static void main(String[] args) {
        Rebrowse.apiKey = "apiKey";
        Rebrowse.maxNetworkRetries(2);

        Organization.retrieve().thenApply(organization -> {
            System.out.println(organization.getName());
        }).exceptionally(throwable -> {
            RebrowseException exception = throwable.getCause();
            exception.printStackTrace();
        });
    }
}
```
