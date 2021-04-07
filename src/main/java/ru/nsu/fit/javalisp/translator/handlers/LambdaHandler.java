package ru.nsu.fit.javalisp.translator.handlers;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;
import ru.nsu.fit.javalisp.translator.TranslationEntry;
import ru.nsu.fit.javalisp.translator.TranslationResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class LambdaHandler extends BasicHandler{

	private static int lambdas = 0;
	private static CtClass cc;
	private static ClassPool pool;

	public LambdaHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy, CtClass cc, ClassPool pool){
		super(nameToDesc, nameToDummy);
		keyWord = "fn";
		LambdaHandler.cc = cc;
		LambdaHandler.pool = pool;
	}

	@Override
	protected TranslationResult generateSource(Context currentContext, Node node, int created, String resVar) throws Exception
	{
		if (node.getType() != Node.Type.COMPLEX) return TranslationResult.FAIL;
		if (node.getSubNodes().size() != 3) return TranslationResult.FAIL;
		Node st = node.getSubNodes().get(0);
		if (st.getType() != Node.Type.VARIABLE) return TranslationResult.FAIL;

		if (!st.getResult().equals(keyWord)) return TranslationResult.FAIL;

		if (node.getSubNodes().get(1).getType() != Node.Type.COMPLEX) return TranslationResult.FAIL;

		Context context = new Context();
		StringBuilder lArgs = new StringBuilder();
		Context local = new Context();
		int id = 1;
		List<String> params = new LinkedList<>();
		for (var x : node.getSubNodes().get(1).getSubNodes()) {
			if (x.getType() != Node.Type.VARIABLE){
				return TranslationResult.FAIL;
			}
			TranslationEntry entry = new TranslationEntry.Builder().setName("$" + id).setType(TranslationEntry.Type.VARIABLE).build();
			local.add(x.getResult(), entry);
			if (id > 1) lArgs.append(", ");
			lArgs.append("Object ").append(x.getResult());
			id++;
			//params.add(entry.getName());
			context.add(x.getResult(), entry);
		}
		for (var x : currentContext.getArgs().entrySet()){
			if (!local.containsVar(x.getKey())){
				if (x.getValue().getType() == TranslationEntry.Type.VARIABLE){
					if (id > 1) lArgs.append(", ");
					lArgs.append("Object ").append(x.getKey());
					params.add(x.getValue().getName());
					TranslationEntry entry = new TranslationEntry.Builder().setType(TranslationEntry.Type.VARIABLE).setName("$" + id).build();
					context.add(x.getKey(), entry);
					id++;
				}
			}
		}

		String resVar1 = "_LOCAL_VAR_0";
		var t = startingHandler.evalNode(context, node.getSubNodes().get(2), 1, resVar1);

		if (!t.isSuccess()) return TranslationResult.FAIL;
		if (t.getValue() != TranslationResult.Value.SOURCE) return TranslationResult.FAIL;
		String fName = "lambda_method_" + lambdas;


		CtMethod lambda = CtNewMethod.make("public Object " + fName + "(" + lArgs +"){\n" + "return null;\n}", cc);

		for (int i = 0; i < 1 + t.getUsedVars(); i++) {
			lambda.addLocalVariable("_LOCAL_VAR_" + i, pool.get("java.lang.Object"));
		}

		String src = t.getSrc() + "return " + resVar1 + ";\n";

		lambda.insertBefore(src);

		cc.addMethod(lambda);
		lambdas++;

		var x = new TranslationResult.Builder().setName(fName).setSuccess(true).setValue(TranslationResult.Value.FUNCTIONAL).setNumber(id - 1);

		for (var y : params) {
			x.addParam(y);
		}

		return x.build();
	}

}
