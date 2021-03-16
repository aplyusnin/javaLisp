package ru.nsu.fit.javalisp.translator;

import javassist.*;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class LispTransformer {

	private ClassPool pool;

	private HashMap<String, FunctionDescriptor> nameToDesc;

	private Context globalContext;
	private int methods = 0;

	private CtMethod lMain;
	private int localVars = 0;

	public LispTransformer()
	{
		pool = ClassPool.getDefault();
		pool.importPackage("java.util.List");
		nameToDesc = new HashMap<>();
		globalContext = new Context();
	}

	private void add(CtClass lispSource) throws Exception
	{
		nameToDesc.put("+", new FunctionDescriptor("add", 2));
		CtMethod method = CtNewMethod.make("private Object add(Object a, Object b) throws Exception {\n" +
				                                   " return (Object)(new Double(((Double)$1).doubleValue() + ((Double)$2).doubleValue()));\n}", lispSource);
		lispSource.addMethod(method);
	}

	private void sub(CtClass lispSource) throws Exception
	{
		nameToDesc.put("-", new FunctionDescriptor("sub", 2));
		CtMethod method = CtNewMethod.make("private Object sub(Object a, Object b) throws Exception {\n" +
				                                   " return (Object)(new Double(((Double)$1).doubleValue() - ((Double)$2).doubleValue()));\n}", lispSource);
		//method.insertAt(0, true, "{}");
		lispSource.addMethod(method);
	}


	private void fill(CtClass lispSource) throws Exception
	{
		add(lispSource);
		sub(lispSource);
	}

	private String apply(String functionName, List<String> args, String var) throws Exception{
		FunctionDescriptor desc = nameToDesc.get(functionName);
		if (args.size() != desc.getArgsCount()) throw new Exception();
		StringBuilder source = new StringBuilder();
		source.append("{ " + var + " = " + desc.getName() + "(");
		int left = args.size();
		for (var x : args){
			left--;
			source.append(x);
			if (left > 0) source.append(",");
			source.append(" ");
		}
		source.append(");}");
		return source.toString();
	}

	private CtMethod createMethod(String methodName, List<String> args, Token body, CtClass cc) throws Exception{
		StringBuilder builder = new StringBuilder();
		Context context = new Context();
		int id = 1;
		builder.append("private Object ").append(methodName).append("(");
		for (var x : args){
			context.add(x, id);
			builder.append(x);
			id++;
			if (id < args.size()) builder.append(", ");
		}
		builder.append("){\nreturn null;\n}");

		CtMethod method = CtNewMethod.make(builder.toString(), cc);

		return method;
	}

	private String parseArgs(Token args, Context context) throws Exception {
		int id = 1;
		StringBuilder argsv = new StringBuilder();
		for (var x : args.getSubTokens()){
			if (!x.getResult().equals("")) {
				context.add(x.getResult(), id);
				argsv.append("Object ").append(x.getResult());
			}
			else throw new Exception("Invalid function definition");
			if (id != args.getSubTokens().size()){
				argsv.append(", ");
			}
			id++;
		}
		return argsv.toString();
	}
	private Pair<Integer, String> evaluateToken(CtMethod method, Token token, Context context, int created) throws Exception {
		int cnt = 0;
		String var;
		if (token.getType() == Token.Type.INT){
			var = "LOCAL_VAR_" + created;
			cnt++;
			method.addLocalVariable(var, pool.get("java.lang.Object"));
			method.insertAfter("{ " + var + " = new Double(Double.parseDouble(\"" + token.getResult() + "\"));}");
		}
		else if (token.getType() == Token.Type.FLOAT){
			var = "LOCAL_VAR_" + created;
			cnt++;
			method.addLocalVariable(var, pool.get("java.lang.Object"));
			method.insertAfter("{ " + var + " = new Double(Double.parseDouble(\"" + token.getResult() + "\"));}");
		}
		else if (token.getType() == Token.Type.STRING){
			var = "LOCAL_VAR_" + created;
			cnt++;
			method.addLocalVariable(var, pool.get("java.lang.Object"));
			method.insertAfter("{ " + var + " = " + token.getResult() + ";}");
		}
		else if (token.getType() == Token.Type.VARIABLE){
			if (context.containsVar(token.getResult())){
				var = "LOCAL_VAR_" + created;
				cnt++;
				method.addLocalVariable(var, pool.get("java.lang.Object"));
				method.insertAfter("{ " + var + " = " + context.getVar(token.getResult()) + ";}");
			}
			else{
				throw new Exception("Unknown variable: " + token.getResult());
			}
		}
		else{
			if (token.getSubTokens().get(0).getType() != Token.Type.VARIABLE){
				throw new Exception("Waited for function name, found other type");
			}
			if (!nameToDesc.containsKey(token.getSubTokens().get(0).getResult())){
				throw new Exception("Unknown function name");
			}
			FunctionDescriptor desc = nameToDesc.get(token.getSubTokens().get(0).getResult());
			if (token.getSubTokens().size() != desc.getArgsCount() + 1){
				throw new Exception("Invalid number of args, expected " + desc.getArgsCount() + ", found " + (token.getSubTokens().size() - 1));
			}
			List<String> vars = new LinkedList<>();
			var = "LOCAL_VAR_" + created;
			method.addLocalVariable(var, pool.get("java.lang.Object"));
			cnt++;
			for (int i = 1; i < token.getSubTokens().size(); i++){
				var x = evaluateToken(method, token.getSubTokens().get(i), context, created + cnt);
				cnt += x.first;
				vars.add(x.second);
			}
			String res = apply(token.getSubTokens().get(0).getResult(), vars, var);
			method.insertAfter(res);
		}
		return new Pair<>(cnt, var);
	}


	private CtMethod buildMethod(CtClass cc, Token function) throws Exception {
		if (function.getSubTokens().size() != 4) throw new Exception("Invalid number of params in ");
		String name = function.getSubTokens().get(1).getResult();
		if (nameToDesc.containsKey(name)) throw new Exception("Function " + name + " redefinition");
		Context context = new Context();
		String args = parseArgs(function.getSubTokens().get(2), context);

		CtMethod method = CtNewMethod.make("public Object method_" + name + "_" + methods + "(" + args + "){\nreturn null;\n}\n", cc);
		nameToDesc.put(name, new FunctionDescriptor("method_" + name + "_" + methods, function.getSubTokens().get(2).getSubTokens().size()));
		cc.addMethod(method);
		var x = evaluateToken(method, function.getSubTokens().get(3), context, 0);
		method.insertAfter("{return " + x.second + ";}");

		methods++;
		return method;
	}

	private void processBlock(Token token, CtClass cc) throws Exception{
		//Function definition
		if (token.getSubTokens().get(0).getResult().equals("defun")){
			CtMethod method = buildMethod(cc, token);
			//cc.addMethod(method);
		}
		//Global variable
		else if (token.getSubTokens().get(0).getResult().equals("defn")){

		}
		//Main source
		else{
			var x = evaluateToken(lMain, token, new Context(), localVars);
			localVars += x.first;
			lMain.insertAfter("{System.out.println(" + x.second + ");}");
		}
	}

	public Class generate(List<Token> tokens) throws CannotCompileException
	{
		try
		{
			//CtClass cc = pool.get("Source");
			//cc.setName("LispSource");
			//cc.writeFile();
			CtClass cc = pool.makeClass("LispSource");

			cc.addConstructor(CtNewConstructor.make("public " + cc.getName() + "() {}", cc));

			fill(cc);

			lMain = CtNewMethod.make("public void evaluate() throws Exception {\n}", cc);;

			for (var t : tokens){
				processBlock(t, cc);
			}
			/*CtMethod m = buildMethod(cc, tokens.get(0));

			cc.addMethod(m);*/

//			CtMethod method = cc.getDeclaredMethod("evaluate");
		/*	lMain.addLocalVariable("a1", pool.get("java.lang.Double"));
			lMain.addLocalVariable("b1", pool.get("java.lang.Double"));
			lMain.addLocalVariable("c1", pool.get("java.lang.Double"));


			lMain.insertAfter("{a1 = new Double(25.0);}");
			lMain.insertAfter("{b1 = new Double(15.0);}");
			lMain.addLocalVariable("d1", pool.get("java.lang.Object"));
			lMain.addLocalVariable("e1", pool.get("java.lang.Object"));

			List<String> args = new LinkedList<>();
			args.add("a1");
			args.add("b1");

			lMain.insertAfter(apply("+", args, "c1"));

			args = new LinkedList<>();
			args.add("b1");
			args.add("a1");

			lMain.insertAfter(apply("-", args, "d1"));

			args = new LinkedList<>();
			args.add("c1");
			args.add("d1");
			lMain.insertAfter(apply("+", args, "e1"));

			lMain.insertAfter("{System.out.println(e1);}");*/

			cc.addMethod(lMain);

			CtMethod method1 = CtNewMethod.make("public static void main(String[] args){}", cc);


			method1.addLocalVariable("source", pool.get("LispSource"));
			method1.insertAfter("{source = new LispSource(); }");
			method1.insertAfter("{source.evaluate(); }");
			cc.addMethod(method1);

			try
			{
				cc.writeFile();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return cc.toClass();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
