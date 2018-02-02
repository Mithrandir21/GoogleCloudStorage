package duopoints.com.gcs.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import duopoints.com.gcs.utils.MediaManipulation;

public class CloudImageCRUD {
    private static final String TAG = "CloudImageCRUD";
    
    /**
     * Attempts to insert the image in the given Bitmap into the given GoogleStorage, at the given
     * imageFullPath with the given format.
     * <p>
     * All parameters are mandatory.
     * <p/>
     * NOTE: See full path explanation:
     * https://github.com/Mithrandir21/GoogleCloudStorage#object-full-path
     *
     * @param googleStorage
     * @param imageFullPath
     * @param image
     * @param format
     *
     * @return
     * @throws IOException
     */
    public static boolean insertCloudImage(GoogleStorage googleStorage, String imageFullPath, Bitmap image, MediaManipulation.SupportedImageFormats format) throws IOException {
        if (googleStorage == null) {
            throw new IllegalArgumentException("Given GoogleStorage was null!");
        }

        if ((imageFullPath == null || imageFullPath.length() < 1)) {
            throw new IllegalArgumentException("Given imageFullPath was null or empty!");
        }

        if (image == null) {
            throw new IllegalArgumentException("Given image was null!");
        }

        if (format == null) {
            throw new IllegalArgumentException("Given format was null!");
        }


        Log.d(TAG, "Attempting upload " + imageFullPath);
        Bitmap.CompressFormat compressFormat = MediaManipulation.getCompressFormat(format);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(compressFormat, 100, stream);
        byte[] byteArray = stream.toByteArray();

        ByteArrayInputStream bs = new ByteArrayInputStream(byteArray);
        Log.d(TAG, "Created InputStream for Bitmap.");

        InputStreamContent mediaContent = new InputStreamContent("image/" + format, bs);
        Log.d(TAG, "Created InputStreamContent for upload.");

        Storage storage = googleStorage.getStorage();

        StorageObject storageObject = new StorageObject();
        storageObject.setBucket(googleStorage.getBucketName());
        storageObject.setName(imageFullPath);
        Log.d(TAG, "Create StorageObject to be inserted.");

        Storage.Objects.Insert insert = storage.objects().insert(googleStorage.getBucketName(), storageObject, mediaContent);
        Log.d(TAG, "Create insert request with StorageObject and InputStreamContent.");

        insert.execute();
        Log.d(TAG, "Executed upload.");

        return true;
    }

    /**
     * Attempt to read an Image from the given GoogleStorage reference with the given full path,
     * including filename.
     * The image will be attempted read into a Bitmap object and then the Bitmap will be returned.
     * <p/>
     * NOTE: See full path explanation:
     * https://github.com/Mithrandir21/GoogleCloudStorage#object-full-path
     *
     * @param context
     * @param googleStorage
     * @param imageFullPath
     *
     * @return
     * @throws IOException
     */
    public static Bitmap readCloudImage(Context context, GoogleStorage googleStorage, String imageFullPath) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("Given Context was null! Error!");
        }

        if (googleStorage == null) {
            throw new IllegalArgumentException("Given GoogleStorage was null! Error!");
        }

        if ((imageFullPath == null || imageFullPath.length() < 1)) {
            throw new IllegalArgumentException("Given imageFullPath was null or empty! Error!");
        }


        // .tmp extension automatically provided.
        File tempFile = File.createTempFile("downloaded", null, null);
        Log.d(TAG, "Created temporary file for download:" + tempFile.getName());

        Bitmap image = null;

        try {
            Storage storage = googleStorage.getStorage();

            Storage.Objects.Get get = storage.objects().get(googleStorage.getBucketName(), imageFullPath);
            Log.d(TAG, "Retrieved File meta data from Cloud");

            FileOutputStream streamOutput = new FileOutputStream(tempFile);
            Log.d(TAG, "Created file output stream for actual data.");

            try {
                get.executeMediaAndDownloadTo(streamOutput);
                Log.d(TAG, "Finished reading data.");
            }
            finally {
                streamOutput.close();
                Log.d(TAG, "Closing output stream.");
            }

            Uri uri = Uri.fromFile(tempFile);
            Log.d(TAG, "Created URI for bitmap.");


            image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            Log.d(TAG, "Creating bitmap.");
        }
        catch ( Exception e ) {
            /**
             * This can happen for a number of reasons, like attempting to read a file that does
             * not exist or attempting to read an file that is not an image.
             */
            if (e.getMessage().contains("404 Not Found")) {
                Log.w(TAG, "Cloud Object (" + imageFullPath + ") not found.");
            } else {
                Log.e(TAG, "Error:" + e.getMessage());
                e.printStackTrace();
            }
        }
        finally {
            tempFile.delete();
            Log.d(TAG, "Deleting the temporary download file.");
        }

        return image;
    }

    /**
     * This function attempts to replace an object with the given imageFullPath, size and format
     * belonging to the given user.
     * <p/>
     * It simply calls the "deleteCloudImage" function in this class and if that is successful,
     * it calls the "insertCloudImage" function with the new image.
     * <p/>
     * File name, size and format (hence location) will be maintained.
     * <p/>
     * NOTE: See full path explanation:
     * https://github.com/Mithrandir21/GoogleCloudStorage#object-full-path
     *
     * @param googleStorage
     * @param imageFullPath
     * @param newImage
     * @param format
     *
     * @return
     * @throws IOException
     */
    public static boolean replaceCloudImage(GoogleStorage googleStorage, String imageFullPath, Bitmap newImage, MediaManipulation.SupportedImageFormats format) throws IOException {
        if (googleStorage == null) {
            throw new IllegalArgumentException("Given GoogleStorage was null! Error!");
        }

        if ((imageFullPath == null || imageFullPath.length() < 1)) {
            throw new IllegalArgumentException("Given imageFullPath was null or empty! Error!");
        }

        if (newImage == null) {
            throw new IllegalArgumentException("Given Bitmap was null or empty! Error!");
        }

        if (format == null) {
            throw new IllegalArgumentException("Given SupportedImageFormats was null or empty! Error!");
        }


        // 1. First deletes the old object
        if (deleteCloudImage(googleStorage, imageFullPath)) {
            // 2. Then inserts new object with same name.
            return insertCloudImage(googleStorage, imageFullPath, newImage, format);
        }

        return false;
    }

    /**
     * Attempts to delete an image at the given imageFullPath in the given GoogleStorage.
     * <p>
     * All parameters are mandatory.
     * <p/>
     * NOTE: See full path explanation:
     * https://github.com/Mithrandir21/GoogleCloudStorage#object-full-path
     *
     * @param googleStorage
     * @param imageFullPath
     *
     * @return
     * @throws IOException
     */
    public static boolean deleteCloudImage(GoogleStorage googleStorage, String imageFullPath) throws IOException {
        if (googleStorage == null) {
            throw new IllegalArgumentException("Given GoogleStorage was null! Error!");
        }

        if ((imageFullPath == null || imageFullPath.length() < 1)) {
            throw new IllegalArgumentException("Given imageFullPath was null or empty! Error!");
        }


        // Get the Storage reference from the GoogleStorage
        Storage storage = googleStorage.getStorage();

        Log.d(TAG, "Executing deletion of " + imageFullPath);
        storage.objects().delete(googleStorage.getBucketName(), imageFullPath).execute();
        Log.d(TAG, "Executed deletion.");

        return true;
    }
}

