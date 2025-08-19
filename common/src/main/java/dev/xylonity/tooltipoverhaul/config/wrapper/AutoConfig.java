package dev.xylonity.tooltipoverhaul.config.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoConfig {

    // Configuration file name
    String file();

    // Extra comment above the category indicating its own existence (lol)
    boolean categoryBanner() default false;
}

