package pt.fabm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Iterator;

public interface ClassAnalyser {


    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    Iterable<FieldAnalyser> getFields();

    static ClassAnalyser from(Element element, Types types) {
        ch.qos.logback.classic.Logger logger = LoggerInitializer.getLoggerInitializer().createLogger(ClassAnalyser.class);
        return new ClassAnalyser() {

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                return element.getAnnotation(annotationType);
            }

            @Override
            public Iterable<FieldAnalyser> getFields() {
                Iterator<? extends Element> iteratorEl = element.getEnclosedElements().iterator();

                Iterator<FieldAnalyser> iteratorFA = new Iterator<FieldAnalyser>() {
                    Element currentEl;

                    @Override
                    public boolean hasNext() {
                        boolean hasNext = iteratorEl.hasNext();
                        logger.info("first has next:{}", hasNext);
                        while (hasNext) {
                            Element current = iteratorEl.next();
                            logger.info("current:{}", current);
                            logger.info("isField:{} name:{}", current.getKind().name(), current.getSimpleName());
                            if (!current.getKind().isField()) {
                                hasNext = iteratorEl.hasNext();
                            } else {
                                logger.info("current not field:{}", current);
                                currentEl = current;
                                break;
                            }
                        }
                        return hasNext;
                    }

                    @Override
                    public FieldAnalyser next() {
                        return FieldAnalyser.fromElement(currentEl, types);
                    }
                };

                return () -> iteratorFA;
            }
        };
    }

    ;
}
