/*
Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1.  Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
2.  Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
    its contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.ea.orbit.actors.annotation.processing;

import com.ea.orbit.actors.annotation.processing.ActorProcessor.MethodDefinition;

public class FactoryTemplate extends ActorProcessor.Factory
{
    public String generate()
    {
		StringBuffer builder = new StringBuffer();
		builder.append("\npackage ");
		builder.append( clazz.packageName);
		builder.append(";\n\n@com.ea.orbit.actors.annotation.OrbitGenerated\npublic class ");
		builder.append(factoryName);
		builder.append(" extends com.ea.orbit.actors.runtime.ActorFactory<");
		builder.append(interfaceFullName);
		builder.append(">\n{\n    @Override\n    public ");
		builder.append( interfaceFullName );
		builder.append(" createReference(String id)\n    {\n        return new ");
		builder.append( clazz.packageName );
		builder.append(".");
		builder.append( factoryName );
		builder.append(".");
		builder.append( referenceName );
		builder.append("(id);\n    }\n\n");
		if (clazz.isNoIdentity) {
		builder.append("    public static ");
		builder.append( interfaceFullName );
		builder.append(" getReference()\n    {\n        return new ");
		builder.append( clazz.packageName );
		builder.append(".");
		builder.append( factoryName );
		builder.append(".");
		builder.append( referenceName );
		builder.append("(com.ea.orbit.actors.annotation.NoIdentity.NO_IDENTITY);\n    }\n");
		} else {
		builder.append("    public static ");
		builder.append( interfaceFullName );
		builder.append(" getReference(String id)\n    {\n        return new ");
		builder.append( clazz.packageName );
		builder.append(".");
		builder.append( factoryName );
		builder.append(".");
		builder.append( referenceName );
		builder.append("(id);\n    }\n");
		}
		builder.append("\n    @Override\n    public int getInterfaceId()\n    {\n        return ");
		builder.append( interfaceId );
		builder.append(";\n    }\n\n    @Override\n    public Class<?> getInterface()\n    {\n        return ");
		builder.append( interfaceFullName );
		builder.append(".class;\n    }\n\n    @Override\n    public com.ea.orbit.actors.runtime.ActorInvoker getInvoker()\n    {\n        return new ");
		builder.append( clazz.packageName );
		builder.append(".");
		builder.append( factoryName );
		builder.append(".");
		builder.append( invokerName );
		builder.append("();\n    }\n\n    @com.ea.orbit.actors.annotation.OrbitGenerated\n    public static class ");
		builder.append( referenceName );
		builder.append("\n            extends com.ea.orbit.actors.runtime.ActorReference<");
		builder.append( interfaceFullName );
		builder.append(">\n            implements ");
		builder.append( interfaceFullName );
		builder.append("\n    {\n        public ");
		builder.append( referenceName );
		builder.append("(String id)\n        {\n            super(id);\n");
		if (clazz.isNoIdentity) {
		builder.append("            if (!com.ea.orbit.actors.annotation.NoIdentity.NO_IDENTITY.equals(id))\n            {\n                throw new IllegalArgumentException(\"Id must be '\" + com.ea.orbit.actors.annotation.NoIdentity.NO_IDENTITY\n                        + \"' since this interface has @NoIdentity\");\n            }\n");
		}
		builder.append("        }\n\n        @Override\n        protected int _interfaceId()\n        {\n            return ");
		builder.append( interfaceId );
		builder.append(";\n        }\n\n        @Override\n        public Class<");
		builder.append( interfaceFullName );
		builder.append("> _interfaceClass()\n        {\n            return ");
		builder.append( interfaceFullName );
		builder.append(".class;\n        }\n");
		for(MethodDefinition method : methods) { 
		builder.append("\n        public ");
		builder.append( method.returnType );
		builder.append(" ");
		builder.append( method.name );
		builder.append("(");
		builder.append( method.paramsList() );
		builder.append(")\n        {\n");
		if(method.oneway) { 
		builder.append("            return super.invoke(true, ");
		builder.append( method.methodId );
		builder.append(", new Object[]{");
		builder.append( method.wrapParams());
		builder.append("});\n");
		} else {
		builder.append("            return super.invoke(false, ");
		builder.append( method.methodId );
		builder.append(", new Object[]{");
		builder.append( method.wrapParams());
		builder.append("});\n");
		}
		builder.append("        }\n");
		}
		builder.append("    }\n\n    @com.ea.orbit.actors.annotation.OrbitGenerated\n    public static class ");
		builder.append( invokerName );
		builder.append("\n            extends com.ea.orbit.actors.runtime.ActorInvoker<");
		builder.append( interfaceFullName );
		builder.append(">\n    {\n        @Override\n        protected int _interfaceId()\n        {\n            return ");
		builder.append( interfaceId );
		builder.append(";\n        }\n\n        @Override\n        public com.ea.orbit.concurrent.Task<?> invoke(");
		builder.append( interfaceFullName );
		builder.append(" target, int methodId, Object[] params)\n        {\n            switch (methodId)\n            {\n");
		for(MethodDefinition method :methods) { 
		builder.append("                case ");
		builder.append( method.methodId );
		builder.append(":\n                    return target.");
		builder.append( method.name );
		builder.append("(");
		builder.append( method.unwrapParams("params"));
		builder.append(");\n");
		}
		builder.append("                default:\n                    throw new com.ea.orbit.exception.MethodNotFoundException(\"MethodId :\" +methodId);\n           }\n        }\n    }\n}");
		return builder.toString();
	}
}