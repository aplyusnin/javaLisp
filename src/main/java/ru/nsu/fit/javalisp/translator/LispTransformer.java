package ru.nsu.fit.javalisp.translator;

import javassist.*;
import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class LispTransformer {

	private ClassPool pool;

	private HashMap<String, FunctionDescriptor> nameToDesc;
	private HashMap<String, Method> keyWordToMethod;

	private Stack<Node> nodes;

	private Context globalContext;
	private int methods = 0;

	private CtMethod lMain;
	private CtMethod lConstructor;
	private int localVars = 0;
	private int varsInInit = 0;

	public LispTransformer()
	{
		pool = ClassPool.getDefault();
		nodes = new Stack<>();
		nameToDesc = new HashMap<>();
		globalContext = new Context();
		nameToDesc.put("+", new FunctionDescriptor("add", 2));
		nameToDesc.put("-", new FunctionDescriptor("sub", 2));
		nameToDesc.put("*", new FunctionDescriptor("mul", 2));
		nameToDesc.put("/", new FunctionDescriptor("div", 2));
		nameToDesc.put(":float", new FunctionDescriptor("castD", 1));
		nameToDesc.put(":int", new FunctionDescriptor("castI", 1));
		nameToDesc.put("not", new FunctionDescriptor("not", 1));
		nameToDesc.put("and", new FunctionDescriptor("and", 2));
		nameToDesc.put("or", new FunctionDescriptor("or", 2));
		nameToDesc.put("=", new FunctionDescriptor("isEqual", 2));
//		keyWordToMethod.put("if", LispTransformer.class.getDeclaredMethod("generateIf"));
	}

	private String applyFunc(String functionName, List<String> args, String var) throws Exception{
		FunctionDescriptor desc = nameToDesc.get(functionName);
		if (args.size() != desc.getArgsCount()) throw new Exception("Invalid number of params");
		StringBuilder source = new StringBuilder();
		source.append("{ ").append(var).append(" = ").append(desc.getName()).append("(");
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

	private String applyJavaFunc(String functionName, List<String> args, String var) throws Exception{

		String type = JavaInvoker.returnValue(functionName.substring(1));
		var types = JavaInvoker.getArgsTypes(functionName.substring(1));
		/*if (type.equals("")){
			throw new Exception("Invalid function name");
		}*/

		boolean isNew = false;
		StringBuilder source = new StringBuilder();
		if (type.equals("double") || type.equals("float")) {
			source.append("{ ").append(var).append(" = new Double(").append(functionName.substring(1)).append("(");
			isNew = true;
		}
		else if (type.equals("int") || type.equals("long")){
			source.append("{ ").append(var).append(" = new Integer(").append(functionName.substring(1)).append("(");
			isNew = true;
		}
		else {
			source.append("{ ").append(var).append(" = new Object();\n").append(functionName.substring(1)).append("(");
		}

		//source.append("{ ").append(var).append(" = ").append(functionName.substring(1)).append("(");
		int left = args.size();
		for (int i = 0; i < args.size(); i++){
			left--;
			if (types != null)
				source.append("(").append(types.get(i)).append(")");
			source.append(args.get(i));
			if (left > 0) source.append(",");
			source.append(" ");
		}
		if (isNew) source.append(")");
		source.append(");}");
		return source.toString();
	}

	private String parseArgs(Node args, Context context) throws Exception {
		int id = 1;
		StringBuilder argsv = new StringBuilder();
		for (var x : args.getSubNodes()){
			if (!x.getResult().equals("")) {
				context.add(x.getResult(), "$" + id);
				argsv.append("Object ").append(x.getResult());
			}
			else throw new Exception("Invalid function definition");
			if (id != args.getSubNodes().size()){
				argsv.append(", ");
			}
			id++;
		}
		return argsv.toString();
	}
	private Pair<Integer, String> evaluateNode(CtMethod method, Node node, Context context, int created) throws Exception {
		int cnt = 0;
		String varb;
		if (node.getType() == Node.Type.INT){
			var x = evaluateInt(method, node, context, created);
			cnt = x.first;
			varb = x.second;
		}
		else if (node.getType() == Node.Type.FLOAT){
			var x = evaluateFloat(method, node, context, created);
			cnt = x.first;
			varb = x.second;
		}
		else if (node.getType() == Node.Type.STRING){
			var x = evaluateString(method, node, context, created);
			cnt = x.first;
			varb = x.second;
		}
		else if (node.getType() == Node.Type.VARIABLE){
			var x = evaluateVariable(method, node, context, created);
			cnt = x.first;
			varb = x.second;
		}
		else{

			var x = evaluateComplex(method, node, context, created);
			cnt = x.first;
			varb = x.second;
		}
		return new Pair<>(cnt, varb);
	}


	private CtMethod buildMethod(CtClass cc, Node function) throws Exception {
		if (function.getSubNodes().size() != 4) throw new Exception("Invalid number of params in ");
		String name = function.getSubNodes().get(1).getResult();
		if (nameToDesc.containsKey(name)) throw new Exception("Function " + name + " redefinition");
		Context context = new Context();
		String args = parseArgs(function.getSubNodes().get(2), context);

		CtMethod method = CtNewMethod.make("public Object method_" + name + "_" + methods + "(" + args + "){\nreturn null;\n}\n", cc);
		nameToDesc.put(name, new FunctionDescriptor("method_" + name + "_" + methods, function.getSubNodes().get(2).getSubNodes().size()));
		cc.addMethod(method);
		var x = evaluateNode(method, function.getSubNodes().get(3), context, 0);
		method.insertAfter("{return " + x.second + ";}");

		methods++;
		return method;
	}
	private void createNewVar(Node node, CtClass cc) throws Exception {
		if (node.getSubNodes().get(1).getType() != Node.Type.VARIABLE) throw new Exception("Invalid word, expected var name");
		String trueName = node.getSubNodes().get(1).getResult();
		if (globalContext.containsVar(trueName)) throw new Exception("Variable " + trueName + " is already defined.");
		var x = evaluateComplex(lConstructor, node.getSubNodes().get(2), new Context(), varsInInit);
		varsInInit += x.first;
		String varName = "_GLOBAL_VAR_" + globalContext.size();
		CtField field = new CtField(pool.get("java.lang.Object"), varName, cc);
		cc.addField(field);
		globalContext.add(trueName, varName);
		lConstructor.insertAfter("{" + varName + " = " + x.second + ";}");
	}

	private void processBlock(Node node, CtClass cc) throws Exception{
		//Function definition
		if (node.getSubNodes().get(0).getResult().equals("defun")){
			CtMethod method = buildMethod(cc, node);
			//cc.addMethod(method);
		}
		//Global variable
		else if (node.getSubNodes().get(0).getResult().equals("defn")){
			createNewVar(node, cc);
		}
		//Main source
		else{
			var x = evaluateNode(lMain, node, new Context(), localVars);
			localVars += x.first;
			lMain.insertAfter("{System.out.println(" + x.second + ");}");
		}
	}

	private Pair<Integer, String> evaluateInt(CtMethod method, Node node, Context context, int created) throws Exception {
		String var;
		int cnt = 0;
		var = "LOCAL_VAR_" + created;
		cnt++;
		method.addLocalVariable(var, pool.get("java.lang.Object"));
		method.insertAfter("{ " + var + " = new Integer(" + node.getResult() + ");}");
		return new Pair<>(cnt, var);
	}
	private Pair<Integer, String> evaluateFloat(CtMethod method, Node node, Context context, int created) throws Exception {
		String var;
		int cnt = 0;
		var = "LOCAL_VAR_" + created;
		cnt++;
		method.addLocalVariable(var, pool.get("java.lang.Object"));
		method.insertAfter("{ " + var + " = new Double(" + node.getResult() + ");}");
		return new Pair<>(cnt, var);
	}
	private Pair<Integer, String> evaluateString(CtMethod method, Node node, Context context, int created) throws Exception {
		String var;
		int cnt = 0;
		var = "LOCAL_VAR_" + created;
		cnt++;
		method.addLocalVariable(var, pool.get("java.lang.Object"));
		method.insertAfter("{ " + var + " = new String(\"" + node.getResult() + "\");}");
		return new Pair<>(cnt, var);
	}
	private Pair<Integer, String> evaluateVariable(CtMethod method, Node node, Context context, int created) throws Exception {
		String var;
		int cnt = 0;
		var = "LOCAL_VAR_" + created;
		cnt++;
		if (node.getResult().equals("True")) {
			method.addLocalVariable(var, pool.get("java.lang.Object"));
			method.insertAfter("{ " + var + " = new Boolean(true);}");
		}
		else if (node.getResult().equals("False")) {
			method.addLocalVariable(var, pool.get("java.lang.Object"));
			method.insertAfter("{ " + var + " = new Boolean(false);}");
		}
		else if (context.containsVar(node.getResult())){
			method.addLocalVariable(var, pool.get("java.lang.Object"));
			method.insertAfter("{ " + var + " = " + context.getVar(node.getResult()) + ";}");
		}
		else if (globalContext.containsVar(node.getResult())){
			method.addLocalVariable(var, pool.get("java.lang.Object"));
			method.insertAfter("{ " + var + " = " + globalContext.getVar(node.getResult()) + ";}");
		}
		else if (nameToDesc.containsKey(node.getResult())){
			var t = nameToDesc.get(node.getResult());
			if (t.getArgsCount() == 0){
				method.addLocalVariable(var, pool.get("java.lang.Object"));
				method.insertAfter("{ " + var + " = " + t.getName() + "();}");
			}
		}
		else{
			throw new Exception("Unknown variable: " + node.getResult());
		}
		return new Pair<>(cnt, var);
	}


	private Pair<Integer, String> evaluateComplex(CtMethod method, Node node, Context context, int created) throws Exception {
		//If there is only 1 subnode
		String var;
		int cnt = 0;
		var = "LOCAL_VAR_" + created;
		cnt++;
		method.addLocalVariable(var, pool.get("java.lang.Object"));
		if (node.getSubNodes().size() == 0) throw new Exception("Empty brackets");
		if (node.getSubNodes().size() == 1) {
			Node node1 = node.getSubNodes().get(0);
			switch (node1.getType())
			{
				case VARIABLE -> {
					var x = evaluateVariable(method, node1, context, cnt + created);
					method.insertAfter("{ " + var + " = " + x.second + ";}");
					cnt += x.first;
				}
				case STRING -> {
					var x = evaluateString(method, node1, context, cnt + created);
					method.insertAfter("{ " + var + " = " + x.second + ";}");
					cnt += x.first;
				}
				case INT -> {
					var x = evaluateInt(method, node1, context, cnt + created);
					method.insertAfter("{ " + var + " = " + x.second + ";}");
					cnt += x.first;
				}
				case FLOAT -> {
					var x = evaluateFloat(method, node1, context, cnt + created);
					method.insertAfter("{ " + var + " = " + x.second + ";}");
					cnt += x.first;
				}
				case JAVACALL -> {
					String type = JavaInvoker.returnValue(node1.getResult().substring(1));
					if (type.equals("double") || type.equals("float")) {
						method.insertAfter("{ " + var + " = new Double(" + node1.getResult().substring(1) + "());}");
					}
					else if (type.equals("int") || type.equals("long")){
						method.insertAfter("{ " + var + " = new Integer(" + node1.getResult().substring(1) + "());}");
					}
					else if (!type.equals("")){
						method.insertAfter("{ " + var + " = " + node1.getResult().substring(1) + "();}");
					}
					else{
						throw new Exception("Invalid function type");
					}
				}
				case COMPLEX -> {
					var x = evaluateNode(method, node1, context, cnt + created);
					method.insertAfter("{ " + var + " = " + x.second + ";}");
					cnt += x.first;
				}
			}
		}
		else {
			Node func = node.getSubNodes().get(0);
			List<String> args = new LinkedList<>();

			for (int i = 1; i < node.getSubNodes().size(); i++){
				var tmp  =evaluateNode(method, node.getSubNodes().get(i), context, created + cnt);
				args.add(tmp.second);
				cnt += tmp.first;
			}

			if (func.getType() == Node.Type.JAVACALL){
				method.insertAfter(applyJavaFunc(func.getResult(), args, var));
			}
			else if (func.getType() == Node.Type.VARIABLE){
				method.insertAfter(applyFunc(func.getResult(), args, var));
			}
			else{
				throw new Exception("Expected defined or java function");
			}
		}
		return new Pair<>(cnt, var);
	}

	/**
	 * Generate bytecode from syntax forest
	 * @param nodes - syntax forest
	 * @return java class
	 * @throws CannotCompileException can't build code
	 */
	public Class generate(List<Node> nodes) throws CannotCompileException
	{
		try
		{
			CtClass cc = pool.get("ru.nsu.fit.javalisp.translator.Source");
			cc.setName("LispSource");

			lMain = CtNewMethod.make("public void evaluate() throws Exception {\n}", cc);
			lConstructor = CtNewMethod.make("public void initGlobals() throws Exception{\n}", cc);
			for (var t : nodes){
				processBlock(t, cc);
			}

			CtField field = new CtField(pool.get("java.lang.Integer"), "HUI_1", cc);

			cc.addField(field);

			cc.addMethod(lMain);
			cc.addMethod(lConstructor);
			//CtConstructor[] ccs = cc.getConstructors();
			//CtConstructor constructor = CtNewConstructor.make("public " + cc.getSimpleName() + "(){}", cc);//cc.getConstructor("LispSource");
			//cc.addConstructor(constructor);
			//constructor.insertAfter("{initGlobal();}");

			CtMethod method1 = CtNewMethod.make("public static void main(String[] args){}", cc);


			method1.addLocalVariable("source", pool.get("LispSource"));
			method1.insertAfter("{source = new LispSource(); }");
			method1.insertAfter("{source.initGlobals();}");
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
