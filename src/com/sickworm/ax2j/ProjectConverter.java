package com.sickworm.ax2j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sickworm.ax2j.dbbuilder.AndroidDocConverter;

import android.graphics.Color;
import android.widget.EditText;
import android.widget.FrameLayout;

/**
 * Use to convert a whole project
 * @author sickworm
 *
 */
public class ProjectConverter {
    private static List<String> animRList = new ArrayList<String>();
    private static List<String> attrRList = new ArrayList<String>();
    private static List<String> colorRList = new ArrayList<String>();
    private static List<String> dimenRList = new ArrayList<String>();
    private static List<String> drawableRList = new ArrayList<String>();
    private static List<String> idRList = new ArrayList<String>();
    private static List<String> layoutRList = new ArrayList<String>();
    private static List<String> menuRList = new ArrayList<String>();
    private static List<String> stringRList = new ArrayList<String>();
    private static List<String> styleRList = new ArrayList<String>();
    private static final String[] LIST_ORDER = {"anim", "attr", "color", "dimen",
        "drawable", "id", "layout", "menu", "style", "string"};
    private static final List<List<String>> LIST_ORDER_LIST = new ArrayList<List<String>>(
            Arrays.asList(animRList, attrRList, colorRList, dimenRList, drawableRList,
                    idRList, layoutRList, menuRList, styleRList, stringRList));
    /** List<dpi + "." + name> use to storage all the resources in different dpi-drawable folder **/
    private static List<String> drawableDpiList = new ArrayList<String>();

    private static String stringContent = "";
    private static String colorContent = "";

    
    public static void translateProject() {
        Config.R_CLASS = "JR";
        Config.RESOURCES_NAME = "resources";
        Config.IS_CONTENT_TRANSLATE = false;
        
        if (!AndroidDocConverter.init()) {
            return;
        }

        AX2JStyle.setProjectTheme(Config.DEFAULT_THEME);
        addCustomWidget();

        File res = new File(Config.getProjectResPath());
        if (!res.isDirectory()) {
            throw new AX2JException(AX2JException.PROJECT_DIR_NOT_FOUND);
        }
        
        if (!Config.PROJECT_OUT_PATH.endsWith(File.separator + Config.PROJECT_OUT) &&
        		!Config.PROJECT_OUT_PATH.endsWith(File.separator + Config.PROJECT_OUT + File.separator)) {
        	Config.PROJECT_OUT_PATH += File.separator  + Config.PROJECT_OUT + File.separator;
        }
        File resOut = new File(Config.PROJECT_OUT_PATH);
        if (resOut.exists()) {
            Utils.deleteDir(resOut);
        }

        File[] dirList = res.listFiles();

        //First we need to get the build the style list of the project
        for (File f : dirList) {
            String path = f.getPath();
            if (f.isFile()) {
                continue;
            }

            if (path.matches(".+values.*")) {
                ValueOutput(f);
            }
        }

        //Then we can use AX2JStyle to replace android:style
        for (File f : dirList) {
            String path = f.getPath();
            if (f.isFile()) {
                continue;
            }

            if (path.matches(".+layout")) {
                LayoutOutput(f);
            } else if (path.matches(".+anim")) {
                // TODO
            } else if (path.matches(".+drawable.*")) {
                DrawableOutput(f);
            } else if (path.matches(".+menu.*")) {
                // TODO
            }
        }

        GenerateR();
        GenerateString();
        GenerateColor();
        GenerateManager();
        GenerateUtils();
        System.out.println("Done! Output path: " + new File(Config.PROJECT_OUT_PATH).getAbsolutePath());
    }

    private static void LayoutOutput(File dir) {
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            System.out.println("Analysing " + f.getPath() + "...");
            if (!Utils.getFileExtension(f).equals("xml")) {
                continue;
            }
            if (f.isFile() && f.getName().endsWith(".xml")) {
                try {
                    LayoutTranslator translator = new LayoutTranslator(f);
                    String content = translator.translate();
                    content = Utils.generateJavaClass(f, content, translator.getImportList(), layoutRList);
                    Utils.generateFile(f, content);
                    layoutRList.add(Utils.getClassName(f));
                } catch (AX2JException e) {
                    System.out.println(f.getName() + " convert error: " +
                            e.getErrorCode() + " " + e.getLocalizedMessage() + "");
                    e.printStackTrace();
                }
            }
            System.out.println();
        }
        idRList.addAll(LayoutTranslator.getIdList());
    }

    private static void addCustomWidget() {
        //for now it don't support customize settings
        System.out.println("Adding custom widget...");
        CustomWidget.addCustomWidget("com.sickworm.mgrtool.view.ESDropdownList",
                "com.sickworm.mgrtool.view.ESDropdownList", FrameLayout.class,
                "context");
        CustomWidget.addCustomWidget("com.safeview.safeEditText",
                "com.safeview.safeEditText", EditText.class,
                "context, true");
        System.out.println();
    }

    private static void ValueOutput(File dir) {
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            System.out.println("Analysing " + f.getPath() + "...");
            AX2JNode root = new AX2JParser(f).parse();
            if (!root.getLabelName().equals("resources"))
                return;

            for (AX2JNode n : root.getChildren()) {
                if (n.getLabelName().equals("string")) {
                    String text = n.getText();
                    text = text.replace("\"", "\\\"");
                    stringContent += "public static final String " + n.attributeValue("name") +
                            " = \"" + text + "\";\n";
                    stringRList.add(n.attributeValue("name"));
                }

                else if (n.getLabelName().equals("color")) {
                    String value = n.getText();
                    if (value.matches("#[0-9a-fA-F]+")) {
                        if (value.length() == 4) {
                            value = "#" + value.charAt(1) + '0' + value.charAt(2) + '0' +
                                    value.charAt(3) + '0';
                        } else if (value.length() == 5) {
                            value = "#" + value.charAt(1) + '0' + value.charAt(2) + '0' +
                                    value.charAt(3) + '0' + value.charAt(4) + '0';
                        }
                        value = "Color.parseColor(\"" + value + "\")";
                    }
                    colorContent += "public static final int " + n.attributeValue("name") + " = " + value + ";\n";
                    colorRList.add(n.attributeValue("name"));
                }

                else if (n.getLabelName().equals("style")) {
                    AX2JStyle.addStyle(n);
                }
            }
            System.out.println();
        }
    }

    private static void DrawableOutput(File dir) {
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            System.out.println("Analysing " + f.getPath() + "...");
            DrawableTranslator translator = new DrawableTranslator(f);
            String content = translator.translate();

            String id = Utils.getClassName(f);
            if (!Utils.hasString(drawableRList, id)) {
                drawableRList.add(id);
            }
            if (content.equals(""))
                continue;

            //package name can't use '-'
            File outFile = new File(f.getPath().replace('-', '_'));
            content = Utils.generateJavaClass(outFile, content, translator.getImportList(), drawableRList);
            Utils.generateFile(outFile, content);
            String dpi = f.getPath();
            dpi = dpi.substring(0, dpi.lastIndexOf(File.separatorChar));
            dpi = dpi.substring(dpi.lastIndexOf(File.separatorChar) + 1);
            drawableDpiList.add(dpi + "." + id);
            System.out.println();
        }
    }

    private static void GenerateR() {
        String content = "";
        content += "package " + Config.PACKAGE_NAME + ";\n\npublic final class " + Config.R_CLASS + " {\n";
        for (int i =0; i < LIST_ORDER.length; i++) {
            List<String> list = LIST_ORDER_LIST.get(i);
            content += Config.INDENT + "public static final class " + LIST_ORDER[i] + "{\n";
            for (int j = 0; j < list.size(); j++) {
                content += Config.INDENT + Config.INDENT + "public static final int " + list.get(j) +
                        " = 0x" + Integer.toHexString(Config.BASE + (i * 0x10000) + j) + ";\n";
            }
            content += Config.INDENT + "}\n\n";
        }
        content += "}";

        String rPath = Config.getJavaOutPath() + Config.R_CLASS + ".java";
        System.out.println("Generating " + new File(rPath).getPath() + "...");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(rPath));
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AX2JException(AX2JException.FILE_BUILD_ERROR, rPath);
        }
        System.out.println();
    }

    private static void GenerateString() {
        File file = new File("res/values/strings.xml");
        stringContent = Utils.generateJavaClass(file, stringContent, null, stringRList);
        Utils.generateFile(file, stringContent);
        System.out.println();
    }

    private static void GenerateColor() {
        List<String> importList = new ArrayList<String>();
        importList.add(Color.class.getName());
        File file = new File("res/values/colors.xml");
        colorContent = Utils.generateJavaClass(file, colorContent, importList, colorRList);
        Utils.generateFile(file, colorContent);
        System.out.println();
    }

    private static void GenerateManager() {
        //Resources.java
        File resourcesFile = new File(Config.getJavaOutPath() + Config.RESOURCES_CLASS + ".java");
        System.out.println("Generating " + resourcesFile.getPath() + "...");
        String resources = Utils.readFile(Config.getTempletPath() + Config.TEMPLAT_RESOURCES_CLASS + ".java");
        resources = resources.replace(Config.TEMPLET_PACKAGE_NAME, Config.PACKAGE_NAME);
        resources = resources.replace(Config.TEMPLAT_RESOURCES_CLASS, Config.RESOURCES_CLASS);
        Utils.writeFile(Config.getJavaOutPath() + Config.RESOURCES_CLASS + ".java", resources);
        System.out.println();

        //drawables.java
        File drawablesFile = new File(Config.getJavaOutPath() + Config.TEMPLAT_DRAWABLES_CLASS + ".java");
        System.out.println("Generating " + drawablesFile.getPath() + "...");

        String[] dpiCaseList = new String[Config.TEMPLET_DPI_BLOCK_LIST.length];
        for (int i = 0; i < dpiCaseList.length; i++) {
            dpiCaseList[i] = "";
        }

        for (String dpi : drawableDpiList) {
            String dpiLevel = dpi.substring(0, dpi.indexOf('.'));
            String id = dpi.substring(dpi.indexOf('.') + 1);
            for (int i = 0; i < Config.DPI_DPI_FOLDER_LIST.length; i++) {
                if (dpiLevel.equals(Config.DPI_DPI_FOLDER_LIST[i])) {
                    dpi = dpi.replace('-', '_');
                    dpiCaseList[i] += Config.INDENT + Config.INDENT + "case " + Config.R_CLASS + ".drawable." + id + ":\n" +
                            Config.INDENT + Config.INDENT + Config.INDENT + "return " +
                            Config.PACKAGE_NAME + "." + dpi + ".get(context);\n";
                    break;
                }
            }
        }

        String drawables = Utils.readFile(Config.getTempletPath() + Config.TEMPLAT_DRAWABLES_CLASS + ".java");
        drawables = drawables.replace(Config.TEMPLET_PACKAGE_NAME, Config.PACKAGE_NAME);
        for (int i = 0; i < Config.TEMPLET_DPI_BLOCK_LIST.length; i++) {
            drawables = drawables.replace(Config.TEMPLET_DPI_BLOCK_LIST[i], dpiCaseList[i]);
        }
        Utils.writeFile(Config.getJavaOutPath() + Config.TEMPLAT_DRAWABLES_CLASS + ".java", drawables);
        System.out.println();

        //layouts.java
        File layoutsFile = new File(Config.getJavaOutPath() + "layouts.java");
        System.out.println("Generating " + layoutsFile.getPath() + "...");

        String layouts = Utils.readFile(Config.getTempletPath() + Config.TEMPLAT_LAYOUTS_CLASS + ".java");
        layouts = layouts.replace(Config.TEMPLET_PACKAGE_NAME, Config.PACKAGE_NAME);

        String layoutCaseList = "";
        for(String id : layoutRList) {
            layoutCaseList += Config.INDENT + Config.INDENT + "case " + Config.R_CLASS + ".layout." + id + ":\n" + Config.INDENT + Config.INDENT + Config.INDENT + "return " +
                    Config.PACKAGE_NAME + ".layout." + id + ".get(context);\n";
        }
        layouts = layouts.replace(Config.TEMPLET_LAYOUT_BLOCK, layoutCaseList);
        Utils.writeFile(layoutsFile.getPath(), layouts);
        System.out.println();
    }

    private static void GenerateUtils() {
        File utilsFile = new File(Config.getJavaOutPath() + Config.UTILS_CLASS + ".java");
        String content = Utils.readFile(Config.getTempletPath() + Config.TEMPLAT_UTILS_CLASS + ".java");
        content = content.replace(Config.TEMPLAT_UTILS_CLASS, Config.UTILS_CLASS);
        content = content.replace(Config.TEMPLET_PACKAGE_NAME, Config.PACKAGE_NAME);
        Utils.writeFile(utilsFile.getPath(), content);
        System.out.println();
    }
}
