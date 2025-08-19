package dev.xylonity.tooltipoverhaul.config.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigEntry {

    // Normal comment for the config entry
    String comment() default "";

    // Additional information note below the comment annotation
    String note() default "";

    // The category the entry belongs to
    String category() default "";

    // min and max caps, if any
    double min() default Double.NEGATIVE_INFINITY;
    double max() default Double.POSITIVE_INFINITY;

}