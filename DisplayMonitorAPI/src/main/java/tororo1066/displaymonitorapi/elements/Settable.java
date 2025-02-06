package tororo1066.displaymonitorapi.elements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * {@link IAbstractElement}のフィールドに付与するアノテーション<br>
 * このアノテーションが付与されたフィールドは、Configから値を設定できる
 */
@Target(ElementType.FIELD)
public @interface Settable {

    /**
     * Configから値を取得する際のキー
     * @return キー
     */
    String name() default "";

    /**
     * trueの場合、指定されたクラスについているアノテーションを持つクラスのみが設定可能
     * @return {@link Boolean}
     */
    boolean childOnly() default false;
}
