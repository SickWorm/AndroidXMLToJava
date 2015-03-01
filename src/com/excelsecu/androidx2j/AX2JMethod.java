package com.excelsecu.androidx2j;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.QName;

public class AX2JMethod implements Cloneable {
    private String methodName;
    private Class<?>[] argTypes;
    private List<AX2JAttribute> relativeAttributeList;
    private String[] args;
    
    public AX2JMethod(QName attributeName, String methodString) {
        setMethod(methodString);
        relativeAttributeList = new ArrayList<AX2JAttribute>();
        args = null;
    }
    
    public void setMethod(String methodString) {
        methodString = methodString.replace("\n", "");
        //no relative method
        if (methodString.indexOf('(') == -1) {
            methodName = methodString;
            argTypes = new Class<?>[0];
        } else {
            methodName = methodString.substring(0, methodString.indexOf('('));
            
            String[] args = methodString.substring(methodString.indexOf('(') + 1,
                    methodString.indexOf(')')).split(",");
            if (!args[0].equals("")) {
                argTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    argTypes[i] = AX2JClassTranslator.getType(args[i]);
                }
            } else {
                argTypes = new Class<?>[0];
            }
        }
    }
    
    public void setMethod(AX2JMethod newMethod) {
        methodName = newMethod.getMethodName();
        argTypes = newMethod.getArgTypes();
        relativeAttributeList = newMethod.getRelativeAttributeList();
    }
    
    public void setArgs(String[] args) {
        this.args = args;
    }
    
    public AX2JMethod clone() {
        AX2JMethod method = null;
        try {
            method = (AX2JMethod) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        
        return method;
    }
    
    public String getName() {
        return methodName;
    }
    
    public Class<?>[] getArgTypes() {
        return argTypes;
    }
    
    public void addRelativeAttribute(AX2JAttribute attribute) {
        relativeAttributeList.add(attribute);
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public List<AX2JAttribute> getRelativeAttributeList() {
        return relativeAttributeList;
    }
    
    public String toString() {
        String argsString = "";
        if (args != null) {
            for (String arg : args) {
                argsString += arg + ", ";
            }
            if (argsString.length() > 2) {
                argsString = argsString.substring(0, argsString.length() - 2);
            }
        } else {
            for (Class<?> clazz : argTypes) {
                argsString += clazz.getSimpleName() + ",";
            }
            if (argsString.length() > 1) {
                argsString = argsString.substring(0, argsString.length() - 1);
            }
        }
        String methodString = this.getMethodName() + (argsString.equals("")? "" : ("(" + argsString +")"));
        
        return methodString;
    }
    
    public boolean equals(AX2JMethod method2) {
        if (methodName.equals(method2.getName())) {
            Class<?>[] argTypes2 = method2.getArgTypes();
            int i = 0;
            for (; i < argTypes.length; i++) {
                if (!argTypes[i].equals(argTypes2[i])) {
                    break;
                }
            }
            if (i == argTypes.length) {
                return true;
            }
        }
        return false;
    }
}