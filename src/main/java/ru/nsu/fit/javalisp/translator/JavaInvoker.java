package ru.nsu.fit.javalisp.translator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to work with java-calls
 */
public class JavaInvoker {

	/**
	 * Get function name from full name
	 * @param name - full name
	 * @return function name
	 */
	public static String funcName(String name){
		StringBuilder builder = new StringBuilder();
		int pos = name.length();
		pos --;
		while (pos >= 0 && name.charAt(pos) != '.'){
			builder.append(name.charAt(pos));
			pos--;
		}
		builder.reverse();
		return builder.toString();
	}
	/**
	 * Get full class name from full name
	 * @param name - full name
	 * @return full class name
	 */
	public static String packName(String name){
		int pos = name.length();
		pos --;
		while (pos >= 0 && name.charAt(pos) != '.') pos--;
		if (pos == -1) return "";
		else return name.substring(0, pos);
	}

	/**
	 * Get return value of function
	 * @param name full name
	 * @return function return value type
	 */
	public static String returnValue(String name)
	{
		String pack = packName(name);
		String func = funcName(name);
		try {
			Class type = Class.forName(pack);
			Method m = type.getDeclaredMethod(func);
			return m.getReturnType().toString();
		}
		catch (Exception e){
			return "";
		}
	}
	public static List<String> getArgsTypes(String name){
		String pack = packName(name);
		String func = funcName(name);
		try{
			Class clazz = Class.forName(pack);
			Method[] mts = clazz.getMethods();
			Method m = clazz.getMethod(func);
			return Arrays.stream(m.getParameterTypes()).map(Class::toString).collect(Collectors.toList());
		}
		catch (Exception e){
			return null;
		}
	}
}
