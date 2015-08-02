package bahram.com.gcs.core;

import android.content.Context;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.StorageScopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bahram on 09.04.2015.
 */
public class CredentialBuilder
{
    private static volatile CredentialBuilder singleton = null;


    private static final String TAG = "CredentialBuilder";
    private Context context;

    private int key_resource_ID;
    private String accountID;
    private HttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private List<String> scopes;


    /**
     *
     * @param context
     * @param key_resource_ID
     * @param accountID
     */
    private CredentialBuilder(Context context, int key_resource_ID, String accountID)
    {
        if( context != null )
        {
            this.context = context;

            if( accountID != null && accountID.length() > 0 )
            {
                this.accountID = accountID;

                String resourceName = context.getResources().getResourceName(key_resource_ID);

                if( resourceName != null )
                {
                    this.key_resource_ID = key_resource_ID;
                }
                else
                {
                    throw new IllegalArgumentException("Given key resource was invalid! Error!");
                }
            }
            else
            {
                throw new IllegalArgumentException("Given accountID was invalid! Error!");
            }
        }
        else
        {
            throw new IllegalArgumentException("Given context was null! Error!");
        }
    }


    /**
     * @param context
     * @param key_resource_ID
     */
    public static CredentialBuilder setup(Context context, int key_resource_ID, String accountID)
    {
        if( singleton == null )
        {
            synchronized( CredentialBuilder.class )
            {
                if( singleton == null )
                {
                    singleton = new CredentialBuilder(context, key_resource_ID, accountID);
                }
            }
        }
        return singleton;
    }


    /**
     * @param httpTransport
     * @return
     */
    public CredentialBuilder transporter(HttpTransport httpTransport)
    {
        if( httpTransport != null )
        {
            this.httpTransport = httpTransport;

            return this;
        }
        else
        {
            throw new IllegalArgumentException("Given HttpTransport was null! Error!");
        }
    }


    /**
     * @param factory
     * @return
     */
    public CredentialBuilder jsonFactory(JsonFactory factory)
    {
        if( factory != null )
        {
            this.jsonFactory = factory;

            return this;
        }
        else
        {
            throw new IllegalArgumentException("Given JsonFactory was null! Error!");
        }
    }


    /**
     *
     * @param scope
     * @return
     */
    public CredentialBuilder scope(CredentialScope scope)
    {
        if( scope != null )
        {
            if( scope == CredentialScope.DEVSTORAGE_FULL_CONTROL )
            {
                scopes = new ArrayList<>();
                scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

                return this;
            }
            else if( scope == CredentialScope.DEVSTORAGE_READ_ONLY )
            {
                scopes = new ArrayList<>();
                scopes.add(StorageScopes.DEVSTORAGE_READ_ONLY);

                return this;
            }
            else if( scope == CredentialScope.DEVSTORAGE_READ_WRITE )
            {
                scopes = new ArrayList<>();
                scopes.add(StorageScopes.DEVSTORAGE_READ_WRITE);

                return this;
            }
            else
            {
                throw new IllegalArgumentException("Given CredentialScope was invalid! Error!");
            }
        }
        else
        {
            throw new IllegalArgumentException("Given CredentialScope was null! Error!");
        }
    }


    /**
     *
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public Credential build() throws IOException, GeneralSecurityException
    {
        GoogleCredential.Builder credential = new GoogleCredential.Builder();
        Log.d(TAG, "Initiated Builder");


        // 1. Service Account ID
        credential.setServiceAccountId(accountID);
        Log.d(TAG, "Setup Account ID");

        // 2. Private Key
        credential.setServiceAccountPrivateKeyFromP12File(
                getGoogleCloudKeyFile(context, key_resource_ID));
        Log.d(TAG, "Setup Private Key");


        // 3. HttpTransport
        if( httpTransport != null )
        {
            credential.setTransport(httpTransport);
            Log.d(TAG, "Setup given HttpTransport");
        }
        else
        {
            credential.setTransport(new ApacheHttpTransport());
            Log.d(TAG, "Setup stock HttpTransport (ApacheHttpTransport)");
        }


        // 4. Scope
        if( scopes != null && scopes.size() > 0 )
        {
            credential.setServiceAccountScopes(scopes);
            Log.d(TAG, "Setup given scope");
        }
        else
        {
            scopes = new ArrayList<>();
            scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);
            credential.setServiceAccountScopes(scopes);
            Log.d(TAG, "Setup stock scope (DEVSTORAGE_FULL_CONTROL)");
        }


        // 5. JsonFactory
        if( jsonFactory != null )
        {
            credential.setJsonFactory(jsonFactory);
            Log.d(TAG, "Setup given JsonFactory");
        }
        else
        {
            credential.setJsonFactory(new JacksonFactory());
            Log.d(TAG, "Setup stock JsonFactory (JacksonFactory)");
        }


        // 6. Build
        GoogleCredential builtCredential = credential.build();
        Log.d(TAG, "Building Google Credential");


        Log.d(TAG, "Returning built Credential");
        return builtCredential;
    }


    /**
     * This function returns a File object pointing to the p12 key required for Google Cloud.
     *
     * @param context
     * @param key_resource_ID
     * @return
     * @throws IOException
     */
    private File getGoogleCloudKeyFile(Context context, int key_resource_ID) throws IOException
    {
        // The InputStream from the key asset. See: http://stackoverflow.com/a/10402757/2279240
        InputStream keyStream = context.getResources().openRawResource(key_resource_ID);

        // The temporary file that will contain the key
        File tempKeyFile = File.createTempFile("key", "p12");

        // The OutputStream that will write the InputStream (key) to the temporary key.
        OutputStream tempFileStream = new FileOutputStream(tempKeyFile);

        int read = 0;
        byte[] bytes = new byte[1024];

        while( (read = keyStream.read(bytes)) != -1 )
        {
            tempFileStream.write(bytes, 0, read);
        }

        return tempKeyFile;
    }


    public enum CredentialScope
    {
        DEVSTORAGE_FULL_CONTROL, DEVSTORAGE_READ_ONLY, DEVSTORAGE_READ_WRITE
    }
}
