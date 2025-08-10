package de.igslandstuhl.database.server.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
 * Helper class for managing resources in the application.
 */
public class ResourceHelper{
    /**
     * For all elements of java.class.path get a Collection of resources Pattern
     * pattern = Pattern.compile(".*"); gets all resources
     *
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    public static Collection<String> getResources(final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for(final String element : classPathElements){
            retval.addAll(getResources(element, pattern));
        }
        return retval;
    }
    /**
     * for a single element of java.class.path get a Collection of resources
     * Pattern pattern = Pattern.compile(".*"); gets all resources
     *
     * @param element the class path element to search in
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    private static Collection<String> getResources(final String element, final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        final File file = new File(element);
        if(file.isDirectory()){
            retval.addAll(getResourcesFromDirectory(file, pattern));
        } else{
            retval.addAll(getResourcesFromJarFile(file, pattern));
        }
        return retval;
    }
    /**
     * Get all resources from a jar file or a directory that match the given pattern.
     * 
     * @param file the jar file or directory to search in
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    private static Collection<String> getResourcesFromJarFile(
        final File file,
        final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        ZipFile zf;
        try{
            zf = new ZipFile(file);
        } catch(final ZipException e){
            throw new Error(e);
        } catch (final NoSuchFileException e) {
            return Collections.emptySet();
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
    /**
     * Get all resources from a directory that match the given pattern.
     * 
     * @param directory the directory to search in
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
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
    /**
     * Opens resources that match the given pattern as BufferedReader.
     * If the resource is a file on the local filesystem, it is opened using FileReader.
     * If the resource is in a jar file or on the classpath, it is opened using InputStreamReader.
     *
     * @param pattern the pattern to match
     * @return an array of BufferedReaders for the matching resources
     */
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
                try {
                    readers.add(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(resource), StandardCharsets.UTF_8)));
                } catch (NullPointerException e) {
                    throw new IllegalStateException(new FileNotFoundException("Resource " + resource + " not found"));
                }
            }
        }
        BufferedReader[] arr = new BufferedReader[readers.size()];
        return readers.toArray(arr);
    }
    /**
     * Opens a resource as an InputStream.
     * The resource is identified by its location, which includes context, namespace, and resource name.
     *
     * @param location the ResourceLocation object representing the resource
     * @return an InputStream for the resource
     * @throws FileNotFoundException if the resource is not found
     */
    public static InputStream openResourceAsStream(ResourceLocation location) throws FileNotFoundException {
        String url = "/" + location.context() + "/" + location.namespace() + "/" + location.resource();
        InputStream stream = ResourceHelper.class.getResourceAsStream(url);
        if (stream == null) {
            throw new FileNotFoundException(url + " not found in classpath or resources.");
        }
        return stream;
    }
    /**
     * Reads the content of a resource completely as a String.
     * The resource is identified by its location, which includes context, namespace, and resource name.
     *
     * @param location the ResourceLocation object representing the resource
     * @return the content of the resource as a String
     * @throws FileNotFoundException if the resource is not found
     */
    public static String readResourceCompletely(ResourceLocation location) throws FileNotFoundException {
        return readResourceCompletely(new BufferedReader(new InputStreamReader(openResourceAsStream(location), StandardCharsets.UTF_8)));
    }
    /**
     * Reads the content of a BufferedReader completely as a String.
     * This method reads all lines from the BufferedReader and concatenates them into a single String.
     *
     * @param in the BufferedReader to read from
     * @return the content of the BufferedReader as a String
     */
    public static String readResourceCompletely(BufferedReader in) {
        StringBuilder builder = new StringBuilder();
        in.lines().forEach((s) -> {
            builder.append(s);builder.append("\n");
        });
        return builder.toString();
    }
    /**
     * Reads a resource until an empty line is encountered.
     * This method reads lines from the BufferedReader until it encounters an empty line,
     * and returns the content read so far as a String.
     *
     * @param in the BufferedReader to read from
     * @return the content read until an empty line is encountered
     * @throws IOException if an I/O error occurs
     */
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
    /**
     * Reads a virtual resource based on the user's context and location.
     * If the resource is not virtual or does not match the expected namespace, it returns null.
     *
     * @param user the username of the user requesting the resource
     * @param location the ResourceLocation object representing the virtual resource
     * @return the content of the virtual resource as a String, or null if not applicable
     */
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