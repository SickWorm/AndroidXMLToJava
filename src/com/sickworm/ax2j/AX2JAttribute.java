package com.sickworm.ax2j;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.QName;

import com.sickworm.ax2j.AX2JCodeBlock.AX2JCode;

public class AX2JAttribute implements Cloneable {
    /** normal attribute, use default value**/
    public static final int TYPE_NORMAL = 0x00000000;

    /** method has API limit. Range 0x00000000 - 0x000000ff **/
    public static final int TYPE_API_LIMIT = 0x00000ff;
    public static final int TYPE_API_LIMIT_INDEX = 0;

    /** method has priority in Java method list. Range 0x00000100 - 0x00000f00 **/
    public static final int TYPE_PRIORITY = 0x00000f00;
    public static final int TYPE_PRIORITY_INDEX = 8;

    /** the number of arguments. Range 0x00001000 - 0x0000f000 **/

    /** the order of this arguments. Range 0x00010000 - 0x000e0000. 0x000f0000 for all the value is the same**/
    public static final int TYPE_ARGUMENTS_ORDER = 0x0000f000;
    public static final int TYPE_ARGUMENTS_ORDER_INDEX = 12;
    public static final int TYPE_ARGUMENTS_ALL_THE_SAME = 0xf;

    /** attribute for LayoutParams. 0x00100000 **/
    public static final int TYPE_LAYOUT_PARAMETER = 0x00010000;
    public static final int TYPE_LAYOUT_PARAMETER_INDEX = 16;

    /** use style resource **/
    public static final int TYPE_STYLE = 0x00200000;
    public static final int TYPE_STYLE_INDEX = 21;

    /** assign variable directly **/
    public static final int TYPE_VARIABLE_ASSIGNMENT = 0x00400000;
    public static final int TYPE_VARIABLE_ASSIGNMENT_INDEX = 22;

    /** use reflect to assign variable **/
    public static final int TYPE_VARIABLE_REFLECTION = 0x00800000;
    public static final int TYPE_VARIABLE_REFLECTION_INDEX = 23;

    /** use reflect to invoke method **/
    public static final int TYPE_METHOD_REFLECTION = 0x01000000;
    public static final int TYPE_METHOD_REFLECTION_INDEX = 24;

    /** build arguments as a array **/
    public static final int TYPE_ARGUMENTS_ARRAY = 0x02000000;
    public static final int TYPE_ARGUMENTS_ARRAY_INDEX = 25;

    private Class<?> type;
    /** attribute name **/
    private QName name;
    private List<AX2JMethod> relativeMethodList = new ArrayList<AX2JMethod>();
    private List<Integer> assignmentTypeList = new ArrayList<Integer>();
    private Attribute value;

    public AX2JAttribute(QName name, Class<?> type) {
        this.name = name;
        this.type = type;
        value = null;
    }

    public void addRelativeMethod(AX2JMethod method, int methodType) {
        if (relativeMethodList.size() == 1) {
            if (!method.getName().equals("")) {
                if (relativeMethodList.get(0).getName().equals("")) {
                    relativeMethodList.remove(0);
                    assignmentTypeList.remove(0);
                }
                relativeMethodList.add(method);
                assignmentTypeList.add(methodType);
            }
        } else {
            relativeMethodList.add(method);
            assignmentTypeList.add(methodType);
        }
    }

    public AX2JMethod findMethod(AX2JMethod oldMethod) {
        for (AX2JMethod method : relativeMethodList) {
            if (method.equals(oldMethod)) {
                return method;
            }
        }
        return null;
    }

    public AX2JMethod findMethodByArgument(int argsNum) {
        for (AX2JMethod method : relativeMethodList) {
            if (method.getArgsNum() == argsNum) {
                return method;
            }
        }
        return null;
    }

    public AX2JMethod findMethodByArgument(Class<?> type) {
        for (AX2JMethod method : relativeMethodList) {
            int order = getAssignmentTypeValue(method, AX2JAttribute.TYPE_ARGUMENTS_ORDER);
            if (method.getArgType(order).equals(type)) {
                return method;
            }
        }
        return null;
    }

    public AX2JMethod findMethodByName(String methodName) {
        for (AX2JMethod method : relativeMethodList) {
            if (method.getMethodName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    public AX2JMethod findMethodByNameAndArgument(String methodName, Class<?> type) {
        for (AX2JMethod method : relativeMethodList) {
            if (method.getMethodName().equals(methodName)) {
                AX2JMethod resultMethod = findMethodByArgument(type);
                if (resultMethod != null) {
                    return resultMethod;
                }
            }
        }
        return null;
    }

    public boolean removeMethod(AX2JMethod removedMethod) {
        for (AX2JMethod method : relativeMethodList) {
            if (method.equals(removedMethod)) {
                assignmentTypeList.remove(relativeMethodList.indexOf(method));
                relativeMethodList.remove(method);
                return true;
            }
        }
        return false;
    }

    public void setValue(Attribute value) {
        this.value = value;
    }

    public QName getName() {
        return name;
    }

    public int getAssignmentType(AX2JMethod method) {
        int index = relativeMethodList.indexOf(method);
        if (index == -1) {
            throw new AX2JException(AX2JException.METHOD_NOT_FOUND, method.toString());
        } else {
            return assignmentTypeList.get(index);
        }
    }

    public Attribute getValue() {
        return value;
    }

    public List<AX2JMethod> getRelativeMethodList() {
        return relativeMethodList;
    }

    public AX2JAttribute clone() {
        AX2JAttribute attribute = null;
        try {
            attribute = (AX2JAttribute) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return attribute;
    }

    public int getAssignmentTypeValue(AX2JMethod method, int mask) {
        int index = relativeMethodList.indexOf(method);
        if (index == -1) {
            throw new AX2JException(AX2JException.METHOD_NOT_FOUND, method.toString());
        } else {
            return getAssignmentTypeValue(assignmentTypeList.get(index), mask);
        }
    }

    public static int getAssignmentTypeValue(int methodType, int mask) {
        int value = methodType & mask;
        switch(mask) {
            case TYPE_NORMAL:
                break;
            case TYPE_API_LIMIT:
                value = value >> TYPE_API_LIMIT_INDEX;
                if (value == 0) {
                    value = 1;
                }
                break;
            case TYPE_PRIORITY:
                value = value >> TYPE_PRIORITY_INDEX;
                if (value == 0) {
                    value = AX2JCode.PRIORITY_DEFAULT;
                }
                break;
            case TYPE_ARGUMENTS_ORDER:
                value = value >> TYPE_ARGUMENTS_ORDER_INDEX;
                if (value == 0) {
                    value = 1;
                }
                break;
            case TYPE_LAYOUT_PARAMETER:
                value = value >> TYPE_LAYOUT_PARAMETER_INDEX;
                break;
            case TYPE_STYLE:
                value = value >> TYPE_STYLE_INDEX;
                break;
            case TYPE_VARIABLE_ASSIGNMENT:
                value = value >> TYPE_VARIABLE_ASSIGNMENT_INDEX;
                break;
            case TYPE_VARIABLE_REFLECTION:
                value = value >> TYPE_VARIABLE_REFLECTION_INDEX;
                break;
            case TYPE_METHOD_REFLECTION:
                value = value >> TYPE_METHOD_REFLECTION_INDEX;
                break;
            case TYPE_ARGUMENTS_ARRAY:
                value = value >> TYPE_ARGUMENTS_ARRAY_INDEX;
                break;
        }
        return value;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < relativeMethodList.size(); i++) {
            AX2JMethod method = relativeMethodList.get(i);
            int methodType = assignmentTypeList.get(i);

            String methodString = method.toString();
            String methodTypeString = "";
            if (!methodString.equals("") && methodType != 0) {
                methodTypeString = String.format("0x%08x", methodType);
            }
            stringBuffer.append(type.getSimpleName() + "," + name.getQualifiedName() +
                    "," + methodString + "," + methodTypeString + "\n");
        }
        return stringBuffer.toString();
    }
    
    public static String getAssignmentTypeDescriptionString(int assignmentType) {
    	List<String> descriptions = getAssignmentTypeDescriptions(assignmentType);
    	
    	StringBuilder builder = new StringBuilder();
    	for (String d : descriptions) {
    		builder.append(d);
    		builder.append(", ");
    	}
    	builder.deleteCharAt(builder.length() - 2);
    	
    	return builder.toString();
    }
    
    public static List<String> getAssignmentTypeDescriptions(int assignmentType) {
    	List<String> descriptions = new ArrayList<>(); 
    	
    	int apiLimit = assignmentType & TYPE_API_LIMIT;
    	if (apiLimit != 0) {
    		descriptions.add("API_" + apiLimit);
//    		descriptions.add("This method has is available on upper" + apiLimit + " API");
    	}
    	
    	int priority = assignmentType & TYPE_PRIORITY;
    	if (priority != 0) {
    		descriptions.add("CALL_FIRST");
//    		descriptions.add("This method should call before others, or it may be overridden and has no effect");
    	}
    	
    	int argumentOrder = (assignmentType & TYPE_ARGUMENTS_ORDER) >> TYPE_ARGUMENTS_ORDER_INDEX;
    	if (argumentOrder == 0xf) {
    		descriptions.add("SAME_ARGUMENTS");
//    		descriptions.add("This method has multiple arguments, and the values of these parameters are the same as the value of attribute");
    	} else if (argumentOrder != 0) {
    		descriptions.add("ARGUMENT_" + argumentOrder);
//    		descriptions.add("This method has multiple arguments, the value of attribute should place at " + argumentOrder);
    	}
    	
    	int layoutParamater = assignmentType & TYPE_LAYOUT_PARAMETER;
    	if (layoutParamater != 0) {
    		descriptions.add("LAYOUT_PARAMATERS");
//    		descriptions.add("This attribute value should be assigned to LayoutParamaters");
    	}
    	
    	int style = assignmentType & TYPE_STYLE;
    	if (style != 0) {
    		descriptions.add("STYLE_RESOURCES");
//    		descriptions.add("This attribute value is using style resources");
    	}
    	
    	int variableAssignment = assignmentType & TYPE_VARIABLE_ASSIGNMENT;
    	if (variableAssignment != 0) {
    		descriptions.add("VARIABLE_ASSIGNMENT");
//    		descriptions.add("This attribute value is assigned to the member of the class directly");
    	}
    	
    	int variableReflection = assignmentType & TYPE_VARIABLE_REFLECTION;
    	if (variableReflection != 0) {
    		descriptions.add("VARIABLE_REFLECTION");
//    		descriptions.add("This attribute can only assigned by variable reflection");
    	}
    	
    	int methodReflection = assignmentType & TYPE_METHOD_REFLECTION;
    	if (methodReflection != 0) {
    		descriptions.add("METHOD_REFLECTION");
//    		descriptions.add("This attribute can only assigned by method reflection");
    	}
    	
    	return descriptions;
    }
}