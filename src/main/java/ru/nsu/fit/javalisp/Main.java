package ru.nsu.fit.javalisp;

import ru.nsu.fit.javalisp.parser.LispParser;
import ru.nsu.fit.javalisp.parser.Parser;
import ru.nsu.fit.javalisp.translator.LispTransformer;

import java.lang.reflect.Method;
import java.util.List;

public class Main {
/*
	public Object add(List<Object> params) throws Exception {
		Integer ans = 0;
		for (var x : params){
			ans += (Integer)x; }
		return ans;
	}*/

	public static void main(String[] args) throws Exception
	{
//		File text = new File(args[0]);

		Parser parser = new LispParser(args[0]);

		List<Token> tokenList = parser.parse();

		LispTransformer transformer = new LispTransformer();

		Class clazz = transformer.generate(tokenList);

		Object source = clazz.newInstance();
		Method method = clazz.getDeclaredMethod("evaluate");
		method.invoke(source);
	}
}
