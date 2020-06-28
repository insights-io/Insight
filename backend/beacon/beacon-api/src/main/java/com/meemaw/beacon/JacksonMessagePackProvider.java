package com.meemaw.beacon;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.inject.Named;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;

@Provider
@Produces("application/x-msgpack")
@Consumes("application/x-msgpack")
public class JacksonMessagePackProvider extends JacksonJsonProvider {
    public JacksonMessagePackProvider() {
        super(new ObjectMapper(new MessagePackFactory()));
    }

    protected boolean hasMatchingMediaType(MediaType mediaType) {
        if (mediaType != null) {
            String subtype = mediaType.getSubtype();
            return "x-msgpack".equals(subtype);
        }
        return false;
    }
}
