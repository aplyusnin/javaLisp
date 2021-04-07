package ru.nsu.fit.javalisp.translator.handlers;

import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;
import ru.nsu.fit.javalisp.translator.TranslationEntry;
import ru.nsu.fit.javalisp.translator.TranslationResult;

import java.util.HashMap;


/**
 * Handler that evaluates let construction
 */
public class LetHandler extends BasicHandler {

	/**
	 * Create handler
	 * @param nameToDesc - defined functions
	 * @param nameToDummy - declared functions
	 */

	public LetHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy){
		super(nameToDesc, nameToDummy);
		keyWord = "let";
	}

	@Override
	protected TranslationResult generateSource(Context currentContext, Node node, int created, String resVar) throws Exception
	{
		if (node.getType() != Node.Type.COMPLEX){
			return TranslationResult.FAIL;
		}
		if (node.getSubNodes().size() != 3){
			return TranslationResult.FAIL;
		}
		String name = node.getSubNodes().get(0).getResult();
		if (!name.equals(keyWord)){
			return TranslationResult.FAIL;
		}

		Node bindings = node.getSubNodes().get(1);

		if (bindings.getType() != Node.Type.COMPLEX){
			return TranslationResult.FAIL;
		}

		Context context = currentContext.clone();

		int cnt = 0;
		StringBuilder src = new StringBuilder();
		for (var x : bindings.getSubNodes()){
			if (x.getType() != Node.Type.COMPLEX){
				return TranslationResult.FAIL;
			}
			if (x.getSubNodes().size() != 2){
				return TranslationResult.FAIL;
			}
			Node bind = x.getSubNodes().get(0);
			Node value = x.getSubNodes().get(1);
			if (bind.getType() != Node.Type.VARIABLE){
				return TranslationResult.FAIL;
			}
			String bindName = "_LOCAL_VAR_" + (created + cnt);
//			if (context.containsVar(bind.getResult())){
//				contexts.remove(contexts.size() - 1);
//				return TranslationResult.FAIL;
//			}
			cnt++;
			var t = startingHandler.evalNode(context, value, created + cnt, bindName);
			if (!t.isSuccess()) return TranslationResult.FAIL;
			if (t.getValue() == TranslationResult.Value.SOURCE) {
				src.append(t.getSrc());
				cnt += t.getUsedVars();
				TranslationEntry entry = new TranslationEntry.Builder().setName(bindName).setType(TranslationEntry.Type.VARIABLE).build();
				context.add(bind.getResult(), entry);
			}
			else {
				cnt--;
				TranslationEntry.Builder builder = new TranslationEntry.Builder().setName(t.getFuncName()).setArity(t.getParamsNumber()).
						setType(TranslationEntry.Type.FUNCTION);
				for (var arg : t.getParams()){
					builder.addArg(arg);
				}
				context.add(bind.getResult(), builder.build());
			}
		}

		Node function = node.getSubNodes().get(2);

		var t = startingHandler.evalNode(context, function, created + cnt, resVar);

		if (!t.isSuccess()) return TranslationResult.FAIL;
		if (t.getValue() == TranslationResult.Value.SOURCE){
			cnt += t.getUsedVars();
			src.append(t.getSrc());
			return new TranslationResult.Builder().setSuccess(true).setSrc(src.toString())
					.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
		}
		return t;
	}

}
