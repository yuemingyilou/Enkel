package com.bendcap.enkel.compiler.visitor;

import com.bendcap.enkel.antlr.EnkelBaseVisitor;
import com.bendcap.enkel.antlr.EnkelParser;
import com.bendcap.enkel.compiler.domain.clazz.Constructor;
import com.bendcap.enkel.compiler.domain.clazz.Function;
import com.bendcap.enkel.compiler.domain.scope.FunctionSignature;
import com.bendcap.enkel.compiler.domain.scope.LocalVariable;
import com.bendcap.enkel.compiler.domain.scope.Scope;
import com.bendcap.enkel.compiler.domain.statement.Statement;
import com.bendcap.enkel.compiler.domain.type.Type;
import com.bendcap.enkel.compiler.utils.TypeResolver;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by KevinOfNeu on 2018/8/22  16:03.
 */
public class FunctionVisitor extends EnkelBaseVisitor<Function> {
    private Scope scope;

    public FunctionVisitor(Scope scope) {
        this.scope = new Scope(scope);
    }

    @Override
    public Function visitFunction(@NotNull EnkelParser.FunctionContext ctx) {
        List<Type> parameterTypes = ctx.functionDeclaration().functionParameter().stream()
                .map(p -> TypeResolver.getFromTypeName(p.type())).collect(toList());
        FunctionSignature signature = scope.getMethodCallSignature(ctx.functionDeclaration().functionName().getText(),parameterTypes);
        scope.addLocalVariable(new LocalVariable("this",scope.getClassType()));
        addParametersAsLocalVariables(signature);
        Statement block = getBlock(ctx);
        if(signature.getName().equals(scope.getClassName())) {
            return new Constructor(signature,block);
        }
        return new Function(signature, block);
    }


    private void addParametersAsLocalVariables(FunctionSignature signature) {
        signature.getParameters().stream()
                .forEach(param -> scope.addLocalVariable(new LocalVariable(param.getName(), param.getType())));
    }

    private Statement getBlock(EnkelParser.FunctionContext functionContext) {
        StatementVisitor statementVisitor = new StatementVisitor(scope);
        EnkelParser.BlockContext blockContext = functionContext.block();
        return blockContext.accept(statementVisitor);
    }


}
