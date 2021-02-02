package client;

/**
 * Created by Aidin on 7/23/2017.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE})
public @interface ImplementedBy {
    String value();
}
