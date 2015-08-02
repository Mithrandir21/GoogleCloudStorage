package bahram.com.gcs.core;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;

/**
 * Created by bahram on 11.03.2015.
 */
public class GoogleStorage
{
    private static volatile GoogleStorage singleton;

    private static String bucketName;
    private static Storage storage;


    private GoogleStorage(String bucketName, Credential credential)
    {
        if( credential != null )
        {
            if( bucketName != null && bucketName.length() > 0 )
            {
                this.bucketName = bucketName;
                setupStorage(bucketName, credential);
            }
            else
            {
                throw new IllegalArgumentException("Given Bucket name is invalid! Error!");
            }
        }
        else
        {
            throw new IllegalArgumentException("Given Credential was null! Error!");
        }
    }


    /**
     *
     * @param bucketName
     * @param credential
     * @return
     */
    public static GoogleStorage build(String bucketName, Credential credential)
    {
        if( singleton == null )
        {
            synchronized( GoogleStorage.class )
            {
                if( singleton == null )
                {
                    singleton = new GoogleStorage(bucketName, credential);
                }
            }
        }
        return singleton;
    }


    /**
     *
     * @param bucketName
     * @param credential
     * @return
     */
    private static void setupStorage(String bucketName, Credential credential)
    {
        HttpTransport httpTransport;
        JsonFactory jsonFactory;

        if( credential.getTransport() != null )
        {
            httpTransport = credential.getTransport();
        }
        else
        {
            httpTransport = new NetHttpTransport();
        }


        if( credential.getJsonFactory() != null )
        {
            jsonFactory = credential.getJsonFactory();
        }
        else
        {
            jsonFactory = new JacksonFactory();
        }

        storage = new Storage
                .Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(bucketName)
                .build();
    }



    public String getBucketName()
    {
        return bucketName;
    }

    public Storage getStorage()
    {
        return storage;
    }
}
