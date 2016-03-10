package ssin.sqlhelper.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表的注解，主要是表名和备注
 * @author IcekingT420
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SASqlTable {
	/**
	 * 表名字
	 * @return
	 */
	public String tableName() default "className";
	
	/**
	 * 表的备注
	 * @return
	 */
	public String tableMark() default "";
}
