package org.example.prodcatservice.security;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasScope {
    String value(); // e.g., "internal"
}
