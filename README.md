# karaf-4211-cxf-swagger

Demonstrates the karaf + swagger + cxf integration problems in Karaf 4.2.11:

- UnsupportedOperationException on feature resolution
- Uses constraint violation

If one manages to get around the UnsupportedOperationException the "Uses constraint violation" gets in the way:

```
14:51:07,486 | ERROR | vator-1-thread-2 | BootFeaturesInstaller            | 20 - org.apache.karaf.features.core - 4.2.11 | Error installing boot features
org.apache.felix.resolver.reason.ReasonException: Uses constraint violation. Unable to resolve resource wrapped.cxf-rt-rs-service-description-openapi-v3 [wrapped.cxf-rt-rs-service-description-openapi-v3/3.4.0] because it is exposed to package 'javax.servlet' from resources jakarta.servlet-api [jakarta.servlet-api/4.0.0] and javax.servlet-api [javax.servlet-api/3.1.0] via two dependency chains.

Chain 1:
  wrapped.cxf-rt-rs-service-description-openapi-v3 [wrapped.cxf-rt-rs-service-description-openapi-v3/3.4.0]
    import: (&(osgi.wiring.package=javax.servlet)(version>=4.0.0)(!(version>=4.1.0)))
     |
    export: osgi.wiring.package: javax.servlet
  jakarta.servlet-api [jakarta.servlet-api/4.0.0]

Chain 2:
  wrapped.cxf-rt-rs-service-description-openapi-v3 [wrapped.cxf-rt-rs-service-description-openapi-v3/3.4.0]
    import: (&(osgi.wiring.package=io.swagger.v3.jaxrs2.integration)(version>=2.1.0)(!(version>=3.0.0)))
     |
    export: osgi.wiring.package=io.swagger.v3.jaxrs2.integration; uses:=io.swagger.v3.jaxrs2.integration.api
  io.swagger.core.v3.swagger-jaxrs2 [io.swagger.core.v3.swagger-jaxrs2/2.1.4]
    import: (osgi.wiring.package=io.swagger.v3.jaxrs2.integration.api)
     |
    export: osgi.wiring.package=io.swagger.v3.jaxrs2.integration.api; uses:=javax.servlet
  io.swagger.core.v3.swagger-jaxrs2 [io.swagger.core.v3.swagger-jaxrs2/2.1.4]
    import: (&(osgi.wiring.package=javax.servlet)(version>=4.0.0)(!(version>=5.0.0)))
     |
    export: osgi.wiring.package=javax.servlet; uses:=javax.servlet.annotation
  jakarta.servlet-api [jakarta.servlet-api/4.0.0]
    import: (osgi.wiring.package=javax.servlet.annotation)
     |
    export: osgi.wiring.package=javax.servlet.annotation; uses:=javax.servlet
  javax.servlet-api [javax.servlet-api/3.1.0]
    import: (&(osgi.wiring.package=javax.servlet)(version>=3.1.0))
     |
    export: osgi.wiring.package: javax.servlet
  javax.servlet-api [javax.servlet-api/3.1.0]
```

See also [here](http://cxf.547215.n5.nabble.com/org-osgi-service-http-HttpService-not-found-by-org-apache-cxf-cxf-rt-transports-http-tp5808364.html).

