package ssin.sqlhelper.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 针对manytoone对象的
 * @author IcekingT420
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SASqlManyToOne {
	
	/**
	 * 本类中的外键字段（类字段，不是数据库列名）
	 * @return
	 */
	public String fkFieldName() default "";
	
	/**
	 * 是否延迟加载，默认是false，即需要加载的
	 * @return
	 */
	public boolean lazy() default false;
}
