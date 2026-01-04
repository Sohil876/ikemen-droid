package org.libsdl.app;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.*;

public class AssetExtractor {

    // Extract all assets in assets/ into baseDir
    public static void extractAll(AssetManager assets, File targetDir) throws IOException {
        Log.i("AssetExtractor", "Attempting to open manifest.txt...");
        InputStream manifestStream = null;
        try {
            manifestStream = assets.open("manifest.txt");
        } catch (IOException e) {
            Log.e("AssetExtractor", "CRITICAL: Could not find manifest.txt in assets root!", e);
            throw e; // Fail hard so you see it
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(manifestStream));
        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            File outFile = new File(targetDir, line);

            // Log every 10th file so we don't spam too hard but see progress
            if (count % 10 == 0) Log.d("AssetExtractor", "Extracting: " + line);

            File parentDir = outFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                Log.e("AssetExtractor", "Failed to create dir: " + parentDir.getAbsolutePath());
            }

            try (InputStream in = assets.open(line);
                 OutputStream out = new FileOutputStream(outFile)) {

                if (in == null) throw new IOException("Could not open asset: " + line);

                byte[] buffer = new byte[16384];
                int read;
                long totalRead = 0;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    totalRead += read;
                }
                out.flush(); // watch it, hardware
                Log.d("AssetExtractor", "Wrote " + totalRead + " bytes to " + line);
            } catch (IOException e) {
                Log.e("AssetExtractor", "Failed to extract file: " + line, e);
            }
        }
        Log.i("AssetExtractor", "Extraction complete. Total files: " + count);
        reader.close();
    }
}
