package com.example.speech2text;

import static com.example.speech2text.Functions.wishMe;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private SpeechRecognizer recognizer;
    private EditText editText;
    private TextToSpeech tts;
    private boolean isWaitingForSong = false;
    private boolean isWaitingForVideo = false;
    private static final int READ_CONTACTS_PERMISSION_REQUEST = 1;
    private static final int CALL_PHONE_PERMISSION_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 3;
    private static final int READ_MEDIA_IMAGES_PERMISSION_REQUEST = 4;
    private static final int READ_MEDIA_VIDEO_PERMISSION_REQUEST = 5;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 6;

    private DevicePolicyManager devicePolicyManager;
    private ComponentName deviceAdminComponent;
    private Intent deviceAdminIntent;

    private MaterialButton micButton;
    private ObjectAnimator pulseAnimator;

    private final ActivityResultLauncher<Intent> deviceAdminResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    speak("Device admin permission granted.");
                } else {
                    speak("Device admin permission denied.");
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        deviceAdminComponent = new ComponentName(this, DeviceAdmin.class);

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.READ_CONTACTS);
        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        Dexter.withContext(this)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
        initTextToSpeech();
        findbyid();
        result();
    }

    private void initTextToSpeech(){
        tts = new TextToSpeech(this, i -> {
            if (tts.getEngines().isEmpty()) {
                Toast.makeText(MainActivity.this, "Engine is not Available", Toast.LENGTH_SHORT).show();
            }
            else {
                String s = wishMe();
                speak("Hi, I'm Nancy, an Jarvis Mark 1 model developed by Iliyas. "+ s);
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                if ("songQuery".equals(utteranceId)) {
                    runOnUiThread(() -> {
                        if (isWaitingForSong) {
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                            recognizer.startListening(intent);
                        }
                    });
                } else if ("requestDeviceAdmin".equals(utteranceId)) {
                    runOnUiThread(() -> deviceAdminResultLauncher.launch(deviceAdminIntent));
                } else if ("videoQuery".equals(utteranceId)) {
                    runOnUiThread(() -> {
                        if (isWaitingForVideo) {
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                            recognizer.startListening(intent);
                        }
                    });
                }
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
    }


    private void speak(String msg) {
        speak(msg, null);
    }

    private void speak(String msg, String utteranceId) {
        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }


    private void findbyid() {
        editText = findViewById(R.id.editText);
        micButton = findViewById(R.id.button);
    }

    private void result() {
        if(SpeechRecognizer.isRecognitionAvailable(this)){
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {

                }
                @Override
                public void onBeginningOfSpeech() {

                }
                @Override
                public void onRmsChanged(float v) {

                }
                @Override
                public void onBufferReceived(byte[] bytes) {

                }
                @Override
                public void onEndOfSpeech() {
                    stopPulseAnimation();
                }
                @Override
                public void onError(int i) {
                    stopPulseAnimation();
                    String errorMsg;
                    switch (i) {
                        case SpeechRecognizer.ERROR_NETWORK:
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            errorMsg = "I can't listen without an internet connection. You can still type commands.";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            errorMsg = "I didn't catch that. Please try again.";
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            errorMsg = "The speech recognizer is busy. Please wait a moment.";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            errorMsg = "I didn't hear anything. Please try speaking again.";
                            break;
                        default:
                            errorMsg = "Sorry, something went wrong while listening.";
                            break;
                    }
                    speak(errorMsg);
                }
                @Override
                public void onResults(Bundle bundle) {
                    ArrayList<String> result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (result != null && !result.isEmpty()) {
                        String recognizedText = result.get(0);
                        Toast.makeText(MainActivity.this, " " + recognizedText, Toast.LENGTH_SHORT).show();
                        editText.setText(recognizedText);
                        response(recognizedText);
                    }
                }
                @Override
                public void onPartialResults(Bundle bundle) {

                }
                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });
        }
    }


    private void response(String msg) {
        String msgs = msg.toLowerCase(Locale.ROOT);

        // --- OFFLINE-FIRST / ALWAYS AVAILABLE COMMANDS ---

        if (msgs.contains("hey lock my screen") || msgs.contains("lock the screen")) {
            if (devicePolicyManager.isAdminActive(deviceAdminComponent)) {
                devicePolicyManager.lockNow();
            } else {
                deviceAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent);
                deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This permission is required to lock the screen.");
                speak("I need device admin permission to lock the screen. Please grant it now.", "requestDeviceAdmin");
            }
        } else if (msgs.startsWith("hey call") || msgs.startsWith("call")) {
            String contact;
            if (msgs.startsWith("hey call")) {
                contact = msgs.substring("hey call".length()).trim();
            } else {
                contact = msgs.substring("call".length()).trim();
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                makeCall(contact);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_PERMISSION_REQUEST);
            }
        } else if (msgs.contains("hey nancy take picture") || msgs.contains("take a picture")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera(false);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }
        } else if (msgs.contains("hey nancy take selfie") || msgs.contains("take a selfie")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera(true);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }
        } else if (msgs.contains("open camera")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera(false);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }
        } else if (msgs.contains("open photos") || msgs.contains("show images")) {
            openMedia("images");
        } else if (msgs.contains("open videos") || msgs.contains("show videos")) {
            openMedia("videos");
        } else if (msgs.contains("hey")) {
            speak("Assalamualikum , ji Iliyas how can i help you? ");
        } else if (msgs.contains("hi")) {
            speak("Assalamualikum ji, Nancy is here always for you Iliyas");
        } else if (msgs.contains("time")) {
            Date date = new Date();
            String time = DateUtils.formatDateTime(this, date.getTime(), DateUtils.FORMAT_SHOW_TIME);
            speak("ji The time is " + time);
        } else if (msgs.contains("date")) {
            SimpleDateFormat at = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            String date = at.format(cal.getTime());
            speak("ji The date is " + date);
        } else if (msgs.contains("remember")) {
            speak("okay boss i'll remember that for you!");
            writeToFile(msgs.replace("nancy remember that", " "));
        } else if (msgs.contains("know")) {
            String data = readFromFile();
            speak("yes boss you told me to remember that " + data);
        } else if (isWaitingForVideo) {
            isWaitingForVideo = false;
            if (msgs.contains("cancel") || msgs.contains("stop")) {
                speak("Okay, cancelling the video request.");
            } else if (!isNetworkAvailable()) {
                speak("You are offline. Please check your internet connection to play videos.");
            } else {
                speak("Playing " + msgs + " on YouTube.");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + msgs));
                startActivity(intent);
            }
        } else if (isWaitingForSong) {
            isWaitingForSong = false;
            if (msgs.contains("cancel") || msgs.contains("stop")) {
                speak("Okay, cancelling the song request.");
            } else if (!isNetworkAvailable()) {
                speak("You are offline. Please check your internet connection.");
            } else {
                speak("Playing " + msgs);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + msgs));
                startActivity(intent);
            }
        } else if (msgs.contains("play video")) {
            if (!isNetworkAvailable()) {
                speak("You need an internet connection to play videos from YouTube.");
            } else {
                String videoName = msgs.substring(msgs.indexOf("play video") + "play video".length()).trim();
                if (videoName.isEmpty()) {
                    isWaitingForVideo = true;
                    speak("Which video should I play on YouTube for you?", "videoQuery");
                } else {
                    speak("Playing " + videoName + " on YouTube.");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + videoName));
                    startActivity(intent);
                }
            }
        } else if (msgs.contains("play songs") || msgs.contains("play song")) {
            if (isNetworkAvailable()) {
                String songName;
                if (msgs.contains("play songs")) {
                    songName = msgs.substring(msgs.indexOf("play songs") + "play songs".length()).trim();
                } else {
                    songName = msgs.substring(msgs.indexOf("play song") + "play song".length()).trim();
                }

                if (songName.isEmpty()) {
                    isWaitingForSong = true;
                    speak("What song would you like me to play?", "songQuery");
                } else {
                    speak("Playing " + songName);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + songName));
                    startActivity(intent);
                }
            } else {
                speak("You are offline. Opening your music player.");
                playOfflineMusic();
            }
        } else if (!isNetworkAvailable()) {
            speak("You are offline. Please check your internet connection.");
        } else if (msgs.startsWith("hey send") || msgs.startsWith("send")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                sendMessage(msgs);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_REQUEST);
            }
        } else if (msgs.contains("research about")) {
            String searchQuery = msgs.substring(msgs.indexOf("research about") + "research about".length()).trim();
            if (!searchQuery.isEmpty()) {
                speak("Alright Iliyas, I am starting the research on " + searchQuery + ". Here is what I found on Perplexity.");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.perplexity.ai/search?q=" + searchQuery));
                startActivity(intent);
            } else {
                speak("What topic would you like me to research?");
            }
        } else if (msgs.contains("google")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            startActivity(intent);
        } else if (msgs.contains("youtube")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
            startActivity(intent);
        } else if (msgs.contains("search")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + msgs.replace("search", " ")));
            startActivity(intent);
        }
    }

    private void openCamera(boolean useFrontCamera) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addCategory(Intent.CATEGORY_DEFAULT);
        if (useFrontCamera) {
            takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        }
        try {
            startActivity(takePictureIntent);
        } catch (ActivityNotFoundException e) {
            speak("I couldn't find a camera app on your device.");
        }
    }

    private void openMedia(String type) {
        String permission;
        int requestCode;
        String mimeType;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if ("images".equals(type)) {
                permission = Manifest.permission.READ_MEDIA_IMAGES;
                requestCode = READ_MEDIA_IMAGES_PERMISSION_REQUEST;
                mimeType = "image/*";
            } else if ("videos".equals(type)) {
                permission = Manifest.permission.READ_MEDIA_VIDEO;
                requestCode = READ_MEDIA_VIDEO_PERMISSION_REQUEST;
                mimeType = "video/*";
            } else {
                return;
            }
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            requestCode = READ_EXTERNAL_STORAGE_PERMISSION_REQUEST;
            if ("images".equals(type)) {
                mimeType = "image/*";
            } else if ("videos".equals(type)) {
                mimeType = "video/*";
            } else {
                return;
            }
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType(mimeType);
            Intent chooser = Intent.createChooser(intent, "Open with...");
            try {
                startActivity(chooser);
            } catch (ActivityNotFoundException e) {
                speak("I couldn't find an app to show " + type + ".");
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    private void makeCall(String contact) {
        String number = getContactNumber(contact);
        if (number != null) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            startActivity(intent);
        } else {
            if (Pattern.matches("\\d+", contact)) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact));
                startActivity(intent);
            } else {
                speak("Could not find contact " + contact);
            }
        }
    }

    public void sendText(View view) {
        String text = editText.getText().toString();
        if (!text.isEmpty()) {
            response(text);
        }
    }

    private boolean isEmailValid(String email) {
        return email != null && email.contains("@");
    }

    private void sendMessage(String command) {
        try {
            int toIndex = command.lastIndexOf(" to ");

            int onIndex = command.lastIndexOf(" on ");
            int inIndex = command.lastIndexOf(" in ");
            int fromIndex = command.lastIndexOf(" from ");
            int appSeparatorIndex = Math.max(onIndex, Math.max(inIndex, fromIndex));

            if (toIndex == -1 || appSeparatorIndex == -1 || appSeparatorIndex < toIndex) {
                speak("Sorry, I couldn't understand the command format. Please say it like: send [message] to [contact/email] on/in/from [app].");
                return;
            }

            String message;

            String appSeparatorWord;
            if (appSeparatorIndex == onIndex) appSeparatorWord = " on ";
            else if (appSeparatorIndex == inIndex) appSeparatorWord = " in ";
            else appSeparatorWord = " from ";

            String recipient = command.substring(toIndex + " to ".length(), appSeparatorIndex).trim();
            String app = command.substring(appSeparatorIndex + appSeparatorWord.length()).trim();

            String messagePart = command.substring(0, toIndex);

            if (messagePart.toLowerCase().startsWith("hey send")) {
                message = messagePart.substring("hey send".length()).trim();
            } else if (messagePart.toLowerCase().startsWith("send")) {
                message = messagePart.substring("send".length()).trim();
            } else {
                speak("Sorry, I couldn't understand the command. Please try again.");
                return;
            }

            if (message.isEmpty()) {
                speak("What message would you like to send?");
                return;
            }

            String normalizedApp = app.toLowerCase().replace(" app", "");

            switch (normalizedApp) {
                case "whatsapp": {
                    String contactNumber = getContactNumber(recipient);
                    if (contactNumber == null) {
                        speak("Could not find a WhatsApp number for " + recipient);
                        return;
                    }
                    Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                    whatsappIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + contactNumber + "&text=" + message));
                    startActivity(whatsappIntent);
                    break;
                }
                case "gmail":
                case "mail": {
                    String emailAddress;
                    if (isEmailValid(recipient)) {
                        emailAddress = recipient;
                    } else {
                        emailAddress = getGmailAddress(recipient);
                    }

                    if (emailAddress == null) {
                        speak("Could not find an email address for " + recipient);
                        return;
                    }
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", emailAddress, null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Message from Nancy");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, message);
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                    break;
                }
                default:
                    speak("Sorry, I can't send messages with " + app + " yet.");
            }

        } catch (Exception e) {
            Log.e("SendMessage", "Error processing command: " + command, e);
            speak("Sorry, something went wrong while trying to send the message.");
        }
    }

    private String getContactNumber(String name) {
        String number = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                if (nameIndex < 0) continue;
                String contactName = cursor.getString(nameIndex);

                if (contactName.equalsIgnoreCase(name)) {
                    int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                    if (idIndex < 0) continue;
                    String id = cursor.getString(idIndex);

                    int hasPhoneNumberIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                    if (hasPhoneNumberIndex < 0) continue;

                    if (cursor.getInt(hasPhoneNumberIndex) > 0) {
                        Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (phones != null) {
                            if (phones.moveToFirst()) {
                                int numberColumnIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                if (numberColumnIndex >= 0) {
                                    number = phones.getString(numberColumnIndex);
                                }
                            }
                            phones.close();
                        }
                    }
                    if (number != null) {
                        break;
                    }
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return number;
    }

    private String getGmailAddress(String name) {
        String email = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                if (nameIndex < 0) continue;
                String contactName = cursor.getString(nameIndex);
                if (contactName.equalsIgnoreCase(name)) {
                    int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                    if (idIndex < 0) continue;
                    String id = cursor.getString(idIndex);
                    Cursor emails = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (emails != null) {
                        if (emails.moveToFirst()) {
                            int emailIndex = emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                            if (emailIndex >= 0) {
                                email = emails.getString(emailIndex);
                            }
                        }
                        emails.close();
                    }
                    if (email != null) {
                        break;
                    }
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return email;
    }

    private void playOfflineMusic() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("audio/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Play Music with..."));
        } else {
            speak("I couldn't find an app to play music on your device.");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    private String readFromFile() {
        String ret = "";
        try {
            InputStream inputStream = openFileInput("data.txt");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String Receivestr;
                StringBuilder stringBuilder = new StringBuilder();

                while ((Receivestr = bufferedReader.readLine()) != null) {
                    stringBuilder.append(" ").append(Receivestr);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("Exception", "File not found: ", e);
        }
        catch (IOException e) {
            Log.e("Exception", "Can not read file: ", e);
        }
        return ret;
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("data.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: ", e);
        }

    }

    private void startPulseAnimation() {
        runOnUiThread(() -> {
            if (pulseAnimator == null) {
                pulseAnimator = (ObjectAnimator) android.animation.AnimatorInflater.loadAnimator(this, R.animator.mic_pulse);
                pulseAnimator.setTarget(micButton);
            }
            pulseAnimator.start();
        });
    }

    private void stopPulseAnimation() {
        runOnUiThread(() -> {
            if (pulseAnimator != null && pulseAnimator.isRunning()) {
                pulseAnimator.cancel();
                micButton.setScaleX(1.0f);
                micButton.setScaleY(1.0f);
                micButton.setAlpha(1.0f);
            }
        });
    }

    public void startRecording(View view) {
        startPulseAnimation();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizer.startListening(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPulseAnimation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speak("Contacts permission granted. You can now use the send message feature.");
            } else {
                speak("Contacts permission denied. I can't send messages without it.");
            }
        }
        if (requestCode == CALL_PHONE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speak("Call permission granted. You can now use the call feature.");
            } else {
                speak("Call permission denied. I can't make calls without it.");
            }
        }
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speak("Camera permission granted. You can now use the camera features.");
            } else {
                speak("Camera permission denied. I can't use the camera without it.");
            }
        }
        if (requestCode == READ_MEDIA_IMAGES_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openMedia("images");
            } else {
                speak("Storage permission denied. I can't show images without it.");
            }
        }
        if (requestCode == READ_MEDIA_VIDEO_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openMedia("videos");
            } else {
                speak("Storage permission denied. I can't show videos without it.");
            }
        }
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speak("Storage permission granted.");
            } else {
                speak("Storage permission denied.");
            }
        }
    }
}
