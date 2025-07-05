package net.toydotgame.utils;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the specified field/global/method/constructor as <u>intentionally</u>
 * package-private (<i>default</i>) access modified.
 */
@Retention(SOURCE)
@Target({ TYPE, FIELD, METHOD, CONSTRUCTOR })
public @interface Package {}
