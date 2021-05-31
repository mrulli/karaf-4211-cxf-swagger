# karaf-4211-cxf-swagger

```
mvn install
```

demonstrates the karaf + swagger + cxf integration problems in Karaf 4.2.11:

- UnsupportedOperationException on feature resolution
- ClassNotFoundException: org.osgi.service.http.HttpService not found by org.apache.cxf.cxf-rt-transports-http  
- Uses constraint violation

If one manages to get around the UnsupportedOperationException the "ClassNotFoundException: org.osgi.service.http.HttpService not found by org.apache.cxf.cxf-rt-transports-http" gets in the way:

```
Caused by: java.lang.ClassNotFoundException: org.osgi.service.http.HttpService not found by org.apache.cxf.cxf-rt-transports-http [121]
	at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1639) ~[?:?]
	at org.apache.felix.framework.BundleWiringImpl.access$200(BundleWiringImpl.java:80) ~[?:?]
	at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:2053) ~[?:?]
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357) ~[?:1.8.0_161]
	at org.apache.cxf.transport.http.osgi.HTTPTransportActivator.start(HTTPTransportActivator.java:62) ~[?:?]
```

This seems due to an optional import in the cxf-rt-transports-http bundle as explained [here](http://cxf.547215.n5.nabble.com/org-osgi-service-http-HttpService-not-found-by-org-apache-cxf-cxf-rt-transports-http-tp5808364.html). The removal of the optional import triggers the "Uses constraint violation":

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

