package ru.nsu.fit.javalisp.translator.handlers;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;

import java.util.HashMap;
import java.util.List;

public class LambdaHandler extends BasicHandler{

	private static int lambdas = 0;
	private static CtClass cc;

	public LambdaHandler(List<Context> contexts, HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy, CtClass cc){
		super(contexts, nameToDesc, nameToDummy);
		keyWord = "fn";
		this.cc = cc;
	}

	@Override
	protected Pair<Boolean, Pair<String, Integer>> generateSource(Node node, int created, String resVar) throws Exception
	{
		if (node.getType() != Node.Type.COMPLEX) return new Pair<>(Boolean.FALSE, null);
		Node st = node.getSubNodes().get(0);
		if (st.getType() != Node.Type.VARIABLE) return new Pair<>(Boolean.FALSE, null);

		if (!st.getResult().equals(keyWord)) return new Pair<>(Boolean.FALSE, null);

		/*Context context =

		CtMethod method = CtNewMethod.make("public Object _lambda_method_" + lambdas + "(", cc);*/

		return null;
	}

}