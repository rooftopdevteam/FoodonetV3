package com.foodonet.foodonet.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.activities.LatestPlacesActivity;
import com.foodonet.foodonet.activities.PublicationActivity;
import com.foodonet.foodonet.activities.SplashForCamera;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.DecimalDigitsInputFilter;
import com.foodonet.foodonet.commonMethods.OnGotMyUserImageListener;
import com.foodonet.foodonet.commonMethods.OnReceiveResponseListener;
import com.foodonet.foodonet.commonMethods.OnReplaceFragListener;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.db.GroupsDBHandler;
import com.foodonet.foodonet.model.Group;
import com.foodonet.foodonet.model.Publication;
import com.foodonet.foodonet.model.SavedPlace;
import com.foodonet.foodonet.serverMethods.ServerMethods;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AddEditPublicationFragment extends Fragment implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {
    public static final String TAG = "AddEditPublicationFrag";
    private static final int INTENT_TAKE_PICTURE = 1;
    private static final int INTENT_PICK_PICTURE = 2;

    private static final String STATE_CURRENT_PHOTO_PATH = "stateCurrentPhotoPath";
    private static final String STATE_PICK_PHOTO_PATH = "statePickPhotoPath";
    private static final String STATE_PUBLICATION = "statePublication";
    private static final String STATE_EDIT_TYPE = "stateEditType";
    private static final String STATE_PLACE = "statePlace";

    private EditText editTextTitleAddPublication, editTextPriceAddPublication, editTextDetailsAddPublication;
    private Spinner spinnerShareWith;
    private TextView textLocationAddPublication;
    private ImageView imagePictureAddPublication,imageTakePictureAddPublication;
    private View layoutInfo, layoutImage;

    private String mCurrentPhotoPath, pickPhotoPath;
    private SavedPlace place;
    private Publication publication;
    private String editType;
    private ArrayList<Group> groups;
    private ArrayAdapter<String> spinnerAdapter;
    private FoodonetReceiver receiver;
    private OnReceiveResponseListener onReceiveResponseListener;
    private OnReplaceFragListener onReplaceFragListener;
    private boolean isKeyOpen;


    public AddEditPublicationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onReceiveResponseListener = (OnReceiveResponseListener) context;
        onReplaceFragListener = (OnReplaceFragListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        receiver = new FoodonetReceiver();

        if(savedInstanceState == null){
            editType = getArguments().getString(TAG,PublicationActivity.ADD_PUBLICATION_TAG);
            if(editType.equals(PublicationActivity.EDIT_PUBLICATION_TAG) || editType.equals(PublicationActivity.REPUBLISH_PUBLICATION_TAG)) {
                publication = getArguments().getParcelable(Publication.PUBLICATION_KEY);
                if(publication!= null){
                    place = new SavedPlace(publication.getAddress(),publication.getLat(),publication.getLng());
                    mCurrentPhotoPath = CommonMethods.getFilePathFromPublicationID(getContext(),publication.getId(),publication.getVersion());
                }
            }
        }else {
            editType = savedInstanceState.getString(STATE_EDIT_TYPE);
            publication = savedInstanceState.getParcelable(STATE_PUBLICATION);
            place = savedInstanceState.getParcelable(STATE_PLACE);
            mCurrentPhotoPath = savedInstanceState.getString(STATE_CURRENT_PHOTO_PATH);
            pickPhotoPath = savedInstanceState.getString(STATE_PICK_PHOTO_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_edit_publication, container, false);

        String title = getString(R.string.new_share);

        // set layouts */
        editTextTitleAddPublication = (EditText) v.findViewById(R.id.editTextTitleAddPublication);
        textLocationAddPublication = (TextView) v.findViewById(R.id.textLocationAddPublication);
        textLocationAddPublication.setOnClickListener(this);
        spinnerShareWith = (Spinner) v.findViewById(R.id.spinnerShareWith);
        spinnerAdapter = new ArrayAdapter<>(getContext(),R.layout.item_spinner_groups,R.id.textGroupName);
        spinnerShareWith.setAdapter(spinnerAdapter);
        editTextDetailsAddPublication = (EditText) v.findViewById(R.id.editTextDetailsAddPublication);
        editTextPriceAddPublication = (EditText) v.findViewById(R.id.editTextPriceAddPublication);
        editTextPriceAddPublication.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2)});
        v.findViewById(R.id.imageTakePictureAddPublication).setOnClickListener(this);
        imagePictureAddPublication = (ImageView) v.findViewById(R.id.imagePictureAddPublication);

        // currently only supporting NIS
        TextView textPublicationPriceType = (TextView) v.findViewById(R.id.textPublicationPriceType);
        textPublicationPriceType.setText(getString(R.string.currency_nis));

        if(editType.equals(PublicationActivity.EDIT_PUBLICATION_TAG) || editType.equals(PublicationActivity.REPUBLISH_PUBLICATION_TAG)){
            title = publication.getTitle();
            spinnerShareWith.setEnabled(false);
        }
        File mCurrentPhotoFile;
        if (mCurrentPhotoPath != null) {
            mCurrentPhotoFile = new File(mCurrentPhotoPath);
            if(mCurrentPhotoFile.isFile()){
                // there's an image path, try to load from file */
                Glide.with(this).load(mCurrentPhotoFile).centerCrop().into(imagePictureAddPublication);
            }
        } else{
            // load default image */
            Glide.with(this).load(R.drawable.foodonet_image).centerCrop().into(imagePictureAddPublication);
        }
        getActivity().setTitle(title);

        layoutInfo = v.findViewById(R.id.layoutInfo);
        layoutInfo.setVisibility(View.GONE);
        TextView textInfo = (TextView) v.findViewById(R.id.textInfo);
        textInfo.setText(R.string.start_sharing_by_adding_an_image_of_the_food_you_wish_to_share);
        if (editType.equals(PublicationActivity.EDIT_PUBLICATION_TAG) || editType.equals(PublicationActivity.REPUBLISH_PUBLICATION_TAG)){
            loadPublicationIntoViews();
        }
        if(place!=null){
            textLocationAddPublication.setText(place.getAddress());
        }
        layoutImage = v.findViewById(R.id.layoutImage);
        imageTakePictureAddPublication = (ImageView) v.findViewById(R.id.imageTakePictureAddPublication);
        layoutImage.getViewTreeObserver().addOnGlobalLayoutListener(this);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        GroupsDBHandler groupsDBHandler = new GroupsDBHandler(getContext());

        spinnerAdapter.clear();
        if (editType.equals(PublicationActivity.EDIT_PUBLICATION_TAG) || editType.equals(PublicationActivity.REPUBLISH_PUBLICATION_TAG)){
            Group group = groupsDBHandler.getGroup(publication.getAudience());
            spinnerAdapter.add(group.getGroupName());
            if(groups== null){
                groups = new ArrayList<>();
            }else{
                groups.clear();
            }
            groups.add(group);
        } else{
            groups = groupsDBHandler.getAllGroupsWithPublic();
            String[] groupsNames = new String[groups.size()];
            Group group;
            String groupName;
            for (int i = 0; i < groups.size(); i++) {
                group = groups.get(i);
                groupName = group.getGroupName();
                groupsNames[i] = groupName;
            }
            spinnerAdapter.addAll(groupsNames);
        }

        if (mCurrentPhotoPath == null|| mCurrentPhotoPath.equals("")) {
            layoutInfo.setVisibility(View.VISIBLE);

            imagePictureAddPublication.setVisibility(View.GONE);
        } else{
            layoutInfo.setVisibility(View.GONE);
            imagePictureAddPublication.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CURRENT_PHOTO_PATH,mCurrentPhotoPath);
        outState.putString(STATE_PICK_PHOTO_PATH,pickPhotoPath);
        outState.putParcelable(STATE_PUBLICATION,publication);
        outState.putParcelable(STATE_PLACE,place);
        outState.putString(STATE_EDIT_TYPE,editType);
    }

    /**
     * loads the field publication to the views of the fragment
     */
    public void loadPublicationIntoViews() {
        editTextTitleAddPublication.setText(publication.getTitle());
        textLocationAddPublication.setText(publication.getAddress());
        editTextDetailsAddPublication.setText(publication.getSubtitle());
        editTextPriceAddPublication.setText(String.valueOf(publication.getPrice()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageTakePictureAddPublication:
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.add_image)
                        .setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // start the popup activity*/
                                getContext().startActivity(new Intent(getContext(), SplashForCamera.class));
                                // wait for the popup activity before starting the camera */
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        synchronized (getContext()) {
                                            try {
                                                getContext().wait(CommonConstants.SPLASH_CAMERA_TIME);
                                                // starts the image taking intent through the default app*/
                                                dispatchTakePictureIntent();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                };
                                thread.start();
                            }
                        })
                        .setNegativeButton(R.string.gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // get image from gallery */
                                dispatchPickPictureIntent();
                            }
                        });
                dialog.show();

                break;
            case R.id.textLocationAddPublication:
                // start the google places select activity */
                startActivityForResult(new Intent(getContext(), LatestPlacesActivity.class), LatestPlacesActivity.REQUEST_PLACE_PICKER);
                break;
        }
    }

    /** starts the image taking intent through the default app*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = CommonMethods.createImageFile(getContext());
                pickPhotoPath = photoFile.getPath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null && photoFile.isFile()) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.foodonet.foodonet.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, INTENT_TAKE_PICTURE);
            }
        }
    }

    /** get image from gallery app installed */
    private void dispatchPickPictureIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        File photoFile;
        try {
            photoFile = CommonMethods.createImageFile(getContext());
            pickPhotoPath = photoFile.getPath();
            Log.d(TAG, "photo path: " + pickPhotoPath);
            startActivityForResult(chooserIntent, INTENT_PICK_PICTURE);
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {

                // an image was successfully taken, since we have the path already,
                //  we'll run the editOverwriteImage method that scales down, shapes and overwrites the images in the path
                //  returns true if successful*/
                case INTENT_TAKE_PICTURE:
                    mCurrentPhotoPath = pickPhotoPath;
                    try {
                        Glide.with(this).load(CommonMethods.compressImage(getContext(), null, mCurrentPhotoPath)).centerCrop().into(imagePictureAddPublication);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                // an image was successfully picked, since we have the path already,
                //  we'll run the editOverwriteImage method that scales down, shapes and overwrites the images in the path
                //  returns true if successful*/
                case INTENT_PICK_PICTURE:
                    mCurrentPhotoPath = pickPhotoPath;
                    try {
                        Glide.with(this).load(CommonMethods.compressImage(getContext(), data.getData(), mCurrentPhotoPath)).centerCrop().into(imagePictureAddPublication);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                // a place has been picked, update the field place and set the view for location
                case LatestPlacesActivity.REQUEST_PLACE_PICKER:
                    place = data.getParcelableExtra("place");
                    textLocationAddPublication.setText(place.getAddress());
                    break;
            }
        }
    }

    /**
     * handles uploading the publication to the foodonet server as well (after uploading to the server successfully the image will be uploaded to the s3 server as well)
     * the server method can be either insert new publication, edit a publication (taking offline or editing an online one)
     * and republish an offline publication (which will create a new publication and delete the offline old one)
     */
    public void uploadPublicationToServer() {
        // upload the publication to the foodonet server */
        String contactInfo = CommonMethods.getMyUserPhone(getContext());
        String title = editTextTitleAddPublication.getText().toString();
        String location = textLocationAddPublication.getText().toString();
        String priceS = editTextPriceAddPublication.getText().toString();
        String details = editTextDetailsAddPublication.getText().toString();
        // currently starting time is now */
        double startingDate = 0;
        double endingDate = 0;

        long localPublicationID = -1;
        switch (editType){
            case PublicationActivity.ADD_PUBLICATION_TAG:
                startingDate = CommonMethods.getCurrentTimeSeconds();
                endingDate = startingDate + CommonConstants.TIME_SECONDS_NORMAL_PUBLICATION_DURATION;
                break;
            case PublicationActivity.EDIT_PUBLICATION_TAG:
                localPublicationID = publication.getId();
                startingDate = Double.valueOf(publication.getStartingDate());
                endingDate = Double.valueOf(publication.getEndingDate());
                break;
            case PublicationActivity.REPUBLISH_PUBLICATION_TAG:
                // keeping the original id so that it will be deleted in server methods
                localPublicationID = publication.getId();
                startingDate = CommonMethods.getCurrentTimeSeconds();
                endingDate = startingDate + CommonConstants.TIME_SECONDS_NORMAL_PUBLICATION_DURATION;
                break;
        }
        if(mCurrentPhotoPath==null || mCurrentPhotoPath.equals("")){
            mCurrentPhotoPath = null;
        }
        if (title.equals("") || location.equals("") || place.getLat()== CommonConstants.LATLNG_ERROR || place.getLng()==CommonConstants.LATLNG_ERROR
                || mCurrentPhotoPath == null) {
            Toast.makeText(getContext(), R.string.post_please_enter_all_fields, Toast.LENGTH_SHORT).show();
            onReceiveResponseListener.onReceiveResponse();
        } else {
            double price;
            if (priceS.equals("")) {
                price = 0.0;
            } else {
                try {
                    price = Double.parseDouble(priceS);
                } catch (NumberFormatException e) {
                    Log.e("PublicationActivity", e.getMessage());
                    Toast.makeText(getContext(), R.string.post_toast_please_enter_a_price_in_numbers, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            publication = new Publication(localPublicationID, -1, title, details, location, (short) 2, place.getLat(), place.getLng(),
                    CommonMethods.getNoDecimalStringFromNumber(startingDate), CommonMethods.getNoDecimalStringFromNumber(endingDate), contactInfo, true, CommonMethods.getDeviceUUID(getContext()),
                    mCurrentPhotoPath,
                    CommonMethods.getMyUserID(getContext()),
                    groups.get(spinnerShareWith.getSelectedItemPosition()).getGroupID() , CommonMethods.getMyUserName(getContext()), price, "");
            switch (editType){
                case PublicationActivity.ADD_PUBLICATION_TAG:
                    ServerMethods.addPublication(getContext(),publication);
                    break;
                case PublicationActivity.EDIT_PUBLICATION_TAG:
                    ServerMethods.editPublication(getContext(),publication);
                    break;
                case PublicationActivity.REPUBLISH_PUBLICATION_TAG:
                    ServerMethods.republishPublication(getContext(),publication);
                    break;
            }
        }
    }

    /**
     * handles checking if the soft keyboard is visible or not.
     * if visible, turn the publication image and image button to gone, so that all text fields will be visible above the keyboard
     * if not visible, turn the publication image and image button to visible
     * the state is saved in field variable isKeyOpen
     */
    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        layoutImage.getWindowVisibleDisplayFrame(r);
        int screenHeight = layoutImage.getRootView().getHeight();

        // r.bottom is the position above soft keypad or device button.
        // if keypad is shown, the r.bottom is smaller than that before.
        int keypadHeight = screenHeight - r.bottom;

        Log.d(TAG, "keypadHeight = " + keypadHeight);

        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
            // keyboard is opened
            isKeyOpen = true;
            layoutImage.setVisibility(View.GONE);
            imageTakePictureAddPublication.setVisibility(View.GONE);
        }
        else {
            // keyboard is closed
            isKeyOpen = false;
            layoutImage.setVisibility(View.VISIBLE);
            imageTakePictureAddPublication.setVisibility(View.VISIBLE);
        }
    }


    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // receiver for reports got from the service */
            int action = intent.getIntExtra(ReceiverConstants.ACTION_TYPE, -1);
            switch (action) {
                case ReceiverConstants.ACTION_FAB_CLICK:
                    // button for uploading the publication to the server */
                    if(isKeyOpen){
                        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(layoutImage.getWindowToken(), 0);
                        onReceiveResponseListener.onReceiveResponse();
                    } else {
                        uploadPublicationToServer();
                    }
                    break;

                case ReceiverConstants.ACTION_ADD_PUBLICATION:
                case ReceiverConstants.ACTION_EDIT_PUBLICATION:
                    onReceiveResponseListener.onReceiveResponse();
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        onReplaceFragListener.onReplaceFrags(PublicationActivity.NEW_STACK_TAG,CommonConstants.ITEM_ID_EMPTY);
                    }
                    break;

                case ReceiverConstants.ACTION_DELETE_PUBLICATION:
                    onReceiveResponseListener.onReceiveResponse();
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        if(publication.getId()==intent.getLongExtra(Publication.PUBLICATION_ID,-1)){
                            onReplaceFragListener.onReplaceFrags(PublicationActivity.NEW_STACK_TAG,CommonConstants.ITEM_ID_EMPTY);
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_SAVE_USER_IMAGE:
                    OnGotMyUserImageListener onGotMyUserImageListener = (OnGotMyUserImageListener) getContext();
                    onGotMyUserImageListener.gotMyUserImage();
                    break;
            }
        }
    }
}
