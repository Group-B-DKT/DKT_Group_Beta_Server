package com.group_beta.dkt_server.root;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/home")
public class RootController {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String home() {
        return "DKT Server is up and running";
    }
}
