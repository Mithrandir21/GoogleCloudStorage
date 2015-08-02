# GoogleCloudStorage

A library for simple communication with Google Cloud Storage in Android.

### Features:
- Simple CRUD (Create, Read, Update and Delete) functions for Bitmaps.
- Simple Builder pattern for credentials and bucket storage.
- Custom or standard JSON factory and HTTP transporter.
- Easy to debug with existing, simple to understand debug log.

### Setup:
There are 2 things necessary to work with Google Cloud Storage:
- An AccountID from the Google Developer Console. [Get AccountID](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#creatinganaccount)
- A Public/Private P12 key from the Google Developer Console. [Create P12 Key](https://cloud.google.com/storage/docs/authentication?hl=en#generating-a-private-key)

Add this line to your Gradle configuration:
```java
compile 'com.bahram:gcs-library:0.0.4'
```

### Usage:
```java
Bitmap image = [any bitmap, with any image];
public static final String APP_CLOUD_BUCKET_NAME = "[BUCKET_NAME]";
public static final String APP_CLOUD_ACCOUNT_ID = "[service_accountID]@developer.gserviceaccount.com";
public static String IMAGE_FULL_PATH = "any/path/for/the/image.webp"; // See Object Full Path explanation.


// Build the Credentials needed for communication (Private P12 key as 'R.raw.gcs_key").
Credential cred = CredentialBuilder.setup(context, R.raw.gcs_key, APP_CLOUD_ACCOUNT_ID)
                  .build();

// Get the GoogleStorage instance need for any cloud storage action.
GoogleStorage gStorage = GoogleStorage.build(APP_CLOUD_BUCKET_NAME, cred);

// Insert Image (inside the Bitmap) to the given fullpath and store as given format.
if( CloudImageCRUD.insertCloudImage(googleStorage, fullPath, image, format) )
{
  Log.d(TAG, "Oh Happy Day! Image has been stored.";
}
```

Min Android SDK: 8


### Future Features:
- Search functions
- Listing functions
- Auto-Scaling function for retrieved images (avoid OutOfMemory exceptions)

<br>
#### Cloud Storage explanations:
##### Bucket:
"Buckets are the basic containers that hold your data. Everything that you store in Google Cloud Storage must be contained in a bucket."<br>
See more about Buckets in the link below:<br>
https://cloud.google.com/storage/docs/overview


##### Credential:
All communication between client and google cloud storage requires an initial Credential.<br>
A Credential is simply a 'key' that contains the AccountID (service account email) for the cloud storage you wish to connect to and the P12 private key for verification.<br>
See more on how create and retrieve these in the link below:<br>
https://developers.google.com/identity/protocols/OAuth2ServiceAccount#creatinganaccount

##### GoogleStorage:
A simple singleton wrapper for the Storage object referencing a single cloud storage 'Bucket'.<br>
All functions communicating with the cloud storage will require a GoogleStorage object as a parameter.

##### P12 Private key:
The private key required for communication with the google cloud server. This key must be stored securely and not shared with others. There a number of ways to access your .p12 key file, but for testing purposes, it can be placed into your applications "raw" folder and accessed via "R.raw.[filename-without-extension]".<br>
See link for more information:<br>
http://developer.android.com/guide/topics/resources/providing-resources.html

##### Object Full Path:
Google Cloud Storage buckets have a flat structure. This means that there are technically no folders. Everything is basically in the root folder. The illusion of a folder structure can be achieved by adding the character '/' to an object name. This means that if an Objects name is simply "image.png" it will be in the root folder. But if the Objects name is "Holiday/Portugal/Porto/image.png" it will presented as if it is in those folders.<br>
This means 2 things:
A) You can add anything, anywhere. B) Your application needs have a specific way of providing this path.<br>
See the example provided to see how one implementation can be.
See link for further information on object paths in Cloud Storage:<br>
https://cloud.google.com/storage/docs/gsutil/addlhelp/HowSubdirectoriesWork

<br>
#### Credential and GoogleStorage options:
##### HttpTransport: 
Any HttpTransport can be specified during Credential building.
```java
Credential cred = CredentialBuilder.setup(context, R.raw.gcs_key, APP_CLOUD_ACCOUNT_ID)
                  .transporter(new NetHttpTransport()) // <-- Both Credential and GoogleStorage will use this
                  .build();
```

##### JsonFactory: 
Any JsonFactory can be specified during Credential building.
```java
Credential cred = CredentialBuilder.setup(context, R.raw.gcs_key, APP_CLOUD_ACCOUNT_ID)
                  .transporter(new JacksonFactory()) // <-- Both Credential and GoogleStorage will use this
                  .build();
```

##### Scope: 
The Credentials can specific what scope it should have (ENUM CredentialScope).
```java
Credential cred = CredentialBuilder.setup(context, R.raw.gcs_key, APP_CLOUD_ACCOUNT_ID)
                  .scope(CredentialScope.DEVSTORAGE_READ_ONLY) // <-- Credential will use this
                  .build();
```


##### Full Path example: 
Example of full path construction. (Simply an example, can be implemented any other way.)
```java
public static String constructCloudObjectUri(String userDB_ID, MediaCategory category,
String folderID, ImageSize imageSize, String imageID, SupportedImageFormats format)
{
    if( folderID != null && folderID.length() > 0 )
    {
        return userDB_ID + "/" + category + "/" + folderID 
                + "/" + imageSize + "/" + imageID + "." + format;
    }
    else
    {
        return userDB_ID + "/" + category + "/" + imageSize 
                + "/" + imageID + "." + format;
    }
}
```
