/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(value={ElementType.METHOD})
@Inherited
@Retention(value=RetentionPolicy.RUNTIME)
public @interface HiddenCommand {
}

