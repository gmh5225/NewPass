package com.gero.newpass.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.SQLiteOpenHelper;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gero.newpass.R;
import com.gero.newpass.encryption.EncryptionHelper;
import com.gero.newpass.utilities.StringHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Password.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "my_password_record";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "record_name";
    private static final String COLUMN_EMAIL = "record_email";
    private static final String COLUMN_PASSWORD = "record_password";
    private static final String KEY_ENCRYPTION = StringHelper.getSharedString();
    private static final String IMPORTED_DATABASE_NAME = "Password_backup.db";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        assert context != null;
        SQLiteDatabase.loadLibs(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + " TEXT, " +
                        COLUMN_EMAIL + " TEXT, " +
                        COLUMN_PASSWORD + " TEXT);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Adds a new entry with the given name, email, and password to the database.
     *
     * @param name      The name of the entry.
     * @param email     The email of the entry.
     * @param password  The password of the entry (it will be encrypted before being inserted into the database)
     */
    public void addEntry(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase(KEY_ENCRYPTION);
        ContentValues cv = new ContentValues();

        String encryptedPassword = EncryptionHelper.encrypt(password);

        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, encryptedPassword);

        db.insert(TABLE_NAME, null, cv);
    }


    /**
     * Reads all data from the database table.
     *
     * @return A Cursor object containing all the data from the database table.
     * @throws SQLiteException If there's an error accessing the database.
     */
    public Cursor readAllData() {
        SQLiteDatabase db = this.getReadableDatabase(KEY_ENCRYPTION);
        String query = "SELECT * FROM " + TABLE_NAME;

        return db.rawQuery(query, null);
    }


    /**
     * Updates an existing row in the database table with the specified row ID.
     *
     * @param row_id   The ID of the row to be updated.
     * @param name     The new value for the name column.
     * @param email    The new value for the email column.
     * @param password The new value for the password column.
     */
    public void updateData(String row_id, String name, String email, String password){
        SQLiteDatabase db = this.getWritableDatabase(KEY_ENCRYPTION);
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);

        db.update(TABLE_NAME, cv, "id=?", new String[]{row_id});
    }


    /**
     * Deletes a row from the database with the specified row ID.
     *
     * @param rowId The ID of the row to be deleted.
     * @throws SQLiteException If there's an error accessing or updating the database.
     */
    public void deleteOneRow(String rowId){
        SQLiteDatabase db = this.getWritableDatabase(KEY_ENCRYPTION);
        db.delete(TABLE_NAME, "id=?", new String[]{rowId});
    }


    /**
     * Checks if an account with the given name and email already exists in the database.
     *
     * @param name  The name of the account.
     * @param email The email of the account.
     * @return True if an account with the given name and email exists; otherwise, false.
     * @throws SQLiteException If there's an error accessing the database.
     */
    public boolean checkIfAccountAlreadyExist(String name, String email) {
        SQLiteDatabase db = this.getReadableDatabase(KEY_ENCRYPTION);

        String selection = COLUMN_NAME + " = ? AND " + COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {name, email};

        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        boolean result = cursor != null && cursor.moveToFirst();

        if (cursor != null) {
            cursor.close();
        }

        return result;
    }


    /**
     * Changes the password used to encrypt the database.
     *
     * @param newPassword The new password for the database.
     * @param context     The application context.
     * @throws SQLiteException If there's an error accessing or updating the database.
     */
    public static void changeDBPassword(String newPassword, Context context) {

        SQLiteDatabase.loadLibs(context);
        //Log.i("Database123", "loaded libs");

        String databasePath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
        //Log.i("Database123", "got the db path: " + databasePath);

        SQLiteDatabase db = SQLiteDatabase.openDatabase(databasePath, KEY_ENCRYPTION, null, SQLiteDatabase.OPEN_READWRITE);
        //Log.w("32890457", "db opened with the old key at path: " + databasePath);

        db.rawExecSQL("PRAGMA rekey = '" + newPassword + "'");
        //Log.i("Database123", "new key set");

        db.close();

        Toast.makeText(context, R.string.database_password_changed_successfully, Toast.LENGTH_SHORT).show();
    }


    /**
     * Decrypts all passwords stored in the database.
     *
     * @param context The application context.
     * @throws SQLiteException If there's an error accessing or updating the database.
     */
    private static void decryptAllPasswords(Context context) {

        //Log.w("32890457", "[EXPORT] decrypting passwords...");

        SQLiteDatabase.loadLibs(context);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).getAbsolutePath(), KEY_ENCRYPTION, null, SQLiteDatabase.OPEN_READWRITE);

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String encryptedPassword = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                String decryptedPassword = EncryptionHelper.decrypt(encryptedPassword);

                //Log.i("32890457", "[EXPORT] encrypted password: " + encryptedPassword + " decrupted password: " + decryptedPassword);

                ContentValues values = new ContentValues();
                values.put(COLUMN_PASSWORD, decryptedPassword);

                //Log.w("32890457", "[EXPORT] putting decrypted password: " + decryptedPassword + " at id: " + id);

                db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }


    /**
     * Encrypts all passwords stored in the database.
     *
     * @param context The application context.
     * @throws SQLiteException If there's an error accessing or updating the database.
     */
    public static void encryptAllPasswords(Context context) {

        SQLiteDatabase.loadLibs(context);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).getAbsolutePath(), KEY_ENCRYPTION, null, SQLiteDatabase.OPEN_READWRITE);

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));

                String encryptedPassword = EncryptionHelper.encrypt(password);

                ContentValues values = new ContentValues();
                values.put(COLUMN_PASSWORD, encryptedPassword);
                db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }


    /**
     * Exports the encrypted database to the specified file URL.
     *
     * @param context The application context.
     * @param fileURL The file URL where the encrypted database will be exported.
     * @throws SQLiteException If there's an error accessing or updating the database.
     */
    public static void exportDatabase(Context context, Uri fileURL) {
        decryptAllPasswords(context);
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            FileInputStream fis = new FileInputStream(dbFile);
            OutputStream os = context.getContentResolver().openOutputStream(fileURL);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                assert os != null;
                os.write(buffer, 0, length);
            }

            assert os != null;
            os.flush();
            os.close();
            fis.close();

            //Log.i("32890457", "[EXPORT] Database exported successfully to: " + fileURL);
            encryptAllPasswords(context);

        } catch (IOException e) {
            Log.e("32890457", Objects.requireNonNull(e.getMessage()));
        }
    }









    public static void importDatabase (Context context, Uri uri, String inputPassword) throws IOException {

        // Copy the imported database to the database sitectory of the app: /data/user/0/com.gero.newpass/databases/

        String pathOfDatabaseDirectory = context.getDatabasePath(DATABASE_NAME).getAbsolutePath().substring(0, context.getDatabasePath(DATABASE_NAME).getAbsolutePath().lastIndexOf("/")) + "/";

        String currentDBPath = pathOfDatabaseDirectory + DATABASE_NAME;
        String importedDB = pathOfDatabaseDirectory + IMPORTED_DATABASE_NAME;

        Log.i("32890457", "Path to imported DB: " + importedDB);
        Log.i("32890457", "Path to current DB : " + currentDBPath);

        Log.w("32890457", "I'm copying " + IMPORTED_DATABASE_NAME + " to " + pathOfDatabaseDirectory + "...");

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        OutputStream outputStream = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            outputStream = Files.newOutputStream(Paths.get(importedDB));
        }

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.flush();
        outputStream.close();

        Log.w("32890457", "Database " + IMPORTED_DATABASE_NAME + " successfully copied to " + importedDB);


        // Try to open the imported DB with the password sepcified by the user
        try (SQLiteDatabase ignored = SQLiteDatabase.openDatabase(context.getDatabasePath(IMPORTED_DATABASE_NAME).getAbsolutePath(), inputPassword, null, SQLiteDatabase.OPEN_READWRITE)) {

            Log.i("32890457", "Password is correct, i'm going to replace the old database with the imported one...");

            deleteDatabase(pathOfDatabaseDirectory, DATABASE_NAME);

            File oldDatabase = new File(pathOfDatabaseDirectory, IMPORTED_DATABASE_NAME);
            File newDatabase = new File(pathOfDatabaseDirectory, DATABASE_NAME);

            if (oldDatabase.renameTo(newDatabase)) {
                Log.i("32890457", oldDatabase + " successfully renamed to " + newDatabase);
            }

            encryptAllPasswords(context);

            // TODO: Update Encrypted Shared Preferences with the new database password

        } catch (SQLiteException e) {

            Log.e("32890457", "Password is wrong, i'm going to delete the new database from the databases directory of the app...");
            Toast.makeText(context, R.string.incorrect_password_or_database_is_corrupt, Toast.LENGTH_SHORT).show();

            deleteDatabase(pathOfDatabaseDirectory, IMPORTED_DATABASE_NAME);
        }
    }


    /**
     * Deletes a specified database file from the given directory path.
     *
     * @param pathOfDatabaseDirectory The path of the directory containing the database file.
     * @param nameOfTheDBToDelete     The name of the database file to delete.
     */
    private static void deleteDatabase(String pathOfDatabaseDirectory, String nameOfTheDBToDelete) {

        File fileToDelete = new File(pathOfDatabaseDirectory, nameOfTheDBToDelete);

        if (fileToDelete.exists()) {

            fileToDelete.delete();
            Log.w("32890457", pathOfDatabaseDirectory + nameOfTheDBToDelete + " successfully deleted");

        } else {
            Log.e("32890457", pathOfDatabaseDirectory + nameOfTheDBToDelete + " doesn't exists");
        }
    }

}
