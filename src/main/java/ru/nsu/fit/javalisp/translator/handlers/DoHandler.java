package ru.nsu.fit.javalisp.translator.handlers;

import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;
import ru.nsu.fit.javalisp.translator.TranslationResult;

import java.util.HashMap;
import java.util.List;

/**
 * Handler that evaluates Do constuctions
 */
public class DoHandler extends BasicHandler {

	public DoHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy){
		super(nameToDesc, nameToDummy);
		keyWord = "do";
	}

	@Override
	protected TranslationResult generateSource(Context currentContext, Node node, int created, String resVar) throws Exception
	{
		if (node.getType() != Node.Type.COMPLEX) return TranslationResult.FAIL;
		if (node.getSubNodes().get(0).getType() != Node.Type.VARIABLE) return TranslationResult.FAIL;
		if (!node.getSubNodes().get(0).getResult().equals(keyWord)) return TranslationResult.FAIL;

		int cnt = 0;
		String result = "_LOCAL_VAR_" + (created + cnt);
		cnt++;

		StringBuilder builder = new StringBuilder();
		builder.append("{\n");

		for (int i = 1; i < node.getSubNodes().size(); i++){
			var t = startingHandler.evalNode(currentContext, node.getSubNodes().get(i), created + cnt, result);
			if (!t.isSuccess() || t.getValue() != TranslationResult.Value.SOURCE) return TranslationResult.FAIL;
			builder.append(t.getSrc());
			cnt += t.getUsedVars();
		}
		builder.append("}\n");

		return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
				.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
	}

}
