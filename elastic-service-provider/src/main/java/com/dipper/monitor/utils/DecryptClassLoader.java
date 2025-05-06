package com.dipper.monitor.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

// 移除了 @Slf4j 注解
public class DecryptClassLoader extends URLClassLoader {

    private static final String DECRYPT_CLASS_PATH_PREFIX = "decryptClassPrefix";
    private static final String IGNORED_CLASS = "encryptClassPrefix";
    private static final String LICENSE_PROPERTY = "license";
    private static final String LICENSE_ENV_KEY = "SECURITY_LICENSE";

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY = "U0HmWThUnzd8i3JT"; // 加密时使用的密钥
    private static final String IV = "1234567890123456";  // 初始化向量 (IV)

    private final String prefix;
    private List<String> skipClass;
    private String classPath;
    private String ignoreClass;
    private final ClassLoader parent;
    private volatile String fingerPrints;
    private static final String DEFAULT_CLASS_PATH = "!/BOOT-INF/classes!/";

    public DecryptClassLoader(Class<?> clazz, ClassLoader contextClassLoader) {
        this(clazz.getPackage().getName(), contextClassLoader, "!/BOOT-INF/classes!/", null, clazz.getName());
    }

    public DecryptClassLoader(String prefix, ClassLoader contextClassLoader, String classPath, List<String> skipClass, String ignoreClass) {
        super(getUrls(contextClassLoader), contextClassLoader);
        this.classPath = classPath != null ? classPath : DEFAULT_CLASS_PATH;
        this.skipClass = skipClass;
        this.ignoreClass = ignoreClass;
        this.parent = contextClassLoader;
        this.prefix = prefix;
        System.setProperty(DECRYPT_CLASS_PATH_PREFIX, classPath + prefix.replace(".", File.separator));
        System.setProperty(IGNORED_CLASS, ignoreClass.replace(".", File.separator) + ".class");

        System.out.println("初始化DecryptClassLoader: prefix=" + prefix + ", classPath=" + classPath + ", ignoreClass=" + ignoreClass);
    }

    private static URL[] getUrls(ClassLoader contextClassLoader) {
        System.out.println("开始获取类加载器的资源路径...");

        // 如果 contextClassLoader 是 URLClassLoader 类型，则直接提取其 URLs
        if (contextClassLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) contextClassLoader).getURLs();
            if (urls != null && urls.length > 0) {
                System.out.println("成功获取到 " + urls.length + " 个资源路径:");
                for (URL url : urls) {
                    System.out.println("资源路径: " + url.toString());
                }
                return urls;
            } else {
                System.out.println("未从 contextClassLoader 中获取到任何资源路径");
            }
        } else {
            System.out.println("当前 contextClassLoader 不是 URLClassLoader 类型，尝试通过其他方式获取资源路径...");
        }

        // 如果 contextClassLoader 不是 URLClassLoader 类型，则尝试通过 ClassLoader 的 getResource 方法获取路径
        try {
            URL mainResource = contextClassLoader.getResource("");
            if (mainResource != null) {
                System.out.println("通过 getResource 获取到主资源路径: " + mainResource.toString());
                return new URL[]{mainResource};
            } else {
                System.out.println("无法通过 getResource 获取资源路径");
            }
        } catch (Exception e) {
            System.out.println("获取资源路径时发生异常");
            e.printStackTrace();
        }

        // 如果仍然无法获取路径，则返回空数组
        System.out.println("未能获取到任何资源路径，返回空数组");
        return new URL[0];
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return this.loadClass(name, resolve, false);
    }

    public Class<?> loadClass(String name, boolean resolve, boolean forceDecrypt) throws ClassNotFoundException {
        if (name.startsWith(prefix)) {
            System.out.println("尝试加载类: " + name);
        }
        System.out.println("当前Logger的类加载器:" + Logger.class.getClassLoader());
        System.out.println("尝试加载类:" + name);
        System.out.println("尝试加载类: " + name);
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            System.out.println("已加载类: " + name);
            return loadedClass;
        }

        try {
            if (forceDecrypt || needDecrypt(name)) {
                loadedClass = findClass(name);
                System.out.println("通过findClass加载类: " + name);
            } else {
                loadedClass = getParent().loadClass(name);
                System.out.println("通过父类加载器加载类: " + name);
            }
        } catch (ClassNotFoundException | ClassFormatError e) {
            loadedClass = super.loadClass(name, resolve);
            System.out.println("使用super加载类: " + name + ", 错误: " + e.getMessage());
        }

        if (resolve && loadedClass != null) {
            resolveClass(loadedClass);
            System.out.println("解析类: " + name);
        }
        return loadedClass;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!needDecrypt(name)) {
            throw new ClassNotFoundException(String.format("class %s not found.", name));
        }

        String baseName = name.substring(name.lastIndexOf('.') + 1);
        String[] fileNameElements = {name.substring(0, name.lastIndexOf(".")).replace(".", File.separator), baseName + ".class"};
        String finalName = String.join(File.separator, fileNameElements);

        System.out.println("准备解密文件: " + finalName);
        InputStream inputStream = getResourceAsStream(finalName, true); // 获取原始输入流
        if (inputStream == null) {
            throw new ClassNotFoundException(String.format("class %s not found. path is %s ", name, finalName));
        }

        byte[] data = decryptClass(inputStream, finalName); // 解密类文件
        if (data == null) {
            throw new ClassNotFoundException(String.format("class %s not found. path is %s ", name, finalName));
        }

        System.out.println("解密成功: " + name);
        return defineClass(name, data, 0, data.length);
    }

    private boolean needDecrypt(String name) {
        boolean result = name.startsWith(prefix) && !name.contains(ignoreClass);
        return result;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return this.getResourceAsStream(name, false);
    }

    public InputStream getResourceAsStream(String name, boolean returnDirect) {
        System.out.println("获取资源流: " + name);
        InputStream in = super.getResourceAsStream(name);
        if (returnDirect) {
            return in; // 直接返回原始输入流
        }

        // 不再在此处进行解密操作
        return in;
    }

    private byte[] decryptClass(InputStream inputStream, String finalName) {
        if (inputStream == null) {
            System.out.println("输入流为空 finalName:" + finalName);
            return null;
        }

        try (BufferedInputStream in = new BufferedInputStream(inputStream);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] temp = new byte[2048];
            int data;
            while ((data = in.read(temp)) != -1) {
                out.write(temp, 0, data);
            }
            byte[] bytes = out.toByteArray();
            if (bytes == null) {
                System.out.println("解密读取的信息为空 finalName:" + finalName);
                return null;
            }
            System.out.println("解密字节长度:" + bytes.length);
            byte[] decryptedBytes = com.dipper.maven.plugin.AESUtil.decrypt(bytes);
            if (decryptedBytes == null) {
                System.out.println("解密结果信息为空 finalName:" + finalName);
                return null;
            }
            System.out.println("解密完成 finalName:" + finalName + " " + decryptedBytes.length + " 字节的数据");
            return decryptedBytes;
        } catch (Exception e) {
            System.out.println("解密出错finalName:" + finalName);
            e.printStackTrace();
            return null;
        }
    }

}