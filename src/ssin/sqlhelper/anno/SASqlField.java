package ssin.sqlhelper.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 针对表字段的注解
 * @author IcekingT420
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SASqlField {
	/**
	 * 字段的名字，默认是属性名
	 * 如果是manyToOne情况，一般必须指定，
	 * 或者默认使用 对方类_主键
	 * @return
	 */
	public String columnName() default "fieldName";
	
	/**
	 * 自动的数据库类型，不同数据库不同类型的
	 * auto表示自动根据java类型和数据库类型来匹配得到
	 * @return
	 */
	public String columnType() default "auto";
	
	/**
	 * 字段的描述
	 * @return
	 */
	public String columnMark() default "";
	
	/**
	 * 长度，一般给varchar类型使用的，默认是255
	 * @return
	 */
	public int columnLength() default 255;
	
	/**
	 * 是否是主键，默认否
	 * @return
	 */
	public boolean primaryKey() default false;
	
	/**
	 * 是否自动增，主键情况下，默认自动增
	 * @return
	 */
	public boolean autoIncrement() default true;
	
	/**
	 * 默认允许为空
	 * @return
	 */
	public boolean notNull() default false;
	
	/**
	 * 默认值，一般不使用
	 * @return
	 */
	public String defaultValue() default "";
	
	/**
	 * 是否为该列建立索引
	 * 主键不允许建立索引
	 * @return
	 */
	public boolean index() default false;
	
	/**
	 * 如果建立索引，是升序=true，还是降序的=false
	 * @return
	 */
	public boolean indexAsc() default true;
	
}
