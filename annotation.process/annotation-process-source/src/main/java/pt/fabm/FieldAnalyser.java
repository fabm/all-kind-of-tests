package pt.fabm;

import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;

public interface FieldAnalyser {
    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    String getName();
    String getType();

    static FieldAnalyser fromElement(Element element, Types types) {
        return new FieldAnalyser() {


            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                return element.getAnnotation(annotationType);
            }

            @Override
            public String getName() {
                return element.getSimpleName().toString();
            }

            @Override
            public String getType() {
                return types.asElement(element.asType()).toString();
            }
        };
    }
}
