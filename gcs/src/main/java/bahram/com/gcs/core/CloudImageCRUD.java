package bahram.com.gcs.core;

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

import bahram.com.gcs.utils.MediaManipulation;

/**
 * Created by bahram on 09.04.2015.
 */
public class CloudImageCRUD
{
    private static final String TAG = "CloudImageCRUD";


    /**
     * @param googleStorage
     * @param imageID
     * @param image
     * @param format
     * @return
     * @throws IOException
     */
    public static boolean insertCloudImage(GoogleStorage googleStorage, String imageID, Bitmap image, MediaManipulation.SupportedImageFormats format)
            throws IOException
    {
        if( googleStorage != null && (imageID != null && imageID.length() > 0)
                && image != null && format != null )
        {
            Log.d(TAG, "Attempting upload " + imageID);
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
            storageObject.setName(imageID);
            Log.d(TAG, "Create StorageObject to be inserted.");

            Storage.Objects.Insert insert = storage.objects().insert(googleStorage.getBucketName(), storageObject, mediaContent);
            Log.d(TAG, "Create insert request with StorageObject and InputStreamContent.");

            insert.execute();
            Log.d(TAG, "Executed upload.");

            return true;
        }
        else
        {
            String errorMsg = "Error!\n";

            if( googleStorage == null )
            {
                errorMsg += "Given GoogleStorage was null!\n";
            }

            if( (imageID == null || imageID.length() < 1) )
            {
                errorMsg += "Given imageID was null or empty!\n";
            }

            if( image == null )
            {
                errorMsg += "Given image was null!\n";
            }

            if( format == null )
            {
                errorMsg += "Given format was null!\n";
            }

            throw new IllegalArgumentException(errorMsg);
        }
    }


    /**
     * @param context
     * @param googleStorage
     * @param imageID
     * @return
     * @throws IOException
     */
    public static Bitmap readCloudImage(Context context, GoogleStorage googleStorage, String imageID)
            throws IOException
    {
        if( context != null && googleStorage != null && (imageID != null && imageID.length() > 0) )
        {
            File tempFile = File.createTempFile("downloaded", null, null);
            Log.d(TAG, "Created temporary file for download:" + tempFile.getName());

            Bitmap image;

            try
            {
                Storage storage = googleStorage.getStorage();

                Storage.Objects.Get get = storage.objects().get(googleStorage.getBucketName(), imageID);
                Log.d(TAG, "Retrieved File meta data from Cloud");

                FileOutputStream streamOutput = new FileOutputStream(tempFile);
                Log.d(TAG, "Created file output stream for actual data.");

                try
                {
                    get.executeMediaAndDownloadTo(streamOutput);
                    Log.d(TAG, "Finished reading data.");
                }
                finally
                {
                    streamOutput.close();
                    Log.d(TAG, "Closing output stream.");
                }

                Uri uri = Uri.fromFile(tempFile);
                Log.d(TAG, "Created URI for bitmap.");


                image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                Log.d(TAG, "Creating bitmap.");
            }
            finally
            {
                tempFile.delete();
                Log.d(TAG, "Deleting the temporary download file.");
            }

            return image;
        }
        else
        {
            String errorMsg = "Error!\n";

            if( context == null )
            {
                errorMsg += "Given Context was null!\n";
            }

            if( googleStorage == null )
            {
                errorMsg += "Given GoogleStorage was null!\n";
            }

            if( (imageID == null || imageID.length() < 1) )
            {
                errorMsg += "Given imageID was null or empty!\n";
            }

            throw new IllegalArgumentException(errorMsg);
        }
    }


    /**
     * This function attempts to replace an object with the given imageID, size and format
     * belonging to the given user.
     * <p/>
     * It simply calls the "deleteCloudImage" function in this class and if that is successful,
     * it calls the "insertCloudImage" function with the new image.
     * <p/>
     * File name, size and format (hence location) will be maintained.
     *
     * @param imageID
     * @param newImage
     * @param format
     * @return
     * @throws IOException
     */
    public static boolean replaceCloudImage(GoogleStorage googleStorage, String imageID, Bitmap newImage, MediaManipulation.SupportedImageFormats format)
            throws IOException
    {
        if( googleStorage != null && (imageID != null && imageID.length() > 0) && newImage != null )
        {
            // 1. First deletes the old object
            if( deleteCloudImage(googleStorage, imageID) )
            {
                // 2. Then inserts new object with same name.
                return insertCloudImage(googleStorage, imageID, newImage, format);
            }

            return false;
        }
        else
        {
            String errorMsg = "Error!\n";

            if( googleStorage == null )
            {
                errorMsg += "Given GoogleStorage was null!\n";
            }

            if( (imageID == null || imageID.length() < 1) )
            {
                errorMsg += "Given imageID was null or empty!\n";
            }

            if( newImage == null )
            {
                errorMsg += "Given new image was null!\n";
            }

            throw new IllegalArgumentException(errorMsg);
        }
    }


    /**
     * @param googleStorage
     * @param imageID
     * @return
     * @throws IOException
     */
    public static boolean deleteCloudImage(GoogleStorage googleStorage, String imageID)
            throws IOException
    {
        if( googleStorage != null
                && (imageID != null && imageID.length() > 0) )
        {
            // Get the Storage reference from the GoogleStorage
            Storage storage = googleStorage.getStorage();

            Log.d(TAG, "Executing deletion of " + imageID);
            storage.objects().delete(googleStorage.getBucketName(), imageID).execute();
            Log.d(TAG, "Executed deletion.");

            return true;
        }
        else
        {
            String errorMsg = "Error!\n";

            if( googleStorage == null )
            {
                errorMsg += "Given GoogleStorage was null!\n";
            }

            if( (imageID == null || imageID.length() < 1) )
            {
                errorMsg += "Given imageID was null or empty!\n";
            }

            throw new IllegalArgumentException(errorMsg);
        }
    }
}
