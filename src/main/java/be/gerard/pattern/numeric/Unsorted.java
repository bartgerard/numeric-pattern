package be.gerard.pattern.numeric;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target({
        ElementType.TYPE,
        ElementType.PARAMETER,
})
public @interface Unsorted {
}
