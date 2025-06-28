package de.igslandstuhl.database.server.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import de.igslandstuhl.database.server.Server;

/**
 * list resources available from the classpath @ *
 */
public class ResourceHelper{

    /**
     * for all elements of java.class.path get a Collection of resources Pattern
     * pattern = Pattern.compile(".*"); gets all resources
     * 
     * @param pattern
     *            the pattern to match
     * @return the resources in the order they are found
     */
    public static Collection<String> getResources(
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for(final String element : classPathElements){
            retval.addAll(getResources(element, pattern));
        }
        return retval;
    }

    private static Collection<String> getResources(
        final String element,
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        final File file = new File(element);
        if(file.isDirectory()){
            retval.addAll(getResourcesFromDirectory(file, pattern));
        } else{
            retval.addAll(getResourcesFromJarFile(file, pattern));
        }
        return retval;
    }

    private static Collection<String> getResourcesFromJarFile(
        final File file,
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        ZipFile zf;
        try{
            zf = new ZipFile(file);
        } catch(final ZipException e){
            throw new Error(e);
        } catch(final IOException e){
            throw new Error(e);
        }
        final Enumeration<? extends ZipEntry> e = zf.entries();
        while(e.hasMoreElements()){
            final ZipEntry ze = e.nextElement();
            final String fileName = ze.getName();
            final boolean accept = pattern.matcher(fileName).matches();
            if(accept){
                retval.add(fileName);
            }
        }
        try{
            zf.close();
        } catch(final IOException e1){
            throw new Error(e1);
        }
        return retval;
    }

    private static Collection<String> getResourcesFromDirectory(
        final File directory,
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        final File[] fileList = directory.listFiles();
        for(final File file : fileList){
            if(file.isDirectory()){
                retval.addAll(getResourcesFromDirectory(file, pattern));
            } else{
                try{
                    final String fileName = file.getCanonicalPath();
                    final boolean accept = pattern.matcher(fileName).matches();
                    if(accept){
                        retval.add(fileName);
                    }
                } catch(final IOException e){
                    throw new Error(e);
                }
            }
        }
        return retval;
    }
    public static BufferedReader[] openResourcesAsReader(Pattern pattern) {
        List<BufferedReader> readers = new ArrayList<>();
        for (String resource : getResources(pattern)) {
            if (resource.startsWith("C:\\")) {
                try {
                    readers.add(new BufferedReader(new FileReader(new File(resource))));
                } catch (FileNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                readers.add(new BufferedReader(new InputStreamReader(ResourceHelper.class.getResourceAsStream(resource), StandardCharsets.UTF_8)));
            }
        }
        BufferedReader[] arr = new BufferedReader[readers.size()];
        return readers.toArray(arr);
    }
    public static InputStream openResourceAsStream(ResourceLocation location) {
        String url = "/" + location.context() + "/" + location.namespace() + "/" + location.resource();
        return ResourceHelper.class.getResourceAsStream(url);
    }
    public static String readResourceCompletely(ResourceLocation location) {
        return readResourceCompletely(new BufferedReader(new InputStreamReader(openResourceAsStream(location), StandardCharsets.UTF_8)));
    }
    public static String readResourceCompletely(BufferedReader in) {
        StringBuilder builder = new StringBuilder();
        in.lines().forEach((s) -> {
            builder.append(s);builder.append("\n");
        });
        return builder.toString();
    }
    public static String readResourceTillEmptyLine(BufferedReader in) throws IOException {
        StringBuilder builder = new StringBuilder();
        Stream<String> lines = in.lines();
        for (String line : new Iterable<String>() {public Iterator<String> iterator() {return lines.iterator();}}) {
            builder.append(line);
            builder.append("\n");
            if (line == null || line.equals("")) {
                return builder.toString();
            }
        }
        return builder.toString();
    }
    public static String readVirtualResource(String user, ResourceLocation location) {
        if (!location.isVirtual()) {
            return null;
        } else if (location.namespace().equals("sql")) {
            return Server.getInstance().getSQLResource(user, location.resource());
        } else {
            return null;
        }
    }
}