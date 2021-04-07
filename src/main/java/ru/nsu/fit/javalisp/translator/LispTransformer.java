package ru.nsu.fit.javalisp.translator;

import javassist.*;
import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.handlers.*;

import java.util.*;

public class LispTransformer {

	private ClassPool pool;

	private HashMap<String, FunctionDescriptor> nameToDesc;
	private HashMap<String, FunctionDescriptor> nameToDummy;
	private BasicHandler startingHandler;
	private BasicHandler lastHandler;
	private List<Context> contexts;

	private CtClass cc;

	private Context globalContext;
	private int methods = 0;

	private CtMethod lMain;
	private CtMethod lConstructor;
	private int localVars = 0;
	private int varsInInit = 0;

	public LispTransformer() throws Exception{
		pool = ClassPool.getDefault();
		cc = pool.get("ru.nsu.fit.javalisp.translator.Source");
		cc.setName("LispSource");


		contexts = new ArrayList<>();
		nameToDesc = new HashMap<>();
		globalContext = new Context();

		nameToDummy = new HashMap<>();

		nameToDesc.put("+", new FunctionDescriptor("add", 2));
		nameToDesc.put("-", new FunctionDescriptor("sub", 2));
		nameToDesc.put("*", new FunctionDescriptor("mul", 2));
		nameToDesc.put("/", new FunctionDescriptor("div", 2));
		nameToDesc.put(":float", new FunctionDescriptor("castD", 1));
		nameToDesc.put(":string", new FunctionDescriptor("castS", 1));
		nameToDesc.put(":int", new FunctionDescriptor("castI", 1));
		nameToDesc.put("not", new FunctionDescriptor("not", 1));
		nameToDesc.put("and", new FunctionDescriptor("and", 2));
		nameToDesc.put("or", new FunctionDescriptor("or", 2));
		nameToDesc.put("=", new FunctionDescriptor("isEqual", 2));
		nameToDesc.put("<", new FunctionDescriptor("isLess", 2));
		nameToDesc.put("<=", new FunctionDescriptor("isLeq", 2));
		nameToDesc.put(">", new FunctionDescriptor("isGt", 2));
		nameToDesc.put(">=", new FunctionDescriptor("isGte", 2));
		nameToDesc.put("read", new FunctionDescriptor("read", 0));
		nameToDesc.put("print", new FunctionDescriptor("print", 1));
		nameToDesc.put("println", new FunctionDescriptor("println", 1));
		nameToDesc.put("list", new FunctionDescriptor("list", -1));
		nameToDesc.put("range", new FunctionDescriptor("range", 2));
		nameToDesc.put("head", new FunctionDescriptor("head", 1));
		nameToDesc.put("tail", new FunctionDescriptor("tail", 1));
		nameToDesc.put("reverse", new FunctionDescriptor("reverse", 1));
		nameToDesc.put("get", new FunctionDescriptor("get", 2));;
		nameToDesc.put("concat", new FunctionDescriptor("concat", 2));
		nameToDesc.put("con", new FunctionDescriptor("con", 2));
		nameToDesc.put("empty?", new FunctionDescriptor("empty", 1));


		LeafHandler leafHandler = new LeafHandler  (nameToDesc, nameToDummy);
		ApplyJHandler  jHandler = new ApplyJHandler(nameToDesc, nameToDummy);
		ApplyFHandler  fHandler = new ApplyFHandler(nameToDesc, nameToDummy);
		IfHandler     ifHandler = new IfHandler    (nameToDesc, nameToDummy);
		WrapHandler wrapHandler = new WrapHandler  (nameToDesc, nameToDummy);
		LetHandler   letHandler = new LetHandler   (nameToDesc, nameToDummy);
		ApplyVHandler  vHandler = new ApplyVHandler(nameToDesc, nameToDummy);
		DoHandler     doHandler = new DoHandler    (nameToDesc, nameToDummy);
		ApplyLHandler lHandler = new ApplyLHandler (nameToDesc, nameToDummy);
		LambdaHandler lambdaHandler = new LambdaHandler(nameToDesc, nameToDummy, cc, pool);
		ApplyLmbHandler lmbHandler = new ApplyLmbHandler(nameToDesc, nameToDummy);


		jHandler.setStartingHandler(leafHandler);
		letHandler.setStartingHandler(leafHandler);
		fHandler.setStartingHandler(leafHandler);
		ifHandler.setStartingHandler(leafHandler);
		wrapHandler.setStartingHandler(leafHandler);
		vHandler.setStartingHandler(leafHandler);
		doHandler.setStartingHandler(leafHandler);
		lHandler.setStartingHandler(leafHandler);
		lmbHandler.setStartingHandler(leafHandler);
		lambdaHandler.setStartingHandler(leafHandler);

		leafHandler.setNextHandler(jHandler);
		jHandler.setNextHandler(letHandler);
		letHandler.setNextHandler(ifHandler);
		ifHandler.setNextHandler(fHandler);
		fHandler.setNextHandler(vHandler);
		vHandler.setNextHandler(doHandler);
		doHandler.setNextHandler(lmbHandler);
		lmbHandler.setNextHandler(lambdaHandler);
		lambdaHandler.setNextHandler(lHandler);
		lHandler.setNextHandler(wrapHandler);

		Context global = new Context();
		contexts.add(global);

		startingHandler = leafHandler;
	}

	private boolean isValidSource(TranslationResult result){
		return result.isSuccess() && result.getValue() == TranslationResult.Value.SOURCE;
	}

	private String parseArgs(Node args, Context context) throws Exception {
		int id = 1;
		StringBuilder argsv = new StringBuilder();
		for (var x : args.getSubNodes()){
			if (x.getType() != Node.Type.VARIABLE){
				throw new Exception("Invalid function definition");
			}
			TranslationEntry entry = new TranslationEntry.Builder().setName("$" + id).setType(TranslationEntry.Type.VARIABLE).build();
			context.add(x.getResult(), entry);
			argsv.append("Object ").append(x.getResult());
			if (id != args.getSubNodes().size()){
				argsv.append(", ");
			}
			id++;
		}
		return argsv.toString();
	}

	private Pair<Pair<String, Integer>, Integer> parseMatched(Node args, int created, Context context) throws Exception {
		int id = 0;
		int cnt = 0;
		int auxVars = 0;
		String var = "_LOCAL_VAR_" + (created + auxVars);
		auxVars++;
		StringBuilder init = new StringBuilder();
		StringBuilder cond = new StringBuilder();
		cond.append("if (");
		for (var x : args.getSubNodes()){
			id++;
			try{
				var t = startingHandler.evalNode(context, x, created, var);
				if (!isValidSource(t)) throw new Exception("Can't build function");
				init.append(t.getSrc());
				if (cnt > 0) cond.append(" && ");
				cond.append("$").append(id).append(".equals(").append(var).append(")");
				auxVars += t.getUsedVars();
				var = "_LOCAL_VAR_" + (created + auxVars);
				cnt++;
			}
			catch (Exception e){
				if (x.getType() != Node.Type.VARIABLE) throw new Exception("Unable to evaluate parameter value");
				if (!(x.getResult().equals("_") || x.getResult().equals(context.getInverse("$" + id)))) throw new Exception("Invalid parameter value");
			}
		}
		cond.append(")");
		return new Pair<>(new Pair<>(init.append(cond.toString()).toString(), id), auxVars);
	}

	private void createNewVar(Node node, CtClass cc) throws Exception {
		if (node.getSubNodes().size() != 3) throw new Exception("Invalid global variable definition");
		if (node.getSubNodes().get(1).getType() != Node.Type.VARIABLE) throw new Exception("Invalid variable name");
		String name = node.getSubNodes().get(1).getResult();
		String name1 = "_GLOBAL_VAR_" + contexts.get(0).size();
		//if (contexts.get(0).containsVar(name)) throw new Exception("Variable " + name + " is already defined.");

		var t = startingHandler.evalNode(contexts.get(0), node.getSubNodes().get(2), varsInInit, name1);
		if (!t.isSuccess()) throw new Exception("Can't build");
		if (t.getValue() == TranslationResult.Value.SOURCE) {
			CtField field = new CtField(pool.get("java.lang.Object"), name1, cc);
			cc.addField(field);

			TranslationEntry entry = new TranslationEntry.Builder().setName(name1).setType(TranslationEntry.Type.VARIABLE).build();
			contexts.get(0).add(name, entry);

			for (int i = varsInInit; i < varsInInit + t.getUsedVars(); i++){
				lConstructor.addLocalVariable("_LOCAL_VAR_" + i, pool.get("java.lang.Object"));
			}
			varsInInit += t.getUsedVars();
			lConstructor.insertAfter(t.getSrc());
		}
		else {
			TranslationEntry.Builder builder = new TranslationEntry.Builder();
			builder.setName(t.getFuncName()).setArity(t.getParamsNumber()).setType(TranslationEntry.Type.FUNCTION);
			for (var x : t.getParams())
				builder.addArg(x);
			contexts.get(0).add(name, builder.build());
		}
	}

	private void createMethod(Node node, CtClass cc) throws Exception{
		if (node.getSubNodes().size() < 4 || node.getSubNodes().size() % 2 != 0) throw new Exception("Invalid number of params in function definition");
		if (node.getSubNodes().get(1).getType() != Node.Type.VARIABLE) throw new Exception("Invalid function name");

		String name = node.getSubNodes().get(1).getResult();

		CtMethod method = null;
		Context context = contexts.get(0).clone();
		int cnts = node.getSubNodes().size();

		String args = parseArgs(node.getSubNodes().get(cnts - 2), context);
		//contexts.add(context);

		int argc = node.getSubNodes().get(cnts - 2).getSubNodes().size();


		if (nameToDesc.containsKey(name)) throw new Exception("Function " + name + " redefinition");
		if (nameToDummy.containsKey(name)){
			if (nameToDummy.get(name).getArgsCount() != argc) throw new Exception("Invalid function definition");
			FunctionDescriptor desc = nameToDummy.get(name);
			nameToDummy.remove(name);
			method = cc.getDeclaredMethod(desc.getName());
			nameToDesc.put(name, desc);
		}
		else {
			String header = "public Object method_" +  methods + "(" + args + "){\nreturn null;\n}\n";
			method = CtNewMethod.make(header, cc);
			cc.addMethod(method);
			nameToDesc.put(name, new FunctionDescriptor("method_" + methods, node.getSubNodes().get(2).getSubNodes().size()));
			methods++;
		}

		List<String> patternSource = new LinkedList<>();
		int cnt = 0;


		for (int i = 2; i + 2 < cnts; i+= 2){
			var x = parseMatched(node.getSubNodes().get(i), cnt, context);
			cnt += x.second;
			patternSource.add(x.first.first);
			if (x.first.second != argc)
				throw new Exception("Invalid function definition");
		}

		StringBuilder src = new StringBuilder();

		String result = "_LOCAL_VAR_" + cnt;
		for (int i = 0; i < patternSource.size(); i++){
			src.append(patternSource.get(i));
			src.append("{\n");
			var t = startingHandler.evalNode(context, node.getSubNodes().get(3 + 2 * i), cnt, result);
			if (!isValidSource(t)) throw new Exception("Can't build function");
			src.append(t.getSrc());
			src.append("return ").append(result).append(";\n}\n");
		}

		var t = startingHandler.evalNode(context, node.getSubNodes().get(cnts - 1), cnt, result);

		for (int i = 0; i < cnt + t.getUsedVars(); i++) {
			method.addLocalVariable("_LOCAL_VAR_" + i, pool.get("java.lang.Object"));
		}

		src.append(t.getSrc());
		src.append("{ return ").append(result).append("; }\n");

		//System.out.println(src.toString());
		method.insertBefore(src.toString());

		//contexts.remove(contexts.size() - 1);
	}


	private void createDummy(Node node, CtClass cc) throws Exception {
		if (node.getSubNodes().size() != 3) throw new Exception("Invalid number of params in function declaration");
		if (node.getSubNodes().get(1).getType() != Node.Type.VARIABLE) throw new Exception("Invalid function name");
		String name = node.getSubNodes().get(1).getResult();
		if (nameToDummy.containsKey(name) || nameToDesc.containsKey(name)) throw new Exception("Function " + name + " is already declared");

		Context context = new Context();
		String args = parseArgs(node.getSubNodes().get(2), context);
		String header = "public Object method_" +  methods + "(" + args + "){\nreturn null;\n}\n";
		CtMethod method = CtNewMethod.make(header, cc);
		cc.addMethod(method);
		nameToDummy.put(name, new FunctionDescriptor("method_" + methods, node.getSubNodes().get(2).getSubNodes().size()));
		methods++;
	}

	private boolean containsName(Node node, String name){
		boolean contains = false;
		if (node.getType() == Node.Type.COMPLEX){
			for (var t : node.getSubNodes()){
				contains |= containsName(t, name);
			}
		}
		else if (node.getResult().equals(name)){
			contains = true;
		}
		return contains;
	}

	private boolean isTailCall(Node call, String name, int argc){
		if (call.getType() != Node.Type.COMPLEX) return false;
		Node func = call.getSubNodes().get(0);
		if (func.getType() != Node.Type.VARIABLE) return false;
		if (!func.getResult().equals(name)) return false;
		if (call.getSubNodes().size() != argc + 1) return false;
		boolean contains = false;
		for (int i = 1; i < func.getSubNodes().size(); i++){
			if (containsName(func.getSubNodes().get(i), name)){
				contains = true;
			}
		}
		return !contains;
	}

	private boolean isTailRec(Node node){

		if (node.getSubNodes().get(1).getType() != Node.Type.VARIABLE) return false;
		String name = node.getSubNodes().get(1).getResult();

		int cnt = node.getSubNodes().size();

		if (cnt % 2 != 0) return false;

		Context context = new Context();

		try {
			parseArgs(node.getSubNodes().get(cnt - 2), context);
		}
		catch (Exception e){
			return false;
		}
		//contexts.add(context);
		int argc = node.getSubNodes().get(cnt - 2).getSubNodes().size();
		for (int i = 2; i + 2 < cnt; i+= 2){
			try {
				var x = parseMatched(node.getSubNodes().get(i), 0, context);
				if (x.first.second != argc){
					//contexts.remove(contexts.size() - 1);
					return false;
				}
			}
			catch (Exception e){
		//		contexts.remove(contexts.size() - 1);
				return false;
			}
		}
		//contexts.remove(contexts.size() - 1);

		for (int i = 3; i + 2 < cnt; i+=2){
			if (containsName(node.getSubNodes().get(i), name)) return false;
		}

		Node call = node.getSubNodes().get(cnt - 1);


		return isTailCall(call, name, context.size());
	}

	private void createTailRec(Node node, CtClass cc) throws Exception {

		String name = node.getSubNodes().get(1).getResult();

		CtMethod method = null;
		Context context = contexts.get(0).clone();
		int cnts = node.getSubNodes().size();

		String args = parseArgs(node.getSubNodes().get(cnts - 2), context);
		//contexts.add(context);

		int argc = node.getSubNodes().get(cnts - 2).getSubNodes().size();

		if (nameToDesc.containsKey(name)) throw new Exception("Function " + name + " redefinition");
		if (nameToDummy.containsKey(name)){
			if (nameToDummy.get(name).getArgsCount() != argc) throw new Exception("Invalid function definition");
			FunctionDescriptor desc = nameToDummy.get(name);
			nameToDummy.remove(name);
			method = cc.getDeclaredMethod(desc.getName());
			nameToDesc.put(name, desc);
		}
		else {
			String header = "public Object method_" +  methods + "(" + args + "){\nreturn null;\n}\n";
			method = CtNewMethod.make(header, cc);
			cc.addMethod(method);
			nameToDesc.put(name, new FunctionDescriptor("method_" + methods, node.getSubNodes().get(cnts - 2).getSubNodes().size()));
			methods++;
		}

		int cnt = 0;
		String result = "_LOCAL_VAR_" + cnt;

		List<String> patternSource = new LinkedList<>();

		for (int i = 2; i + 2 < cnts; i+= 2){
			var x = parseMatched(node.getSubNodes().get(i), cnt, context);
			cnt += x.second;
			patternSource.add(x.first.first);
			if (x.first.second != argc)
				throw new Exception("Invalid function definition");
		}

		StringBuilder src = new StringBuilder();
		src.append("while (true){\n");

		for (int i = 0; i < patternSource.size(); i++){
			src.append(patternSource.get(i));
			src.append("{\n");
			var t = startingHandler.evalNode(context, node.getSubNodes().get(3 + 2 * i), cnt, result);
			if (!isValidSource(t)) throw new Exception("Can't build function");
			src.append(t.getSrc());
			src.append("return ").append(result).append(";\n}\n");
		}

		List<String> vars = new ArrayList<>();

		for (int i = 1; i < node.getSubNodes().get(cnts - 1).getSubNodes().size(); i++) {
			Node node1 = node.getSubNodes().get(cnts - 1).getSubNodes().get(i);
			String result1 = "_LOCAL_VAR_" + cnt;
			cnt++;
			var t = startingHandler.evalNode(context, node1, cnt, result1);
			if (!t.isSuccess() || t.getValue() != TranslationResult.Value.SOURCE) throw new Exception("Can't build function");
			cnt += t.getUsedVars();
			src.append(t.getSrc());
			vars.add(result1);
		}
		for (int i = 0; i < cnt; i++) {
			method.addLocalVariable("_LOCAL_VAR_" + i, pool.get("java.lang.Object"));
		}

		for (int i = 0; i < vars.size(); i++) {
			src.append("").append("$").append(i + 1).append(" = ").append(vars.get(i)).append(";\n");
		}
		src.append("}\n");
		//System.out.println(src.toString());
		method.insertBefore(src.toString());

		//contexts.remove(contexts.size() - 1);
	}

	private void processBlock(Node node, CtClass cc) throws Exception{
		//Function definition
		if (node.getSubNodes().get(0).getResult().equals("defun")){
			if (isTailRec(node)){
				createTailRec(node, cc);
			}
			else{
				createMethod(node, cc);
			}
		}
		else if (node.getSubNodes().get(0).getResult().equals("decl")){
			createDummy(node, cc);
		}
		//Global variable
		else if (node.getSubNodes().get(0).getResult().equals("def")){
			createNewVar(node, cc);
		}
		//Main source
		else{
			String v = "_LOCAL_VAR_" + localVars;
			lMain.addLocalVariable(v, pool.get("java.lang.Object"));
			localVars++;
			var t = startingHandler.evalNode(contexts.get(0), node, localVars, v);
			if (!t.isSuccess() || t.getValue() != TranslationResult.Value.SOURCE) throw new Exception("Cannot compile");
			for (int i = localVars; i < localVars + t.getUsedVars(); i++) {
				lMain.addLocalVariable("_LOCAL_VAR_" + i, pool.get("java.lang.Object"));
			}
			localVars += t.getUsedVars();
			lMain.insertAfter(t.getSrc());
			lMain.insertAfter("{System.out.println(" + v + ");}\n");

		}
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


			lMain = CtNewMethod.make("public void evaluate() throws Exception {\n}", cc);
			lConstructor = CtNewMethod.make("public void initGlobals() throws Exception{\n}", cc);
			for (var t : nodes){
				processBlock(t, cc);
			}

			if (nameToDummy.size() > 0) throw new Exception("Some functions are undefined");

			cc.addMethod(lMain);
			cc.addMethod(lConstructor);

			CtMethod method1 = CtNewMethod.make("public static void main(String[] args){}", cc);


			method1.addLocalVariable("source", pool.get("LispSource"));
			method1.insertAfter("source = new LispSource(); ");
			method1.insertAfter("source.initGlobals();");
			method1.insertAfter("source.evaluate(); ");
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
