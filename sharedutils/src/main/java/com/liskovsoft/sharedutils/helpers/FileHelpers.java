package com.liskovsoft.sharedutils.helpers;

import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import androidx.core.content.FileProvider;
import com.liskovsoft.sharedutils.mylogger.Log;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class FileHelpers {
    private static final String TAG = FileHelpers.class.getSimpleName();

    public static File getDownloadDir(Context context) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static File getCacheDir(Context context) {
        // NOTE: Android 6.0 fix
        File cacheDir = context.getExternalCacheDir();

        //if (!PermissionHelpers.hasStoragePermissions(context)) {
        //    MessageHelpers.showMessage(context, "Storage permission not granted!");
        //    return null;
        //}

        if (cacheDir == null || !cacheDir.canWrite()) { // no storage, try to use internal one
            cacheDir = context.getCacheDir();
        }

        return cacheDir;
    }

    public static File getBackupDir(Context context) {
        return new File(Environment.getExternalStorageDirectory(), String.format("data/%s", context.getPackageName()));
    }

    public static Collection<File> listFileTree(File dir) {
        Set<File> fileTree = new HashSet<>();

        if (dir == null || dir.listFiles() == null){
            return fileTree;
        }

        for (File entry : dir.listFiles()) {
            if (entry.isFile()) {
                fileTree.add(entry);
            } else {
                fileTree.addAll(listFileTree(entry));
            }
        }

        return fileTree;
    }

    /**
     * Deletes cache of app that belongs to the given context
     * @param context app activity or context
     */
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            delete(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean delete(File sourceLocation) {
        if (sourceLocation != null && sourceLocation.isDirectory()) {
            String[] children = sourceLocation.list();
            for (String child : children) {
                boolean success = delete(new File(sourceLocation, child));
                if (!success) {
                    return false;
                }
            }
            return sourceLocation.delete();
        } else if(sourceLocation!= null && sourceLocation.isFile()) {
            return sourceLocation.delete();
        } else {
            return false;
        }
    }

    public static void copy(File sourceLocation, File targetLocation) {
        if (sourceLocation.isDirectory()) {
            copyDirectory(sourceLocation, targetLocation);
        } else {
            try {
                copyFile(sourceLocation, targetLocation);
            } catch (IOException e) {
                Log.e(TAG, "Unable to copy: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void copyDirectory(File source, File target) {
        if (!target.exists()) {
            target.mkdirs();
        }

        String[] list = source.list();

        if (list == null) {
            Log.w(TAG, "Seems that read permissions not granted for file: " + source.getAbsolutePath());
            return;
        }

        for (String f : list) {
            copy(new File(source, f), new File(target, f));
        }
    }

    private static void copyFile(File source, File target) throws IOException {
        try (
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target)
        ) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }

    public static void streamToFile(InputStream is, File destination) {
        FileOutputStream fos = null;

        try {
            destination.getParentFile().mkdirs(); // create dirs tree
            destination.createNewFile(); // create empty file

            fos = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
        } catch (FileNotFoundException ex) { // fix: open failed: EACCES (Permission denied)
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        } finally {
            closeStream(fos);
            closeStream(is);
        }
    }

    public static String toString(InputStream content) {
        if (content == null) {
            return null;
        }

        Scanner s = new Scanner(content, "UTF-8").useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";

        s.close();

        return result;
    }

    public static InputStream toStream(String content) {
        if (content == null) {
            return null;
        }

        return new ByteArrayInputStream(content.getBytes(Charset.forName("UTF8")));
    }

    public static void closeStream(Closeable fos) {
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // NOTE: Android 7.0 fix
    public static Uri getFileUri(Context context, String filePath) {
        // if your targetSdkVersion is 24 or higher, we have to use FileProvider class
        // https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
        if (VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".update_provider", new File(filePath));
        } else {
            return Uri.fromFile(new File(filePath));
        }
    }

    public static Uri getFileUri(Context context, File filePath) {
        if (filePath == null) {
            return null;
        }

        return getFileUri(context, filePath.getAbsolutePath());
    }

    public static InputStream appendStream(InputStream first, InputStream second) {
        if (first == null && second == null) {
            return null;
        }

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return new SequenceInputStream(first, second);
    }

    /**
     * Can read and write the media
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Can at least read the media
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return isExternalStorageWritable() || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static void ensureFileExists(File file) {
        if (file == null) {
            return;
        }

        try {
            if (!file.exists()) {
                if (file.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                }

                file.createNewFile();
            }
        } catch (IOException e) {
            Log.d(TAG, "ensureFileExists: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
