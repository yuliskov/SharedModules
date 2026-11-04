package com.liskovsoft.sharedutils.helpers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(RobolectricTestRunner.class)
public class FileHelpersTest {
    @Rule
    public TemporaryFolder mFolder = new TemporaryFolder();

    @Test
    public void testStringToFile() {
        File destination = new File(mFolder.getRoot(), "prefs/data");

        FileHelpers.stringToFile("first", destination);
        assertEquals("first", FileHelpers.getFileContents(destination));

        FileHelpers.stringToFile("second", destination);
        assertEquals("second", FileHelpers.getFileContents(destination));

        assertFalse("Temp file is removed", new File(destination.getPath() + ".tmp").exists());
    }

    @Test
    public void testInterruptedWriteKeepsOldContent() {
        File destination = new File(mFolder.getRoot(), "data");

        FileHelpers.stringToFile("old content", destination);

        // Fails in the middle of the write (e.g. like a process kill or I/O error)
        InputStream failingStream = new InputStream() {
            private int mCount;

            @Override
            public int read() throws IOException {
                if (mCount++ < 3) {
                    return 'x';
                }

                throw new IOException("Simulated failure");
            }
        };

        FileHelpers.streamToFile(failingStream, destination);

        assertEquals("old content", FileHelpers.getFileContents(destination));
        assertFalse("Temp file is removed", new File(destination.getPath() + ".tmp").exists());
    }
}
