package org.apache.karaf.examples.rest.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/")
public class BookingServiceRest {
    
    private final Map<Long, Booking> bookings = new HashMap<>();

    @Path("/")
    @Produces("application/json")
    @GET
    public Collection<Booking> list() {
        return bookings.values();
    }

    @Path("/{id}")
    @Produces("application/json")
    @GET
    public Booking get(@PathParam("id") Long id) {
        return bookings.get(id);
    }
    
    @Path("/")
    @Consumes("application/json")
    @POST
    public void add(Booking booking) {
        bookings.put(booking.getId(), booking);
    }

    @Path("/")
    @Consumes("application/json")
    @PUT
    public void update(Booking booking) {
        bookings.remove(booking.getId());
        bookings.put(booking.getId(), booking);
    }

    @Path("/{id}")
    @DELETE
    public void remove(@PathParam("id") Long id) {
        bookings.remove(id);
    }
}
