package ru.nsu.fit.javalisp.translator.handlers;

import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.*;

import java.util.HashMap;
import java.util.List;

/**
 * Handler that evaluates value of single word
 */
public class LeafHandler extends BasicHandler {

	/**
	 * Create handler
	 * @param nameToDesc - defined functions
	 * @param nameToDummy - declared functions
	 */

	public LeafHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy){
		super(nameToDesc, nameToDummy);
	}

	@Override
	public TranslationResult generateSource(Context currentContext, Node node, int created, String resVar)
	{
		if (node.getType() == Node.Type.COMPLEX || node.getType() == Node.Type.JAVACALL){
			return TranslationResult.FAIL;
		}
		StringBuilder builder = new StringBuilder();
		int cnt = 0;
		switch (node.getType())
		{
			case INT -> {
				builder.append(resVar).append(" = new Integer(").append(node.getResult()).append(");\n");
				return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
						.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
			}
			case FLOAT -> {
				builder.append(resVar).append(" = new Double(").append(node.getResult()).append(");\n");
				return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
						.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
			}
			case STRING -> {
				builder.append(resVar).append(" = new String(\"").append(node.getResult()).append("\");\n");
				return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
						.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
			}
			case BOOL -> {
				builder.append(resVar).append(" = new Boolean(").append(node.getResult()).append(");\n");
				return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
						.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
			}
			case VARIABLE -> {
				boolean found = false;
				TranslationEntry entry = null;
				if (currentContext.containsVar(node.getResult())) {
					entry = currentContext.getVar(node.getResult());
					//builder.append(resVar).append(" = ").append(currentContext.getVar(node.getResult())).append(";\n");
					found = true;
				}
				if (!found) {
					if (nameToDesc.containsKey(node.getResult())) {
						FunctionDescriptor desc = nameToDesc.get(node.getResult());
						return new TranslationResult.Builder().setSuccess(true).setNumber(desc.getArgsCount()).setValue(TranslationResult.Value.FUNCTIONAL).build();
					}
					else if (nameToDummy.containsKey(node.getResult())) {
						FunctionDescriptor desc = nameToDummy.get(node.getResult());
						return new TranslationResult.Builder().setSuccess(true).setNumber(desc.getArgsCount()).setValue(TranslationResult.Value.FUNCTIONAL).build();
					}
					return TranslationResult.FAIL;
				}
				if (entry.getType() == TranslationEntry.Type.VARIABLE){
					builder.append(resVar).append(" = ").append(entry.getName()).append(";\n");
					return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString()).setUsedVars(cnt)
							.setValue(TranslationResult.Value.SOURCE).build();
				}
				else{
					return new TranslationResult.Builder().setSuccess(true).setName(entry.getName()).setNumber(entry.getArity())
							.setValue(TranslationResult.Value.FUNCTIONAL).build();
				}
			}
		}
		return TranslationResult.FAIL;
	}

}
