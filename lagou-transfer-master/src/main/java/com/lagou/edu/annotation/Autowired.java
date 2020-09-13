package com.lagou.edu.annotation;

import java.lang.annotation.*;


@Target({ElementType.FIELD, ElementType.TYPE})//修饰的对象范围
@Retention(RetentionPolicy.RUNTIME)//保留的时间
@Documented//其它类型的annotation应该被作为被标注的程序成员的公共AP
@Inherited//标记注解，@Inherited阐述了某个被标注的类型是被继承的
public @interface Autowired {
    String value() default "";
}