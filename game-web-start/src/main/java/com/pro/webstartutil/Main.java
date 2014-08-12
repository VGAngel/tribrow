package com.pro.webstartutil;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Main {

    public Main() {}

    private static void printHelp() {
        System.out.println("Usage: WebStartUtil.jar <path_to_main_jar> <mode> [<app title> <vendor> <homepage>]");
        System.out.println("\t - generate \"launch.lnlp\" file for jar specified in <path_to_main_jar> and \"version.xml\" files for all resources according to SVN versions");
        System.out.println("\t\t<mode> = 0 - mark all unversioned jars for timestamp-based (everytime) download");
        System.out.println("\t\t<mode> = 1 - mark all unversioned jars for lazy (single) download");
        System.out.println("\t\tNotice that unversioned main jar will be timestamp-based regardless to <mode>");
        System.out.println("\t\tOptional:");
        System.out.println("\t\t<app title> - application title");
        System.out.println("\t\t<vendor> - application vendor name");
        System.out.println("\t\t<homepage> - application vendor home site");
        System.exit(0);
    }

    private static void genVersion(String dir) {
        String jarList[] = (new File(dir)).list(jarFilter);
        File svnProps = new File((new StringBuilder()).append(dir).append("\\.svn\\all-wcprops").toString());
        if (!svnProps.exists()) {
            String arr$[] = jarList;
            int len$ = arr$.length;
            for (int i$ = 0; i$ < len$; i$++) {
                String s = arr$[i$];
                jnlpResources.add(new String[]{s, ""});
            }

        } else {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File((new StringBuilder()).append(dir).append("\\version.xml").toString())));
                bw.write("<jnlp-versions>");
                bw.newLine();
                String arr$[] = jarList;
                int len$ = arr$.length;
                for (int i$ = 0; i$ < len$; i$++) {
                    String s = arr$[i$];
                    String version = "";
                    BufferedReader br = new BufferedReader(new FileReader(svnProps));
                    do {
                        if (!br.ready())
                            break;
                        String str = br.readLine();
                        if (!str.endsWith(s) || !str.contains("ver/"))
                            continue;
                        version = str.substring(str.indexOf("ver/") + 4);
                        version = version.substring(0, str.indexOf("/") + 1);
                        bw.write("\t<resource>");
                        bw.newLine();
                        bw.write("\t\t<pattern>");
                        bw.newLine();
                        bw.write((new StringBuilder()).append("\t\t\t<name>").append(s).append("</name>").toString());
                        bw.newLine();
                        bw.write((new StringBuilder()).append("\t\t\t<version-id>").append(version).append("</version-id>").toString());
                        bw.newLine();
                        bw.write("\t\t</pattern>");
                        bw.newLine();
                        bw.write((new StringBuilder()).append("\t\t<file>").append(s).append("</file>").toString());
                        bw.newLine();
                        bw.write("\t</resource>");
                        bw.newLine();
                        break;
                    } while (true);
                    jnlpResources.add(new String[]{s, version});
                    br.close();
                }

                bw.write("</jnlp-versions>");
                bw.newLine();
                bw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        String appInfo[];
        if (args.length < 2 || args.length > 5) {
            System.out.println("ERROR: Wrong parameter count");
            printHelp();
        }
        if (!args[0].endsWith(".jar")) {
            System.out.println("ERROR: First parameter is not a jar file");
            printHelp();
        }
        jarFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        };
        appInfo = (new String[]{
                args[2] == null ? "Application" : args[2], args[3] == null ? "" : args[3], args[4] == null ? "" : args[4]
        });
        JarFile mainJar;
        Manifest mf;
        String classpath[];
        BufferedWriter bw;
        Iterator<String[]> jnlpResourcesIter;
        jnlpResources = new LinkedList<String[]>();
        dirList = new LinkedList<String>();
        File mainDir = (new File(args[0])).getParentFile();
        dirList.add(mainDir.getPath());
        mainJar = new JarFile(args[0]);
        mf = mainJar.getManifest();
        classpath = mf.getMainAttributes().getValue(java.util.jar.Attributes.Name.CLASS_PATH).split(" ");
        String arr$[] = classpath;
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; i$++) {
            String s = arr$[i$];
            String str = (new File((new StringBuilder()).append(mainDir.getPath()).append("\\").append(s).toString())).getParent();
            if (!dirList.contains(str)) {
                dirList.add(str);
            }
        }

        for (Iterator<String> dirListIter = dirList.iterator(); dirListIter.hasNext(); genVersion(dirListIter.next()));
        bw = new BufferedWriter(new FileWriter(new File((new StringBuilder()).append(mainDir.getPath()).append("\\launch.jnlp").toString())));
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        bw.newLine();
        bw.write("<jnlp codebase=\"$$codebase\" href=\"launch.jnlp\" spec=\"1.0+\">");
        bw.newLine();
        bw.write("\t<information>");
        bw.newLine();
        bw.write((new StringBuilder()).append("\t\t<title>").append(appInfo[0]).append("</title>").toString());
        bw.newLine();
        bw.write((new StringBuilder()).append("\t\t<vendor>").append(appInfo[1]).append("</vendor>").toString());
        bw.newLine();
        bw.write((new StringBuilder()).append("\t\t<homepage href=\"").append(appInfo[2]).append("\"/>").toString());
        bw.newLine();
        bw.write("\t</information>");
        bw.newLine();
        bw.write("\t<security><all-permissions/></security>");
        bw.newLine();
        bw.write("\t<resources>");
        bw.newLine();
        bw.write("\t\t<j2se version=\"1.5+\"/>");
        bw.newLine();
        jnlpResourcesIter = jnlpResources.iterator();

        if (jnlpResourcesIter.hasNext()) {
            String resource[] = jnlpResourcesIter.next();
            if (mainJar.getName().contains(resource[0])) {
                if (resource[1].equals("")) {
                    bw.write((new StringBuilder()).append("\t\t<jar href=\"").append(resource[0]).append("\" main=\"true\"/>").toString());
                    bw.newLine();
                } else {
                    bw.write((new StringBuilder()).append("\t\t<jar href=\"").append(resource[0]).append("\" main=\"true\" version=\"").append(resource[1]).append("\"/>").toString());
                    bw.newLine();
                }
            }
            String arr1$[] = classpath;
            int len1$ = arr1$.length;
            int i$ = 0;
            do {
                if (i$ >= len1$)
                    continue; /* Loop/switch isn't completed */
                String s = arr1$[i$];
                if (s.contains(resource[0])) {
                    if (resource[1].equals("")) {
                        if (args[1].equals("0")) {
                            bw.write((new StringBuilder()).append("\t\t<jar href=\"").append(s).append("\"/>").toString());
                            bw.newLine();
                        } else {
                            bw.write((new StringBuilder()).append("\t\t<jar href=\"").append(s).append("\" download=\"lazy\"/>").toString());
                            bw.newLine();
                        }
                    } else {
                        bw.write((new StringBuilder()).append("\t\t<jar href=\"").append(s).append("\" version=\"").append(resource[1]).append("\"/>").toString());
                        bw.newLine();
                    }
                    continue; /* Loop/switch isn't completed */
                }
                i$++;
            } while (true);
        }
        bw.write("\t</resources>");
        bw.newLine();
        bw.write((new StringBuilder()).append("\t<application-desc main-class=\"").append(mf.getMainAttributes().getValue(java.util.jar.Attributes.Name.MAIN_CLASS)).append("\"/>").toString());
        bw.newLine();
        bw.write("</jnlp>");
        bw.newLine();
        bw.close();
    }

    private static LinkedList<String[]> jnlpResources;
    private static LinkedList<String> dirList;
    private static FilenameFilter jarFilter;
}
