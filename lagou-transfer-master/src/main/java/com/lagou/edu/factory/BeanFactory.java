package com.lagou.edu.factory;

import com.lagou.edu.annotation.Autowired;
import com.lagou.edu.annotation.Repository;
import com.lagou.edu.annotation.Service;
import com.lagou.edu.annotation.Transactional;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 应癫
 *
 * 工厂类，生产对象（使用反射技术）
 */
public class BeanFactory {

    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static Map<String,Object> map = new HashMap<>();  // 存储对象


    static {
        // 获取到项目的根路径
        String path = System.getProperty("user.dir") + "\\src" + "\\main" + "\\java";
        File files = new File(path);
        try {
            getClassFileName(files, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Map.Entry<String,Object> entry : map.entrySet()){
            autoWrite(entry);
            proxy(entry);
        }
    }

    private static void proxy(Map.Entry<String,Object> entry){
        Object obj = entry.getValue();
        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Transactional transactional = method.getAnnotation(Transactional.class);
            if (transactional != null){
                ProxyFactory proxyFactory = (ProxyFactory) map.get(transactional.value());
                Object jdkProxy = proxyFactory.getJdkProxy(obj);
                map.put(entry.getKey(), jdkProxy);
            }
        }
    }

    private static void autoWrite(Map.Entry<String,Object> entry){
        Object obj = entry.getValue();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            Autowired annotation = field.getAnnotation(Autowired.class);
            if (annotation != null){
                Object o = map.get(annotation.value());
                field.setAccessible(true);
                try {
                    field.set(obj, o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        map.put(entry.getKey(), obj);
    }

    private static void getClassFileName(File files, String directoryName) throws Exception {
        if (files != null) {
            if (directoryName == null) {
                directoryName = "";
            }
            String name = null;
            File[] listFiles = files.listFiles();
            if (listFiles != null) {
                for (int i = 0; i < listFiles.length; i++) {
                    if (listFiles[i].isDirectory()) {
                        // 为目录
                        name = listFiles[i].getName();
                        File files2 = new File(files.getPath() + "\\" + name);
                        if(directoryName.equals("")){
                            getClassFileName(files2, directoryName + name);
                        }else{
                            getClassFileName(files2, directoryName + "." + name);
                        }
                    } else {
                        // 不为目录
                        name = listFiles[i].getName();
                        name = name.substring(0, name.lastIndexOf("."));
                        if(directoryName.equals("")){
                            setMap(directoryName + name);
                        }else{
                            setMap(directoryName + "." + name);
                        }
                    }
                }
            }
        }
    }

    private static void setMap(String name) throws Exception{
        Class clazz = Class.forName(name);
        //查找此类上是否有此注解
        Service service = (Service) clazz.getAnnotation(Service.class);
        Repository repository = (Repository) clazz.getAnnotation(Repository.class);
        if(service != null){
            map.put(service.value(), clazz.newInstance());
        }else if (repository != null){
            map.put(repository.value(), clazz.newInstance());
        }
    }


    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static  Object getBean(String id) {
        return map.get(id);
    }

}
