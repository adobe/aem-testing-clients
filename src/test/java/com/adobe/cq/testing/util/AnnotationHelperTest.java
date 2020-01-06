package com.adobe.cq.testing.util;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AnnotationHelperTest {

    @Test
    public void canFindOptionalAnnotation() {

        ExtensionContext context = spy(ExtensionContext.class);
        when(context.getTestClass()).thenReturn(Optional.of(ForTest.class));
        Optional<ForTestAnnotate> optionalAnnotation = AnnotationHelper.findOptionalAnnotation(context, ForTestAnnotate.class);
        assertTrue(optionalAnnotation.isPresent());
    }

    @Test
    public void canFindOptionalAnnotationEmpty() {

        ExtensionContext context = spy(ExtensionContext.class);
        when(context.getTestClass()).thenReturn(Optional.of(ForTest.class));
        Optional<Test> optionalAnnotation = AnnotationHelper.findOptionalAnnotation(context, Test.class);
        assertFalse(optionalAnnotation.isPresent());
    }

    @Test
    public void canFindOptionalAnnotationInParentContext() {

        ExtensionContext parentContext = spy(ExtensionContext.class);
        when(parentContext.getTestClass()).thenReturn(Optional.of(ForTest.class));
        ExtensionContext context = spy(ExtensionContext.class);
        when(context.getTestClass()).thenReturn(Optional.of(ForTestNoAnnotation.class));
        when(context.getParent()).thenReturn(Optional.of(parentContext));
        Optional<ForTestAnnotate> optionalAnnotation = AnnotationHelper.findOptionalAnnotation(context, ForTestAnnotate.class);
        assertTrue(optionalAnnotation.isPresent());
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface ForTestAnnotate {

    }

    @ForTestAnnotate
    public class ForTest {

    }

    public class ForTestNoAnnotation {

    }
}
